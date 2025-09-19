package jhkim105.tutorials.image.image_resizer

import java.awt.*
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.nio.file.Paths
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.MemoryCacheImageOutputStream
import org.springframework.web.multipart.MultipartFile
import kotlin.math.roundToInt
import com.drew.imaging.ImageMetadataReader // (선택) EXIF
import com.drew.metadata.exif.ExifIFD0Directory   // (선택) EXIF
import java.awt.geom.AffineTransform

enum class ResizeMode { EXACT, FIT, FILL } // EXACT: 강제, FIT: 비율유지 맞춤, FILL: 잘라내기

data class ResizeOptions(
    val width: Int,
    val height: Int,
    val mode: ResizeMode = ResizeMode.FIT,
    val jpegQuality: Float = 0.85f,
    val maxPixels: Long = 40_000_000L // 4천만 픽셀 방어
)

object ImageResizer {
    
    private fun determineImageFormat(file: MultipartFile): String {
        val fromCt = file.contentType?.substringAfter('/')?.lowercase()
        val fromName = file.originalFilename?.let { Paths.get(it).fileName.toString().substringAfterLast('.', "") }?.lowercase()
        return when {
            fromCt in setOf("jpeg", "jpg", "png", "gif", "bmp", "webp") -> fromCt!!
            fromName in setOf("jpeg", "jpg", "png", "gif", "bmp", "webp") -> fromName!!
            else -> "jpg" // 기본 jpg
        }.let { if (it == "jpeg") "jpg" else it }
    }
    
    fun resizeImage(file: MultipartFile, opt: ResizeOptions): ByteArray {
        // 입력 검증
        require(file.size > 0) { "Empty file provided" }
        require(opt.width > 0 && opt.height > 0) { "width/height must be > 0" }
        require(opt.width <= 10000 && opt.height <= 10000) { "Target dimensions too large: ${opt.width}x${opt.height}" }
        require(opt.jpegQuality in 0.0f..1.0f) { "JPEG quality must be between 0.0 and 1.0" }
        
        val bytes = file.bytes // 한 번만 읽기
        require(bytes.isNotEmpty()) { "File content is empty" }

        // 포맷 결정: contentType -> 확장자 -> 기본값
        val ext = determineImageFormat(file)

        // 이미지 읽기
        val original = try {
            ImageIO.read(ByteArrayInputStream(bytes))
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to read image: ${e.message}", e)
        } ?: throw IllegalArgumentException("Unsupported or corrupted image format")

        // 기본 검증
        require(original.width > 0 && original.height > 0) { "Invalid image dimensions: ${original.width}x${original.height}" }
        
        // 과도한 픽셀 방어 (원본 이미지)
        val srcPixels = original.width.toLong() * original.height.toLong()
        require(srcPixels <= opt.maxPixels) { "Source image too large: ${original.width}x${original.height} (${srcPixels} pixels)" }

        // EXIF 회전 보정(JPEG일 때만 시도)
        val oriented = try {
            if (ext == "jpg") applyExifOrientation(bytes, original) else original
        } catch (e: Exception) { 
            // EXIF 처리 실패 시 원본 사용
            println("Warning: EXIF orientation processing failed: ${e.message}")
            original 
        }

        // 타겟 사이즈 계산
        val (tw, th) = when (opt.mode) {
            ResizeMode.EXACT -> opt.width to opt.height
            ResizeMode.FIT -> fitSize(oriented.width, oriented.height, opt.width, opt.height)
            ResizeMode.FILL -> fillSize(oriented.width, oriented.height, opt.width, opt.height)
        }
        
        // 성능 최적화: 크기가 같으면 리사이징 없이 포맷만 변환
        if (tw == oriented.width && th == oriented.height && opt.mode != ResizeMode.FILL) {
            return when (ext) {
                "jpg", "jpeg" -> writeJpeg(oriented, opt.jpegQuality)
                "png", "gif", "bmp" -> writeWithImageIO(oriented, ext)
                "webp" -> {
                    runCatching { writeWithImageIO(oriented, "webp") }.getOrElse { writeJpeg(oriented, opt.jpegQuality) }
                }
                else -> writeJpeg(oriented, opt.jpegQuality)
            }
        }

        // FILL이면 가운데로 크롭
        val sourceForScale = if (opt.mode == ResizeMode.FILL) {
            val scale = maxOf(opt.width.toDouble() / oriented.width, opt.height.toDouble() / oriented.height)
            val cw = (opt.width / scale).roundToInt()
            val ch = (opt.height / scale).roundToInt()
            val x = ((oriented.width - cw) / 2).coerceAtLeast(0)
            val y = ((oriented.height - ch) / 2).coerceAtLeast(0)
            oriented.getSubimage(x, y, cw.coerceAtMost(oriented.width - x), ch.coerceAtMost(oriented.height - y))
        } else oriented

        val hasAlpha = sourceForScale.colorModel.hasAlpha()
        val targetType = if (hasAlpha && ext != "jpg") BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB
        val target = BufferedImage(tw, th, targetType)

        // 고품질 리사이징
        val g2 = target.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.drawImage(sourceForScale, 0, 0, tw, th, null)
        g2.dispose()

        // 쓰기
        return when (ext) {
            "jpg", "jpeg" -> writeJpeg(target, opt.jpegQuality)
            "png", "gif", "bmp" -> writeWithImageIO(target, ext)
            "webp" -> {
                // WebP 플러그인 필요(예: TwelveMonkeys + WebP 플러그인). 없으면 jpg로 폴백.
                runCatching { writeWithImageIO(target, "webp") }.getOrElse { writeJpeg(target, opt.jpegQuality) }
            }
            else -> writeJpeg(target, opt.jpegQuality)
        }
    }

    private fun fitSize(sw: Int, sh: Int, tw: Int, th: Int): Pair<Int, Int> {
        val s = minOf(tw.toDouble() / sw, th.toDouble() / sh)
        return (sw * s).roundToInt().coerceAtLeast(1) to (sh * s).roundToInt().coerceAtLeast(1)
    }

    private fun fillSize(sw: Int, sh: Int, tw: Int, th: Int): Pair<Int, Int> = tw to th

    private fun writeWithImageIO(img: BufferedImage, format: String): ByteArray {
        val bos = ByteArrayOutputStream()
        ImageIO.write(img, format, bos)
        return bos.toByteArray()
    }

    private fun writeJpeg(img: BufferedImage, quality: Float): ByteArray {
        val writer: ImageWriter = ImageIO.getImageWritersByFormatName("jpg").asSequence().firstOrNull()
            ?: error("No JPEG writer found")
        
        return ByteArrayOutputStream().use { bos ->
            MemoryCacheImageOutputStream(bos).use { imageOutputStream ->
                writer.output = imageOutputStream
                val param = writer.defaultWriteParam.apply {
                    compressionMode = ImageWriteParam.MODE_EXPLICIT
                    compressionQuality = quality.coerceIn(0.0f, 1.0f)
                }
                try {
                    writer.write(null, IIOImage(img, null, null), param)
                    imageOutputStream.flush()
                } finally {
                    writer.dispose()
                }
                bos.toByteArray()
            }
        }
    }

    /**
     * EXIF Orientation 보정: 1,3,6,8,2 처리
     * 90/270도 회전 시 캔버스 크기도 적절히 조정
     */
    private fun applyExifOrientation(bytes: ByteArray, img: BufferedImage): BufferedImage {
        val metadata = ImageMetadataReader.readMetadata(ByteArrayInputStream(bytes))
        val dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java) ?: return img
        val orientation = dir.getInteger(ExifIFD0Directory.TAG_ORIENTATION) ?: return img
        
        return when (orientation) {
            1 -> img // 정상
            2 -> flipHorizontal(img) // 수평 뒤집기
            3 -> rotate180(img) // 180도 회전
            6 -> rotate90CW(img) // 90도 시계방향
            8 -> rotate90CCW(img) // 90도 반시계방향
            else -> img
        }
    }
    
    private fun flipHorizontal(img: BufferedImage): BufferedImage {
        val result = BufferedImage(img.width, img.height, img.type)
        val g2 = result.createGraphics()
        g2.drawImage(img, img.width, 0, 0, img.height, 0, 0, img.width, img.height, null)
        g2.dispose()
        return result
    }
    
    private fun rotate180(img: BufferedImage): BufferedImage {
        val result = BufferedImage(img.width, img.height, img.type)
        val g2 = result.createGraphics()
        g2.rotate(Math.PI, img.width / 2.0, img.height / 2.0)
        g2.drawImage(img, 0, 0, null)
        g2.dispose()
        return result
    }
    
    private fun rotate90CW(img: BufferedImage): BufferedImage {
        // 90도 회전 시 width와 height가 바뀜
        val result = BufferedImage(img.height, img.width, img.type)
        val g2 = result.createGraphics()
        g2.rotate(Math.PI / 2, result.width / 2.0, result.height / 2.0)
        g2.drawImage(img, (result.width - img.width) / 2, (result.height - img.height) / 2, null)
        g2.dispose()
        return result
    }
    
    private fun rotate90CCW(img: BufferedImage): BufferedImage {
        // 270도 회전 시 width와 height가 바뀜
        val result = BufferedImage(img.height, img.width, img.type)
        val g2 = result.createGraphics()
        g2.rotate(-Math.PI / 2, result.width / 2.0, result.height / 2.0)
        g2.drawImage(img, (result.width - img.width) / 2, (result.height - img.height) / 2, null)
        g2.dispose()
        return result
    }
}


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
    fun resizeImage(file: MultipartFile, opt: ResizeOptions): ByteArray {
        val bytes = file.bytes // 한 번만 읽기
        require(opt.width > 0 && opt.height > 0) { "width/height must be > 0" }

        // 포맷 결정: contentType -> 확장자 -> 기본값
        val ext = run {
            val fromCt = file.contentType?.substringAfter('/')?.lowercase()
            val fromName = file.originalFilename?.let { Paths.get(it).fileName.toString().substringAfterLast('.', "") }?.lowercase()
            when {
                fromCt in setOf("jpeg", "jpg", "png", "gif", "bmp", "webp") -> fromCt!!
                fromName in setOf("jpeg", "jpg", "png", "gif", "bmp", "webp") -> fromName!!
                else -> "jpg" // 기본 jpg
            }.let { if (it == "jpeg") "jpg" else it }
        }

        // 이미지 읽기
        val original = ImageIO.read(ByteArrayInputStream(bytes))
            ?: error("Unsupported or corrupted image")

        // EXIF 회전 보정(JPEG일 때만 시도)
        val oriented = try {
            if (ext == "jpg") applyExifOrientation(bytes, original) else original
        } catch (_: Exception) { original }

        // 과도한 픽셀 방어
        val srcPixels = oriented.width.toLong() * oriented.height.toLong()
        require(srcPixels <= opt.maxPixels) { "Image too large: ${oriented.width}x${oriented.height}" }

        // 타겟 사이즈 계산
        val (tw, th) = when (opt.mode) {
            ResizeMode.EXACT -> opt.width to opt.height
            ResizeMode.FIT -> fitSize(oriented.width, oriented.height, opt.width, opt.height)
            ResizeMode.FILL -> fillSize(oriented.width, oriented.height, opt.width, opt.height)
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
        val bos = ByteArrayOutputStream()
        writer.output = MemoryCacheImageOutputStream(bos)
        val param = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = quality.coerceIn(0.0f, 1.0f)
        }
        writer.write(null, IIOImage(img, null, null), param)
        writer.dispose()
        return bos.toByteArray()
    }

    // (선택) EXIF Orientation 보정: 1,3,6,8만 처리
    private fun applyExifOrientation(bytes: ByteArray, img: BufferedImage): BufferedImage {
        val metadata = ImageMetadataReader.readMetadata(ByteArrayInputStream(bytes))
        val dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java) ?: return img
        val orientation = dir.getInteger(ExifIFD0Directory.TAG_ORIENTATION) ?: return img
        val (theta, flipH) = when (orientation) {
            1 -> 0.0 to false
            3 -> Math.PI to false
            6 -> Math.PI / 2 to false     // 90 CW
            8 -> -Math.PI / 2 to false    // 90 CCW
            2 -> 0.0 to true              // horizontal flip
            else -> 0.0 to false
        }
        if (theta == 0.0 && !flipH) return img

        val tx = AffineTransform()
        if (flipH) {
            tx.scale(-1.0, 1.0)
            tx.translate(-img.width.toDouble(), 0.0)
        }
        if (theta != 0.0) {
            tx.rotate(theta, (img.width / 2.0), (img.height / 2.0))
        }
        val op = AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC)
        val b = BufferedImage(img.width, img.height, img.type)
        op.filter(img, b)
        return b
    }
}


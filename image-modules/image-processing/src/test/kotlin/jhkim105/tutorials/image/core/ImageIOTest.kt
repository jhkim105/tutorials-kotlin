package jhkim105.tutorials.image.core

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.FileImageOutputStream
import kotlin.test.Test

class ImageIOTest {

    @Test
    fun compressImage() {
        val inputPath = "images/3218_4291.jpg"
        val outputPath = "build/3218_4291_imageio_compressed.jpeg"
        val quality = 0.5f // 0.0 (최대 압축, 낮은 품질) ~ 1.0 (최소 압축, 높은 품질)
        val inputImage: BufferedImage = ImageIO.read(File(inputPath))

        val writers = ImageIO.getImageWritersByFormatName("jpeg")
        val writer: ImageWriter = writers.next()

        val output = FileImageOutputStream(File(outputPath))
        writer.output = output

        val param: ImageWriteParam = writer.defaultWriteParam

        if (param.canWriteCompressed()) {
            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
            param.compressionQuality = quality
        }

        writer.write(null, IIOImage(inputImage, null, null), param)
        writer.dispose()
        output.close()
    }

    @Test
    fun compressImage2() {
        val inputPath = "images/4527_3015.jpg"
        val outputPath = "build/4527_3015_imageio_compressed.jpeg"
        val quality = 0.5f // 0.0 (최대 압축, 낮은 품질) ~ 1.0 (최소 압축, 높은 품질)
        val inputImage: BufferedImage = ImageIO.read(File(inputPath))

        val writers = ImageIO.getImageWritersByFormatName("jpeg")
        val writer: ImageWriter = writers.next()

        val output = FileImageOutputStream(File(outputPath))
        writer.output = output

        val param: ImageWriteParam = writer.defaultWriteParam

        if (param.canWriteCompressed()) {
            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
            param.compressionQuality = quality
        }

        writer.write(null, IIOImage(inputImage, null, null), param)
        writer.dispose()
        output.close()
    }
}
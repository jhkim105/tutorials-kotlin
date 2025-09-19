package jhkim105.tutorials.image.core

import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class ImageScaledInstanceTest {

    @Test
    fun resizeImage() {
        val inputPath = "images/3218_4291.jpg"
        val outputPath = "build/3218_4291_image_scaled_instance_resized.jpg"
        val width = 500
        val height = 500

        val inputFile = File(inputPath)
        val originalImage = ImageIO.read(inputFile)

        val scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_DEFAULT)
        val resizedBufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val g2d = resizedBufferedImage.createGraphics()
        g2d.drawImage(scaledImage, 0, 0, null)
        g2d.dispose()


        val formatName = outputPath.substringAfterLast('.')
        ImageIO.write(resizedBufferedImage, formatName, File(outputPath))
    }

    @Test
    fun resizeImage2() {
        val inputPath = "images/4527_3015.jpg"
        val outputPath = "build/4527_3015_image_scaled_instance_resized.jpg"
        val width = 500
        val height = 500

        val inputFile = File(inputPath)
        val originalImage = ImageIO.read(inputFile)

        val scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_DEFAULT)
        val resizedBufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        val g2d = resizedBufferedImage.createGraphics()
        g2d.drawImage(scaledImage, 0, 0, null)
        g2d.dispose()


        val formatName = outputPath.substringAfterLast('.')
        ImageIO.write(resizedBufferedImage, formatName, File(outputPath))
    }
}
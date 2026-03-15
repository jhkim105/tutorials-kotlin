package jhkim105.tutorials.image.core

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class Graphic2DTest {

    @Test
    fun resizeImage() {
        val inputPath = "images/3218_4291.jpg"
        val outputPath = "build/3218_4291_graphic2d_resized.jpg"
        val width = 500
        val height = 500

        val inputFile = File(inputPath)
        val originalImage = ImageIO.read(inputFile)

        val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d: Graphics2D = resizedImage.createGraphics()
        g2d.drawImage(originalImage, 0, 0, width, height, null)
        g2d.dispose()

        val formatName = outputPath.substringAfterLast('.')
        ImageIO.write(resizedImage, formatName, File(outputPath))
    }

    @Test
    fun resizeImage2() {
        val inputPath = "images/4527_3015.jpg"
        val outputPath = "build/4527_3015_graphic2d_resized.jpg"
        val width = 500
        val height = 500

        val inputFile = File(inputPath)
        val originalImage = ImageIO.read(inputFile)

        val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d: Graphics2D = resizedImage.createGraphics()
        g2d.drawImage(originalImage, 0, 0, width, height, null)
        g2d.dispose()

        val formatName = outputPath.substringAfterLast('.')
        ImageIO.write(resizedImage, formatName, File(outputPath))
    }


}
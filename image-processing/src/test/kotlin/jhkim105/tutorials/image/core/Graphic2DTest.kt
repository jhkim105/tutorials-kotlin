package jhkim105.tutorials.image.core

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class Graphic2DTest {

    @Test
    fun resizeImage() {
        val inputPath = "src/test/resources/files/2mb.jpg"
        val outputPath = "build/graphic2d_resized.jpg"
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
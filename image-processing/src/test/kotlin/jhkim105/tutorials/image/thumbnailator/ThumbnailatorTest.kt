package jhkim105.tutorials.image.thumbnailator

import net.coobird.thumbnailator.Thumbnails
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class ThumbnailatorTest {

    @Test
    fun resizeImage() {
        val inputPath = "src/test/resources/files/2mb.jpg"
        val outputPath = "build/thumbnailator_resized.jpg"
        val width = 500
        val height = 500

        Thumbnails.of(inputPath)
            .size(width, height)
            .outputFormat("jpg")
            .outputQuality(0.5f)
            .toFile(outputPath)
    }
}
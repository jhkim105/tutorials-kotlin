package jhkim105.tutorials.image.thumbnailator

import net.coobird.thumbnailator.Thumbnails
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class ThumbnailatorTest {

    @Test
    fun thumbnail() {
        val inputPath = "src/test/resources/files/3218_4291.jpg"
        val outputPath = "build/3218_4291_thumbnailator_resized.jpg"
        val width = 500
        val height = 500

        Thumbnails.of(inputPath)
            .size(width, height)
            .outputFormat("jpg")
            .outputQuality(0.5f)
            .toFile(outputPath)
    }

    @Test
    fun thumbnail2() {
        val inputPath = "images/4527_3015.jpg"
        val outputPath = "build/4527_3015_thumbnailator_resized.jpg"
        val width = 500
        val height = 500

        Thumbnails.of(inputPath)
            .size(width, height)
            .outputFormat("jpg")
            .outputQuality(0.5f)
            .toFile(outputPath)
    }
}
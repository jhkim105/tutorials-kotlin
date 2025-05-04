package jhkim105.tutorials.image.imgscalr

import net.coobird.thumbnailator.Thumbnails
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class ImgscalrTest {

    @Test
    fun resizeImage() {
        val inputPath = "src/test/resources/files/2mb.jpg"
        val outputPath = "build/imgscalr_resized.jpg"
        val width = 500
        val height = 500

        val original: BufferedImage = ImageIO.read(File(inputPath))
        val resized: BufferedImage = Scalr.resize(original, width, height)
        val formatName = outputPath.substringAfterLast('.')
        ImageIO.write(resized, formatName, File(outputPath))
    }
}
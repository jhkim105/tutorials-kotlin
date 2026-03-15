package jhkim105.tutorials.image.image_resizer

import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileOutputStream
import kotlin.test.Test

class ImageResizerTest {

    @Test
    fun resizeImage() {
        val inputPath = "images/4527_3015.jpg"
        val outputPath = "build/4527_3015_image_resizer_fit_resized.jpg"
        
        // Create MockMultipartFile from existing image
        val inputFile = File(inputPath)
        val mockFile = MockMultipartFile(
            "file",
            inputFile.name,
            "image/jpeg",
            inputFile.readBytes()
        )
        
        // Create resize options
        val resizeOptions = ResizeOptions(
            width = 500,
            height = 500,
            mode = ResizeMode.FIT,
            jpegQuality = 0.8f
        )
        
        // Execute resizeImageSafe method
        val resizedBytes = ImageResizer.resizeImage(mockFile, resizeOptions)
        
        // Save the result to file
        FileOutputStream(outputPath).use { fos ->
            fos.write(resizedBytes)
        }
    }

    @Test
    fun resizeImageExactMode() {
        val inputPath = "images/4527_3015.jpg"
        val outputPath = "build/4527_3015_image_resizer_exact_resized.jpg"
        
        // Create MockMultipartFile from existing image
        val inputFile = File(inputPath)
        val mockFile = MockMultipartFile(
            "file",
            inputFile.name,
            "image/jpeg",
            inputFile.readBytes()
        )
        
        // Create resize options with EXACT mode
        val resizeOptions = ResizeOptions(
            width = 500,
            height = 500,
            mode = ResizeMode.EXACT,
            jpegQuality = 0.9f
        )
        
        // Execute resizeImageSafe method
        val resizedBytes = ImageResizer.resizeImage(mockFile, resizeOptions)
        
        // Save the result to file
        FileOutputStream(outputPath).use { fos ->
            fos.write(resizedBytes)
        }
    }

    @Test
    fun resizeImageFillMode() {
        val inputPath = "images/4527_3015.jpg"
        val outputPath = "build/4527_3015_image_resizer_fill_resized.jpg"
        
        // Create MockMultipartFile from existing image
        val inputFile = File(inputPath)
        val mockFile = MockMultipartFile(
            "file",
            inputFile.name,
            "image/jpeg",
            inputFile.readBytes()
        )
        
        // Create resize options with FILL mode
        val resizeOptions = ResizeOptions(
            width = 500,
            height = 500,
            mode = ResizeMode.FILL,
            jpegQuality = 0.85f
        )
        
        // Execute resizeImageSafe method
        val resizedBytes = ImageResizer.resizeImage(mockFile, resizeOptions)
        
        // Save the result to file
        FileOutputStream(outputPath).use { fos ->
            fos.write(resizedBytes)
        }
    }
}
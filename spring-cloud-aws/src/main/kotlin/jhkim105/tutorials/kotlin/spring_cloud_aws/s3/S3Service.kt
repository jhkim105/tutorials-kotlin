package jhkim105.tutorials.kotlin.spring_cloud_aws.s3

import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.WritableResource
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption


@Component
class S3Service(
  private val resourceLoader: ResourceLoader
) {

  fun download(s3Url: String, destDir: String): File {
    val resource: Resource = resourceLoader.getResource(s3Url)

    val destinationDirectory = File(destDir)
    if (!destinationDirectory.exists()) {
      destinationDirectory.mkdirs()
    }

    val downloadedFile = File(destinationDirectory, resource.filename!!)

    resource.inputStream.use { inputStream ->
      Files.copy(inputStream, downloadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
    return downloadedFile
  }

  fun upload(file: File, s3Url: String) {
    val resource = resourceLoader.getResource(s3Url) as WritableResource

    resource.outputStream.use { outputStream ->
      Files.copy(file.toPath(), outputStream)
    }
  }

}
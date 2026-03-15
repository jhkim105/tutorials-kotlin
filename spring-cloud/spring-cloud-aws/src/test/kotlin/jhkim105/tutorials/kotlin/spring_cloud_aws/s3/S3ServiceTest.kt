package jhkim105.tutorials.kotlin.spring_cloud_aws.s3

import org.assertj.core.util.Files
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.file.Paths

@SpringBootTest
class S3ServiceTest {

    @Autowired
    lateinit var s3Service: S3Service

    @Test
    fun testUpload() {
        val objectUrl = "s3://rtm-test/1.txt"

        // Upload to S3
        s3Service.upload(Paths.get("src/test/resources/test.txt").toFile(), objectUrl)
    }

    @Test
    fun testDownload() {
        val objectUrl = "s3://rtm-test/1.txt";
        val tempDir = "build/tmp/s3"

        // Download the file from S3
        val downloadedFile = s3Service.download(objectUrl, tempDir)

        assertTrue(downloadedFile.exists())
    }

}


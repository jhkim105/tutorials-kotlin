package jhkim105.tutorials.kotlin.spring_cloud_aws.s3


import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ResourceLoader

@SpringBootTest
class S3Tests {

  private val log = LoggerFactory.getLogger(javaClass)

  @Autowired
  lateinit var resourceLoader: ResourceLoader


  @Test
  fun testResource() {
    val objectUrl = "s3://rtm-test/1.txt"
    val resource = resourceLoader.getResource(objectUrl)
    log.debug("{}", resource.exists())
    log.debug("{}", resource.filename)
    log.debug("{}", resource.url)
  }

}

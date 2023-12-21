package jhkim105.tutorials

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ResourceLoader


@SpringBootTest
class ResourceLoaderTests {

  private val log = LoggerFactory.getLogger(javaClass)

  @Autowired
  lateinit var resourceLoader: ResourceLoader

  @Test
  fun test() {
    val resource = resourceLoader.getResource("file:src/test/resources/files/test.txt")
    assertThat(resource.filename).isEqualTo("test.txt")
    assertThat(resource.url.path).isEqualTo("src/test/resources/files/test.txt")
    assertThat(resource.file.path).isEqualTo("src/test/resources/files/test.txt")
    log.debug(resource.file.absolutePath)
  }

}
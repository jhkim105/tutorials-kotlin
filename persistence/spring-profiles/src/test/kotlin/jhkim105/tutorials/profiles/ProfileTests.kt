package jhkim105.tutorials.profiles

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@SpringBootTest
class ProfileTests {

    @Nested
    @ActiveProfiles("local")
    inner class LocalProfileTests(
        @Autowired private val serviceProperties: ServiceProperties
    ) {
        @Test
        fun test() {
            assertThat(serviceProperties.version).isEqualTo("1.0.1")
        }
    }

    @Nested
    @ActiveProfiles("staging")
    inner class StagingProfileTests(
        @Autowired private val serviceProperties: ServiceProperties
    ) {
        @Test
        fun test() {
            assertThat(serviceProperties.version).isEqualTo("1.0.0")
        }
    }

}

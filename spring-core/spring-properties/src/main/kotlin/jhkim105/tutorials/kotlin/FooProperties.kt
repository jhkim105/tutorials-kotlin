package jhkim105.tutorials.kotlin

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "foo")
data class FooProperties(
    val name: String, val bar: BarProperties
) {
    data class BarProperties(
        val name: String
    )
}





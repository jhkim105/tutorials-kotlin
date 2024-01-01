package jhkim105.tutorials.core_kotlin

import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertTrue


@Disabled
class StringTests {


  @Test
  fun removePrefix() {
    assertTrue {  "abc".removePrefix("a") == "bc"}
  }

}
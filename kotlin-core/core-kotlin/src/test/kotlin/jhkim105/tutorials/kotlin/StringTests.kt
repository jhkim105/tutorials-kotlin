package jhkim105.tutorials.kotlin

import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertTrue


class StringTests {


  @Test
  @Disabled
  fun removePrefix() {
    assertTrue { "abc".removePrefix("a") == "bc" }
  }

  @Test
  fun string() {
    println(getString("abc"))
    println(getString(""))
    println(getString(null))
  }

  private fun getString(s: String?): String {
    val str =
      """
      getString: {
        v: ${s?.let { s.uppercase() } ?: ""}
      }
    """.trimIndent()

    println(s)

    return str
  }


  private fun doSomething(s: String): String = "doSomething: $s"

}
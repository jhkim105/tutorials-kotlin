package jhkim105.tutorials.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ScopeFunctionTests {


  /**
   * let: 객체를 람다함수 인자로 전달(it)하고, 람다함수 결과를 반환한다.
   */
  @Nested
  inner class LetTests {

    @Test
    fun `let`() {
      val result = "Hello World!"
        .let { it.length }

      assertThat(result).isEqualTo("Hello World!".length)
    }

    @Test
    fun `call it conditionally with Elvis`() {
      val message: String? = "Hello World!"
      assertThat(doSomethingByMessage(message)).isEqualTo("value was not null: Hello World!")
      assertThat(doSomethingByMessage(null)).isEqualTo("value was null")
    }

    private fun doSomethingByMessage(value: String?): String =
      value?.let {
        "value was not null: $it"
      } ?: "value was null"

  }

  /**
   * run: 객체에 대해 람다 함수를 실행(this)하고, 람다 함수 결과를 반환한다.
   */
  @Test
  fun `run`() {
    val result = StringBuilder().run {
      append("Hello")
      append(" World!")
      toString()
    }


  }

  /**
   * with: 객체를 인자로 받아 실행(this)하고, 람다함수 결과를 반환한다.
   */
  @Test
  fun `with`() {
    val result = with(StringBuilder()) {
      append("Hello")
      append(" World!")
      toString()
    }
    assertThat(result).isEqualTo("Hello World!")
  }

  /**
   * apply: 객체에 대해 람다 함수를 실행(this)하고, 객체 자체를 반환한다.
   */
  @Test
  fun `apply`() {
    val result = StringBuilder().apply {
      append("Hello")
      append(" World!")
    }.toString()
    assertThat(result).isEqualTo("Hello World!")
  }

  /**
   * also: 객체를 람다 함수의 인자로 전달(it)하고, 객체 자체를 반환한다.
   */
  @Test
  fun `also`() {
    val result = StringBuilder().also {
      it.append("Hello")
      it.append(" World!")
    }.toString()
    assertThat(result).isEqualTo("Hello World!")
  }



}
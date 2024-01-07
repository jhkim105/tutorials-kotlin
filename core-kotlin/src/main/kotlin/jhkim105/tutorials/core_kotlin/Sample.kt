package jhkim105.tutorials.core_kotlin

import org.apache.commons.lang3.StringUtils

class Sample(
  var name: String? = null) {





  fun getUppercaseName(): String =
    "mimeType=${name?.let { StringUtils.upperCase(name)}}"
}
package jhkim105.tutorials.kotlin.kotlin_core

enum class SampleEnum {
    FOO, BAR;

    companion object {
        fun byNameIgnoreCaseOrNull(value: String): SampleEnum? {
            entries.forEach {
                if (it.name == value)
                    return it
            }
            return null
        }
    }
}
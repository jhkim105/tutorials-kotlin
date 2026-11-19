package jhkim105.ssecompare.common

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StreamRequest(
    @field:NotBlank(message = "prompt must not be blank")
    @field:Size(min = 1, max = 4000, message = "prompt length must be between 1 and 4000")
    val prompt: String = "",

    @field:Min(value = 1, message = "events must be at least 1")
    @field:Max(value = 100_000, message = "events must be at most 100000")
    val events: Int = 0,

    @field:Min(value = 0, message = "intervalMs must be at least 0")
    @field:Max(value = 60_000, message = "intervalMs must be at most 60000")
    val intervalMs: Long = -1,

    val persist: Boolean = false,

    @field:Min(value = 1, message = "failAt must be at least 1")
    val failAt: Int? = null,
) {
    @AssertTrue(message = "failAt must be less than or equal to events")
    fun isFailAtValid(): Boolean = failAt == null || failAt <= events
}

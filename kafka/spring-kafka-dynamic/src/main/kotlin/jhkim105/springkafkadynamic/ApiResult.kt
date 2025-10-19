package jhkim105.springkafkadynamic

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResult<T>(
    val ok: Boolean,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T? = null, message: String? = null): ApiResult<T> =
            ApiResult(ok = true, data = data, message = message)

        fun <T> failure(message: String, data: T? = null): ApiResult<T> =
            ApiResult(ok = false, data = data, message = message)
    }
}

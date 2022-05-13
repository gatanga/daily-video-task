package data.network.rest.helper

sealed class ApiResponseWrapper<out T> {
    data class Success<out T>(val value: T) : ApiResponseWrapper<T>()
    data class GenericError(val code: Int? = null, val error: String? = null) :
        ApiResponseWrapper<Nothing>()

    object NetworkError : ApiResponseWrapper<Nothing>()
}
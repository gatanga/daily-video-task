package data.result

sealed class RoomApiResult<out R> {
    data class Success<out T>(val value: T) : RoomApiResult<T>()
    data class Error(val exception: Exception) : RoomApiResult<Nothing>()
}

package com.anjor.bookscout.util

/**
 * Classifies the kind of failure that occurred during a network request so the UI can
 * show a helpful, specific message instead of a generic one.
 */
enum class ErrorType {
    /** No internet connection / DNS failure (IOException). */
    NETWORK,

    /** The request took too long (SocketTimeoutException). */
    TIMEOUT,

    /** The API responded with a non-2xx status code (HttpException). */
    HTTP,

    /** The response could not be parsed (SerializationException). */
    SERIALIZATION,

    /** Anything else we did not specifically anticipate. */
    UNKNOWN
}

/**
 * A typed error carrying a [type] for branching in the UI and a human readable [message].
 */
data class AppError(
    val type: ErrorType,
    val message: String? = null
)

/**
 * Result wrapper returned by the repository for every network call.
 * - [Success] carries the parsed data.
 * - [Failure] carries an [AppError] without throwing.
 */
sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>
    data class Failure(val error: AppError) : ApiResult<Nothing>

    fun isSuccess(): Boolean = this is Success<T>
    fun getOrNull(): T? = if (this is Success<T>) data else null
}

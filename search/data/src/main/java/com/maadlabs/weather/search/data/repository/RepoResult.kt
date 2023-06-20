package com.maadlabs.weather.search.data.repository

import com.maadlabs.weather.search.data.model.DataResult
import com.maadlabs.weather.search.data.model.ErrorResponse

sealed class RepoResult<out T, out E : RepoErrorType> {
    object Loading : RepoResult<Nothing, Nothing>()
    data class Available<out T>(val repoData: T): RepoResult<T, Nothing>()
    data class NotAvailable<out E: RepoErrorType>(val error: RepoErrorType): RepoResult<Nothing, E>()
}

sealed interface RepoErrorType {
    object NetworkError : RepoErrorType
    object ServerError : RepoErrorType
    object UnknownError: RepoErrorType
}

internal fun<T, V, E: ErrorResponse, F: RepoErrorType> DataResult<T, E>.toRepoResult(successMapper : (T) -> V) : RepoResult<V, F> {
    return when(this) {
        is DataResult.Success -> RepoResult.Available(successMapper(data))
        is DataResult.Error ->
            when(this.errorType) {
                is ErrorResponse.UnknownError -> RepoResult.NotAvailable(RepoErrorType.UnknownError)
                is ErrorResponse.ServerError -> RepoResult.NotAvailable(RepoErrorType.ServerError)
                is ErrorResponse.NetworkError -> RepoResult.NotAvailable(RepoErrorType.NetworkError)
                else -> RepoResult.NotAvailable(RepoErrorType.UnknownError)
            }
    }
}
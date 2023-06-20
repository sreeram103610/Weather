package com.maadlabs.weather.search.data.api.adapter

import com.maadlabs.weather.search.data.model.DataResult
import com.maadlabs.weather.search.data.model.ErrorResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type


internal class DataResultCall<R>(
    private val delegate: Call<R>,
    private val successType: Type)
    : Call<DataResult<R, ErrorResponse>> {
    override fun enqueue(callback: Callback<DataResult<R, ErrorResponse>>) = delegate.enqueue(
        object : Callback<R> {
            override fun onResponse(call: Call<R>, response: Response<R>) {
                callback.onResponse(this@DataResultCall, Response.success(response.toDataResult()))
            }

            override fun onFailure(call: Call<R>, t: Throwable) {
                val response = when(t) {
                    is IOException -> ErrorResponse.ServerError
                    else -> ErrorResponse.UnknownError
                }
                callback.onResponse(this@DataResultCall, Response.success(DataResult.Error(response)))
            }

        }
    )

    override fun clone() = DataResultCall(delegate.clone(), successType)

    override fun execute() = throw UnsupportedOperationException("DataResultCallAdapter does not support execute()")

    override fun isExecuted() = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled() = delegate.isCanceled

    override fun request() = delegate.request()

    override fun timeout() = delegate.timeout()

}

private fun <T> Response<T>.toDataResult() : DataResult<T, ErrorResponse> {
    val body = body()
    val code = code()

    if(!isSuccessful || body == null) {
        return when(code) {
            500, 502, 503 -> DataResult.Error(ErrorResponse.ServerError)
            400, 404, 403, 401 -> DataResult.Error(ErrorResponse.NetworkError)
            else -> DataResult.Error(ErrorResponse.UnknownError)
        }
    }

    return DataResult.Success(body)

}

internal class DataResultCallAdapter<R>(
    private val successType: Type
) : CallAdapter<R, Call<DataResult<R, ErrorResponse>>> {

    override fun adapt(call: Call<R>): Call<DataResult<R, ErrorResponse>> = DataResultCall(call, successType)

    override fun responseType(): Type = successType
}
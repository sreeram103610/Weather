package com.maadlabs.weather.search.data.api.adapter

import com.maadlabs.weather.search.data.model.DataResult
import com.maadlabs.weather.search.data.model.ErrorResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class DataResultCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        check(returnType is ParameterizedType) { "Return type must be a parameterized type." }

        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != DataResult::class.java) return null
        check(responseType is ParameterizedType) { "Response type must be a parameterized type." }

        val rightType = getParameterUpperBound(1, responseType)
        if (getRawType(rightType) != ErrorResponse::class.java) return null

        val leftType = getParameterUpperBound(0, responseType)

        return DataResultCallAdapter<Any>(leftType)
    }
}
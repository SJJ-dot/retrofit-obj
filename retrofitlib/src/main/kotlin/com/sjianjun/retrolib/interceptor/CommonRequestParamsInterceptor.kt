package com.sjianjun.retrolib.interceptor

import okhttp3.*

/**
 * 向所有的get post(表单)网络请求添加通用参数。
 * 如果已经有的基础参数不会再次添加避免覆盖，重复
 */
open class CommonRequestParamsInterceptor(private val baseParams: (originParams: MutableMap<String, String>) -> MutableMap<String, String> = { mutableMapOf() }) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.method() == "GET") {
            val url = request.url()
            //构造新的请求
            request = request.newBuilder().url(baseRequestParams(url)).build()

        } else if (request.method() == "POST") {
            val body = request.body()

                request = request.newBuilder().post(baseRequestParams(body)).build()

        }
        return chain.proceed(request)
    }

    /**
     * GET参数
     */
    open fun baseRequestParams(url: HttpUrl): HttpUrl {
        val originParams = mutableMapOf<String, String>()

        for (i in 0 until url.querySize()) {
            originParams[url.queryParameterName(i)] = url.queryParameterValue(i)
        }

        val map = baseParams(originParams)

        //添加不足的通用参数
        val urlBuilder = url.newBuilder()
        map.forEach { entry ->
            urlBuilder.setQueryParameter(entry.key, entry.value)
        }
        return urlBuilder.build()
    }

    /**
     * POST参数
     */
    open fun baseRequestParams(body: RequestBody?): RequestBody {

        if (body != null && body !is FormBody) {
                return body
        } else {
            val originParams = mutableMapOf<String, String>()
            if (body is FormBody) {
                for (i in 0 until body.size()) {
                    originParams[body.name(i)] = body.value(i)
                }
            }

            val map = baseParams(originParams)

            val formBody = FormBody.Builder()

            map.forEach { entry ->
                formBody.add(entry.key, entry.value)
            }
            return formBody.build()
        }


    }
}
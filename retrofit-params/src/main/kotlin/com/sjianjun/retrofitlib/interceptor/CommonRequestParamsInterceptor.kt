package com.sjianjun.retrofitlib.interceptor

import okhttp3.*

/**
 * 向所有的get post(表单)网络请求添加通用参数。
 * 如果已经有的基础参数不会再次添加避免覆盖，重复
 */
open class CommonRequestParamsInterceptor(private val baseParams: (request: Request, originParams: MutableMap<String, String>) -> MutableMap<String, String> = {_,_-> mutableMapOf() }) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.method() == "GET") {
            //构造新的请求
            request = request.newBuilder().url(baseParamsGet(request)).build()

        } else if (request.method() == "POST") {
            request = request.newBuilder().post(baseParamsPost(request)).build()

        }
        return chain.proceed(request)
    }

    /**
     * GET参数
     */
    open fun baseParamsGet(request: Request): HttpUrl {
        val url = request.url()
        val originParams = mutableMapOf<String, String>()

        val urlBuilder = url.newBuilder()

        for (i in 0 until url.querySize()) {
            originParams[url.queryParameterName(i)] = url.queryParameterValue(i)
            //将参数移除
            urlBuilder.removeAllQueryParameters(url.queryParameterName(i))
        }

        val map = baseParams(request, originParams)

        //重新添加参数
        map.forEach { entry ->
            urlBuilder.setQueryParameter(entry.key, entry.value)
        }
        return urlBuilder.build()
    }

    /**
     * POST参数
     */
    open fun baseParamsPost(request: Request): RequestBody {
        val body = request.body()
        if (body != null && body !is FormBody) {
            return body
        } else {
            val originParams = mutableMapOf<String, String>()
            if (body is FormBody) {
                for (i in 0 until body.size()) {
                    originParams[body.name(i)] = body.value(i)
                }
            }

            val map = baseParams(request, originParams)

            val formBody = FormBody.Builder()

            map.forEach { entry ->
                formBody.add(entry.key, entry.value)
            }
            return formBody.build()
        }


    }
}
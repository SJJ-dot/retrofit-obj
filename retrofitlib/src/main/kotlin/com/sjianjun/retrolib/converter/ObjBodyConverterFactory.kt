package com.sjianjun.retrolib.converter

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sjianjun.retrolib.Obj
import okhttp3.*
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 需要添加 注解 [Obj]
 * @POST("app/receivecoin/bookcase")
 * Observable<BaseResult<String>> reportBookcase2(@Body @Obj BaseRequestParams params);
 *
 * @GET("app/receivecoin/lists")
 * Observable<BaseResult<String>> reportBookcase3(@Query(value = ObjBodyConverterFactory.PLACEHOLDER) @Obj BaseRequestParams params);
 *详细说明：RTFSC
 */
open class ObjBodyConverterFactory(private val gson: Gson) : Converter.Factory() {
    companion object {

        const val PLACEHOLDER = "placeholder"

        @JvmStatic
        fun create(gson: Gson): ObjBodyConverterFactory {
            return ObjBodyConverterFactory(gson)
        }

        @JvmStatic
        fun create(): ObjBodyConverterFactory {
            return create(Gson())
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        parameterAnnotations.forEach {
            if (it is Obj) {
                return Converter<Any, RequestBody> { value ->
                    if (it.type.equals("JSON", true)) {
                        val jsonTree = gson.toJsonTree(value)
                        RequestBody.create(
                            MediaType.get("application/json;charset=utf-8"),
                            jsonTree.toString()
                        )
                    } else {
                        val jsonTree = gson.toJsonTree(value)
                        val form = FormBody.Builder()
                        buildKV(jsonTree).forEach { e ->
                            form.add(e.first, e.second)
                        }
                        form.build()
                    }

                }
            }
        }
        return null
    }

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        annotations.forEach {
            if (it is Obj) {
                return Converter<Any, String> { value ->
                    val jsonTree = gson.toJsonTree(value)

                    val sb = StringBuilder()
                    buildKV(jsonTree).forEach { e ->
                        sb.append("${e.first.encode()}=${e.second.encode()}&")
                    }
                    if (sb.isNotEmpty()) {
                        sb.deleteCharAt((sb.length - 1))
                    }
                    sb.toString()
                }
            }
        }
        return null
    }


    private fun buildKV(
        obj: JsonElement,
        dest: MutableList<Pair<String, String>> = mutableListOf()
    ): MutableList<Pair<String, String>> {

        if (obj is JsonArray) {
            obj.forEach { e ->
                buildKV(e, dest)
            }
        } else if (obj is JsonObject) {
            obj.entrySet().forEach { entry ->
                dest.add(entry.key to getValue(entry.value))
            }
        }
        return dest
    }

    private fun getValue(jsonElement: JsonElement): String {
        return if (jsonElement.isJsonPrimitive) {
            jsonElement.asString
        } else {
            jsonElement.toString()
        }
    }

    class ObjConverterInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()

            if (request.method() == "GET") {
                val parameter = request.url().queryParameter(PLACEHOLDER)
                if (parameter != null) {
                    val urlBuilder = request.url().newBuilder()
                    urlBuilder.removeAllQueryParameters(PLACEHOLDER)
                    val decode = parameter.split("&")
                    decode.forEach {
                        val split = it.split("=")
                        urlBuilder.addQueryParameter(split[0].decode(), split[1].decode())
                    }
                    request = request.newBuilder().url(urlBuilder.build()).build()
                }
            }

            return chain.proceed(request)
        }
    }
}

private fun String.decode(): String {
    try {
        return URLDecoder.decode(this, "utf-8")
    } catch (e: Exception) {
        throw e
    }
}

private fun String.encode(): String {
    try {
        return URLEncoder.encode(this, "utf-8")
    } catch (e: Exception) {
        throw e
    }
}


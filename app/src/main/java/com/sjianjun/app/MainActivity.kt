package com.sjianjun.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sjianjun.retrofitlib.Obj
import com.sjianjun.retrofitlib.converter.ObjBodyConverterFactory
import com.sjianjun.retrofitlib.interceptor.CommonRequestParamsInterceptor
import com.sjianjun.scheduler.CoroutineScheduler
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import sjj.alog.Log

class MainActivity : AppCompatActivity() {


    /**
     * - CallAdapter 首先被调用。决定okhttp怎样使用，在哪个线程等等
     * - retrofit 转换器 将非字符串类型转换成字符串
     * - Interceptor okhttp 拦截器从上到下依次调用
     * - 首先将占位符的参数转换为真实请求的格式
     * - 处理通用参数
     * - 打印请求日志
     */
    private val testInterface: TestInterface
        get() = Retrofit.Builder()
            .baseUrl("https://www.baidu.com/")
            .client(
                OkHttpClient.Builder()

                    .addInterceptor(ObjBodyConverterFactory.ObjConverterInterceptor())
                    .addInterceptor(CommonRequestParamsInterceptor {
                        Log.e("通用参数处理 :$it")
                        it
                    })
                    .addInterceptor(HttpLoggingInterceptor {
                        Log.e("http:$it")
                    }.setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            )
            .addConverterFactory(ObjBodyConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(CoroutineScheduler.IO))
            .build().create(TestInterface::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        get.setOnClickListener {

            testInterface.getTest("aaaaaaaaaaaaaaaaaaaaa", Param()).subscribe({
                Log.e("result:$it")
            }, {
                Log.e("error $it ", it)
            })

        }

        post_from.setOnClickListener {
            testInterface.postTest(Param()).subscribe({
                Log.e("result:$it")
            }, {
                Log.e("error $it ", it)
            })
        }

        post_json.setOnClickListener {
            testInterface.postJson(Param()).subscribe({
                Log.e("result:$it")
            }, {
                Log.e("error $it ", it)
            })
        }
    }

    interface TestInterface {
        /**
         * 测试 ：将对象转换为get 请求的 k v
         */
        @GET("s")
        fun getTest(@Query("wd") wd: String, @Query(value = ObjBodyConverterFactory.PLACEHOLDER) @Obj params: Param): Observable<String>

        /**
         * 测试 ：POST 请求 ，参数对象 body 转换为 表单
         */
        @POST("s")
        fun postTest( @Body @Obj params: Param): Observable<String>
        /**
         * 测试 ：POST 请求 ，参数对象 body 转换为 json
         */
        @POST("s")
        fun postJson(@Body @Obj(type = "JSON") params: Param): Observable<String>
    }

    class Param {
        val rsv_bp = 1
        val testpppp = 1
    }


}

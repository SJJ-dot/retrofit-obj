package com.sjianjun.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.sjianjun.retrolib.Obj
import com.sjianjun.retrolib.converter.ObjBodyConverterFactory
import com.sjianjun.retrolib.interceptor.CommonRequestParamsInterceptor
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.collections.get

class MainActivity : AppCompatActivity() {


    private val testInterface: TestInterface
        get() = Retrofit.Builder()
            .baseUrl("https://www.baidu.com/")
            .client(OkHttpClient.Builder().addInterceptor(CommonRequestParamsInterceptor {

                it
            }).addInterceptor(ObjBodyConverterFactory.ObjConverterInterceptor()).build())
            .addConverterFactory(ObjBodyConverterFactory.create())
            .build().create(TestInterface::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        get.setOnClickListener {

            testInterface.getTest("aaaaaaaaaaaaaaaaaaaaa", Param())

        }
    }

    interface TestInterface {
        @GET("s")
        fun getTest(@Query("wd") wd: String, @Query(value = ObjBodyConverterFactory.PLACEHOLDER) @Obj params: Param): Observable<String>
    }

    class Param {
        val rsv_bp = 1
    }


}

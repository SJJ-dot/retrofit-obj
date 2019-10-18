package com.sjianjun.retrofitlib

/**
 * 将添加此注解的对象转化为form表单形式提交
 * @param type 该参数用于指定 post 请求时body 类型（可选值：'FROM、JSON' ，忽略大小写，默认FROM）。 get 请求时该参数不起作用。
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@MustBeDocumented
annotation class Obj(val type: String = "FROM")
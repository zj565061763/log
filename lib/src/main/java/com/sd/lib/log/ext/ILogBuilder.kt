package com.sd.lib.log.ext

import android.view.View

interface ILogBuilder {
    /**
     * 设置格式化对象
     */
    fun setFormatter(formatter: ILogFormatter?): ILogBuilder

    /**
     * 添加一段内容，如果[content]为null或者空，不添加
     */
    fun add(content: Any?): ILogBuilder

    /**
     * 添加一段内容，如果[content]为null或者空，不添加，否则用hash的方式显示对象
     */
    fun addHash(content: Any?): ILogBuilder

    /**
     * 添加一对内容，如果[key]为null或者空，则不添加
     */
    fun pair(key: String?, value: Any?): ILogBuilder

    /**
     * 用hash的方式显示[value]
     */
    fun pairHash(key: String?, value: Any?): ILogBuilder

    /**
     * 用[Object.toString]的方式显示[value]
     */
    fun pairStr(key: String?, value: Any?): ILogBuilder

    /**
     * 等价与：pair("instance", instance)，用hash的方式显示instance对象
     */
    fun instance(instance: Any?): ILogBuilder

    /**
     * 等价与：pair("instanceStr", instance)，用[Object.toString]的方式显示instance对象
     */
    fun instanceStr(instance: Any?): ILogBuilder

    /**
     * 等价与：pair("uuid", uuid)
     */
    fun uuid(uuid: String?): ILogBuilder

    /**
     * 换行
     */
    fun nextLine(): ILogBuilder

    /**
     * 等价与：add(clazz.getSimpleName())
     */
    fun clazz(clazz: Class<*>): ILogBuilder

    /**
     * 等价与：add(clazz.getName())
     */
    fun clazzFull(clazz: Class<*>): ILogBuilder

    /**
     * 清空
     */
    fun clear(): ILogBuilder

    /**
     * 构建字符串
     */
    fun build(): String

    interface ILogFormatter {
        /**
         * 返回key-value之间的分隔符，默认：":"
         */
        val separatorForKeyValue: String?

        /**
         * 返回段与段之间的分隔符，默认："|"
         */
        val separatorBetweenPart: String?
    }
}
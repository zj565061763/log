package com.sd.lib.log.ext

interface ILogBuilder {
    fun setFormatter(formatter: ILogFormatter?): ILogBuilder

    fun setHashPairView(hashView: Boolean): ILogBuilder

    fun add(content: Any?): ILogBuilder

    fun pair(key: String?, value: Any?): ILogBuilder

    fun pairHash(key: String?, value: Any?): ILogBuilder

    fun pairStr(key: String?, value: Any?): ILogBuilder

    fun instance(instance: Any?): ILogBuilder

    fun instanceStr(instance: Any?): ILogBuilder

    fun uuid(uuid: String?): ILogBuilder

    fun nextLine(): ILogBuilder

    fun clazz(clazz: Class<*>?): ILogBuilder

    fun clazzFull(clazz: Class<*>?): ILogBuilder

    fun clear(): ILogBuilder

    fun build(): String

    interface ILogFormatter {
        val separatorForKeyValue: String?
        val separatorBetweenPart: String?
    }
}
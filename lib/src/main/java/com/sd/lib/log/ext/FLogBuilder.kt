package com.sd.lib.log.ext

import android.view.View
import com.sd.lib.log.ext.ILogBuilder.ILogFormatter

class FLogBuilder : ILogBuilder {
    private val _list = mutableListOf<KeyValue>()
    private var _formatter: ILogFormatter? = null
    private var _hashPairView = true

    override fun setFormatter(formatter: ILogFormatter?): ILogBuilder {
        _formatter = formatter
        return this
    }

    override fun setHashPairView(hash: Boolean): ILogBuilder {
        _hashPairView = hash
        return this
    }

    private val formatter: ILogFormatter
        get() = _formatter ?: InternalLogFormatter.sDefault

    override fun add(content: Any?): ILogBuilder {
        if (content == null) return this
        if (content is String && content.isEmpty()) return this
        _list.add(KeyValue(null, content))
        return this
    }

    override fun pair(key: String?, value: Any?): ILogBuilder {
        if (key.isNullOrEmpty()) return this

        val stringValue = if (value == null) {
            "null"
        } else {
            if (_hashPairView && value is View) {
                getInstanceHash(value)
            } else {
                value.toString()
            }
        }
        _list.add(KeyValue(key, stringValue))
        return this
    }

    override fun pairHash(key: String?, value: Any?): ILogBuilder {
        return pair(key, getInstanceHash(value))
    }

    override fun pairStr(key: String?, value: Any?): ILogBuilder {
        return pair(key, getInstanceString(value))
    }

    override fun instance(instance: Any?): ILogBuilder {
        return pair("instance", getInstanceHash(instance))
    }

    override fun instanceStr(instance: Any?): ILogBuilder {
        val stringValue = instance?.toString() ?: "null"
        return pair("instanceStr", stringValue)
    }

    override fun uuid(uuid: String?): ILogBuilder {
        return pair("uuid", uuid)
    }

    override fun nextLine(): ILogBuilder {
        return add("\r\n")
    }

    override fun clazz(clazz: Class<*>?): ILogBuilder {
        val stringValue = clazz?.simpleName ?: "null"
        return add(stringValue)
    }

    override fun clazzFull(clazz: Class<*>?): ILogBuilder {
        val stringValue = clazz?.name ?: "null"
        return add(stringValue)
    }

    override fun clear(): ILogBuilder {
        _list.clear()
        return this
    }

    override fun build(): String {
        if (_list.isEmpty()) return ""

        val builder = StringBuilder()
        _list.forEachIndexed { index, item ->
            builder.append(formatter.separatorBetweenPart)

            if (item.key.isNullOrEmpty()) {
                builder.append(item.value)
            } else {
                builder.append(item.key).append(formatter.separatorForKeyValue).append(item.value)
            }

            if (index == _list.lastIndex) {
                builder.append(" ")
            }
        }
        return builder.toString()
    }

    override fun toString(): String {
        return build()
    }

    private inner class KeyValue(val key: String?, val value: Any?)

    private class InternalLogFormatter : ILogFormatter {
        override val separatorForKeyValue: String
            get() = ":"

        override val separatorBetweenPart: String
            get() = "|"

        companion object {
            val sDefault = InternalLogFormatter()
        }
    }

    companion object {
        private fun getInstanceHash(`object`: Any?): String? {
            return if (`object` == null) null else `object`.javaClass.name + "@" + Integer.toHexString(`object`.hashCode())
        }

        private fun getInstanceString(`object`: Any?): String? {
            return `object`?.toString()
        }
    }
}
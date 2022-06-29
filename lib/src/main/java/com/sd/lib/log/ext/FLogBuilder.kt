package com.sd.lib.log.ext

import android.view.View
import com.sd.lib.log.ext.ILogBuilder.ILogFormatter

class FLogBuilder : ILogBuilder {
    private val _list = mutableListOf<KeyValue>()
    private var _formatter: ILogFormatter? = null

    private val formatter: ILogFormatter
        get() = _formatter ?: defaultFormatter

    override fun setFormatter(formatter: ILogFormatter?) = apply {
        _formatter = formatter
    }

    override fun add(content: Any?) = apply {
        if (content == null) return@apply
        if (content is String && content.isEmpty()) return@apply
        _list.add(KeyValue(null, content))
    }

    override fun addHash(content: Any?) = apply {
        if (content == null) return@apply
        if (content is String && content.isEmpty()) return@apply
        _list.add(KeyValue(null, getInstanceHash(content)))
    }

    override fun pair(key: String?, value: Any?) = apply {
        if (key.isNullOrEmpty()) return@apply

        val stringValue = if (value is View) {
            getInstanceHash(value)
        } else {
            value.toString()
        }
        _list.add(KeyValue(key, stringValue))
    }

    override fun pairHash(key: String?, value: Any?) = apply {
        pair(key, getInstanceHash(value))
    }

    override fun pairStr(key: String?, value: Any?) = apply {
        pair(key, value.toString())
    }

    override fun instance(instance: Any?) = apply {
        pair("instance", getInstanceHash(instance))
    }

    override fun instanceStr(instance: Any?) = apply {
        pair("instanceStr", instance.toString())
    }

    override fun uuid(uuid: String?) = apply {
        pair("uuid", uuid)
    }

    override fun nextLine() = apply {
        add("\r\n")
    }

    override fun clazz(clazz: Class<*>) = apply {
        add(clazz.simpleName)
    }

    override fun clazzFull(clazz: Class<*>) = apply {
        add(clazz.name)
    }

    override fun clear() = apply {
        _list.clear()
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
    }

    companion object {
        private val defaultFormatter = InternalLogFormatter()

        private fun getInstanceHash(instance: Any?): String? {
            if (instance == null) return null
            return instance.javaClass.name + "@" + Integer.toHexString(instance.hashCode())
        }
    }
}
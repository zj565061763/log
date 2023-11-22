package com.sd.lib.log

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

internal fun consolePublisher(): FLogPublisher = ConsolePublisher()

internal fun defaultPublisher(file: File): FLogPublisher = DefaultPublisher(file)

internal fun emptyPublisher(): FLogPublisher = EmptyPublisher()

interface FLogPublisher {
    fun publish(record: FLogRecord)
    fun limitMB(size: Int)
    fun close()
}

private class EmptyPublisher : FLogPublisher {
    override fun publish(record: FLogRecord) {}
    override fun limitMB(size: Int) {}
    override fun close() {}
}

private class ConsolePublisher : FLogPublisher {
    override fun publish(record: FLogRecord) {
        when (record.level) {
            FLogLevel.Debug -> Log.d(record.tag, record.msg)
            FLogLevel.Info -> Log.i(record.tag, record.msg)
            FLogLevel.Warning -> Log.w(record.tag, record.msg)
            FLogLevel.Error -> Log.e(record.tag, record.msg)
            else -> {}
        }
    }

    override fun limitMB(size: Int) {}

    override fun close() {}
}

private class DefaultPublisher(file: File) : FLogPublisher {
    private val _file: File = file
    private var _limit: Int = 0

    private var _output: CounterOutputStream? = null
    private val _formatter: FLogFormatter = fLogFormatter()

    init {
        if (file.isDirectory) error("file should not be a directory.")
    }

    @Synchronized
    override fun publish(record: FLogRecord) {
        val output = getOutput() ?: return

        val msg = try {
            _formatter.format(record)
        } catch (e: Exception) {
            e.printStackTrace()
            "\n format error:${e} | ${record.millis} ${record.msg} \n"
        }
        val data = msg.toByteArray()

        try {
            output.write(data)
            output.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            close()
            return
        }

        checkLimit()
    }

    @Synchronized
    override fun limitMB(size: Int) {
        _limit = size * 1024 * 1024
        checkLimit()
    }

    @Synchronized
    override fun close() {
        try {
            _output?.flush()
            _output?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _output = null
        }
    }

    private fun checkLimit() {
        val output = _output ?: return
        if (_limit > 0 && output.written > _limit) {
            close()
            _file.deleteRecursively()
        }
    }

    private fun getOutput(): CounterOutputStream? {
        val output = _output
        return if (output == null) {
            createOutput()
        } else {
            if (_file.exists()) output else createOutput()
        }
    }

    private fun createOutput(): CounterOutputStream? {
        close()
        if (!_file.fCreateFile()) return null
        return FileOutputStream(_file, true)
            .buffered()
            .let { CounterOutputStream(it, _file.length().toInt()) }
            .also { _output = it }
    }

    private class CounterOutputStream(output: OutputStream, length: Int) : OutputStream() {
        private val _output = output
        private var _written = length

        val written: Int get() = _written

        override fun write(b: Int) {
            _output.write(b)
            _written++
        }

        override fun write(buff: ByteArray) {
            _output.write(buff)
            _written += buff.size
        }

        override fun write(buff: ByteArray, off: Int, len: Int) {
            _output.write(buff, off, len)
            _written += len
        }

        override fun flush() {
            _output.flush()
        }

        override fun close() {
            _output.close()
        }
    }
}

private fun File?.fCreateFile(): Boolean {
    try {
        if (this == null) return false
        if (this.isFile) return true
        if (this.isDirectory) this.deleteRecursively()
        return this.parentFile.fMakeDirs() && this.createNewFile()
    } catch (e: Exception) {
        return false
    }
}

private fun File?.fMakeDirs(): Boolean {
    try {
        if (this == null) return false
        if (this.isDirectory) return true
        if (this.isFile) this.delete()
        return this.mkdirs()
    } catch (e: Exception) {
        return false
    }
}
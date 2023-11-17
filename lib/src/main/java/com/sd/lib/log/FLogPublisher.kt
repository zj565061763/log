package com.sd.lib.log

import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

internal fun defaultConsolePublisher(): FLogPublisher = DefaultConsolePublisher()

internal fun defaultLogPublisher(
    directory: File,
    limitMB: Int,
): FLogPublisher = DefaultLogPublisher(
    directory = directory,
    limitMB = limitMB,
)

internal interface FLogPublisher {
    fun publish(record: FLogRecord)
    fun close()
}

private class DefaultConsolePublisher : FLogPublisher {
    override fun publish(record: FLogRecord) {
        when (record.level) {
            FLogLevel.Debug -> Log.d(record.tag, record.msg)
            FLogLevel.Info -> Log.i(record.tag, record.msg)
            FLogLevel.Waring -> Log.w(record.tag, record.msg)
            FLogLevel.Error -> Log.e(record.tag, record.msg)
            else -> {}
        }
    }

    override fun close() {}
}

private class DefaultLogPublisher(
    directory: File,
    limitMB: Int,
) : FLogPublisher {
    private val _dir = directory
    private val _limit = limitMB * 1024 * 1024

    private var _output: CounterOutputStream? = null

    private val _logFile = _dir.resolve("default.log")
    private val _formatter = defaultLogFormatter()

    private fun getOutput(): CounterOutputStream? {
        val output = _output
        if (output != null) return output

        val logFile = _logFile
        if (!logFile.ensureFileExist()) return null

        return FileOutputStream(logFile, true)
            .let { BufferedOutputStream(it) }
            .let { CounterOutputStream(it, logFile.length().toInt()) }.also { _output = it }
    }

    override fun publish(record: FLogRecord) {
        val output = getOutput() ?: return

        val msg = _formatter.format(record)
        val data = msg.toByteArray()

        try {
            output.write(data)
            output.flush()
        } catch (e: Exception) {
            flushAndClose()
            return
        }

        if (_limit > 0 && output.written > _limit) {
            flushAndClose()
            _logFile.deleteRecursively()
        }
    }

    override fun close() {
        flushAndClose()
    }

    private fun flushAndClose() {
        try {
            _output?.flush()
            _output?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _output = null
        }
    }

    private class CounterOutputStream(
        output: OutputStream,
        length: Int,
    ) : OutputStream() {
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

private fun File.ensureFileExist(): Boolean {
    try {
        if (isFile) return true
        if (isDirectory) deleteRecursively()
        return parentFile?.mkdirs() == true && createNewFile()
    } catch (e: Exception) {
        return false
    }
}
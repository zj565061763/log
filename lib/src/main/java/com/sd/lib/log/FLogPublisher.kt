package com.sd.lib.log

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

internal fun defaultConsolePublisher(): FLogPublisher = DefaultConsolePublisher()

internal fun defaultLogPublisher(
    file: File,
    limitMB: Int,
): FLogPublisher = DefaultLogPublisher(
    file = file,
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
            FLogLevel.Warning -> Log.w(record.tag, record.msg)
            FLogLevel.Error -> Log.e(record.tag, record.msg)
            else -> {}
        }
    }

    override fun close() {}
}

private class DefaultLogPublisher(
    file: File,
    limitMB: Int,
) : FLogPublisher {
    private val _logFile = file
    private val _limit = limitMB * 1024 * 1024

    private var _output: CounterOutputStream? = null
    private val _formatter = defaultLogFormatter()

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
            "\n format error:${e} \r"
        }

        val data = msg.toByteArray()
        try {
            output.write(data)
            output.flush()
        } catch (e: Exception) {
            close()
            return
        }

        if (_limit > 0 && output.written > _limit) {
            close()
            _logFile.deleteRecursively()
        }
    }

    private fun getOutput(): CounterOutputStream? {
        val logFile = _logFile
        val output = _output
        return if (output == null) {
            createOutput(logFile)
        } else {
            if (logFile.exists()) output else createOutput(logFile)
        }
    }

    /**
     * 创建输出流
     */
    private fun createOutput(logFile: File): CounterOutputStream? {
        // 关闭旧的输出流，创建新的输出流
        close()
        return if (logFile.fCreateFile()) {
            FileOutputStream(logFile, true)
                .buffered()
                .let { CounterOutputStream(it, logFile.length().toInt()) }
                .also { _output = it }
        } else null
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
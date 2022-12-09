package com.draco.ladb.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import com.draco.ladb.R
import java.io.File

class ADBControl(private val context: Context) {

    companion object {
        const val MAX_OUTPUT_BUFFER_SIZE = 1024 * 16

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ADBControl? = null
        fun getInstance(context: Context): ADBControl = instance ?: synchronized(this) {
            instance ?: ADBControl(context).also { instance = it }
        }
    }

    data class Result(
        private val message: String?,
        private val error: String?,
        private val hasMessage: Boolean,
        private val hasError: Boolean
    )

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    private val adbPath = "${context.applicationInfo.nativeLibraryDir}/libadb.so"

    private val outputBufferFile: File = File.createTempFile("buffer", ".txt").also {
        it.deleteOnExit()
    }

    private fun getOutputBufferSize(): Int {
        val userValue = sharedPrefs.getString(context.getString(R.string.buffer_size_key), "16384")!!
        return try {
            Integer.parseInt(userValue)
        } catch (_: NumberFormatException) {
            MAX_OUTPUT_BUFFER_SIZE
        }
    }

    /**
     * Send a raw ADB command
     */
    fun adb(redirect: Boolean, vararg command: String): Process {
        val commandList = command.toMutableList().also {
            it.add(0, adbPath)
        }
        return shell(redirect, commandList)
    }

    /**
     * Send a raw shell command
     */
    fun shell(redirect: Boolean, command: List<String>): Process {
        val processBuilder = ProcessBuilder(command)
            .directory(context.filesDir)
            .apply {
                redirectErrorStream(true)
                if (redirect) {
                    redirectOutput(outputBufferFile)
                }

                environment().apply {
                    put("HOME", context.filesDir.path)
                    put("TMPDIR", context.cacheDir.path)
                }
            }

        return processBuilder.start()
    }

    /**
     * Write a debug message to the user
     */
    fun debug(msg: String) {
        synchronized(outputBufferFile) {
            if (outputBufferFile.exists())
                outputBufferFile.appendText("* $msg" + System.lineSeparator())
        }
    }
}
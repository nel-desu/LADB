package com.draco.ladb.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.draco.ladb.utils.ADBControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InterruptedIOException
import java.util.concurrent.ArrayBlockingQueue

class ControlActivityViewModel(application: Application): AndroidViewModel(application) {

    companion object {
        private const val TAG = "ControlActivityViewMode"
        private const val QUEUE_CAPACITY = 200
    }

    private val adb = ADBControl.getInstance(getApplication<Application>().applicationContext)

    val logFile: File = File.createTempFile("temp", ".txt").also {
        it.deleteOnExit()
    }
    val screenshotFile: File = File.createTempFile("screenshot", ".png").also {
        it.deleteOnExit()
    }
    private val installFile: File = File.createTempFile("install", ".apk").also {
        it.deleteOnExit()
    }

    init {
        startServer()
    }

    private val _progressVisibility = MutableLiveData(false)
    val progressVisibility: LiveData<Boolean> = _progressVisibility

    private val shellMessageQueue: ArrayBlockingQueue<String> = ArrayBlockingQueue(QUEUE_CAPACITY)
    private val _shellMessage = MutableLiveData("")
    val shellMessage: LiveData<String> = _shellMessage

    private var logcatLock = Any()
    private var startLogcat = false

    private var logcatProcess: Process? = null
    private var connectProcess: Process? = null

    private fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            val ps = adb.adb(false, "start-server")
            val lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)
        }
    }

    fun connectDevice(ip: String) {
        if (_progressVisibility.value == true) {
            return
        }
        _progressVisibility.value = true
        viewModelScope.launch(Dispatchers.IO) {
            connectProcess = adb.adb(false, "connect", ip)
            var lines = connectProcess!!.inputStream.bufferedReader().readLines()
            putMessage(lines)

            val ps = adb.adb(false, "devices")
            lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)

            _progressVisibility.postValue(false)
        }
    }

    fun disconnectAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val ps = adb.adb(false, "disconnect")
            val lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)
        }
    }

    fun uninstall(packageName: String) {
        if (_progressVisibility.value == true) {
            return
        }
        _progressVisibility.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val ps = adb.adb(false, "uninstall", packageName)
            val lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)

            _progressVisibility.postValue(false)
        }
    }

    fun copyAndInstall(inputStream: InputStream) {
        if (_progressVisibility.value == true) {
            return
        }
        _progressVisibility.value = true
        viewModelScope.launch(Dispatchers.IO) {
            installFile.deleteOnExit()
            val outputStream = FileOutputStream(installFile)
            inputStream.copyTo(outputStream)

            val ps = adb.adb(false, "install", "-r", installFile.absolutePath)
            val lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)

            _progressVisibility.postValue(false)
        }
    }

    @SuppressLint("SdCardPath")
    fun screenshot(callback: () -> Unit) {
        if (_progressVisibility.value == true) {
            return
        }
        _progressVisibility.value = true
        viewModelScope.launch(Dispatchers.IO) {
            screenshotFile.deleteOnExit()

            var ps = adb.adb(false, "shell", "screencap", "-p", "/sdcard/screenshot.png")
            var lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)

            ps = adb.adb(false, "pull", "/sdcard/screenshot.png", screenshotFile.absolutePath)
            lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)

            _progressVisibility.postValue(false)
            callback()
        }
    }

    fun logcatStart() {
        synchronized(logcatLock) {
            if (startLogcat) {
                return
            }
            viewModelScope.launch(Dispatchers.IO) {
                logcatProcess = adb.adb(false, "logcat")
                try {
                    val reader = logcatProcess!!.inputStream.bufferedReader()

                    startLogcat = true
                    while (startLogcat) {
                        putMessage(listOf(reader.readLine()))
                    }
                } catch (_: InterruptedIOException) { }
            }
        }
    }

    fun logcatStop() {
        synchronized(logcatLock) {
            startLogcat = false
            logcatProcess?.destroyForcibly()
            putMessage(listOf("----- LOGCAT STOP -----"))
        }
    }

    fun clearMessage() {
        synchronized(shellMessageQueue) {
            shellMessageQueue.clear()
            _shellMessage.postValue("----- CLEAR -----")
        }
    }

    private fun putMessage(msgList: List<String?>) {
        synchronized(shellMessageQueue) {
            for (msg in msgList) {
                if (shellMessageQueue.size >= QUEUE_CAPACITY - 1) {
                    shellMessageQueue.take()
                }
                if (!msg.isNullOrEmpty()) {
                    shellMessageQueue.put(msg)
                }
                logFile.appendText("${msg.orEmpty()}\n")
            }
            val sb = StringBuilder()
            for (msg in shellMessageQueue) {
                sb.append(msg).append("\n")
            }
            _shellMessage.postValue(sb.toString())
        }
    }
}
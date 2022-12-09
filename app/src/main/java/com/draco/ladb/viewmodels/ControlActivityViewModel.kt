package com.draco.ladb.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.draco.ladb.utils.ADBControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ArrayBlockingQueue

class ControlActivityViewModel(application: Application): AndroidViewModel(application) {

    companion object {
        private const val TAG = "ControlActivityViewMode"
        private const val QUEUE_CAPACITY = 100
    }

    private val adb = ADBControl.getInstance(getApplication<Application>().applicationContext)

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

    private fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            val ps = adb.adb(false, "start-server")
            val lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)
        }
    }

    fun connectDevice(ip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ps = adb.adb(false, "connect", ip)
            val lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)
        }
    }

    fun disconnectAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val ps = adb.adb(false, "disconnect")
            val lines = ps.inputStream.bufferedReader().readLines()
            putMessage(lines)
        }
    }

    fun logcatStart() {
        synchronized(logcatLock) {
            if (startLogcat) {
                return
            }
            viewModelScope.launch(Dispatchers.IO) {
                val ps = adb.adb(false, "logcat")
                val reader = ps.inputStream.bufferedReader()

                startLogcat = true
                while (startLogcat) {
                    putMessage(listOf(reader.readLine()))
                }
            }
        }
    }

    fun logcatStop() {
        synchronized(logcatLock) {
            startLogcat = false
            putMessage(listOf("----- LOGCAT STOP -----"))
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
            }
            val sb = StringBuilder()
            for (msg in shellMessageQueue) {
                sb.append(msg).append("\n")
            }
            _shellMessage.postValue(sb.toString())
        }
    }

}
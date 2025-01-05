package com.socket

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socket.data.TcpClient
import com.socket.data.UdpClient
import com.socket.data.TcpServer
import com.socket.data.UdpServer
import com.socket.model.MessageObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _messages = MutableStateFlow<List<MessageObject>>(emptyList())
    val messages: StateFlow<List<MessageObject>> = _messages

    private var tcpClient: TcpClient? = null
    private var udpClient: UdpClient? = null

    private val tcpServer = TcpServer { message ->
        addMessage(message)
    }
    private val udpServer = UdpServer()

    var numOfMessages = MutableStateFlow(1)

    private val _tcpAverageDelay = MutableStateFlow(0L)
    val tcpAverageDelay: StateFlow<Long> = _tcpAverageDelay

    private val _udpAverageDelay = MutableStateFlow(0L)
    val udpAverageDelay: StateFlow<Long> = _udpAverageDelay

    fun connectTcpClient(serverIp: String, serverPort: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpClient = TcpClient(serverIp, serverPort).apply {
                    connect()
                }
                startListeningForTcpMessages()
                withContext(Dispatchers.Main) {
                    _isConnected.value = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isConnected.value = false
                }
            }
        }
    }

    fun sendTcpMessage(message: MessageObject) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpServer.resetMetrics()
                repeat(numOfMessages.value) {
                    tcpClient?.sendMessage(message)
                    //updateAverageDelay(tcpServer.averageDelay)
                    //Log.d(TAG, "Count: " + tcpServer.messageCount);
                    withContext(Dispatchers.Main) {
                        addMessage(message)

                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun disconnectTcpClient() {
        viewModelScope.launch(Dispatchers.IO) {
            tcpClient?.close()
            tcpClient = null
            withContext(Dispatchers.Main) {
                _isConnected.value = false
            }
        }
    }

    fun connectUdpClient(serverIp: String, serverPort: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                udpClient = UdpClient(serverIp, serverPort)
                withContext(Dispatchers.Main) {
                    _isConnected.value = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _isConnected.value = false
                }
            }
        }
    }

    fun sendUdpMessage(message: MessageObject) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                udpServer.resetMetrics()
                repeat(numOfMessages.value) {
                    udpClient?.sendMessage(message)
                    withContext(Dispatchers.Main) {
                        addMessage(message)

                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun disconnectUdpClient() {
        viewModelScope.launch(Dispatchers.IO) {
            udpClient = null
            withContext(Dispatchers.Main) {
                _isConnected.value = false
            }
        }
    }

    fun startTcpServer(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpServer.start(port)
                withContext(Dispatchers.Main) {
                    _isConnected.value = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun startUdpServer(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                udpServer.setMessageListener { message ->
                    viewModelScope.launch(Dispatchers.Main) {
                        addMessage(message)
                    }
                }
                udpServer.start(port)
                withContext(Dispatchers.Main) {
                    _isConnected.value = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopTcpServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpServer.stop()
                withContext(Dispatchers.Main) {
                    _isConnected.value = false
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun stopUdpServer() {
        viewModelScope.launch(Dispatchers.IO) {
            udpServer.stop()
            withContext(Dispatchers.Main) {
                _isConnected.value = false
            }
        }
    }

    fun sendTcpServerMessage(message: MessageObject) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpServer.sendMessageToAll(message)
                withContext(Dispatchers.Main) {
                    addMessage(message)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun addMessage(message: MessageObject) {
        viewModelScope.launch(Dispatchers.Main) {
            val updatedMessages = _messages.value.toMutableList()
            updatedMessages.add(message)
            _messages.value = updatedMessages
            Log.d(TAG, "COUNT: " + tcpServer.messageCount);
            updateTcpAverageDelay(tcpServer.averageDelay)
            updateUdpAverageDelay(udpServer.averageDelay)

        }
    }

    private fun startListeningForTcpMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val message = tcpClient?.receiveMessage() ?: break
                    withContext(Dispatchers.Main) {
                        addMessage(message)
                    }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateTcpAverageDelay(delay: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            _tcpAverageDelay.value = delay
        }
    }


    private fun updateUdpAverageDelay(delay: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            _udpAverageDelay.value = delay
        }
    }

}
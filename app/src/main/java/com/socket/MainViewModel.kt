package com.socket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socket.data.TcpClient
import com.socket.data.UdpClient
import com.socket.data.TcpServer
import com.socket.data.UdpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private var tcpClient: TcpClient? = null
    private var udpClient: UdpClient? = null

    private val tcpServer = TcpServer { message ->
        addMessage("$message")
    }
    private val udpServer = UdpServer()

    fun connectTcpClient(serverIp: String, serverPort: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            tcpClient = TcpClient(serverIp, serverPort).apply {
                connect()
            }
            startListeningForTcpMessages()
            withContext(Dispatchers.Main) {
                _isConnected.value = true
            }

        }
    }

    fun sendTcpMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpClient?.sendMessage("Client: $message")
                withContext(Dispatchers.Main) {
                    addMessage("Client: $message")
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

    fun sendUdpMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                udpClient?.sendMessage("Client: $message")
                withContext(Dispatchers.Main) {
                    addMessage("Client: $message")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun disconnectUdpClient() {
        viewModelScope.launch(Dispatchers.IO) {
            udpClient?.close()
            udpClient = null
            withContext(Dispatchers.Main) {
                _isConnected.value = false
            }
        }
    }

    fun startTcpServer(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            tcpServer.start(port)
            withContext(Dispatchers.Main) {
                _isConnected.value = true
            }

        }
    }

    fun startUdpServer(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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

    fun sendServerMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpServer.sendMessageToAll("Server: $message")
                withContext(Dispatchers.Main) {
                    addMessage("Server: $message")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun addMessage(message: String) {
        _messages.value = _messages.value + message
    }

    private fun startListeningForTcpMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(tcpClient?.getInputStream()))
                while (true) {
                    val message = reader.readLine() ?: break
                    withContext(Dispatchers.Main) {
                        addMessage(message)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

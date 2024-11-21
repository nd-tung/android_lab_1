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
import java.io.IOException

class MainViewModel : ViewModel() {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private var tcpClient: TcpClient? = null
    private var udpClient: UdpClient? = null

    private val tcpServer = TcpServer { message ->
        addMessage("Server: $message")
    }
    private val udpServer = UdpServer()

    fun connectTcpClient(serverIp: String, serverPort: Int) {
        viewModelScope.launch {
            tcpClient = TcpClient(serverIp, serverPort).apply {
                connect()
            }
            _isConnected.value = true
        }
    }

    fun sendTcpMessage(message: String) {
        viewModelScope.launch {
            tcpClient?.sendMessage(message)
            addMessage("Client: $message")
        }
    }

    fun connectUdpClient(serverIp: String, serverPort: Int) {
        viewModelScope.launch {
            udpClient = UdpClient(serverIp, serverPort)
            _isConnected.value = true
        }
    }

    fun sendUdpMessage(message: String) {
        viewModelScope.launch {
            udpClient?.sendMessage(message)
            addMessage("Client: $message")
            udpClient?.close()
        }
    }

    fun startTcpServer(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpServer.start(port)
                _isConnected.value = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun startUdpServer(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                udpServer.start(port)
                _isConnected.value = true

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun sendServerMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tcpServer.sendMessageToAll(message)
            addMessage("Server: $message")
        }
    }

    private fun addMessage(message: String) {
        _messages.value = _messages.value + message
    }
}
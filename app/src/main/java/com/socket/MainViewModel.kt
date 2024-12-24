// MainViewModel.kt (Updated to incorporate UDP and existing TCP logic)
package com.socket

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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.ObjectInputStream

class MainViewModel : ViewModel() {

    // StateFlow to keep track of connection status
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    // StateFlow to keep track of the list of messages (now of type MessageObject)
    private val _messages = MutableStateFlow<List<MessageObject>>(emptyList())
    val messages: StateFlow<List<MessageObject>> = _messages

    // TCP and UDP clients
    private var tcpClient: TcpClient? = null
    private var udpClient: UdpClient? = null

    // TCP Server setup with message callback
    private val tcpServer = TcpServer(object : TcpServer.MessageCallback {
        // Handling the received string messages
        override fun onMessageReceived(message: String) {
            addMessage(MessageObject("TCP Server received: $message", System.currentTimeMillis()))
        }

        // Handling the received MessageObject
        override fun onObjectReceived(message: MessageObject) {
            addMessage(MessageObject("TCP Server received object: ${message.message}", System.currentTimeMillis()))
        }
    })

    private val udpServer = UdpServer()

    // TCP Client methods

    // Connect to TCP server with provided IP and port
    fun connectTcpClient(serverIp: String, serverPort: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Initialize and connect the TCP client
                tcpClient = TcpClient(serverIp, serverPort).apply {
                    connect()
                }
                // Start listening for incoming messages
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

    // Send a single TCP message
    fun sendTcpMessage(message: MessageObject) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpClient?.sendMessage(message)
                withContext(Dispatchers.Main) {
                    addMessage(MessageObject("Client (TCP): ${message.message}", System.currentTimeMillis()))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Send message to all connected TCP clients
    fun sendTcpServerMessage(message: MessageObject) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tcpServer.sendMessageToAll(message)
                withContext(Dispatchers.Main) {
                    addMessage(MessageObject("Server (TCP): ${message.message}", System.currentTimeMillis()))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Send multiple TCP messages
    fun sendMassTcpMessage(message: MessageObject, numPackets: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Sending the message multiple times
                for (i in 0 until numPackets) {
                    tcpClient?.sendMessage(message)
                    withContext(Dispatchers.Main) {
                        addMessage(MessageObject("Client (TCP): ${message.message}", System.currentTimeMillis()))
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Disconnect TCP client
    fun disconnectTcpClient() {
        viewModelScope.launch(Dispatchers.IO) {
            tcpClient?.close()
            tcpClient = null
            withContext(Dispatchers.Main) {
                _isConnected.value = false
            }
        }
    }

    // UDP Client methods

    // Connect to UDP server with provided IP and port
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

    // Send a single UDP message
    fun sendUdpMessage(message: MessageObject) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                udpClient?.sendMessage(message)
                withContext(Dispatchers.Main) {
                    addMessage(MessageObject("Client (UDP): ${message.message}", System.currentTimeMillis()))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Send multiple UDP messages
    fun sendMassUdpMessage(message: MessageObject, numPackets: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Sending the message multiple times
                for (i in 0 until numPackets) {
                    udpClient?.sendMessage(message)
                    withContext(Dispatchers.Main) {
                        addMessage(MessageObject("Client (UDP): ${message.message}", System.currentTimeMillis()))
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Disconnect UDP client
    fun disconnectUdpClient() {
        viewModelScope.launch(Dispatchers.IO) {
            udpClient = null
            withContext(Dispatchers.Main) {
                _isConnected.value = false
            }
        }
    }

    // TCP Server methods

    // Start the TCP server on the given port
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

    // Stop the TCP server
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



    // UDP Server methods

    // Start the UDP server on the given port
    fun startUdpServer(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Set message listener for UDP server
                udpServer.setMessageListener { message ->
                    viewModelScope.launch(Dispatchers.Main) {
                        addMessage(MessageObject("Server (UDP) received: ${message.message}", System.currentTimeMillis()))
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

    // Stop the UDP server
    fun stopUdpServer() {
        viewModelScope.launch(Dispatchers.IO) {
            udpServer.stop()
            withContext(Dispatchers.Main) {
                _isConnected.value = false
            }
        }
    }

    // Method to add a new message to the list
    private fun addMessage(message: MessageObject) {
        viewModelScope.launch(Dispatchers.Main) {
            val updatedMessages = _messages.value.toMutableList()
            updatedMessages.add(message)
            _messages.value = updatedMessages
        }
    }

    // Listen for incoming TCP messages and update the UI
    private fun startListeningForTcpMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val message = tcpClient?.readMessage()
                    if (message != null) {
                        withContext(Dispatchers.Main) {
                            addMessage(MessageObject("TCP Client received: ${message.message}", System.currentTimeMillis()))
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

package com.socket.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.socket.MainViewModel
import com.socket.model.MessageObject

@Composable
fun ClientScreen(navController: NavHostController, viewModel: MainViewModel) {
    // Local state for managing user inputs like IP, port, protocol, and messages
    var message by remember { mutableStateOf("") }
    var serverIp by remember { mutableStateOf("192.168.1.1") }
    var serverPort by remember { mutableStateOf(12345) }
    var protocol by remember { mutableStateOf("TCP") }
    var numberOfMessage by remember { mutableStateOf(10000) }

    // Collect the connection state and messages from the ViewModel
    val isConnected by viewModel.isConnected.collectAsState()
    val messages by viewModel.messages.collectAsState()

    // Context to show toast messages
    val context = LocalContext.current
    val toastFlag = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Server IP input field
        TextField(
            value = serverIp,
            onValueChange = { serverIp = it },
            label = { Text("Server IP") },
            enabled = !isConnected // Disable input when connected
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Server Port input field
        TextField(
            value = serverPort.toString(),
            onValueChange = { serverPort = it.toIntOrNull() ?: 12345 },
            label = { Text("Server Port") },
            enabled = !isConnected // Disable input when connected
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Number of messages input field
        TextField(
            value = numberOfMessage.toString(),
            onValueChange = { numberOfMessage = it.toIntOrNull() ?: 10000 },
            label = { Text("Number of messages") },
            enabled = !isConnected // Disable input when connected
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Protocol selection (TCP or UDP)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = protocol == "TCP",
                onClick = { protocol = "TCP" },
                enabled = !isConnected // Disable protocol selection when connected
            )
            Text("TCP")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = protocol == "UDP",
                onClick = { protocol = "UDP" },
                enabled = !isConnected // Disable protocol selection when connected
            )
            Text("UDP")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Connection status label
        Text(
            if (isConnected) "STATUS: running!" else "STATUS: stopped!",
            color = if (isConnected) Color.Green else Color.Red
        )

        // Connect/Disconnect Button
        Button(
            onClick = {
                // Handle connection and disconnection logic based on current state
                toastFlag.value = true
                try {
                    if (!isConnected) {
                        // Connect to the server based on the selected protocol
                        if (protocol == "TCP") {
                            viewModel.connectTcpClient(serverIp, serverPort)
                        } else {
                            viewModel.connectUdpClient(serverIp, serverPort)
                        }
                    } else {
                        // Disconnect from the server based on the selected protocol
                        if (protocol == "TCP") {
                            viewModel.disconnectTcpClient()
                        } else {
                            viewModel.disconnectUdpClient()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(if (isConnected) "Disconnect" else "Connect")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Message Display Area (List of received messages)
        Text("Messages", color = Color.Red, modifier = Modifier.padding(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(8.dp)
                .border(1.dp, Color.Gray)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display each received message
            messages.forEach {
                Text("${it.message}")
                Divider()
            }
        }

        // Message input field
        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            enabled = isConnected // Disable message input when not connected
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Send Message Button
        Button(
            onClick = {
                // Create a MessageObject and send it
                val messageObject = MessageObject(message, System.currentTimeMillis())
                if (protocol == "TCP") {
                    viewModel.sendTcpMessage(messageObject)
                } else {
                    viewModel.sendUdpMessage(messageObject)
                }
            },
            enabled = isConnected // Disable button when not connected
        ) {
            Text("Send Message")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Send Mass Messages Button
        Button(
            onClick = {
                // Send multiple messages in a batch (here we send 10 messages as an example)
                val messageObject = MessageObject(message, System.currentTimeMillis())
                if (protocol == "TCP") {
                    viewModel.sendMassTcpMessage(messageObject, 10)  // Sending 10 packets for example
                } else {
                    viewModel.sendMassUdpMessage(messageObject, 10)
                }
            },
            enabled = isConnected // Disable button when not connected
        ) {
            Text("Send Mass Messages")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
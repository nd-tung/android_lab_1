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
fun ServerScreen(navController: NavHostController, viewModel: MainViewModel) {

    // Local state for server port, protocol selection, and message input
    var serverPort by remember { mutableStateOf(12345) }
    var protocol by remember { mutableStateOf("TCP") }
    var serverMessage by remember { mutableStateOf("") } // This will hold the message input as String
    val isConnected by viewModel.isConnected.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val context = LocalContext.current

    // UI layout structure
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Server Port Input
        TextField(
            value = serverPort.toString(),
            onValueChange = { serverPort = it.toIntOrNull() ?: 12345 },
            label = { Text("Server Port") },
            enabled = !isConnected // Disable when connected
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Protocol selection (TCP or UDP)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = protocol == "TCP",
                onClick = { protocol = "TCP" },
                enabled = !isConnected // Disable when connected
            )
            Text("TCP")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = protocol == "UDP",
                onClick = { protocol = "UDP" },
                enabled = !isConnected // Disable when connected
            )
            Text("UDP")
        }

        // Connection status
        Text(
            if (isConnected) "STATUS: running!" else "STATUS: stopped!",
            color = if (isConnected) Color.Green else Color.Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Start/Stop Server button
        Button(
            onClick = {
                try {
                    if (!isConnected) {
                        // Start server based on selected protocol
                        if (protocol == "TCP") {
                            viewModel.startTcpServer(serverPort)
                            Toast.makeText(context, "TCP Server is started!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.startUdpServer(serverPort)
                            Toast.makeText(context, "UDP Server is started!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Stop server based on selected protocol
                        if (protocol == "TCP") {
                            viewModel.stopTcpServer()
                            Toast.makeText(context, "TCP Server is stopped!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.stopUdpServer()
                            Toast.makeText(context, "UDP Server is stopped!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(if (isConnected) "Stop Server" else "Start Server")
        }

        // Message Display Area
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
            // Display the messages
            messages.forEach {
                Text(it.message) // Display each message's content
                Divider()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message Input Field
        TextField(
            value = serverMessage,
            onValueChange = { serverMessage = it },
            label = { Text("Server Message") },
            enabled = isConnected && protocol == "TCP" // Only enabled for TCP protocol when connected
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Send Message Button
        Button(
            onClick = {
                // Convert the server message to MessageObject and send it
                val messageObject = MessageObject(serverMessage, System.currentTimeMillis())
                if (protocol == "TCP") {
                    viewModel.sendTcpServerMessage(messageObject) // Send TCP message
                }
                serverMessage = "" // Clear the input field after sending the message
            },
            enabled = isConnected && protocol == "TCP" // Only enabled for TCP when connected
        ) {
            Text("Send Message")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
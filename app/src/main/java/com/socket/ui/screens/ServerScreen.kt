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

    var serverPort by remember { mutableStateOf(12345) }
    var protocol by remember { mutableStateOf("TCP") }
    var serverMessage by remember { mutableStateOf("") }
    val isConnected by viewModel.isConnected.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = serverPort.toString(),
            onValueChange = { serverPort = it.toIntOrNull() ?: 12345 },
            label = { Text("Server Port") },
            enabled = !isConnected
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = protocol == "TCP",
                onClick = { protocol = "TCP" },
                enabled = !isConnected
            )
            Text("TCP")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = protocol == "UDP",
                onClick = { protocol = "UDP" },
                enabled = !isConnected
            )
            Text("UDP")
        }

        //connection status
        Text(if (isConnected) "STATUS: running!" else "STATUS: stopped!", color = Color.Red)

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                try {
                    if (!isConnected) {
                        if (protocol == "TCP") {
                            viewModel.startTcpServer(serverPort)
                            Toast.makeText(context, "TCP Server is started!", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            viewModel.startUdpServer(serverPort)
                            Toast.makeText(context, "UDP Server is started!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else if (protocol == "TCP") {
                        viewModel.stopTcpServer()
                        Toast.makeText(context, "Server is stopped!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.stopUdpServer()
                        Toast.makeText(context, "Server is stopped!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
        ) {
            if (isConnected)
                Text("Stop Server")
            else
                Text("Start Server")
        }

        //message display area
        Text("Messages", color = Color.Red, modifier = Modifier.padding(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
                .border(1.dp, Color.Gray)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Bottom
        ) {
            messages.forEach {
                Text("Source: ${it.source}, Message: ${it.message}, Timestamp: ${it.timestamp}")
                Divider()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        //message field
        TextField(
            value = serverMessage,
            onValueChange = { serverMessage = it },
            label = { Text("Server Message") },
            enabled = isConnected && protocol == "TCP",
        )
        Spacer(modifier = Modifier.height(8.dp))

        //send message button
        Button(
            onClick = {
                val messageObject = MessageObject(serverMessage, System.currentTimeMillis(), "Server")
                viewModel.sendTcpServerMessage(messageObject)
                //clear textfield
                serverMessage = ""
            },
            enabled = isConnected && protocol == "TCP"
        ) {
            Text("Send Message")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
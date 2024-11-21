package com.socket.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                try {
                    if (protocol == "TCP") {
                        viewModel.startTcpServer(serverPort)
                        Toast.makeText(context, "TCP Server is started!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.startUdpServer(serverPort)
                        Toast.makeText(context, "UDP Server is started!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isConnected
        ) {
            Text("Start Server")
        }


        //message display area
        Text("Messages", color = Color.Red, modifier = Modifier.padding(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
                .border(1.dp, Color.Gray),
            verticalArrangement = Arrangement.Bottom
        ) {
            messages.forEach {
                Text(it)
                Divider()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        //message field
        TextField(
            value = serverMessage,
            onValueChange = { serverMessage = it },
            label = { Text("Server Message") },
            enabled = isConnected
        )
        Spacer(modifier = Modifier.height(8.dp))

        //send message button
        Button(
            onClick = {
                viewModel.sendServerMessage(serverMessage)
            },
            enabled = isConnected
        ) {
            Text("Send Message")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
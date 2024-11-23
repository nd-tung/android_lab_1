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

@Composable
fun ClientScreen(navController: NavHostController, viewModel: MainViewModel) {
    var message by remember { mutableStateOf("") }
    var serverIp by remember { mutableStateOf("192.168.1.1") }
    var serverPort by remember { mutableStateOf(12345) }
    var protocol by remember { mutableStateOf("TCP") }
    val isConnected by viewModel.isConnected.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val context = LocalContext.current
    val toastFlag = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        //server ip field
        TextField(
            value = serverIp,
            onValueChange = { serverIp = it },
            label = { Text("Server IP") },
            enabled = !isConnected
        )
        Spacer(modifier = Modifier.height(8.dp))

        //server port field
        TextField(
            value = serverPort.toString(),
            onValueChange = { serverPort = it.toIntOrNull() ?: 12345 },
            label = { Text("Server Port") },
            enabled = !isConnected
        )
        Spacer(modifier = Modifier.height(8.dp))


        //protocol selection
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

        //connection status
        Text(if (isConnected) "STATUS: running!" else "STATUS: stopped!", color = Color.Red)

        //connect button
        Button(
            onClick = {
                toastFlag.value = true
                try {
                    if (!isConnected) {
                        if (protocol == "TCP") {
                            viewModel.connectTcpClient(serverIp, serverPort)

                        } else {
                            viewModel.connectUdpClient(serverIp, serverPort)
                        }
                    } else {
                        if (protocol == "TCP") {
                            viewModel.disconnectTcpClient()

                        } else {
                            viewModel.disconnectUdpClient()

                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
        ) {
            if(isConnected)
                Text("Disconnect")
            else
                Text("Connect")
        }
        Spacer(modifier = Modifier.height(8.dp))

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
                Text(it)
                Divider()
            }
        }

        //message field
        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            enabled = isConnected
        )
        Spacer(modifier = Modifier.height(8.dp))

        //send message button
        Button(
            onClick = {
                if (protocol == "TCP") {
                    viewModel.sendTcpMessage(message)
                } else {
                    viewModel.sendUdpMessage(message)
                }
            },
            enabled = isConnected
        ) {
            Text("Send Message")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
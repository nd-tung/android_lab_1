package com.socket.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen (
    val route: String,
    val title: String,
    val icon: ImageVector
){
    object Client : BottomBarScreen("client", "Client", Icons.Default.Call)
    object Server : BottomBarScreen("server", "Server", Icons.Default.Home)
}
package com.socket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.socket.MainViewModel
import com.socket.ui.screens.ServerScreen
import com.socket.ui.screens.ClientScreen

@Composable
fun BottomNavGraph(navController: NavHostController, viewModel : MainViewModel) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Client.route
    ){
        composable(route = BottomBarScreen.Client.route){
            ClientScreen(navController = navController, viewModel = viewModel)
        }
        composable(route = BottomBarScreen.Server.route){
            ServerScreen(navController = navController, viewModel = viewModel)
        }
    }
}
package com.myemerg.myemergdemo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myemerg.myemergdemo.R
import com.myemerg.myemergdemo.ui.theme.MyEmergTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEmergAppBar() {
    TopAppBar(
        title = { Text(
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.titleLarge,
            text = stringResource(id = R.string.prepare_visit_screen_title)
        ) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(),
        navigationIcon = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button)
                )
            }
        }
    )
}

@Composable
fun MyEmergApp() {
    MyEmergTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val fullRoute = backStackEntry?.destination?.route ?: MyEmergDestinations.PrepareVisit.name
        val currentScreen = MyEmergDestinations.fromRoute(fullRoute)

        Scaffold(
            Modifier.background(Color.Transparent),
            topBar = {
                if (currentScreen == MyEmergDestinations.PrepareVisit) MyEmergAppBar()
            }
        ) {
            MyEmergNavGraph(
                navController = navController,
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(Color.Transparent)
            )
        }
    }
}

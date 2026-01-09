package com.davidstudioz.david

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidstudioz.david.ui.theme.DavidAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DavidAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var isListening by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("🤖", fontSize = 64.sp)
            Text(
                "DAVID AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Voice-First Assistant",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        // Main Content
        Spacer(modifier = Modifier.weight(1f))
        
        // Voice Button
        FloatingActionButton(
            onClick = { isListening = !isListening },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Voice Input",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
package com.example.fastconnect.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * NotificationInboxActivity — Implemented using Jetpack Compose (Required by Assignment #04).
 *
 * Displays a list of real-time campus notifications and system alerts.
 * Demonstrates a modern declarative UI design.
 */
class NotificationInboxActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotificationScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBack: () -> Unit) {
    val notifications = getSampleNotifications()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Campus Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3F51B5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notifications yet", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = notification.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.message,
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.time,
                fontSize = 12.sp,
                color = Color(0xFFBDBDBD),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

data class NotificationModel(
    val title: String,
    val message: String,
    val time: String
)

fun getSampleNotifications(): List<NotificationModel> {
    return listOf(
        NotificationModel("Fee Deadline Extended", "The deadline for Spring 2026 fee payment has been extended to May 15.", "2 hours ago"),
        NotificationModel("New Event Posted", "FAST Computing Society added a new workshop: Cloud Native Development.", "5 hours ago"),
        NotificationModel("Exam Schedule Out", "Final exam schedule for Semester Spring 2026 is now available on FAST Hub.", "Yesterday"),
        NotificationModel("Library Hours", "The library will be open 24/7 during the final exam week.", "2 days ago"),
        NotificationModel("Guest Speaker", "Join us for a talk by industry leaders on 'The Future of AI'. Room 201.", "3 days ago")
    )
}

package com.example.musicdownloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicdownloader.data.remote.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(RetrofitClient.BASE_URL) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 服务器设置
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "服务器设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("服务器地址") },
                        placeholder = { Text("http://10.0.2.2:8000") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Computer, null)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "提示：使用 Android 模拟器时，10.0.2.2 指向宿主机",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            RetrofitClient.updateBaseUrl(serverUrl)
                            showSaveSuccess = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存设置")
                    }
                }
            }

            // 关于
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "关于",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ListItem(
                        headlineContent = { Text("版本") },
                        supportingContent = { Text("1.0.0") },
                        leadingContent = {
                            Icon(Icons.Default.Info, null)
                        }
                    )
                    
                    ListItem(
                        headlineContent = { Text("后端") },
                        supportingContent = { Text("Python FastAPI + musicdl") },
                        leadingContent = {
                            Icon(Icons.Default.Code, null)
                        }
                    )
                }
            }
        }

        // 保存成功提示
        if (showSaveSuccess) {
            AlertDialog(
                onDismissRequest = { showSaveSuccess = false },
                title = { Text("保存成功") },
                text = { Text("服务器地址已更新") },
                confirmButton = {
                    TextButton(onClick = { showSaveSuccess = false }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

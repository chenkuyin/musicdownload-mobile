package com.example.musicdownloader.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicdownloader.data.model.Song
import com.example.musicdownloader.ui.viewmodel.SearchViewModel

// 辅助函数：将 URI 转换为路径
private fun getPathFromUri(context: Context, uri: Uri): String? {
    // 尝试获取真实路径
    if (DocumentsContract.isDocumentUri(context, uri)) {
        val docId = DocumentsContract.getTreeDocumentId(uri)
        val split = docId.split(":")
        if (split.size >= 2) {
            val type = split[0]
            val path = split[1]
            return when (type) {
                "primary" -> "/storage/emulated/0/$path"
                else -> "/storage/$type/$path"
            }
        }
    }
    // 如果无法解析，返回 URI 字符串
    return uri.toString()
}

// 颜色定义 - 匹配原项目
val PrimaryBlue = Color(0xFF0078d4)
val PrimaryBlueHover = Color(0xFF1089e5)
val PrimaryBluePressed = Color(0xFF005a9e)
val SuccessGreen = Color(0xFF10b981)
val SuccessGreenHover = Color(0xFF059669)
val ErrorRed = Color(0xFFdc2626)
val Gray50 = Color(0xFFf9fafb)
val Gray100 = Color(0xFFf3f4f6)
val Gray200 = Color(0xFFe5e7eb)
val Gray300 = Color(0xFFd1d5db)
val Gray400 = Color(0xFF9ca3af)
val Gray500 = Color(0xFF6b7280)
val Gray600 = Color(0xFF4b5563)
val Gray700 = Color(0xFF374151)
val Gray800 = Color(0xFF1f2937)
val LightBlue50 = Color(0xFFe0f2fe)
val LightBlue700 = Color(0xFF0369a1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToDownloads: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 目录选择器
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // 持久化权限
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            // 转换为实际路径
            val path = getPathFromUri(context, it)
            viewModel.updateSaveDir(path ?: it.toString())
        }
        viewModel.onDirectoryPickerDismissed()
    }

    // 触发目录选择器
    LaunchedEffect(uiState.triggerDirectoryPicker) {
        if (uiState.triggerDirectoryPicker) {
            directoryPickerLauncher.launch(null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🎵 音乐下载器",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Gray100
                ),
                actions = {
                    IconButton(onClick = onNavigateToDownloads) {
                        BadgedBox(
                            badge = {
                                if (uiState.downloadCount > 0) {
                                    Badge { Text(uiState.downloadCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Download, "下载列表")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        },
        containerColor = Gray100
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            // ========== 1. 音乐源选择 ==========
            SourceSelectionCard(
                sources = uiState.sources,
                selectedSources = uiState.selectedSources,
                onSourceToggle = viewModel::onSourceToggle
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ========== 2. 设置行 ==========
            SettingsCard(
                searchLimit = uiState.searchLimit,
                onLimitChange = viewModel::onLimitChange,
                saveDir = uiState.saveDir,
                onBrowseDir = { viewModel.onBrowseSaveDir() },
                autoDownload = uiState.autoDownload,
                onAutoDownloadChange = viewModel::onAutoDownloadChange
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ========== 3. 搜索行 ==========
            SearchCard(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = { viewModel.search() },
                searchMode = uiState.searchMode,
                onModeChange = viewModel::onSearchModeChange
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ========== 4. 批量操作行 ==========
            if (uiState.songs.isNotEmpty()) {
                BatchOperationCard(
                    selectedSongs = uiState.selectedSongs,
                    totalSongs = uiState.songs.size,
                    downloadScope = uiState.downloadScope,
                    onScopeChange = viewModel::onDownloadScopeChange,
                    onDownload = { viewModel.onDownloadSelected(context) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ========== 5. 结果表格 ==========
            when {
                uiState.isLoading -> {
                    LoadingView()
                }
                uiState.error != null -> {
                    ErrorView(message = uiState.error!!)
                }
                uiState.songs.isNotEmpty() -> {
                    ResultsTable(
                        songs = uiState.songs,
                        selectedSongs = uiState.selectedSongs,
                        onSongSelect = viewModel::onSongSelect,
                        onDownload = { viewModel.downloadSong(it) }
                    )
                }
                else -> {
                    EmptyView()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 下载进度对话框
        if (uiState.downloadMessage.isNotEmpty()) {
            DownloadProgressDialog(
                message = uiState.downloadMessage,
                progress = uiState.downloadProgress,
                onDismiss = viewModel::clearDownloadStatus
            )
        }
    }
}

// ========== 1. 音乐源选择卡片 ==========
@Composable
private fun SourceSelectionCard(
    sources: List<com.example.musicdownloader.data.model.MusicSource>,
    selectedSources: Set<String>,
    onSourceToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 标题
            Text(
                text = "选择音乐源 (${selectedSources.size}/${sources.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // 使用 FlowRow 风格的布局 - 自适应排列
            if (sources.isEmpty()) {
                Text(
                    text = "正在加载音乐源...",
                    fontSize = 12.sp,
                    color = Gray500,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // 使用 Column + Row 实现流式布局效果
                val itemsPerRow = 3
                val rows = sources.chunked(itemsPerRow)
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rows.forEach { rowSources ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowSources.forEach { source ->
                                SourceCheckbox(
                                    name = source.name,
                                    checked = selectedSources.contains(source.id),
                                    onCheckedChange = { onSourceToggle(source.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // 填充剩余空间
                            repeat(itemsPerRow - rowSources.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceCheckbox(
    name: String,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onCheckedChange() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            modifier = Modifier.size(18.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryBlue,
                uncheckedColor = Gray400
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            color = if (checked) Gray800 else Gray600,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ========== 2. 设置卡片 ==========
@Composable
private fun SettingsCard(
    searchLimit: Int,
    onLimitChange: (Int) -> Unit,
    saveDir: String,
    onBrowseDir: () -> Unit,
    autoDownload: Boolean,
    onAutoDownloadChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 单源获取数量
                Text(
                    text = "单源获取数量：",
                    fontSize = 12.sp,
                    color = Gray700
                )
                
                // SpinBox 风格
                Row(
                    modifier = Modifier
                        .width(90.dp)
                        .height(32.dp)
                        .border(1.dp, Gray300, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (searchLimit > 1) onLimitChange(searchLimit - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("▼", fontSize = 10.sp, color = Gray500)
                    }
                    Text(
                        text = "$searchLimit",
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Gray800
                    )
                    IconButton(
                        onClick = { if (searchLimit < 100) onLimitChange(searchLimit + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("▲", fontSize = 10.sp, color = Gray500)
                    }
                }

                Text(
                    text = "条",
                    fontSize = 12.sp,
                    color = Gray600,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 保存目录
                Text(
                    text = "保存目录：",
                    fontSize = 12.sp,
                    color = Gray700
                )
                
                Text(
                    text = saveDir,
                    fontSize = 11.sp,
                    color = Gray600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Button(
                    onClick = { onBrowseDir() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("📁 浏览...", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 自动下载复选框
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAutoDownloadChange(!autoDownload) }
            ) {
                Checkbox(
                    checked = autoDownload,
                    onCheckedChange = { onAutoDownloadChange(it) },
                    modifier = Modifier.size(18.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = ErrorRed,
                        uncheckedColor = Gray400
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🚀 搜索后自动下载全部",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            }
        }
    }
}

// ========== 3. 搜索卡片 ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    searchMode: String,
    onModeChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 搜索模式下拉框
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.width(110.dp)
            ) {
                OutlinedTextField(
                    value = searchMode,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        color = Gray800
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Gray300
                    ),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("搜索歌曲", fontSize = 12.sp) },
                        onClick = {
                            onModeChange("搜索歌曲")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("解析歌单链接", fontSize = 12.sp) },
                        onClick = {
                            onModeChange("解析歌单链接")
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 搜索输入框
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (searchMode == "搜索歌曲")
                            "请输入关键词或输入歌单链接，按回车键也可搜索..."
                        else
                            "请输入歌单链接...",
                        fontSize = 12.sp,
                        color = Gray400
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 13.sp,
                    color = Gray800
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Gray300,
                    cursorColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(6.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 立即搜索按钮
            Button(
                onClick = onSearch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("🔍 立即搜索", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ========== 4. 批量操作卡片 ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchOperationCard(
    selectedSongs: Set<String>,
    totalSongs: Int,
    downloadScope: String,
    onScopeChange: (String) -> Unit,
    onDownload: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "下载范围：",
                fontSize = 12.sp,
                color = Gray700
            )

            // 范围下拉框
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.width(90.dp)
            ) {
                OutlinedTextField(
                    value = downloadScope,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
                        color = Gray800
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Gray300
                    ),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("勾选", "全选", "未勾选").forEach { scope ->
                        DropdownMenuItem(
                            text = { Text(scope, fontSize = 12.sp) },
                            onClick = {
                                onScopeChange(scope)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 下载按钮
            Button(
                onClick = onDownload,
                enabled = selectedSongs.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    disabledContainerColor = Gray400
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("⬇️ 下载选中内容", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ========== 5. 结果表格 ==========
@Composable
private fun ResultsTable(
    songs: List<Song>,
    selectedSongs: Set<String>,
    onSongSelect: (String) -> Unit,
    onDownload: (Song) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // 表头
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Gray100)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("选择", 40.dp)
                TableHeaderCell("专辑封面", 65.dp)
                TableHeaderCell("歌曲名", 140.dp)
                TableHeaderCell("歌手", 100.dp)
                TableHeaderCell("专辑", 100.dp)
                TableHeaderCell("格式", 50.dp)
                TableHeaderCell("大小", 70.dp)
                TableHeaderCell("时长", 60.dp)
                TableHeaderCell("来源", 60.dp)
            }

            Divider(color = Gray200, thickness = 1.dp)

            // 表格内容
            songs.forEachIndexed { index, song ->
                val isSelected = selectedSongs.contains(song.identifier)
                val backgroundColor = if (index % 2 == 0) Color.White else Gray50

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clickable { onSongSelect(song.identifier) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 选择
                    Box(
                        modifier = Modifier.width(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onSongSelect(song.identifier) },
                            modifier = Modifier.size(18.dp),
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryBlue,
                                uncheckedColor = Gray400
                            )
                        )
                    }

                    // 专辑封面
                    Box(
                        modifier = Modifier.width(65.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!song.coverUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = song.coverUrl,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp)
                            )
                        } else {
                            Text(
                                "🎵",
                                fontSize = 20.sp,
                                color = Gray300,
                                modifier = Modifier.size(44.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 歌曲名
                    TableCell(
                        text = song.songName,
                        width = 140.dp,
                        fontWeight = FontWeight.Medium
                    )

                    // 歌手
                    TableCell(text = song.singers, width = 100.dp)

                    // 专辑
                    TableCell(text = song.album, width = 100.dp)

                    // 格式
                    TableCell(
                        text = song.ext.uppercase(),
                        width = 50.dp,
                        textAlign = TextAlign.Center
                    )

                    // 大小
                    TableCell(
                        text = song.fileSize,
                        width = 70.dp,
                        textAlign = TextAlign.Center
                    )

                    // 时长
                    TableCell(
                        text = song.duration,
                        width = 60.dp,
                        textAlign = TextAlign.Center
                    )

                    // 来源
                    TableCell(
                        text = song.source,
                        width = 60.dp,
                        textAlign = TextAlign.Center
                    )
                }

                if (index < songs.size - 1) {
                    Divider(color = Gray100, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Gray600,
        modifier = Modifier.width(width),
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Gray700,
        fontWeight = fontWeight,
        modifier = Modifier.width(width),
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

// ========== 状态视图 ==========
@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrimaryBlue)
            Spacer(modifier = Modifier.height(12.dp))
            Text("搜索中...", fontSize = 13.sp, color = Gray600)
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = ErrorRed
        )
    }
}

@Composable
private fun EmptyView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "请输入关键词搜索音乐",
            fontSize = 13.sp,
            color = Gray500
        )
    }
}

// ========== 下载进度对话框 ==========
@Composable
private fun DownloadProgressDialog(
    message: String,
    progress: Float,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (progress >= 100f || message.contains("失败")) onDismiss() },
        title = { 
            Text(
                "下载进度",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(message, fontSize = 12.sp, color = Gray700)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryBlue,
                    trackColor = Gray200
                )
                Text(
                    "${progress.toInt()}%",
                    fontSize = 11.sp,
                    color = Gray600,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        },
        confirmButton = {
            if (progress >= 100f || message.contains("失败")) {
                TextButton(onClick = onDismiss) {
                    Text("确定", color = PrimaryBlue)
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(10.dp)
    )
}

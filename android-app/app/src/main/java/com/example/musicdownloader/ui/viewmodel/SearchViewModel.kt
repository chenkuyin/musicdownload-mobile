package com.example.musicdownloader.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicdownloader.data.model.MusicSource
import com.example.musicdownloader.data.model.Song
import com.example.musicdownloader.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

data class SearchUiState(
    // 搜索相关
    val query: String = "",
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val error: String? = null,

    // 音源设置（与原项目一致）
    val sources: List<MusicSource> = emptyList(),
    val selectedSources: Set<String> = setOf("KuwoMusicClient", "KugouMusicClient"), // 默认酷我、酷狗

    // 搜索设置（与原项目一致）
    val searchLimit: Int = 10,
    val saveDir: String = "/storage/emulated/0/Music/已下载音乐",
    val autoDownload: Boolean = false,
    val searchMode: String = "搜索歌曲", // 搜索歌曲 / 解析歌单链接

    // 批量选择
    val selectedSongs: Set<String> = emptySet(),
    val downloadScope: String = "勾选", // 勾选 / 全选 / 未勾选

    // 下载状态
    val downloadTaskId: String? = null,
    val downloadProgress: Float = 0f,
    val downloadMessage: String = "",
    val downloadCount: Int = 0,
    val isDownloading: Boolean = false,
    val downloadedFiles: List<String> = emptyList(), // 已下载到本地的文件路径

    // 目录选择触发器
    val triggerDirectoryPicker: Boolean = false
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadSources()
    }

    private fun loadSources() {
        viewModelScope.launch {
            repository.getSources().onSuccess { sources ->
                _uiState.update { it.copy(sources = sources) }
            }
        }
    }

    // ========== 搜索相关 ==========
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null) }
    }

    fun search() {
        if (_uiState.value.query.isNotBlank()) {
            if (_uiState.value.searchMode == "解析歌单链接") {
                parsePlaylist(_uiState.value.query)
            } else {
                performSearch(_uiState.value.query)
            }
        }
    }

    fun onSearchModeChange(mode: String) {
        _uiState.update { it.copy(searchMode = mode) }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedSongs = emptySet()) }

            val sourcesToSearch = _uiState.value.selectedSources.toList()

            repository.searchMusic(query, sourcesToSearch, _uiState.value.searchLimit)
                .onSuccess { songs ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            songs = songs,
                            error = if (songs.isEmpty()) "未找到相关歌曲" else null,
                            selectedSongs = if (it.autoDownload) songs.map { s -> s.identifier }.toSet() else emptySet()
                        )
                    }
                    // 自动下载 - 需要在 UI 层传入 context
                    // 暂时禁用自动下载，或在外部处理
                    // if (_uiState.value.autoDownload && songs.isNotEmpty()) {
                    //     downloadSelectedSongs(context)
                    // }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "搜索失败"
                        )
                    }
                }
        }
    }

    private fun parsePlaylist(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.parsePlaylist(url)
                .onSuccess { songs ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            songs = songs,
                            error = if (songs.isEmpty()) "歌单为空或解析失败" else null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "歌单解析失败"
                        )
                    }
                }
        }
    }

    // ========== 音源选择 ==========
    fun onSourceToggle(sourceId: String) {
        _uiState.update { state ->
            val current = state.selectedSources
            val updated = if (current.contains(sourceId)) {
                current - sourceId
            } else {
                current + sourceId
            }
            state.copy(selectedSources = updated)
        }
    }

    // ========== 搜索设置 ==========
    fun onLimitChange(limit: Int) {
        _uiState.update { it.copy(searchLimit = limit) }
    }

    fun onBrowseSaveDir() {
        // 触发目录选择器
        _uiState.update { it.copy(triggerDirectoryPicker = true) }
    }

    fun onDirectoryPickerDismissed() {
        _uiState.update { it.copy(triggerDirectoryPicker = false) }
    }

    // 供Activity调用更新目录
    fun updateSaveDir(newDir: String) {
        _uiState.update { it.copy(saveDir = newDir, triggerDirectoryPicker = false) }
    }

    fun onAutoDownloadChange(enabled: Boolean) {
        _uiState.update { it.copy(autoDownload = enabled) }
    }

    // ========== 批量选择 ==========
    fun onSongSelect(identifier: String) {
        _uiState.update { state ->
            val current = state.selectedSongs
            val updated = if (current.contains(identifier)) {
                current - identifier
            } else {
                current + identifier
            }
            state.copy(selectedSongs = updated)
        }
    }

    // ========== 下载范围 ==========
    fun onDownloadScopeChange(scope: String) {
        _uiState.update { state ->
            val newSelectedSongs = when (scope) {
                "全选" -> state.songs.map { it.identifier }.toSet()
                "未勾选" -> state.songs.map { it.identifier }.toSet() - state.selectedSongs
                else -> state.selectedSongs
            }
            state.copy(downloadScope = scope, selectedSongs = newSelectedSongs)
        }
    }

    // ========== 下载相关 ==========
    fun downloadSong(song: Song) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    downloadTaskId = null,
                    downloadProgress = 0f,
                    downloadMessage = "创建下载任务...",
                    isDownloading = true
                )
            }

            repository.createDownloadTask(song)
                .onSuccess { taskId ->
                    _uiState.update { it.copy(downloadTaskId = taskId) }
                    pollDownloadStatus(taskId)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            downloadMessage = "下载失败: ${error.message}",
                            isDownloading = false
                        )
                    }
                }
        }
    }

    fun onDownloadSelected(context: Context) {
        downloadSelectedSongs(context)
    }

    private fun downloadSelectedSongs(context: Context) {
        val selected = _uiState.value.selectedSongs
        if (selected.isEmpty()) return

        val songsToDownload = _uiState.value.songs.filter {
            selected.contains(it.identifier)
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    downloadMessage = "开始下载 ${songsToDownload.size} 首歌曲...",
                    downloadProgress = 0f,
                    isDownloading = true
                )
            }

            var successCount = 0
            var failCount = 0

            // 批量下载 - 等待每个任务完成
            songsToDownload.forEachIndexed { index, song ->
                _uiState.update {
                    it.copy(
                        downloadMessage = "正在下载: ${song.songName} (${index + 1}/${songsToDownload.size})"
                    )
                }

                repository.createDownloadTask(song)
                    .onSuccess { taskId ->
                        // 轮询等待下载完成
                        val completed = pollDownloadStatusSync(context, taskId, song.songName)
                        if (completed) {
                            successCount++
                        } else {
                            failCount++
                        }
                    }
                    .onFailure {
                        failCount++
                    }

                _uiState.update {
                    it.copy(
                        downloadProgress = (index + 1) * 100f / songsToDownload.size
                    )
                }
            }

            _uiState.update {
                it.copy(
                    downloadProgress = 100f,
                    downloadMessage = "下载完成！成功: $successCount, 失败: $failCount",
                    downloadCount = it.downloadCount + successCount,
                    selectedSongs = emptySet(),
                    isDownloading = false
                )
            }
        }
    }

    // 同步轮询下载状态，直到完成或失败
    private suspend fun pollDownloadStatusSync(context: Context, taskId: String, songName: String): Boolean {
        var attempts = 0
        val maxAttempts = 300 // 最多等待300秒（5分钟）

        while (attempts < maxAttempts) {
            val result = repository.getDownloadStatus(taskId)
            result.getOrNull()?.let { task ->
                _uiState.update {
                    it.copy(
                        downloadProgress = task.progress,
                        downloadMessage = task.message
                    )
                }

                if (task.status == "completed") {
                    // 下载完成后，从服务器获取文件并保存到本地
                    val fileName = task.fileName ?: "$songName.mp3"
                    saveFileToLocal(context, taskId, fileName)
                    return true
                }
                if (task.status == "failed") {
                    return false
                }
            }

            delay(1000) // 每秒轮询一次
            attempts++
        }

        return false // 超时
    }
    
    // 从服务器下载文件并保存到本地
    private suspend fun saveFileToLocal(context: Context, taskId: String, fileName: String) {
        _uiState.update { it.copy(downloadMessage = "正在保存到本地...") }
        
        repository.downloadFileToLocal(context, taskId, fileName, _uiState.value.saveDir)
            .onSuccess { file ->
                _uiState.update { state ->
                    state.copy(
                        downloadedFiles = state.downloadedFiles + file.absolutePath,
                        downloadMessage = "已保存: ${file.name}"
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(downloadMessage = "保存失败: ${error.message}") }
            }
    }

    private fun pollDownloadStatus(taskId: String) {
        repository.pollDownloadStatus(taskId)
            .onEach { task ->
                _uiState.update {
                    it.copy(
                        downloadProgress = task.progress,
                        downloadMessage = task.message,
                        isDownloading = task.status != "completed" && task.status != "failed"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearDownloadStatus() {
        _uiState.update {
            it.copy(
                downloadTaskId = null,
                downloadProgress = 0f,
                downloadMessage = "",
                isDownloading = false
            )
        }
    }
}

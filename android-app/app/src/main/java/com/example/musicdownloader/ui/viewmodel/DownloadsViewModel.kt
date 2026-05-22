package com.example.musicdownloader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicdownloader.data.model.DownloadedFile
import com.example.musicdownloader.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(
    val isLoading: Boolean = false,
    val files: List<DownloadedFile> = emptyList(),
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
    }

    fun loadDownloads() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.getDownloads()
                .onSuccess { files ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            files = files
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "获取下载列表失败"
                        )
                    }
                }
        }
    }

    fun deleteFile(filename: String) {
        viewModelScope.launch {
            repository.deleteDownload(filename)
                .onSuccess {
                    _uiState.update { it.copy(deleteSuccess = true) }
                    loadDownloads()  // 刷新列表
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(error = error.message ?: "删除失败")
                    }
                }
        }
    }

    fun clearDeleteStatus() {
        _uiState.update { it.copy(deleteSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024))
            bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

package com.example.musicdownloader.data.model

import com.google.gson.annotations.SerializedName

// ============ 音乐源 ============
data class MusicSource(
    val id: String,
    val name: String,
    val enabled: Boolean = true
)

data class SourcesResponse(
    val sources: List<MusicSource>
)

// ============ 搜索结果 ============
data class SearchResponse(
    val success: Boolean,
    val count: Int,
    val results: List<Song>
)

data class Song(
    @SerializedName("song_name")
    val songName: String,
    val singers: String,
    val album: String,
    val source: String,
    val identifier: String,
    val duration: String,
    @SerializedName("file_size")
    val fileSize: String,
    @SerializedName("cover_url")
    val coverUrl: String?,
    @SerializedName("download_url")
    val downloadUrl: String?,
    val ext: String
) {
    fun getDisplayTitle(): String = "$songName - $singers"
}

// ============ 下载请求 ============
data class DownloadRequest(
    val identifier: String,
    val source: String,
    @SerializedName("song_name")
    val songName: String,
    val singers: String,
    val album: String? = "",
    val ext: String? = "mp3",
    @SerializedName("download_url")
    val downloadUrl: String? = null,
    @SerializedName("cover_url")
    val coverUrl: String? = null,
    val duration: String? = "",
    @SerializedName("file_size")
    val fileSize: String? = ""
)

data class DownloadResponse(
    val success: Boolean,
    @SerializedName("task_id")
    val taskId: String
)

// ============ 下载任务状态 ============
data class DownloadTask(
    @SerializedName("task_id")
    val taskId: String,
    val status: String,  // pending, downloading, completed, failed
    val progress: Float,
    val message: String,
    @SerializedName("file_path")
    val filePath: String? = null,
    @SerializedName("file_name")
    val fileName: String? = null
) {
    fun isCompleted(): Boolean = status == "completed"
    fun isFailed(): Boolean = status == "failed"
    fun isDownloading(): Boolean = status == "downloading"
}

// ============ 已下载文件 ============
data class DownloadedFile(
    val name: String,
    val size: Long,
    val modified: Double,
    val path: String
)

data class DownloadsResponse(
    val files: List<DownloadedFile>
)

// ============ API 错误 ============
data class ApiError(
    val detail: String
)

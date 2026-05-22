package com.example.musicdownloader.data.repository

import android.content.Context
import android.os.Environment
import com.example.musicdownloader.data.model.*
import com.example.musicdownloader.data.remote.MusicApiService
import com.example.musicdownloader.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor() {
    
    // 每次使用时获取最新的 apiService，以支持动态修改服务器地址
    private val apiService: MusicApiService
        get() = RetrofitClient.getApiService()
    
    // ============ 音乐源 ============
    suspend fun getSources(): Result<List<MusicSource>> {
        return try {
            val response = apiService.getSources()
            if (response.isSuccessful) {
                Result.success(response.body()?.sources ?: emptyList())
            } else {
                Result.failure(Exception("获取音乐源失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ 搜索 ============
    suspend fun searchMusic(keyword: String, source: String? = null): Result<List<Song>> {
        return try {
            val response = apiService.searchMusic(keyword, source)
            if (response.isSuccessful) {
                Result.success(response.body()?.results ?: emptyList())
            } else {
                Result.failure(Exception("搜索失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchMusic(keyword: String, sources: List<String>, limit: Int): Result<List<Song>> {
        return try {
            // 多音源搜索 - 合并结果
            val allResults = mutableListOf<Song>()
            sources.forEach { source ->
                val response = apiService.searchMusic(keyword, source)
                if (response.isSuccessful) {
                    response.body()?.results?.let { songs ->
                        allResults.addAll(songs.take(limit))
                    }
                }
            }
            Result.success(allResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun parsePlaylist(url: String): Result<List<Song>> {
        return try {
            // TODO: 实现歌单解析 API
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ 下载 ============
    suspend fun createDownloadTask(song: Song): Result<String> {
        return try {
            val request = DownloadRequest(
                identifier = song.identifier,
                source = song.source,
                songName = song.songName,
                singers = song.singers,
                album = song.album,
                ext = song.ext,
                downloadUrl = song.downloadUrl,
                coverUrl = song.coverUrl,
                duration = song.duration,
                fileSize = song.fileSize
            )
            val response = apiService.createDownloadTask(request)
            if (response.isSuccessful) {
                Result.success(response.body()?.taskId ?: "")
            } else {
                Result.failure(Exception("创建下载任务失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDownloadStatus(taskId: String): Result<DownloadTask> {
        return try {
            val response = apiService.getDownloadStatus(taskId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("获取状态失败"))
            } else {
                Result.failure(Exception("获取状态失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 轮询下载状态
    fun pollDownloadStatus(taskId: String): Flow<DownloadTask> = flow {
        while (true) {
            val result = getDownloadStatus(taskId)
            result.getOrNull()?.let { task ->
                emit(task)
                if (task.isCompleted() || task.isFailed()) {
                    return@flow
                }
            }
            delay(1000)  // 每秒轮询一次
        }
    }.flowOn(Dispatchers.IO)
    
    // ============ 已下载文件 ============
    suspend fun getDownloads(): Result<List<DownloadedFile>> {
        return try {
            val response = apiService.getDownloads()
            if (response.isSuccessful) {
                Result.success(response.body()?.files ?: emptyList())
            } else {
                Result.failure(Exception("获取下载列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteDownload(filename: String): Result<Boolean> {
        return try {
            val response = apiService.deleteDownload(filename)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("删除失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ 下载文件到本地 ============
    suspend fun downloadFileToLocal(
        context: Context,
        taskId: String,
        fileName: String,
        saveDir: String,
        deleteAfterDownload: Boolean = true
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.downloadFile(taskId)
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("下载文件失败: ${response.code()}"))
            }
            
            val body = response.body()
                ?: return@withContext Result.failure(Exception("下载内容为空"))
            
            // 确保保存目录存在
            val targetDir = File(saveDir)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            
            // 保存文件
            val targetFile = File(targetDir, fileName)
            FileOutputStream(targetFile).use { outputStream ->
                body.byteStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            // 下载成功后删除后端文件
            if (deleteAfterDownload) {
                try {
                    apiService.deleteDownload(fileName)
                } catch (e: Exception) {
                    // 删除失败不影响本地保存成功
                    e.printStackTrace()
                }
            }
            
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 获取系统默认音乐目录
    fun getDefaultMusicDirectory(): String {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        return "${musicDir?.absolutePath ?: "/storage/emulated/0/Music"}/已下载音乐"
    }
}

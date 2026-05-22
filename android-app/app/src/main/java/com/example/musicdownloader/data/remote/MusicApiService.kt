package com.example.musicdownloader.data.remote

import com.example.musicdownloader.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface MusicApiService {
    
    @GET("/")
    suspend fun getStatus(): Response<Map<String, Any>>
    
    @GET("/sources")
    suspend fun getSources(): Response<SourcesResponse>
    
    @GET("/search")
    suspend fun searchMusic(
        @Query("keyword") keyword: String,
        @Query("source") source: String? = null
    ): Response<SearchResponse>
    
    @POST("/download")
    suspend fun createDownloadTask(
        @Body request: DownloadRequest
    ): Response<DownloadResponse>
    
    @GET("/download/{taskId}")
    suspend fun getDownloadStatus(
        @Path("taskId") taskId: String
    ): Response<DownloadTask>
    
    @GET("/downloads")
    suspend fun getDownloads(): Response<DownloadsResponse>
    
    @DELETE("/downloads/{filename}")
    suspend fun deleteDownload(
        @Path("filename") filename: String
    ): Response<Map<String, Any>>
    
    @GET("/download/{taskId}/file")
    @Streaming
    suspend fun downloadFile(
        @Path("taskId") taskId: String
    ): Response<okhttp3.ResponseBody>
}

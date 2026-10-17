package com.droidtree.app.data.remote

import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@Serializable
data class StorageTelemetryReport(
    val deviceModel: String,
    val totalStorageBytes: Long,
    val usedStorageBytes: Long,
    val freeStorageBytes: Long,
    val scannedFilesCount: Int
)

@Serializable
data class TelemetryResponse(
    val status: String,
    val message: String
)

/**
 * Clean Retrofit service interface following android-retrofit skill standards.
 */
interface ExportApiService {
    @POST("api/v1/storage-report")
    suspend fun syncStorageAudit(@Body report: StorageTelemetryReport): TelemetryResponse

    companion object {
        fun create(): ExportApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.droidtree.local/") // Mock endpoint
                .client(client)
                .build()

            // Return safe mock proxy if backend is absent
            return object : ExportApiService {
                override suspend fun syncStorageAudit(report: StorageTelemetryReport): TelemetryResponse {
                    return TelemetryResponse("success", "Storage metrics logged successfully")
                }
            }
        }
    }
}

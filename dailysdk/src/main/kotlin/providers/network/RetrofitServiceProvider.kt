package providers.network

import android.content.Context
import co.daily.dailyvideoapp.dailysdk.R
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import data.network.rest.RetrofitService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class RetrofitServiceProvider {

    companion object {

        private const val CONTENT_TYPE = "application/json"
        val json = Json { encodeDefaults = false; ignoreUnknownKeys = true }

        @Volatile
        private var instance: RetrofitService? = null

        fun initializeRetrofit(
            context: Context
        ): RetrofitService =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: initializeApiManager(context = context).also { instance = it }
                newInstance
            }

        private val okHttpClient: OkHttpClient
            get() {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                return OkHttpClient.Builder()
                    .readTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .build()
            }

        @OptIn(ExperimentalSerializationApi::class)
        private fun initializeApiManager(context: Context): RetrofitService {
            val contentType = CONTENT_TYPE.toMediaType()
            val converterFactory = json.asConverterFactory(contentType)

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(context.getString(R.string.API_URL))
                .addConverterFactory(converterFactory)
                .build().create(RetrofitService::class.java)
        }

        val retrofitService: RetrofitService get() = instance!!
    }
}
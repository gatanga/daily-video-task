package co.daily.sdk

import android.content.Context
import org.mediasoup.droid.Logger
import org.mediasoup.droid.MediasoupClient
import providers.network.ApiManagerProvider
import providers.network.RetrofitServiceProvider
import providers.repository.ApiRepositoryProvider

object DailySdk {

    fun startSdk(context: Context) {
        Logger.setLogLevel(Logger.LogLevel.LOG_DEBUG)
        Logger.setDefaultHandler()
        MediasoupClient.initialize(context.applicationContext)
        RetrofitServiceProvider.initializeRetrofit(context = context)
        ApiManagerProvider.initializeApiManager()
        ApiRepositoryProvider.initializeApiRepository()
    }
}
package providers.network

import data.network.rest.ApiManager
import data.network.rest.DailySdkApiManager

// In an app module, this would be achieved using dependency injection,
// but I opted against using DI in the SDK
class ApiManagerProvider {

    companion object {
        @Volatile
        private var instance: ApiManager? = null

        fun initializeApiManager(): ApiManager =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: DailySdkApiManager()
                        .also { instance = it }
                newInstance
            }

        val apiManager: ApiManager get() = instance!!
    }
}
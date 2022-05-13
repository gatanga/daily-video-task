package providers.repository

import domain.repositories.ApiRepository
import domain.repositories.DailySdkApiRepository

class ApiRepositoryProvider {

    companion object {

        @Volatile
        private var instance: ApiRepository? = null

        fun initializeApiRepository(): ApiRepository =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: DailySdkApiRepository()
                        .also { instance = it }
                newInstance
            }

        val apiRepository: ApiRepository get() = instance!!
    }
}
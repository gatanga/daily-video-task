package providers.repository

import android.content.Context
import domain.repositories.ApiRepository
import domain.repositories.DailySdkApiRepository

class ApiRepositoryProvider {

    companion object {

        @Volatile
        private var instance: ApiRepository? = null

        fun initializeApiRepository(context: Context): ApiRepository =
            instance ?: synchronized(this) {
                val newInstance = instance
                    ?: DailySdkApiRepository(context = context)
                        .also { instance = it }
                newInstance
            }

        val apiRepository: ApiRepository get() = instance!!
    }
}
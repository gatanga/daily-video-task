package background

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import co.daily.sdk.CallClient
import kotlinx.coroutines.runBlocking

class PeerSyncWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val callClient = CallClient.instance(context)

    override fun doWork(): Result {
        runBlocking {
            callClient.syncPeers()
        }

        return Result.success()
    }
}
package com.ebuspass.smartpassapp
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DeleteDataWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.d("DeleteDataWorker", "Deleting data...")
        return try {
            val database = FirebaseDatabase.getInstance()
            val passesRef = database.reference.child("Passes")
            passesRef.removeValue().await()
            Log.d("DeleteDataWorker", "Data deletion successful")
            Result.success()
        } catch (e: Exception) {
            Log.e("DeleteDataWorker", "Data deletion failed: ${e.message}")
            Result.failure()
        }
    }
}

fun scheduleDataDeletionWork() {
    val workRequest = PeriodicWorkRequestBuilder<DeleteDataWorker>(
        repeatInterval = 1, repeatIntervalTimeUnit = TimeUnit.DAYS
    ).setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS).build()
    WorkManager.getInstance().enqueue(workRequest)
}

private fun calculateInitialDelay(): Long {
    val currentTime = System.currentTimeMillis()
    val targetTime = getTargetTimeMillis()
    val initialDelay = targetTime - currentTime
    Log.d("DeletePasses", "Initial delay for data deletion: $initialDelay ms")
    return initialDelay
}

private fun getTargetTimeMillis(): Long {
    val now = Calendar.getInstance()
    val targetTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(now)) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }
    return targetTime.timeInMillis
}
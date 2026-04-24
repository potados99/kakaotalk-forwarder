package com.potados.kakaotalkforwarder.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardLogDao {

    @Query("SELECT * FROM forward_logs ORDER BY created_at DESC")
    fun observeAll(): Flow<List<ForwardLog>>

    @Query("SELECT * FROM forward_logs WHERE id = :id")
    suspend fun findById(id: Long): ForwardLog?

    @Insert
    suspend fun insert(log: ForwardLog): Long

    @Query(
        """
        UPDATE forward_logs
        SET status = 'SUCCESS', http_code = :httpCode, error_message = NULL, last_attempt_at = :now
        WHERE id = :id
        """
    )
    suspend fun markSuccess(id: Long, httpCode: Int, now: Long)

    @Query(
        """
        UPDATE forward_logs
        SET status = 'FAILED', http_code = :httpCode, error_message = :errorMessage, last_attempt_at = :now
        WHERE id = :id
        """
    )
    suspend fun markFailed(id: Long, httpCode: Int?, errorMessage: String?, now: Long)

    @Query(
        """
        UPDATE forward_logs
        SET status = 'PENDING', error_message = NULL, last_attempt_at = :now
        WHERE id = :id
        """
    )
    suspend fun markPending(id: Long, now: Long)

    @Query(
        """
        UPDATE forward_logs
        SET status = 'FAILED', error_message = :reason, last_attempt_at = :now
        WHERE status = 'PENDING'
        """
    )
    suspend fun failAllPending(reason: String, now: Long): Int

    @Query("DELETE FROM forward_logs")
    suspend fun deleteAll()
}

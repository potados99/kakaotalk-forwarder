package com.potados.kakaotalkforwarder.data

import com.potados.kakaotalkforwarder.data.db.ForwardLog
import com.potados.kakaotalkforwarder.data.db.ForwardLogDao
import com.potados.kakaotalkforwarder.data.db.ForwardStatus
import com.potados.kakaotalkforwarder.data.prefs.SettingsRepository
import com.potados.kakaotalkforwarder.data.remote.ForwardResult
import com.potados.kakaotalkforwarder.data.remote.MenuPostsApi
import kotlinx.coroutines.flow.Flow

class ForwardingRepository(
    private val dao: ForwardLogDao,
    private val api: MenuPostsApi,
    private val settings: SettingsRepository,
) {

    val logs: Flow<List<ForwardLog>> = dao.observeAll()

    suspend fun logAndForward(senderTitle: String, menuText: String) {
        val now = System.currentTimeMillis()
        val id = dao.insert(
            ForwardLog(
                createdAt = now,
                senderTitle = senderTitle,
                menuText = menuText,
                status = ForwardStatus.PENDING,
                lastAttemptAt = now,
            )
        )
        send(id, menuText)
    }

    suspend fun retry(id: Long) {
        val existing = dao.findById(id) ?: return
        dao.markPending(id, System.currentTimeMillis())
        send(id, existing.menuText)
    }

    suspend fun clearAll() = dao.deleteAll()

    suspend fun failOrphanedPending() {
        dao.failAllPending(reason = REASON_APP_TERMINATED, now = System.currentTimeMillis())
    }

    private suspend fun send(id: Long, menuText: String) {
        val config = settings.current()
        if (!config.isConfigured) {
            dao.markFailed(id, null, REASON_NOT_CONFIGURED, System.currentTimeMillis())
            return
        }

        val now = { System.currentTimeMillis() }
        when (val result = api.post(config.apiUrl, config.bearerToken, menuText)) {
            is ForwardResult.Success -> dao.markSuccess(id, result.httpCode, now())
            is ForwardResult.Failure -> dao.markFailed(id, result.httpCode, result.message, now())
            is ForwardResult.NetworkError -> dao.markFailed(id, null, result.message, now())
        }
    }

    private companion object {
        const val REASON_NOT_CONFIGURED = "API URL 또는 토큰이 설정되지 않았습니다."
        const val REASON_APP_TERMINATED = "앱 종료로 전송이 중단되었습니다."
    }
}

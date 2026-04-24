package com.potados.kakaotalkforwarder.service

import android.app.Notification
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.potados.kakaotalkforwarder.app
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ForwarderNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lastSeenByKey = mutableMapOf<String, Long>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        runCatching { capture(sbn) }
            .onFailure { Log.w(TAG, "알림 처리 중 오류", it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun capture(sbn: StatusBarNotification) {
        if (sbn.packageName != KAKAOTALK_PACKAGE) return
        if (isDuplicate(sbn.key)) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        if (title.isEmpty() || text.isEmpty()) return

        scope.launch { handle(title, text) }
    }

    private suspend fun handle(title: String, text: String) {
        val settings = app.settingsRepository.current()
        if (title != settings.filterNickname) return
        app.forwardingRepository.logAndForward(title, text)
    }

    private fun isDuplicate(key: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        lastSeenByKey.entries.removeAll { now - it.value > DEDUP_WINDOW_MS }
        val last = lastSeenByKey[key]
        if (last != null && now - last < DEDUP_WINDOW_MS) return true
        lastSeenByKey[key] = now
        return false
    }

    private companion object {
        const val KAKAOTALK_PACKAGE = "com.kakao.talk"
        const val DEDUP_WINDOW_MS = 5_000L
        const val TAG = "ForwarderListener"
    }
}

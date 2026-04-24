package com.potados.kakaotalkforwarder.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ForwardStatus { PENDING, SUCCESS, FAILED }

@Entity(tableName = "forward_logs")
data class ForwardLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "sender_title")
    val senderTitle: String,

    @ColumnInfo(name = "menu_text")
    val menuText: String,

    @ColumnInfo(name = "status")
    val status: ForwardStatus,

    @ColumnInfo(name = "http_code")
    val httpCode: Int? = null,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long,
)

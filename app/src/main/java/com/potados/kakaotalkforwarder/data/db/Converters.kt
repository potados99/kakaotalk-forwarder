package com.potados.kakaotalkforwarder.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStatus(status: ForwardStatus): String = status.name

    @TypeConverter
    fun toStatus(raw: String): ForwardStatus = ForwardStatus.valueOf(raw)
}

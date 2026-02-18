package com.example.umind.data.local.converter

import androidx.room.TypeConverter
import com.example.umind.domain.model.FocusModeType
import com.example.umind.domain.model.TimeRestriction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class Converters {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(TimeRestriction::class.java, TimeRestrictionAdapter())
        .create()

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromStringSet(value: Set<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringSet(value: String?): Set<String>? {
        return value?.split(",")?.filter { it.isNotEmpty() }?.toSet()
    }

    @TypeConverter
    fun fromTimeRestrictionList(value: List<TimeRestriction>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTimeRestrictionList(value: String?): List<TimeRestriction>? {
        if (value == null) return null
        val type = object : TypeToken<List<TimeRestriction>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromFocusModeType(value: FocusModeType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFocusModeType(value: String?): FocusModeType? {
        return value?.let { FocusModeType.valueOf(it) }
    }
}

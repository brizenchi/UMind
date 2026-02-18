package com.example.umind.data.local.converter

import com.example.umind.domain.model.TimeRestriction
import com.google.gson.*
import java.lang.reflect.Type
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Gson TypeAdapter for TimeRestriction
 * Handles serialization of LocalTime and DayOfWeek
 */
class TimeRestrictionAdapter : JsonSerializer<TimeRestriction>, JsonDeserializer<TimeRestriction> {

    override fun serialize(src: TimeRestriction?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) return JsonNull.INSTANCE

        return JsonObject().apply {
            addProperty("id", src.id)
            addProperty("startTime", src.startTime.toString())
            addProperty("endTime", src.endTime.toString())
            add("daysOfWeek", JsonArray().apply {
                src.daysOfWeek.forEach { add(it.name) }
            })
        }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): TimeRestriction {
        val obj = json?.asJsonObject ?: throw JsonParseException("Invalid TimeRestriction JSON")

        val id = obj.get("id").asString
        val startTime = LocalTime.parse(obj.get("startTime").asString)
        val endTime = LocalTime.parse(obj.get("endTime").asString)
        val daysOfWeek = obj.getAsJsonArray("daysOfWeek").map {
            DayOfWeek.valueOf(it.asString)
        }.toSet()

        return TimeRestriction(
            id = id,
            startTime = startTime,
            endTime = endTime,
            daysOfWeek = daysOfWeek
        )
    }
}

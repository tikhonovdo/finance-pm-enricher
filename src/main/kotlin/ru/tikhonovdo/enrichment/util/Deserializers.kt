package ru.tikhonovdo.enrichment.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import ru.tikhonovdo.enrichment.service.importscenario.ScenarioState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ZeroAsNullDeserializer: JsonDeserializer<Long?>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Long? {
        val value = parser.valueAsLong
        return if (value == 0L) {
            null
        } else {
            value
        }
    }
}

class MillisToLocalDateTimeDeserializer: JsonDeserializer<LocalDateTime>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDateTime {
        val value = parser.valueAsLong
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault())
    }
}

class ScenarioStateDeserializer: JsonDeserializer<ScenarioState?>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): ScenarioState? {
        val value = parser.valueAsString
        return ScenarioState.entries.firstOrNull { it.stepName == value }
    }
}

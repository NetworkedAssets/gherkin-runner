package com.networkedassets.gherkin.runner.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.text.SimpleDateFormat

object JSONSerializer {
    @JvmStatic
    fun toJson(obj: Any): String {
        return createObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(obj)
    }

    @JvmStatic
    fun toObjectNode(obj: Any): ObjectNode {
        return createObjectMapper().convertValue(obj, ObjectNode::class.java)
    }

    private fun createObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        return objectMapper
    }
}
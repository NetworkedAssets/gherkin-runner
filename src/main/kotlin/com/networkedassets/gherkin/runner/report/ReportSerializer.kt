package com.networkedassets.gherkin.runner.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.networkedassets.gherkin.runner.report.data.Report
import java.text.SimpleDateFormat

object ReportSerializer {
    @JvmStatic
    fun reportToJson(report: Report): String {
        val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(report)
    }
}
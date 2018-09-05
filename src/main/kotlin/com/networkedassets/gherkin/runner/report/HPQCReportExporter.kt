package com.networkedassets.gherkin.runner.report

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import com.networkedassets.gherkin.runner.report.data.FeatureReport
import com.networkedassets.gherkin.runner.report.data.Report
import java.awt.Color
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HPQCReportExporter: ReportExporter {
    override val name = "HPQC"

    override fun export(report: Report) {
        val path = "${reportsDir()}/hpqc-report.xlsx"
        val wb = XSSFWorkbook()
        report.featureReports.forEachIndexed { featureIndex, featureReport ->
            val sheet = wb.createSheet("TO ${featureIndex + 1} ${featureReport.feature.name}")
            sheet.putReportHeaders()
            sheet.putReport(featureReport, featureIndex)
        }

        val fileOut = FileOutputStream(path)
        wb.write(fileOut)
        fileOut.flush()
        fileOut.close()
    }

    private fun XSSFSheet.putReportHeaders() {
        val bigHeaderFont = this.workbook.createArialFont(10, bold = true, underline = true)
        val mediumHeaderFont = this.workbook.createArialFont(10)
        val smallHeaderFont = this.workbook.createArialFont()

        val row = this.createRow(0)

        val subject = XSSFRichTextString()
        subject.append("Subject\n", bigHeaderFont)
        subject.append("Test Object Name\n", mediumHeaderFont)
        subject.append("e.g. TO 5 WAP Service\n", smallHeaderFont)
        row.putHeaderCell(0, subject)

        val testName = XSSFRichTextString()
        testName.append("Test Name\n", bigHeaderFont)
        testName.append("e.g. 5.1 Login\n", smallHeaderFont)
        row.putHeaderCell(1, testName)

        val description = XSSFRichTextString()
        description.append("Description\n", bigHeaderFont)
        description.append("e.g. \"Delete mail permanently\"\nSpace for additional informations like\n\"Preconditions\", \"Author vendor\", \"Responsible VF/vendor\" etc.", smallHeaderFont)
        row.putHeaderCell(2, description)

        val stepName = XSSFRichTextString()
        stepName.append("Step Name (Design Steps)", bigHeaderFont)
        row.putHeaderCell(3, stepName)

        val descriptionSteps = XSSFRichTextString()
        descriptionSteps.append("Description (Design Steps)\n", bigHeaderFont)
        descriptionSteps.append("e.g. \"Visit \"Einstellung\" - \"Allgemeines\" - …\n", smallHeaderFont)
        descriptionSteps.append("Set option to\"Nein\" …\"\n", smallHeaderFont)
        row.putHeaderCell(4, descriptionSteps)

        val expectedResult = XSSFRichTextString()
        expectedResult.append("Expected Result (Design Steps)\n", bigHeaderFont)
        expectedResult.append("e.g. \"Mail is deleted permanently\"\n", smallHeaderFont)
        row.putHeaderCell(5, expectedResult)

        val type = XSSFRichTextString()
        type.append("Type", bigHeaderFont)
        row.putHeaderCell(6, type)

        val creationDate = XSSFRichTextString()
        creationDate.append("Creation Date", bigHeaderFont)
        row.putHeaderCell(7, creationDate)

        val designer = XSSFRichTextString()
        designer.append("Designer", bigHeaderFont)
        row.putHeaderCell(8, designer)
    }

    private fun XSSFWorkbook.createArialFont(size: Short = 8, bold: Boolean = false, underline: Boolean = false): XSSFFont {
        val font = this.createFont()
        font.fontName = "Arial"
        font.fontHeightInPoints = size
        font.bold = bold
        if (underline) font.underline = FontUnderline.SINGLE.byteValue
        return font
    }

    private fun XSSFWorkbook.createHeaderCellStyle(): XSSFCellStyle {
        val style = this.createCellStyle()
        style.setFillForegroundColor(XSSFColor(Color(153, 204, 255)))
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        style.setAlignment(HorizontalAlignment.CENTER)
        style.setVerticalAlignment(VerticalAlignment.TOP)
        style.wrapText = true
        style.setBorderRight(BorderStyle.THIN)
        style.setBorderBottom(BorderStyle.THIN)
        return style
    }

    private fun XSSFSheet.putReport(featureReport: FeatureReport, featureIndex: Int) {
        var rowNumCounter = 1
        featureReport.scenarioReports.forEachIndexed { scenarioIndex, scenarioReport ->
            scenarioReport.stepReports.forEachIndexed { stepIndex, stepReport ->
                val row = this.createRow(rowNumCounter++)
                val featureIndexString = "${featureIndex + 1}"
                val scenarioIndexString = "$featureIndexString.${scenarioIndex + 1}"
                row.putCell(0, "TO $featureIndexString ${featureReport.feature.name}")
                row.putCell(1, scenarioIndexString + " " + scenarioReport.scenario.name)
                row.putCell(2, scenarioReport.scenario.description ?: "-")
                row.putCell(3, "Step ${stepIndex + 1}")
                row.putCell(4, stepReport.step.fullContent)
                row.putCell(5, "Passed auto test step result")
                row.putCell(6, "AUTO")
                row.putCell(7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                row.putCell(8, "Auto tests runner")
            }
        }
        autoSizeColumns(0..8)
    }

    private fun XSSFSheet.autoSizeColumns(range: IntRange) {
        range.forEach { this.autoSizeColumn(it) }
    }

    private fun XSSFRow.putCell(columnIndex: Int, value: String) {
        val cell = this.createCell(columnIndex)
        val cellValue = XSSFRichTextString()
        cellValue.append(value, this.sheet.workbook.createArialFont(10))
        cell.setCellValue(cellValue)
    }

    private fun XSSFRow.putHeaderCell(columnIndex: Int, value: XSSFRichTextString) {
        val cell = this.createCell(columnIndex)
        cell.cellStyle = this.sheet.workbook.createHeaderCellStyle()
        cell.setCellValue(value)
    }
}
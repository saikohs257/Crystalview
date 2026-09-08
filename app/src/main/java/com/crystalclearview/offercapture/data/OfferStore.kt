package com.crystalclearview.offercapture.data

import android.content.Context
import org.json.JSONObject
import java.io.File

class OfferStore(context: Context) {
    private val root = File(context.filesDir, "crystalclearview").apply { mkdirs() }
    private val jsonlFile = File(root, "offers.jsonl")
    private val csvFile = File(root, "offers.csv")

    @Synchronized
    fun append(record: OfferRecord) {
        val json = JSONObject().apply {
            put("capturedAtMs", record.capturedAtMs)
            put("sourcePackage", record.sourcePackage)
            put("rawText", record.rawText)
            put("payout", record.payout)
            put("miles", record.miles)
            put("minutes", record.minutes)
            put("dollarsPerMile", record.dollarsPerMile)
            put("dollarsPerHour", record.dollarsPerHour)
            put("parserConfidence", record.parserConfidence)
            put("parserNotes", record.parserNotes)
        }
        jsonlFile.appendText(json.toString() + "\n")
    }

    @Synchronized
    fun exportCsv(): File {
        if (!csvFile.exists() || csvFile.length() == 0L) {
            csvFile.writeText(
                "captured_at_ms,source_package,raw_text,payout,miles,minutes,dollars_per_mile,dollars_per_hour,parser_confidence,parser_notes\n"
            )
        }
        return csvFile
    }

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"").replace("\n", " ")}\""
}

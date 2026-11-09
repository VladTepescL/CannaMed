package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.data.local.AppDatabase
import com.example.data.model.CannabisFormat
import com.example.data.model.Cartridge
import com.example.data.model.DosageLog
import com.example.data.model.LegalExtract
import com.example.data.model.Prescription
import com.example.data.model.StrainInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    suspend fun createFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("appName", "CannaMed")
        root.put("version", 2)
        root.put("createdAt", System.currentTimeMillis())

        // 1. Logs
        val logs = database.dosageLogDao().getAllLogs().first()
        val logsJson = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("timestamp", log.timestamp)
            obj.put("format", log.format.name)
            obj.put("strainName", log.strainName)
            obj.put("strainType", log.strainType)
            obj.put("dosageValue", log.dosageValue)
            obj.put("dosageUnit", log.dosageUnit)
            obj.put("puffsCount", log.puffsCount)
            obj.put("temperatureCelsius", log.temperatureCelsius ?: -1)
            obj.put("cartridgeId", log.cartridgeId ?: -1L)
            obj.put("thcPercentage", log.thcPercentage)
            obj.put("cbdPercentage", log.cbdPercentage)
            obj.put("terpenesNote", log.terpenesNote)
            obj.put("reliefScore", log.reliefScore)
            obj.put("treatedSymptoms", log.treatedSymptoms)
            obj.put("moodEffect", log.moodEffect)
            obj.put("sideEffects", log.sideEffects)
            obj.put("subjectiveNotes", log.subjectiveNotes)
            obj.put("puffIntensity", log.puffIntensity)
            obj.put("equivalentGramsDriedFlower", log.equivalentGramsDriedFlower)
            logsJson.put(obj)
        }
        root.put("dosageLogs", logsJson)

        // 2. Cartridges
        val cartridges = database.cartridgeDao().getAllCartridges().first()
        val cartsJson = JSONArray()
        cartridges.forEach { cart ->
            val obj = JSONObject()
            obj.put("id", cart.id)
            obj.put("brandName", cart.brandName)
            obj.put("varietyName", cart.varietyName)
            obj.put("formatType", cart.formatType.name)
            obj.put("capacityMl", cart.capacityMl)
            obj.put("thcPercentage", cart.thcPercentage)
            obj.put("cbdPercentage", cart.cbdPercentage)
            obj.put("cbnPercentage", cart.cbnPercentage)
            obj.put("cbgPercentage", cart.cbgPercentage)
            obj.put("terpenesSummary", cart.terpenesSummary)
            obj.put("totalPuffsLogged", cart.totalPuffsLogged)
            obj.put("installedTimestamp", cart.installedTimestamp)
            obj.put("finishedTimestamp", cart.finishedTimestamp ?: -1L)
            obj.put("isActive", cart.isActive)
            obj.put("batchLotNumber", cart.batchLotNumber)
            obj.put("notes", cart.notes)
            cartsJson.put(obj)
        }
        root.put("cartridges", cartsJson)

        // 3. Prescriptions + Documents (Base64)
        val prescriptions = database.prescriptionDao().getAllPrescriptions().first()
        val presJson = JSONArray()
        prescriptions.forEach { pres ->
            val obj = JSONObject()
            obj.put("id", pres.id)
            obj.put("doctorName", pres.doctorName)
            obj.put("licenseNumber", pres.licenseNumber)
            obj.put("medicalCenter", pres.medicalCenter)
            obj.put("issueDateMillis", pres.issueDateMillis)
            obj.put("expiryDateMillis", pres.expiryDateMillis)
            obj.put("prescribedDailyGrams", pres.prescribedDailyGrams)
            obj.put("prescribedFormat", pres.prescribedFormat)
            obj.put("prescribedDailyDosage", pres.prescribedDailyDosage)
            obj.put("clinicalDiagnosis", pres.clinicalDiagnosis)
            obj.put("instructions", pres.instructions)
            obj.put("documentFileName", pres.documentFileName ?: "")
            obj.put("documentMimeType", pres.documentMimeType ?: "")

            // Encode attached document if available
            pres.localDocumentPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    try {
                        val bytes = if (com.example.util.EncryptedFileManager.isEncryptedDocPath(path)) {
                            com.example.util.EncryptedFileManager.openDecryptedInputStream(context, file).use { it.readBytes() }
                        } else {
                            FileInputStream(file).use { it.readBytes() }
                        }
                        obj.put("documentBase64", Base64.encodeToString(bytes, Base64.NO_WRAP))
                    } catch (_: Exception) {}
                }
            }
            presJson.put(obj)
        }
        root.put("prescriptions", presJson)

        // 4. Legal Extracts
        val extracts = database.legalExtractDao().getAllLegalExtracts().first()
        val extractsJson = JSONArray()
        extracts.forEach { ext ->
            val obj = JSONObject()
            obj.put("id", ext.id)
            obj.put("country", ext.country)
            obj.put("title", ext.title)
            obj.put("category", ext.category)
            obj.put("fullLegalText", ext.fullLegalText)
            obj.put("practicalSummary", ext.practicalSummary)
            obj.put("relevanceForPatient", ext.relevanceForPatient)
            obj.put("isOfficialPreset", ext.isOfficialPreset)
            obj.put("customNotes", ext.customNotes)
            extractsJson.put(obj)
        }
        root.put("legalExtracts", extractsJson)

        root.toString(2)
    }

    suspend fun restoreFromBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // Restore Cartridges
            if (root.has("cartridges")) {
                val carts = root.getJSONArray("cartridges")
                for (i in 0 until carts.length()) {
                    val o = carts.getJSONObject(i)
                    val cart = Cartridge(
                        id = o.optLong("id", 0L),
                        brandName = o.getString("brandName"),
                        varietyName = o.getString("varietyName"),
                        formatType = try { CannabisFormat.valueOf(o.getString("formatType")) } catch (_: Exception) { CannabisFormat.CONCENTRATE_VAPE },
                        capacityMl = o.optDouble("capacityMl", 1.0).toFloat(),
                        thcPercentage = o.optDouble("thcPercentage", 0.0).toFloat(),
                        cbdPercentage = o.optDouble("cbdPercentage", 0.0).toFloat(),
                        cbnPercentage = o.optDouble("cbnPercentage", 0.0).toFloat(),
                        cbgPercentage = o.optDouble("cbgPercentage", 0.0).toFloat(),
                        terpenesSummary = o.optString("terpenesSummary", ""),
                        totalPuffsLogged = o.optInt("totalPuffsLogged", 0),
                        installedTimestamp = o.optLong("installedTimestamp", System.currentTimeMillis()),
                        finishedTimestamp = if (o.optLong("finishedTimestamp", -1L) > 0) o.getLong("finishedTimestamp") else null,
                        isActive = o.optBoolean("isActive", true),
                        batchLotNumber = o.optString("batchLotNumber", ""),
                        notes = o.optString("notes", "")
                    )
                    database.cartridgeDao().insert(cart)
                }
            }

            // Restore Prescriptions + Files
            if (root.has("prescriptions")) {
                val presArray = root.getJSONArray("prescriptions")
                val docsDir = File(context.filesDir, "medical_docs")
                if (!docsDir.exists()) docsDir.mkdirs()

                for (i in 0 until presArray.length()) {
                    val o = presArray.getJSONObject(i)
                    var localSavedPath: String? = null
                    val fileName = o.optString("documentFileName", "")
                    if (o.has("documentBase64") && fileName.isNotBlank()) {
                        try {
                            val base64Str = o.getString("documentBase64")
                            val bytes = Base64.decode(base64Str, Base64.DEFAULT)
                            val destFile = com.example.util.EncryptedFileManager.saveEncryptedFile(
                                context,
                                bytes.inputStream(),
                                "restored_${System.currentTimeMillis()}_$fileName"
                            )
                            localSavedPath = destFile.absolutePath
                        } catch (_: Exception) {}
                    }

                    val pres = Prescription(
                        id = o.optLong("id", 0L),
                        doctorName = o.getString("doctorName"),
                        licenseNumber = o.optString("licenseNumber", ""),
                        medicalCenter = o.optString("medicalCenter", ""),
                        issueDateMillis = o.optLong("issueDateMillis", System.currentTimeMillis()),
                        expiryDateMillis = o.optLong("expiryDateMillis", System.currentTimeMillis()),
                        prescribedDailyGrams = o.optDouble("prescribedDailyGrams", 3.0),
                        prescribedFormat = o.optString("prescribedFormat", "Flores secas"),
                        prescribedDailyDosage = o.optString("prescribedDailyDosage", ""),
                        clinicalDiagnosis = o.optString("clinicalDiagnosis", ""),
                        instructions = o.optString("instructions", ""),
                        localDocumentPath = localSavedPath,
                        documentFileName = if (fileName.isNotBlank()) fileName else null,
                        documentMimeType = o.optString("documentMimeType", null)
                    )
                    database.prescriptionDao().insert(pres)
                }
            }

            // Restore Dosage Logs
            if (root.has("dosageLogs")) {
                val logsArray = root.getJSONArray("dosageLogs")
                for (i in 0 until logsArray.length()) {
                    val o = logsArray.getJSONObject(i)
                    val log = DosageLog(
                        id = o.optLong("id", 0L),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                        format = try { CannabisFormat.valueOf(o.getString("format")) } catch (_: Exception) { CannabisFormat.VAPORIZED_FLOWER },
                        strainName = o.optString("strainName", ""),
                        strainType = o.optString("strainType", "Híbrida"),
                        dosageValue = o.optDouble("dosageValue", 0.0),
                        dosageUnit = o.optString("dosageUnit", "Caladas"),
                        puffsCount = o.optInt("puffsCount", 0),
                        temperatureCelsius = if (o.optInt("temperatureCelsius", -1) != -1) o.getInt("temperatureCelsius") else null,
                        cartridgeId = if (o.optLong("cartridgeId", -1L) != -1L) o.getLong("cartridgeId") else null,
                        thcPercentage = o.optDouble("thcPercentage", 0.0).toFloat(),
                        cbdPercentage = o.optDouble("cbdPercentage", 0.0).toFloat(),
                        terpenesNote = o.optString("terpenesNote", ""),
                        reliefScore = o.optInt("reliefScore", 8),
                        treatedSymptoms = o.optString("treatedSymptoms", ""),
                        moodEffect = o.optString("moodEffect", ""),
                        sideEffects = o.optString("sideEffects", ""),
                        subjectiveNotes = o.optString("subjectiveNotes", ""),
                        puffIntensity = o.optString("puffIntensity", "Estándar (3-4s)"),
                        equivalentGramsDriedFlower = o.optDouble("equivalentGramsDriedFlower", 0.0)
                    )
                    database.dosageLogDao().insert(log)
                }
            }

            // Restore Legal Extracts
            if (root.has("legalExtracts")) {
                val legArray = root.getJSONArray("legalExtracts")
                for (i in 0 until legArray.length()) {
                    val o = legArray.getJSONObject(i)
                    val ext = LegalExtract(
                        id = o.optLong("id", 0L),
                        country = o.optString("country", "Chile"),
                        title = o.getString("title"),
                        category = o.optString("category", "Ley"),
                        fullLegalText = o.getString("fullLegalText"),
                        practicalSummary = o.optString("practicalSummary", ""),
                        relevanceForPatient = o.optString("relevanceForPatient", ""),
                        isOfficialPreset = o.optBoolean("isOfficialPreset", true),
                        customNotes = o.optString("customNotes", "")
                    )
                    database.legalExtractDao().insert(ext)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

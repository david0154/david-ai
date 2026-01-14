package com.davidstudioz.david.chat

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException

/**
 * ScriptureLoader - Load complete scriptures from JSON files
 * ✅ Load from assets/scriptures/ folder
 * ✅ Parse JSON format
 * ✅ Fallback to built-in samples
 * ✅ Multi-language support
 * ✅ Error handling
 */
class ScriptureLoader(private val context: Context) {

    private val gson = Gson()

    /**
     * Load Bhagavad Gita from JSON file
     */
    fun loadBhagavadGita(): BhagavadGitaData? {
        return try {
            val json = loadJsonFromAssets("scriptures/bhagavad_gita.json")
            if (json != null) {
                gson.fromJson(json, BhagavadGitaData::class.java)
            } else {
                Log.w(TAG, "Bhagavad Gita JSON not found, using built-in samples")
                null
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing Bhagavad Gita JSON", e)
            null
        }
    }

    /**
     * Load Ramayana from JSON file
     */
    fun loadRamayana(): RamayanaData? {
        return try {
            val json = loadJsonFromAssets("scriptures/ramayana.json")
            if (json != null) {
                gson.fromJson(json, RamayanaData::class.java)
            } else {
                Log.w(TAG, "Ramayana JSON not found, using built-in samples")
                null
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing Ramayana JSON", e)
            null
        }
    }

    /**
     * Load Puranas from JSON file
     */
    fun loadPuranas(): PuranasData? {
        return try {
            val json = loadJsonFromAssets("scriptures/puranas.json")
            if (json != null) {
                gson.fromJson(json, PuranasData::class.java)
            } else {
                Log.w(TAG, "Puranas JSON not found, using built-in samples")
                null
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing Puranas JSON", e)
            null
        }
    }

    /**
     * Load JSON file from assets
     */
    private fun loadJsonFromAssets(fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e(TAG, "File not found: $fileName", e)
            null
        }
    }

    /**
     * Check if scripture files are available
     */
    fun areScripturesAvailable(): ScriptureAvailability {
        val gitaAvailable = try {
            context.assets.open("scriptures/bhagavad_gita.json").close()
            true
        } catch (e: IOException) {
            false
        }

        val ramayanaAvailable = try {
            context.assets.open("scriptures/ramayana.json").close()
            true
        } catch (e: IOException) {
            false
        }

        val puranasAvailable = try {
            context.assets.open("scriptures/puranas.json").close()
            true
        } catch (e: IOException) {
            false
        }

        return ScriptureAvailability(
            bhagavadGita = gitaAvailable,
            ramayana = ramayanaAvailable,
            puranas = puranasAvailable
        )
    }

    /**
     * Get download instructions if files are missing
     */
    fun getDownloadInstructions(): String {
        val availability = areScripturesAvailable()
        
        if (availability.allAvailable()) {
            return "✅ All scripture files loaded successfully!"
        }

        val missing = mutableListOf<String>()
        if (!availability.bhagavadGita) missing.add("Bhagavad Gita")
        if (!availability.ramayana) missing.add("Ramayana")
        if (!availability.puranas) missing.add("Puranas")

        return """
            ⚠️ Missing Scripture Files: ${missing.joinToString(", ")}
            
            📥 DOWNLOAD INSTRUCTIONS:
            
            1. Visit app/src/main/assets/scriptures/README.md
            2. Follow download links for missing files
            3. Place JSON files in: app/src/main/assets/scriptures/
            4. Rebuild and restart app
            
            Currently using built-in sample verses (15-20 verses).
            Download complete files for 850+ verses!
            
            Quick Links:
            • Bhagavad Gita: https://www.gita-society.com/
            • Ramayana: http://www.valmikiramayan.net/
            • Puranas: https://archive.org/details/vishnu-puran-gita-press
        """.trimIndent()
    }

    companion object {
        private const val TAG = "ScriptureLoader"
    }
}

/**
 * Data classes for JSON parsing
 */
data class BhagavadGitaData(
    val metadata: GitaMetadata,
    val chapters: List<GitaChapter>
)

data class GitaMetadata(
    val name: String,
    val total_chapters: Int,
    val total_verses: Int,
    val language: String
)

data class GitaChapter(
    val chapter_number: Int,
    val name: String,
    val verses: List<GitaVerse>
)

data class GitaVerse(
    val verse_number: Int,
    val sanskrit: String,
    val transliteration: String,
    val english: String,
    val hindi: String?,
    val theme: String
)

data class RamayanaData(
    val metadata: RamayanaMetadata,
    val kandas: List<RamayanaKanda>
)

data class RamayanaMetadata(
    val name: String,
    val total_kandas: Int,
    val author: String
)

data class RamayanaKanda(
    val kanda_number: Int,
    val name: String,
    val verses: List<RamayanaVerse>
)

data class RamayanaVerse(
    val sarga: Int,
    val verse_number: Int,
    val sanskrit: String,
    val transliteration: String,
    val english: String,
    val theme: String
)

data class PuranasData(
    val metadata: PuranasMetadata,
    val puranas: List<PuranaData>
)

data class PuranasMetadata(
    val total_puranas: Int,
    val category: String
)

data class PuranaData(
    val name: String,
    val verses: List<PuranaVerse>
)

data class PuranaVerse(
    val chapter: String,
    val sanskrit: String,
    val transliteration: String,
    val english: String,
    val theme: String
)

data class ScriptureAvailability(
    val bhagavadGita: Boolean,
    val ramayana: Boolean,
    val puranas: Boolean
) {
    fun allAvailable(): Boolean = bhagavadGita && ramayana && puranas
    fun anyAvailable(): Boolean = bhagavadGita || ramayana || puranas
    fun noneAvailable(): Boolean = !anyAvailable()
}
package com.davidstudioz.david.chat

import android.util.Log

/**
 * SpellCorrector - Auto-corrects common spelling mistakes
 * ✅ Handles common typos
 * ✅ Preserves intent
 * ✅ Works with Indian English
 */
class SpellCorrector {
    
    private val corrections = mapOf(
        // Common typos
        "wether" to "weather",
        "wheather" to "weather",
        "temprature" to "temperature",
        "temperture" to "temperature",
        "todays" to "today's",
        "tomorow" to "tomorrow",
        "tommorow" to "tomorrow",
        "tommorrow" to "tomorrow",
        
        // Developer-related
        "devloper" to "developer",
        "devoloper" to "developer",
        "devolped" to "developed",
        "developd" to "developed",
        "creater" to "creator",
        "creatd" to "created",
        
        // Common words
        "teh" to "the",
        "taht" to "that",
        "thsi" to "this",
        "thier" to "their",
        "recieve" to "receive",
        "beleive" to "believe",
        "seperate" to "separate",
        "definitly" to "definitely",
        "definately" to "definitely",
        
        // Questions
        "wat" to "what",
        "wht" to "what",
        "hw" to "how",
        "wen" to "when",
        "whr" to "where",
        "y" to "why",
        "u" to "you",
        "ur" to "your",
        "r" to "are",
        
        // Commands
        "opn" to "open",
        "strt" to "start",
        "stp" to "stop",
        "trn" to "turn",
        "trun" to "turn",
        
        // Features
        "motivaton" to "motivation",
        "motovation" to "motivation",
        "inspration" to "inspiration",
        "qoute" to "quote",
        "bhagvad" to "bhagavad"
    )
    
    fun correct(input: String): String {
        if (input.isBlank()) return input
        
        val words = input.split(" ")
        val correctedWords = words.map { word ->
            val lowerWord = word.lowercase()
            val punctuation = word.takeLastWhile { it in ".,!?;:" }
            val cleanWord = word.dropLastWhile { it in ".,!?;:" }
            val cleanLower = cleanWord.lowercase()
            
            val corrected = corrections[cleanLower] ?: cleanWord
            
            // Preserve capitalization
            val finalWord = when {
                cleanWord.firstOrNull()?.isUpperCase() == true -> 
                    corrected.replaceFirstChar { it.uppercase() }
                else -> corrected
            }
            
            finalWord + punctuation
        }
        
        val result = correctedWords.joinToString(" ")
        
        if (result != input) {
            Log.d(TAG, "Corrected: '$input' → '$result'")
        }
        
        return result
    }
    
    companion object {
        private const val TAG = "SpellCorrector"
    }
}
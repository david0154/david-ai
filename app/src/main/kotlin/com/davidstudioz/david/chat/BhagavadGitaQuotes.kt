package com.davidstudioz.david.chat

/**
 * BhagavadGitaQuotes - Motivational wisdom from Bhagavad Gita
 * ✅ Sanskrit verses with translations
 * ✅ Multi-language support
 * ✅ Contextual wisdom
 */
class BhagavadGitaQuotes {
    
    private val quotes = listOf(
        Quote(
            sanskrit = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।",
            transliteration = "Karmanye vadhikaraste ma phaleshu kadachana",
            english = "You have the right to perform your duty, but not to the fruits of your actions.",
            hindi = "तुम्हें केवल कर्म करने का अधिकार है, फल की इच्छा मत करो।",
            meaning = "Focus on your efforts, not the results. Do your best and let go of expectations."
        ),
        Quote(
            sanskrit = "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय।",
            transliteration = "Yogasthah kuru karmani sangam tyaktva dhananjaya",
            english = "Perform your duty with a balanced mind, abandoning attachment to success or failure.",
            hindi = "संतुलित मन से अपना कर्तव्य करो, सफलता-असफलता की चिंता छोड़ दो।",
            meaning = "Stay calm and focused. Don't let success make you arrogant or failure discourage you."
        ),
        Quote(
            sanskrit = "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।",
            transliteration = "Uddhared atmanatmanam natmanam avasadayet",
            english = "Lift yourself up by your own efforts. Do not degrade yourself.",
            hindi = "अपने प्रयासों से स्वयं को ऊपर उठाओ। स्वयं को नीचा मत गिराओ।",
            meaning = "You are your own best friend and worst enemy. Choose to uplift yourself!"
        ),
        Quote(
            sanskrit = "यदा यदा हि धर्मस्य ग्लानिर्भवति भारत।",
            transliteration = "Yada yada hi dharmasya glanir bhavati bharata",
            english = "Whenever there is a decline in righteousness and rise in unrighteousness, I manifest myself.",
            hindi = "जब-जब धर्म की हानि होती है, तब-तब मैं प्रकट होता हूं।",
            meaning = "Good always triumphs over evil. Keep faith in righteousness!"
        ),
        Quote(
            sanskrit = "मात्रास्पर्शास्तु कौन्तेय शीतोष्णसुखदुःखदाः।",
            transliteration = "Matra-sparshas tu kaunteya shitoshna-sukha-duhkha-dah",
            english = "The contact between senses and sense objects gives rise to fleeting cold and heat, pleasure and pain.",
            hindi = "सुख-दुःख, सर्दी-गर्मी ये सब अस्थायी हैं।",
            meaning = "Good times and bad times are temporary. Stay steady through both!"
        ),
        Quote(
            sanskrit = "श्रेयान्स्वधर्मो विगुणः परधर्मात्स्वनुष्ठितात्।",
            transliteration = "Shreyan sva-dharmo vigunah para-dharmat sv-anushthitat",
            english = "It is better to perform one's own duties imperfectly than another's duties perfectly.",
            hindi = "दूसरों के धर्म का पालन करने से अपना अपूर्ण धर्म श्रेष्ठ है।",
            meaning = "Be yourself! Your authentic path is better than imitating others."
        ),
        Quote(
            sanskrit = "सुखदुःखे समे कृत्वा लाभालाभौ जयाजयौ।",
            transliteration = "Sukha-duhkhe same kritva labha-labhau jaya-jayau",
            english = "Treat pleasure and pain, gain and loss, victory and defeat alike.",
            hindi = "सुख-दुःख, लाभ-हानि, जीत-हार को समान समझो।",
            meaning = "Maintain equanimity in all situations. This is true strength!"
        ),
        Quote(
            sanskrit = "क्रोधाद्भवति सम्मोहः सम्मोहात्स्मृतिविभ्रमः।",
            transliteration = "Krodhat bhavati sammohah sammohat smriti-vibhramah",
            english = "From anger comes delusion, from delusion comes loss of memory.",
            hindi = "क्रोध से मोह, मोह से स्मृति भ्रम होता है।",
            meaning = "Stay calm and composed. Anger clouds your judgment!"
        )
    )
    
    fun getRandomQuote(language: String = "english"): String {
        val quote = quotes.random()
        
        return buildString {
            append("🕉️ Bhagavad Gita Wisdom:\n\n")
            append("${quote.sanskrit}\n")
            append("(${quote.transliteration})\n\n")
            
            when (language.lowercase()) {
                "hindi" -> {
                    append("अर्थ: ${quote.hindi}\n\n")
                    append("सार: ${quote.meaning}")
                }
                "bengali" -> {
                    append("অর্থ: ${translateToLanguage(quote.english, "bengali")}\n\n")
                    append("সারাংশ: ${translateToLanguage(quote.meaning, "bengali")}")
                }
                "tamil" -> {
                    append("பொருள்: ${translateToLanguage(quote.english, "tamil")}\n\n")
                    append("சாராம்சம்: ${translateToLanguage(quote.meaning, "tamil")}")
                }
                else -> {
                    append("Meaning: ${quote.english}\n\n")
                    append("Essence: ${quote.meaning}")
                }
            }
        }
    }
    
    private fun translateToLanguage(text: String, language: String): String {
        // Basic translation - can be enhanced with translation API
        return text // Placeholder - integrate translation service
    }
    
    data class Quote(
        val sanskrit: String,
        val transliteration: String,
        val english: String,
        val hindi: String,
        val meaning: String
    )
}
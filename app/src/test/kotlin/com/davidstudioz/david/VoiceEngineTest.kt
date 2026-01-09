package com.davidstudioz.david.voice

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class VoiceEngineTest {
    
    private lateinit var context: Context
    private lateinit var voiceEngine: VoiceEngine
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        voiceEngine = VoiceEngine(context)
    }
    
    @Test
    fun testRecordingStartsSuccessfully() = runBlocking {
        val result = voiceEngine.startRecording()
        assertTrue(result.isSuccess)
        voiceEngine.stopRecording()
    }
    
    @Test
    fun testRecordingStopped() = runBlocking {
        voiceEngine.startRecording()
        val result = voiceEngine.stopRecording()
        assertTrue(result.isSuccess)
    }
}

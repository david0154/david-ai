package com.davidstudioz.david.ai

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageProcessor(private val context: Context) {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    suspend fun processImage(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val image = InputImage.fromBitmap(bitmap, 0)

            val result = labeler.process(image).continueWith { task ->
                if (task.isSuccessful) {
                    val labels = task.result
                    if (labels.isNotEmpty()) {
                        labels.joinToString(", ") { it.text }
                    } else {
                        "I'm not sure what this is."
                    }
                } else {
                    "I couldn't analyze the image."
                }
            }

            result.result
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            "I couldn't process the image."
        }
    }

    companion object {
        private const val TAG = "ImageProcessor"
    }
}

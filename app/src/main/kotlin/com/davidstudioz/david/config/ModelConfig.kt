package com.davidstudioz.david.config

/**
 * Default Model Configuration
 * All models are open source from Hugging Face
 * Users can download models based on their device capacity
 */
object ModelConfig {
    
    // LLM Models (Language Model)
    const val LLM_TINYLLAMA_URL = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q8_0.gguf"
    const val LLM_TINYLLAMA_SIZE = "1.5 GB"
    const val LLM_TINYLLAMA_MIN_RAM = 2 // GB
    
    const val LLM_PHI2_URL = "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q8_0.gguf"
    const val LLM_PHI2_SIZE = "1.4 GB"
    const val LLM_PHI2_MIN_RAM = 3 // GB
    
    const val LLM_QWEN_URL = "https://huggingface.co/second-state/Qwen1.5-1.8B-Chat-GGUF/resolve/main/qwen1.5-1.8b-chat-q8_0.gguf"
    const val LLM_QWEN_SIZE = "1.3 GB"
    const val LLM_QWEN_MIN_RAM = 2 // GB
    
    // Vision Models
    const val VISION_CLIP_URL = "https://huggingface.co/openai/CLIP-vit-base-patch32/resolve/main/model.onnx"
    const val VISION_CLIP_SIZE = "200 MB"
    const val VISION_CLIP_MIN_RAM = 1 // GB
    
    // Speech-to-Text (STT)
    const val STT_WHISPER_URL = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin"
    const val STT_WHISPER_SIZE = "50 MB"
    const val STT_WHISPER_MIN_RAM = 1 // GB
    
    // Text-to-Speech (TTS)
    const val TTS_COQUI_INDIC_URL = "https://huggingface.co/coqui/XTTS-v2/resolve/main/model.pth"
    const val TTS_COQUI_INDIC_SIZE = "2.4 GB"
    const val TTS_COQUI_INDIC_MIN_RAM = 3 // GB
    
    // Embedding Models
    const val EMBEDDING_MINILM_URL = "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/model.safetensors"
    const val EMBEDDING_MINILM_SIZE = "80 MB"
    const val EMBEDDING_MINILM_MIN_RAM = 1 // GB
    
    // Local Storage
    const val CHAT_HISTORY_RETENTION_DAYS = 120
    const val MAX_CHAT_HISTORY_MESSAGES = 10000
    
    // Device Constraints
    const val MIN_DEVICE_RAM_GB = 1.5f
    const val MAX_DEVICE_RAM_GB = 6f
    const val RECOMMENDED_DEVICE_RAM_GB = 4f
}

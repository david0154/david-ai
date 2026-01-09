#!/bin/bash

# DAVID AI - Model Download Script
# This script downloads all required AI models for offline inference

set -e

MODEL_DIR="app/src/main/assets/models"
LOG_FILE="model_download.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🤖 DAVID AI - Model Downloader" > $LOG_FILE
echo "Started at: $(date)" >> $LOG_FILE

# Create model directory
mkdir -p $MODEL_DIR

echo -e "${GREEN}✓${NC} Creating model directory: $MODEL_DIR"

# Function to download models
download_model() {
    local model_name=$1
    local model_url=$2
    local model_size=$3
    
    echo -e "${YELLOW}⏳${NC} Downloading $model_name ($model_size)..."
    echo "Downloading $model_name from $model_url" >> $LOG_FILE
    
    if command -v wget &> /dev/null; then
        wget -q --show-progress "$model_url" -O "$MODEL_DIR/$model_name" 2>> $LOG_FILE
    elif command -v curl &> /dev/null; then
        curl -# -L "$model_url" -o "$MODEL_DIR/$model_name" 2>> $LOG_FILE
    else
        echo -e "${RED}✗${NC} Neither wget nor curl found"
        return 1
    fi
    
    if [ -f "$MODEL_DIR/$model_name" ]; then
        echo -e "${GREEN}✓${NC} Downloaded $model_name"
        echo "Successfully downloaded $model_name" >> $LOG_FILE
        return 0
    else
        echo -e "${RED}✗${NC} Failed to download $model_name"
        echo "Failed to download $model_name" >> $LOG_FILE
        return 1
    fi
}

# Download TinyLLaMA (Main LLM - 1.5GB)
echo -e "\n${YELLOW}[1/4]${NC} Downloading TinyLLaMA..."
download_model "tinyllama-1b-q8.gguf" "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q8_0.gguf" "1.5GB"

# Download Phi-2 (Backup LLM - 1.4GB)
echo -e "\n${YELLOW}[2/4]${NC} Downloading Phi-2..."
download_model "phi-2-q8.gguf" "https://huggingface.co/TheBloke/phi-2-GGUF/resolve/main/phi-2.Q8_0.gguf" "1.4GB"

# Download CLIP (Vision Model - 200MB)
echo -e "\n${YELLOW}[3/4]${NC} Downloading CLIP Vision Model..."
download_model "clip-vit-base-patch32.onnx" "https://huggingface.co/openai/CLIP-vit-base-patch32/resolve/main/model.onnx" "200MB"

# Download Whisper Tiny (STT - 50MB)
echo -e "\n${YELLOW}[4/4]${NC} Downloading Whisper Tiny (STT)..."
download_model "whisper-tiny.en.ggml" "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin" "50MB"

echo -e "\n${GREEN}✓ All models downloaded successfully!${NC}"
echo "Completed at: $(date)" >> $LOG_FILE
echo -e "\n${YELLOW}ℹ${NC} Total size: ~3.2 GB"
echo -e "${YELLOW}ℹ${NC} Log file: $LOG_FILE"

echo -e "\n${GREEN}✓ DAVID AI models are ready for offline inference!${NC}"
echo ""
echo "Next steps:"
echo "1. Run: ./gradlew build"
echo "2. Run: ./gradlew assembleDebug"
echo "3. Run: ./gradlew installDebug"
echo ""

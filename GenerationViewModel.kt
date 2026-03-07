package com.yourapp.imagegenerator.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.imagegenerator.repository.ImageGenerationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class GenerationViewModel : ViewModel() {
    
    private val repository = ImageGenerationRepository()
    
    private val _generatedImage = MutableStateFlow<Bitmap?>(null)
    val generatedImage: StateFlow<Bitmap?> = _generatedImage
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _history = MutableStateFlow<List<GenerationItem>>(emptyList())
    val history: StateFlow<List<GenerationItem>> = _history
    
    fun generateImage(prompt: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = repository.generateImage(prompt)
                result.onSuccess { bitmap ->
                    _generatedImage.value = bitmap
                    
                    // Сохраняем в историю
                    val item = GenerationItem(
                        prompt = prompt,
                        timestamp = System.currentTimeMillis(),
                        bitmap = bitmap
                    )
                    _history.value = listOf(item) + _history.value
                    
                }.onFailure { error ->
                    _error.value = error.message
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveImage() {
        _generatedImage.value?.let { bitmap ->
            val file = File.createTempFile("ai_image_${System.currentTimeMillis()}", ".png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            // Здесь можно показать уведомление о сохранении
        }
    }
    
    fun shareImage() {
        // Реализация шаринга через Intent
    }
}

data class GenerationItem(
    val prompt: String,
    val timestamp: Long,
    val bitmap: Bitmap
)

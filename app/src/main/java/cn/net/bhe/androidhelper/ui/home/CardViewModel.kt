package cn.net.bhe.androidhelper.ui.home

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

abstract class CardViewModel(title: String, description: String, color: Long) : ViewModel() {

    val title = mutableStateOf(title)
    val description = mutableStateOf(description)
    val color = mutableLongStateOf(color)

    fun updateDescription(newValue: String) {
        description.value = newValue
    }

    fun updateColor(newValue: Long) {
        color.longValue = newValue
    }

    abstract fun onClick()

}
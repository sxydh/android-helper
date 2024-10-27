package cn.net.bhe.androidhelper.ui.home

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CardViewModel(title: String, description: String, color: Long) : ViewModel() {

    var title = mutableStateOf(title)
    var description = mutableStateOf(description)
    var color = mutableLongStateOf(color)

    fun updateDescription(newValue: String) {
        description.value = newValue
    }

    fun updateColor(newValue: Long) {
        color.longValue = newValue
    }

}
package cn.net.bhe.androidhelper.ui.home

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel

abstract class CardViewModel : ViewModel() {

    abstract val title: MutableState<String>
    abstract val description: MutableState<String>
    abstract val color: MutableLongState

    open fun updateDescription(newValue: String) {
        description.value = newValue
    }

    open fun updateColor(newValue: Long) {
        color.longValue = newValue
    }

    abstract fun onClick()

}
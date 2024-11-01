package cn.net.bhe.androidhelper.ui.home.impl

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import cn.net.bhe.androidhelper.MainActivity
import cn.net.bhe.androidhelper.ui.home.BaseCard
import cn.net.bhe.androidhelper.ui.home.CardData
import cn.net.bhe.androidhelper.utils.FileServerUtils
import cn.net.bhe.androidhelper.utils.IPUtils
import cn.net.bhe.mutil.StrUtils

@Composable
fun FileServerCard() {
    val context = LocalContext.current as MainActivity
    val cardData = FileServerCardData()
    cardData.init(context)
    BaseCard(cardData) {
        cardData.onClick(context)
    }
}

class FileServerCardData : CardData() {

    companion object {
        const val ACTIVE_COLOR = 0xFF1AEA0B
        const val INACTIVE_COLOR = 0xFFFF9C1D

        var IP: String? = null
        const val PORT = 34567
        var USERNAME: String? = null
        var PASSWORD: String? = null
        var FILE_SERVER: FileServerUtils.FileServer? = null

        const val TITLE = "文件服务器"
        var DESCRIPTION = StrUtils.EMPTY
        var COLOR = INACTIVE_COLOR
    }

    override val title = TITLE
    override var description = mutableStateOf(DESCRIPTION)
    override val color = mutableLongStateOf(COLOR)

    fun init(activity: MainActivity) {
        IP = IPUtils.getLanIP(activity)
        updateDescription(getDescription())
    }

    private fun getDescription(): String {
        return if (COLOR == ACTIVE_COLOR) "$IP:$PORT${System.lineSeparator()}$USERNAME:$PASSWORD" else "$IP"
    }

    private fun updateDescription(newValue: String) {
        description.value = newValue
        DESCRIPTION = newValue
    }

    private fun updateColor(newValue: Long) {
        color.longValue = newValue
        COLOR = newValue
    }

    fun onClick(activity: MainActivity) {
        if (!hasPermission()) {
            requestPermission(activity)
            return
        }

        if (COLOR == INACTIVE_COLOR) {
            USERNAME = StrUtils.randomEn(3)
            PASSWORD = StrUtils.randomNum(6)
            FILE_SERVER = FileServerUtils.build("0.0.0.0", PORT, "/storage/emulated/0/Download/ROOT", USERNAME!!, PASSWORD!!)
            FILE_SERVER?.start()?.let {
                updateColor(ACTIVE_COLOR)
                updateDescription(getDescription())
            }
        } else {
            FILE_SERVER?.stop()?.let {
                updateColor(INACTIVE_COLOR)
                updateDescription(getDescription())
            }
        }
    }

    private fun hasPermission(): Boolean {
        return Environment.isExternalStorageManager()
    }

    private fun requestPermission(activity: MainActivity) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }

}
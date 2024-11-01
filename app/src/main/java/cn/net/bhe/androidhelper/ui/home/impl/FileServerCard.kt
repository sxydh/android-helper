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
import cn.net.bhe.androidhelper.ui.home.CardBase
import cn.net.bhe.androidhelper.ui.home.CardViewModel
import cn.net.bhe.androidhelper.utils.FileServerUtils
import cn.net.bhe.androidhelper.utils.IPUtils
import cn.net.bhe.mutil.StrUtils
import java.lang.ref.WeakReference

@Composable
fun FileServerCardView() {
    CardBase(FileServerCardViewModel(LocalContext.current as MainActivity))
}

class FileServerCardViewModel(activity: MainActivity) : CardViewModel() {

    companion object {
        const val ACTIVE_COLOR = 0xFF1AEA0B
        const val INACTIVE_COLOR = 0xFFFF9C1D

        var IP: String? = null
        const val PORT = 34567
        var USERNAME: String? = null
        var PASSWORD: String? = null
        var FILE_SERVER: FileServerUtils.FileServer? = null

        var TITLE: String = "文件服务器"
        var DESCRIPTION: String = StrUtils.EMPTY
        var COLOR: Long = INACTIVE_COLOR
    }

    override val title = mutableStateOf(TITLE)
    override val description = mutableStateOf(DESCRIPTION)
    override val color = mutableLongStateOf(COLOR)

    private val activityRef: WeakReference<MainActivity> = WeakReference(activity)

    init {
        activityRef.get()?.let {
            IP = IPUtils.getLanIP(it)
        }
        updateDescription(getDescription())
    }

    private fun getDescription(): String {
        return if (COLOR == ACTIVE_COLOR) "$IP:$PORT${System.lineSeparator()}$USERNAME:$PASSWORD" else "$IP"
    }

    override fun updateDescription(newValue: String) {
        super.updateDescription(newValue)
        DESCRIPTION = newValue
    }

    override fun updateColor(newValue: Long) {
        super.updateColor(newValue)
        COLOR = newValue
    }

    override fun onClick() {
        val activity = activityRef.get() ?: return
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
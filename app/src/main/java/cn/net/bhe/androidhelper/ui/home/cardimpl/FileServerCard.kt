package cn.net.bhe.androidhelper.ui.home.cardimpl

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import cn.net.bhe.androidhelper.MainActivity
import cn.net.bhe.androidhelper.ui.home.CardViewModel
import cn.net.bhe.androidhelper.utils.FileServerUtils
import cn.net.bhe.androidhelper.utils.IPUtils
import cn.net.bhe.mutil.StrUtils
import java.lang.ref.WeakReference

class FileServerCard(activity: MainActivity) : CardViewModel("文件服务器", "", INACTIVE) {

    companion object {
        const val ACTIVE = 0xFF1AEA0B
        const val INACTIVE = 0xFFFF9C1D
        const val PORT = 34567
    }

    private val ip = IPUtils.getLanIP(activity) ?: StrUtils.EMPTY
    private var fileServer: FileServerUtils.FileServer? = null
    private val activityRef: WeakReference<MainActivity> = WeakReference(activity)

    init {
        updateDescription(ip)
    }

    override fun onClick() {
        val activity = activityRef.get() ?: return
        if (!hasPermission()) {
            requestPermission(activity)
            return
        }

        if (color.longValue == INACTIVE) {
            val username = StrUtils.randomEn(3)
            val password = StrUtils.randomNum(6)
            fileServer = FileServerUtils.build("0.0.0.0", PORT, "/storage/emulated/0/Download/ROOT", username, password)
            fileServer?.start()?.let {
                updateColor(ACTIVE)
                updateDescription("$ip:$PORT${System.lineSeparator()}$username:$password")
            }
        } else {
            fileServer?.stop()?.let {
                updateColor(INACTIVE)
                updateDescription(ip)
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
        activity.getActivityResultLauncher()?.launch(intent)
    }

}
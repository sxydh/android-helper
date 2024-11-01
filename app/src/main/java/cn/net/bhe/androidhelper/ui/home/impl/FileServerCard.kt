package cn.net.bhe.androidhelper.ui.home.impl

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.net.bhe.androidhelper.MainActivity
import cn.net.bhe.androidhelper.ui.home.BaseCard
import cn.net.bhe.androidhelper.ui.home.CardViewModel
import cn.net.bhe.androidhelper.utils.FileServerUtils
import cn.net.bhe.androidhelper.utils.IPUtils
import cn.net.bhe.mutil.StrUtils

@Composable
fun FileServerCard() {
    val context = LocalContext.current as MainActivity
    val cardViewModel: FileServerCardViewModel = viewModel()
    cardViewModel.init(context)
    BaseCard(cardViewModel) {
        cardViewModel.onClick(context)
    }
}

class FileServerCardViewModel : CardViewModel() {

    private val activeColor = 0xFF1AEA0B
    private val inactiveColor = 0xFFFF9C1D
    private var ip: String = StrUtils.EMPTY
    private val port = 34567
    private var username: String = StrUtils.EMPTY
    private var password: String = StrUtils.EMPTY
    private var fileServer: FileServerUtils.FileServer? = null

    override val title = "文件服务器"
    override var description = mutableStateOf(StrUtils.EMPTY)
    override val color = mutableLongStateOf(inactiveColor)

    fun init(activity: MainActivity) {
        ip = IPUtils.getLanIP(activity) ?: StrUtils.EMPTY
        description.value = getDescription()
    }

    private fun getDescription(): String {
        return if (color.longValue == activeColor) "$ip:$port${System.lineSeparator()}$username:$password" else ip
    }

    fun onClick(activity: MainActivity) {
        if (!checkPermission(activity)) {
            return
        }

        if (color.longValue == inactiveColor) {
            username = StrUtils.randomEn(3)
            password = StrUtils.randomNum(6)
            fileServer = FileServerUtils.build(
                "0.0.0.0",
                port,
                "/storage/emulated/0/Download/ROOT",
                username,
                password
            ).apply {
                start()
                color.longValue = activeColor
                description.value = getDescription()
            }
        } else {
            fileServer?.apply {
                stop()
                color.longValue = inactiveColor
                description.value = getDescription()
            }
        }
    }

    private fun checkPermission(activity: MainActivity): Boolean {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
            return false
        }
        return true
    }

}
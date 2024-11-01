package cn.net.bhe.androidhelper.ui.home.impl

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import cn.net.bhe.androidhelper.MainActivity
import cn.net.bhe.androidhelper.ui.home.BaseCard
import cn.net.bhe.androidhelper.ui.home.CardData
import cn.net.bhe.mutil.StrUtils
import java.lang.ref.WeakReference

@Composable
fun AutoClickCard() {
    val context = LocalContext.current as MainActivity
    val cardData = AutoClickCardData()
    BaseCard(cardData) {
        cardData.onClick(context)
    }
    PreOverlayView(cardData)
}

@Composable
fun PreOverlayView(cardData: AutoClickCardData) {
    val context = LocalContext.current as MainActivity
    val preOverlayViewData = PreOverlayViewData()

    preOverlayViewData.removeView(context)
    if (cardData.isOpenPreOverlayView()) {
        val color by preOverlayViewData.color

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
            setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
            setContent {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .clickable {
                            preOverlayViewData.onClick(context)
                        }
                ) {}
            }
        }
        preOverlayViewData.addView(context, composeView)
    }
}

class AutoClickCardData : CardData() {

    companion object {
        val TAG: String = AutoClickCardData::class.java.simpleName
        const val ACTIVE_COLOR = 0xFF1AEA0B
        const val INACTIVE_COLOR = 0xFF1DFFEC
        val BC_ID: String = "${AutoClickCardData::class.java.name}.onClick"

        const val TITLE = "连击器"
        var DESCRIPTION = StrUtils.EMPTY
        var COLOR = INACTIVE_COLOR
    }

    override val title = TITLE
    override val description = mutableStateOf(DESCRIPTION)
    override val color = mutableLongStateOf(COLOR)

    private fun updateColor(newValue: Long) {
        color.longValue = newValue
        COLOR = newValue
    }

    fun onClick(activity: MainActivity) {
        Log.d(TAG, "onClick")

        if (color.longValue == INACTIVE_COLOR) {
            if (!checkPermission(activity)) {
                return
            }
            updateColor(ACTIVE_COLOR)
        } else {
            updateColor(INACTIVE_COLOR)
        }
    }

    private fun checkPermission(activity: MainActivity): Boolean {
        if (!Settings.canDrawOverlays(activity)) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
            return false
        }
        if (!MyAccessibilityService.isAccessibilityServiceEnabled(activity, MyAccessibilityService::class.java)) {
            val intent = Intent(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            activity.startActivity(intent)
            return false
        }
        return true
    }

    fun isOpenPreOverlayView(): Boolean {
        return color.longValue == ACTIVE_COLOR
    }

}

class PreOverlayViewData {

    companion object {
        val TAG: String = AutoClickCardData::class.java.simpleName
        const val ACTIVE_COLOR = 0xFF1AEA0B
        const val INACTIVE_COLOR = 0xFF1DFFEC

        var VIEW = WeakReference<ComposeView>(null)
        var COLOR = INACTIVE_COLOR
    }

    var color = mutableLongStateOf(COLOR)

    fun addView(activity: MainActivity, composeView: ComposeView) {
        addViewDo(activity, composeView)
        VIEW = WeakReference(composeView)
    }

    private fun updateColor(newValue: Long) {
        color.longValue = newValue
        COLOR = newValue
    }

    fun onClick(activity: MainActivity) {
        Log.d(TAG, "onClick")

        if (color.longValue == INACTIVE_COLOR) {
            updateColor(ACTIVE_COLOR)
        } else {
            updateColor(INACTIVE_COLOR)
        }
    }

    fun removeView(activity: MainActivity) {
        VIEW.get()?.let {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
            VIEW = WeakReference(null)
        }
    }

}

class MyAccessibilityService : AccessibilityService() {

    companion object {
        val TAG: String = MyAccessibilityService::class.java.simpleName

        fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
            val str = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (StrUtils.isEmpty(str)) {
                return false
            }
            return str.contains(service.simpleName)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive")

            onReceiveDo(context)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        val filter = IntentFilter(AutoClickCardData.BC_ID)
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        unregisterReceiver(broadcastReceiver)
    }

    private fun onReceiveDo(context: Context) {
        Log.d(TAG, "onReceiveDo")
    }

}

private fun addViewDo(
    activity: MainActivity,
    composeView: ComposeView,
    wv: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    hv: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    gv: Int = Gravity.CENTER or Gravity.CENTER,
    xv: Int = 0,
    yv: Int = 0
) {
    val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val params = WindowManager.LayoutParams(
        wv,
        hv,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        android.graphics.PixelFormat.TRANSLUCENT
    ).apply {
        gravity = gv
        x = xv
        y = yv
    }
    windowManager.addView(composeView, params)
}
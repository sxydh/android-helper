package cn.net.bhe.androidhelper.ui.home.impl

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
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

    if (cardData.isOpenOverlayView()) {
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
            setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
            setContent {
                FloatingActionButton(
                    onClick = {
                        cardData.onClickOverlayView(context)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(50.dp)
                        .background(Color(cardData.overlayViewColor.longValue))
                ) {}
            }
        }
        cardData.addOverlayView(context, composeView)
    } else {
        cardData.removeOverlayView(context)
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
        var OVERLAY_VIEW = WeakReference<ComposeView>(null)
        var OVERLAY_VIEW_COLOR = INACTIVE_COLOR
    }

    override val title = TITLE
    override val description = mutableStateOf(DESCRIPTION)
    override val color = mutableLongStateOf(COLOR)
    val overlayViewColor = mutableLongStateOf(OVERLAY_VIEW_COLOR)

    private fun updateColor(newValue: Long) {
        color.longValue = newValue
        COLOR = newValue
    }

    fun onClick(activity: MainActivity) {
        Log.d(TAG, "onClick")

        if (color.longValue == INACTIVE_COLOR) {
            if (!Settings.canDrawOverlays(activity)) {
                requestPermission(activity)
                return
            }
            updateColor(ACTIVE_COLOR)
        } else {
            updateColor(INACTIVE_COLOR)
        }
    }

    fun isOpenOverlayView(): Boolean {
        return color.longValue == ACTIVE_COLOR
    }

    fun addOverlayView(activity: MainActivity, composeView: ComposeView) {
        val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        windowManager.addView(composeView, params)
        OVERLAY_VIEW = WeakReference(composeView)
    }

    private fun updateOverlayViewColor(newValue: Long) {
        overlayViewColor.longValue = newValue
        OVERLAY_VIEW_COLOR = newValue
    }

    fun onClickOverlayView(activity: MainActivity) {
        Log.d(TAG, "onClickOverlayView")

        if (overlayViewColor.longValue == INACTIVE_COLOR) {
            updateOverlayViewColor(ACTIVE_COLOR)
        } else {
            updateOverlayViewColor(INACTIVE_COLOR)
        }
    }

    fun removeOverlayView(activity: MainActivity) {
        OVERLAY_VIEW.get()?.let {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
        }
    }

    private fun requestPermission(activity: MainActivity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }

}

class MyAccessibilityService : AccessibilityService() {

    companion object {
        val TAG: String = MyAccessibilityService::class.java.simpleName
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

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import cn.net.bhe.androidhelper.MainActivity
import cn.net.bhe.androidhelper.ui.home.CardBase
import cn.net.bhe.androidhelper.ui.home.CardViewModel
import cn.net.bhe.mutil.StrUtils
import java.lang.ref.WeakReference

@Composable
fun AutoClickCardView() {
    val viewModel = AutoClickCardViewModel(LocalContext.current as MainActivity)
    CardBase(viewModel)

    if (viewModel.isOpen.value) {
        val context = LocalContext.current
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
            setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
            setContent {
                FloatingActionButton(
                    onClick = {},
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(50.dp)
                ) {}
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        params.x = 100
        params.y = 100

        windowManager.addView(composeView, params)
    }
}

class AutoClickCardViewModel(activity: MainActivity) : CardViewModel() {

    companion object {
        val TAG: String = AutoClickCardViewModel::class.java.simpleName
        val BC_ID: String = "${AutoClickCardViewModel::class.java.name}.onClick"

        const val TITLE = "连击器"
        const val DESCRIPTION = StrUtils.EMPTY
        const val COLOR = 0xFFC6F300
    }

    override val title = mutableStateOf(TITLE)
    override val description = mutableStateOf(DESCRIPTION)
    override val color = mutableLongStateOf(COLOR)
    val isOpen = mutableStateOf(false)

    private val activityRef: WeakReference<MainActivity> = WeakReference(activity)

    override fun onClick() {
        Log.d(TAG, "onClick")

        activityRef.get()?.let {
            if (!Settings.canDrawOverlays(it)) {
                requestPermission(it)
                return
            }
            val intent = Intent(BC_ID)
            it.sendBroadcast(intent)
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

        val filter = IntentFilter(AutoClickCardViewModel.BC_ID)
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
        Log.d(TAG, context.packageName)
    }

}

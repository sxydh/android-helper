package cn.net.bhe.androidhelper.ui.home.impl

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import cn.net.bhe.androidhelper.MainActivity
import cn.net.bhe.androidhelper.R
import cn.net.bhe.androidhelper.ui.home.CardViewModel
import cn.net.bhe.mutil.StrUtils
import java.lang.ref.WeakReference

class AutoClickImpl(activity: MainActivity) : CardViewModel() {

    companion object {
        val TAG: String = AutoClickImpl::class.java.simpleName
        val BC_ID: String = "${AutoClickImpl::class.java.name}.onClick"

        const val TITLE = "连击器"
        const val DESCRIPTION = StrUtils.EMPTY
        const val COLOR = 0xFFC6F300
    }

    override val title = mutableStateOf(TITLE)
    override val description = mutableStateOf(DESCRIPTION)
    override val color = mutableLongStateOf(COLOR)

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

    private var floatingView: View? = null
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive")

            onReceiveDo(context)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        val filter = IntentFilter(AutoClickImpl.BC_ID)
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        unregisterReceiver(broadcastReceiver)
    }

    @SuppressLint("InflateParams")
    private fun onReceiveDo(context: Context) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (floatingView == null) {
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                x = 100
                y = 100
            }

            floatingView = LayoutInflater.from(context).inflate(R.layout.layout_auto_click_floating_window, null)
            windowManager.addView(floatingView, layoutParams)
            return
        }

        floatingView?.let {
            windowManager.removeView(it)
            floatingView = null
        }
    }

}
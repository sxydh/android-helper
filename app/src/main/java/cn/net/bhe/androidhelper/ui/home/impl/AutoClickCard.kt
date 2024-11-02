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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import cn.net.bhe.androidhelper.MainActivity
import cn.net.bhe.androidhelper.ui.home.BaseCard
import cn.net.bhe.androidhelper.ui.home.CardViewModel
import cn.net.bhe.androidhelper.ui.home.impl.PreViewModel.Companion
import cn.net.bhe.mutil.StrUtils
import java.lang.ref.WeakReference

@Composable
fun AutoClickCard() {
    val context = LocalContext.current as MainActivity
    val cardViewModel: AutoClickCardViewModel = viewModel()
    BaseCard(cardViewModel) {
        cardViewModel.onClick(context)
    }
    OverlayView(cardViewModel)
}

@Composable
fun OverlayView(cardViewModel: AutoClickCardViewModel) {
    val context = LocalContext.current as MainActivity
    val preViewModel: PreViewModel = viewModel()
    val maskViewModel: MaskViewModel = viewModel()
    val pointerViewModel: PointerViewModel = viewModel()

    pointerViewModel.removeView(context)
    if (maskViewModel.isOpenPointer.value) {
        PointerOverlayView(pointerViewModel)
    }

    maskViewModel.removeView(context)
    if (preViewModel.isOpenMask.value) {
        MaskOverlayView(maskViewModel)
    }

    preViewModel.removeView(context)
    if (cardViewModel.isOpenPre.value) {
        PreOverlayView(preViewModel)
    }
}

@Composable
fun PreOverlayView(preViewModel: PreViewModel) {
    val context = LocalContext.current as MainActivity
    val color by preViewModel.color

    val composeView = ComposeView(context).apply {
        setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
        setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
        setContent {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .clickable { preViewModel.onClick() }
            ) {}
        }
    }
    preViewModel.addView(context, composeView)
}

@Composable
fun MaskOverlayView(maskViewModel: MaskViewModel) {
    val context = LocalContext.current as MainActivity

    val composeView = ComposeView(context).apply {
        setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
        setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset: Offset ->
                            println("${offset.x}, ${offset.y}")
                        }
                    }
            ) {}
        }
    }
    maskViewModel.addView(context, composeView)
}

@Composable
fun PointerOverlayView(pointerViewModel: PointerViewModel) {
    val context = LocalContext.current as MainActivity

    val composeView = ComposeView(context).apply {
        setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
        setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
        setContent { }
    }
    pointerViewModel.addView(context, composeView)
}

class AutoClickCardViewModel : CardViewModel() {

    companion object {
        private val TAG: String = AutoClickCardViewModel::class.java.simpleName
        val BC_ID: String = "${AutoClickCardViewModel::class.java.name}.onClick"
    }

    private val activeColor = 0xFF1AEA0B
    private val inactiveColor = 0xFFFF9C1D

    override val title = mutableStateOf("连击器")
    override var description = mutableStateOf(StrUtils.EMPTY)
    override val color = mutableLongStateOf(inactiveColor)
    val isOpenPre = mutableStateOf(false)

    fun onClick(activity: MainActivity) {
        Log.d(TAG, "onClick")

        if (color.longValue == inactiveColor) {
            if (!checkPermission(activity)) {
                return
            }
            color.longValue = activeColor
            isOpenPre.value = true
        } else {
            color.longValue = inactiveColor
            isOpenPre.value = false
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

}

class PreViewModel : ViewModel() {

    companion object {
        private val TAG: String = AutoClickCardViewModel::class.java.simpleName
    }

    private val activeColor = 0xFF1AEA0B
    private val inactiveColor = 0xFFFF9C1D
    private var view = WeakReference<ComposeView>(null)

    val color = mutableLongStateOf(inactiveColor)
    val isOpenMask = mutableStateOf(false)

    fun onClick() {
        Log.d(TAG, "onClick")

        if (color.longValue == inactiveColor) {
            color.longValue = activeColor
            isOpenMask.value = true
        } else {
            color.longValue = inactiveColor
            isOpenMask.value = false
        }
    }

    fun addView(activity: MainActivity, composeView: ComposeView) {
        addViewDo(activity, composeView)
        view = WeakReference(composeView)
    }

    fun removeView(activity: MainActivity) {
        view.get()?.let {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
            view = WeakReference(null)
        }
    }

}

class MaskViewModel : ViewModel() {

    companion object {
        private val TAG: String = MaskViewModel::class.java.simpleName
    }

    private var view = WeakReference<ComposeView>(null)

    val isOpenPointer = mutableStateOf(false)

    fun onClick() {
        Log.d(TAG, "onClick")
    }

    fun addView(activity: MainActivity, composeView: ComposeView) {
        addViewDo(activity, composeView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        view = WeakReference(composeView)
    }

    fun removeView(activity: MainActivity) {
        view.get()?.let {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
            view = WeakReference(null)
        }
    }

}

class PointerViewModel : ViewModel() {

    companion object {
        private val TAG: String = PointerViewModel::class.java.simpleName
    }

    private var view = WeakReference<ComposeView>(null)

    fun addView(activity: MainActivity, composeView: ComposeView) {
        addViewDo(activity, composeView)
        view = WeakReference(composeView)
    }

    fun removeView(activity: MainActivity) {
        view.get()?.let {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
            view = WeakReference(null)
        }
    }

}

class MyAccessibilityService : AccessibilityService() {

    companion object {
        private val TAG: String = MyAccessibilityService::class.java.simpleName

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
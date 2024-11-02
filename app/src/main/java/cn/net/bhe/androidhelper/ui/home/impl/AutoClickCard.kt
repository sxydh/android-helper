package cn.net.bhe.androidhelper.ui.home.impl

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
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
import cn.net.bhe.mutil.StrUtils
import java.lang.ref.WeakReference
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy
import java.util.concurrent.TimeUnit

@Composable
fun AutoClickCard() {
    Log.d("@Composable", "AutoClickCard")

    val context = LocalContext.current as MainActivity
    val cardViewModel: AutoClickCardViewModel = viewModel()
    BaseCard(cardViewModel) {
        cardViewModel.onClick(context)
    }
    OverlayView()
}

@Composable
fun OverlayView() {
    Log.d("@Composable", "OverlayView")

    val cardViewModel: AutoClickCardViewModel = viewModel()
    val ctrlViewModel: CtrlViewModel = viewModel()
    val maskViewModel: MaskViewModel = viewModel()
    val pointerViewModel: PointerViewModel = viewModel()

    if (maskViewModel.isOpenPointer.value) {
        PointerOverlayView(pointerViewModel)
    }
    if (ctrlViewModel.isOpenMask.value) {
        MaskOverlayView(maskViewModel)
    }
    if (cardViewModel.isOpenCtrl.value) {
        CtrlOverlayView(ctrlViewModel)
    }
}

@Composable
fun CtrlOverlayView(ctrlViewModel: CtrlViewModel) {
    Log.d("@Composable", "CtrlOverlayView")

    val context = LocalContext.current as MainActivity
    val description by ctrlViewModel.description
    val color by ctrlViewModel.color

    val composeView = ComposeView(context).apply {
        setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
        setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
        setContent {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .clickable { ctrlViewModel.onClick(context) }
            ) {
                Text(text = description, modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    DisposableEffect(Unit) {
        ctrlViewModel.addView(context, composeView)
        onDispose {
            ctrlViewModel.removeView(context)
        }
    }
}

@Composable
fun MaskOverlayView(maskViewModel: MaskViewModel) {
    Log.d("@Composable", "MaskOverlayView")

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
                            maskViewModel.onClick(context, offset)
                        }
                    }
            ) {}
        }
    }

    DisposableEffect(Unit) {
        maskViewModel.addView(context, composeView)
        onDispose {
            maskViewModel.removeView(context)
        }
    }
}

@Composable
fun PointerOverlayView(pointerViewModel: PointerViewModel) {
    Log.d("@Composable", "PointerOverlayView")

    val context = LocalContext.current as MainActivity

    val composeView = ComposeView(context).apply {
        setViewTreeLifecycleOwner(LocalLifecycleOwner.current)
        setViewTreeSavedStateRegistryOwner(LocalSavedStateRegistryOwner.current)
        setContent { }
    }

    DisposableEffect(Unit) {
        pointerViewModel.addView(context, composeView)
        onDispose {
            pointerViewModel.removeView(context)
        }
    }
}

class AutoClickCardViewModel : CardViewModel() {

    companion object {
        private val TAG: String = AutoClickCardViewModel::class.java.simpleName
    }

    private val activeColor = 0xFF1AEA0B
    private val inactiveColor = 0xFFFF9C1D

    override val title = mutableStateOf("连击器")
    override var description = mutableStateOf(StrUtils.EMPTY)
    override val color = mutableLongStateOf(inactiveColor)
    val isOpenCtrl = mutableStateOf(false)

    fun onClick(activity: MainActivity) {
        Log.d(TAG, "onClick")

        if (color.longValue == inactiveColor) {
            if (!checkPermission(activity)) {
                return
            }
            color.longValue = activeColor
            isOpenCtrl.value = true
        } else {
            color.longValue = inactiveColor
            isOpenCtrl.value = false
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
        if (!MyAccessibilityService.isAccessibilityServiceEnabled(activity)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            activity.startActivity(intent)
            return false
        }
        return true
    }

}

class CtrlViewModel : ViewModel() {

    companion object {
        private val TAG: String = AutoClickCardViewModel::class.java.simpleName
        val BC: String = "${CtrlViewModel::class.java.name}.Broadcast"
        const val MSG_ACTION_CLICK_COUNT = "MSG_ACTION_CLICK_COUNT"
    }

    private val activeColor = 0xFF1AEA0B
    private val inactiveColor = 0xFFFF9C1D
    private var view = WeakReference<ComposeView>(null)
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val count = intent.getIntExtra("count", 0)
            description.value = count.toString()
        }
    }

    val color = mutableLongStateOf(inactiveColor)
    val description = mutableStateOf(StrUtils.EMPTY)
    val isOpenMask = mutableStateOf(false)

    fun onClick(activity: MainActivity) {
        Log.d(TAG, "onClick")

        if (color.longValue == inactiveColor) {
            color.longValue = activeColor
            isOpenMask.value = true
        } else {
            color.longValue = inactiveColor
            isOpenMask.value = false

            val intent = Intent(MyAccessibilityService.BC)
            intent.putExtra("action", MyAccessibilityService.MSG_ACTION_STOP_CLICK)
            activity.sendBroadcast(intent)
        }
    }

    fun addView(activity: MainActivity, composeView: ComposeView) {
        Log.d(TAG, "addView")

        addViewDo(activity, composeView)
        view = WeakReference(composeView)

        val filter = IntentFilter(BC)
        activity.registerReceiver(receiver, filter)
    }

    fun removeView(activity: MainActivity) {
        Log.d(TAG, "removeView")

        view.get()?.let {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
            view = WeakReference(null)

            activity.unregisterReceiver(receiver)
        }
    }

}

class MaskViewModel : ViewModel() {

    companion object {
        private val TAG: String = MaskViewModel::class.java.simpleName
    }

    private var view = WeakReference<ComposeView>(null)

    val isOpenPointer = mutableStateOf(false)

    fun onClick(activity: MainActivity, offset: Offset) {
        Log.d(TAG, "onClick")

        removeView(activity)
        val intent = Intent(MyAccessibilityService.BC)
        intent.putExtra("action", MyAccessibilityService.MSG_ACTION_AUTO_CLICK)
        intent.putExtra("x", offset.x)
        intent.putExtra("y", offset.y)
        activity.sendBroadcast(intent)
    }

    fun addView(activity: MainActivity, composeView: ComposeView) {
        Log.d(TAG, "addView")

        addViewDo(activity, composeView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        view = WeakReference(composeView)
    }

    fun removeView(activity: MainActivity) {
        Log.d(TAG, "removeView")

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
        Log.d(TAG, "addView")

        addViewDo(activity, composeView)
        view = WeakReference(composeView)
    }

    fun removeView(activity: MainActivity) {
        Log.d(TAG, "removeView")

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
        val BC: String = "${MyAccessibilityService::class.java.name}.Broadcast"
        const val MSG_ACTION_AUTO_CLICK = "MSG_ACTION_AUTO_CLICK"
        const val MSG_ACTION_STOP_CLICK = "MSG_ACTION_STOP_CLICK"
        private val executor = ThreadPoolExecutor(
            2,
            4,
            1000,
            TimeUnit.MILLISECONDS,
            ArrayBlockingQueue(8),
            Executors.defaultThreadFactory(),
            DiscardPolicy()
        )

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val str = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (StrUtils.isEmpty(str)) {
                return false
            }
            return str.contains(MyAccessibilityService::class.java.simpleName)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive")

            onReceiveDo(intent)
        }
    }

    @Volatile
    private var clickJobFlag = MSG_ACTION_STOP_CLICK

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        val filter = IntentFilter(BC)
        registerReceiver(receiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        unregisterReceiver(receiver)
    }

    private fun onReceiveDo(intent: Intent) {
        val action = intent.getStringExtra("action") ?: MSG_ACTION_STOP_CLICK
        if (action == MSG_ACTION_AUTO_CLICK) {
            val x = intent.getFloatExtra("x", 0.0f)
            val y = intent.getFloatExtra("y", 0.0f)
            handleAutoClickJob(x, y)
            return
        }
        if (action == MSG_ACTION_STOP_CLICK) {
            handleStopClickJob()
            return
        }
    }

    private fun handleAutoClickJob(x: Float, y: Float) {
        if (clickJobFlag == MSG_ACTION_AUTO_CLICK) {
            return
        }
        clickJobFlag = MSG_ACTION_AUTO_CLICK
        executor.submit {
            Log.d(TAG, "submit")

            var count = 0
            while (clickJobFlag == MSG_ACTION_AUTO_CLICK) {
                val path = Path().apply {
                    moveTo(x, y)
                    lineTo(x, y)
                }
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0L, 1))
                    .build()
                dispatchGesture(gesture, null, null)

                val intent = Intent(CtrlViewModel.BC)
                intent.putExtra("action", CtrlViewModel.MSG_ACTION_CLICK_COUNT)
                intent.putExtra("count", ++count)
                sendBroadcast(intent)
                Thread.sleep(300)
            }
        }
    }

    private fun handleStopClickJob() {
        clickJobFlag = MSG_ACTION_STOP_CLICK
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
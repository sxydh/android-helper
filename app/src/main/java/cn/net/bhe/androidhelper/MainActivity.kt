package cn.net.bhe.androidhelper

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import cn.net.bhe.androidhelper.ui.home.HomeScreen
import cn.net.bhe.androidhelper.ui.theme.AndroidHelperTheme
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)

            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(sw.toString())
                .setPositiveButton("Exit") { _, _ ->
                    exitProcess(1)
                }
                .setCancelable(false)
                .show()
        }
        Log.d(TAG, "onCreate")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidHelperTheme {
                Surface {
                    HomeScreen()
                }
            }
        }
    }

}

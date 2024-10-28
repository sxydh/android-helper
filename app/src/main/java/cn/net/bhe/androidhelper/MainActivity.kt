package cn.net.bhe.androidhelper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import cn.net.bhe.androidhelper.ui.home.HomeScreen
import cn.net.bhe.androidhelper.ui.theme.AndroidHelperTheme

class MainActivity : ComponentActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initActivityResultLauncher()
        initContent()
    }

    private fun initActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, result.toString())
            }
        }
    }

    private fun initContent() {
        setContent {
            AndroidHelperTheme {
                Surface {
                    HomeScreen(this)
                }
            }
        }
    }

    fun getActivityResultLauncher(): ActivityResultLauncher<Intent>? {
        return activityResultLauncher
    }

}

package cn.net.bhe.androidhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import cn.net.bhe.androidhelper.ui.home.HomeScreen
import cn.net.bhe.androidhelper.ui.theme.AndroidHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidHelperTheme {
                Surface {
                    HomeScreen(this)
                }
            }
        }
    }
}

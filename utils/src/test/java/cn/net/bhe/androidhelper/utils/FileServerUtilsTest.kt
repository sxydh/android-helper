package cn.net.bhe.androidhelper.utils

import cn.net.bhe.mutil.FlUtils
import org.junit.Test

class FileServerUtilsTest {

    @Test
    fun build() {
        val root = FlUtils.combine(FlUtils.getRootTmp(), "ROOT")
        FlUtils.mkdir(root)
        val fileServer = FileServerUtils.build(50, root)
        fileServer.start()
    }
}
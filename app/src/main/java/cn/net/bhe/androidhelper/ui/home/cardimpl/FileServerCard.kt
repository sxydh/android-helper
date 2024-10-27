package cn.net.bhe.androidhelper.ui.home.cardimpl

import cn.net.bhe.androidhelper.ui.home.CardViewModel
import cn.net.bhe.mutil.FlUtils

class FileServerCard : CardViewModel("文件服务器", "", INACTIVE) {

    override fun onClick() {
        if (color.longValue == INACTIVE) {
            updateColor(ACTIVE)
        } else {
            updateColor(INACTIVE)
        }
        println(FlUtils.getRoot())
    }

    companion object {
        const val ACTIVE = 0xFF1AEA0B
        const val INACTIVE = 0xFFFF9C1D
    }

}
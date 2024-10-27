package cn.net.bhe.androidhelper.ui.home.cardimpl

import cn.net.bhe.androidhelper.ui.home.CardViewModel

class FileServerCard : CardViewModel("文件服务器", "", INACTIVE) {


    override fun onClick() {
        updateColor(if (color.longValue == INACTIVE) ACTIVE else INACTIVE)
    }

    companion object {
        const val ACTIVE = 0xFF1AEA0B
        const val INACTIVE = 0xFFFF9C1D
    }

}
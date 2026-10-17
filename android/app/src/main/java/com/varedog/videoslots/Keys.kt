package com.varedog.videoslots

object Keys {

    val CONFIRM = intArrayOf(
        23,   // DPAD_CENTER
        66,   // ENTER
        160,  // NUMPAD_ENTER
        62,   // SPACE
        85,   // MEDIA_PLAY_PAUSE
        96,   // BUTTON_A
        109   // BUTTON_SELECT
    )

    fun isConfirm(code: Int): Boolean = CONFIRM.contains(code)

    fun name(code: Int): String = when (code) {
        23 -> "DPAD_CENTER"
        66 -> "ENTER"
        160 -> "NUMPAD_ENTER"
        62 -> "SPACE"
        85 -> "PLAY_PAUSE"
        96 -> "BUTTON_A"
        109 -> "BUTTON_SELECT"
        4 -> "BACK"
        82 -> "MENU"
        19 -> "上"
        20 -> "下"
        21 -> "左"
        22 -> "右"
        else -> "KEYCODE_$code"
    }
}

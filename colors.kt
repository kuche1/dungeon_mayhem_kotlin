
package colors

// https://ss64.com/nt/syntax-ansi.html
// special
const val COL_RESET = "\u001b[0m"
const val COL_UNDERLINE = "\u001b[4m"
// colors
const val COL_BLACK_BRIGHT = "\u001b[90m" 
const val COL_BLUE_DARK = "\u001b[34m"
const val COL_CYAN_BRIGHT = "\u001b[96m"  
const val COL_CYAN_DARK = "\u001b[36m"
const val COL_GREEN_BRIGHT = "\u001b[92m"
const val COL_GREEN_DARK = "\u001b[32m"
const val COL_MAGENTA_BRIGHT = "\u001b[95m"
const val COL_MAGENTA_DARK = "\u001b[35m"
const val COL_RED_BRIGHT = "\u001b[91m"
const val COL_RED_DARK = "\u001b[31m"
const val COL_YELLOW_BRIGHT = "\u001b[93m"
const val COL_YELLOW_DARK = "\u001b[33m"
// combinations
const val COL_DMG = COL_BLACK_BRIGHT
const val COL_ERROR = "\u001b[101;4m"
const val COL_SHIELD = COL_YELLOW_DARK
const val COL_HEAL = COL_RED_BRIGHT
const val COL_DRAW = COL_GREEN_DARK
const val COL_THUNDER = COL_YELLOW_BRIGHT

// icons
const val ICON_SHIELD:String = "${COL_SHIELD}shield${COL_RESET}"
const val ICON_HEAL:String = "${COL_HEAL}heal${COL_RESET}"
const val ICON_DMG:String = "${COL_DMG}dmg${COL_RESET}"

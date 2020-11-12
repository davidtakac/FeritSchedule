package os.dtakac.feritraspored.views.debounce

import android.os.SystemClock
import android.view.MenuItem
import os.dtakac.feritraspored.common.constants.DEBOUNCE_INTERVAL

class DebouncedMenuItemClickListener(
        private val threshold: Long,
        private val listener: () -> Unit
): MenuItem.OnMenuItemClickListener {
    private var lastClickTime: Long = 0

    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        if(SystemClock.elapsedRealtime() - lastClickTime < threshold) {
            //consume click and prevent others from executing
            return true
        }
        lastClickTime = SystemClock.elapsedRealtime()
        listener.invoke()
        return false
    }
}

fun MenuItem.onDebouncedClick(debounceInterval: Long = DEBOUNCE_INTERVAL, listener: () -> Unit) {
    setOnMenuItemClickListener(DebouncedMenuItemClickListener(debounceInterval, listener))
}
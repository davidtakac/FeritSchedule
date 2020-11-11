package os.dtakac.feritraspored.views.timepicker

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import org.koin.android.ext.android.inject
import os.dtakac.feritraspored.R
import os.dtakac.feritraspored.common.preferences.PreferenceRepository

class TimePickerDialogFragment: DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private val prefs: PreferenceRepository by inject()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = TimePickerDialog(
                requireContext(),
                this,
                prefs.timeHour,
                prefs.timeMinute,
                true
        )
        dialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, getString(R.string.label_back), dialog)
        dialog.setButton(TimePickerDialog.BUTTON_POSITIVE, getString(R.string.label_save), dialog)
        return dialog
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        prefs.timeHour = hour
        prefs.timeMinute = minute
    }
}
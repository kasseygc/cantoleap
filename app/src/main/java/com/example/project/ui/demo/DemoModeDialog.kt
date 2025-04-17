package com.example.project.ui.demo

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.project.R
import com.example.project.ui.utils.DemoManager


class DemoModeDialog : DialogFragment() {

    private lateinit var demoManager: DemoManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        demoManager = DemoManager(requireContext())

        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_demo_mode, null)

        val resetProgressButton: Button = view.findViewById(R.id.button_reset_progress)
        val switchUserGroup: RadioGroup = view.findViewById(R.id.radio_group_user_type)
        val switchUserButton: Button = view.findViewById(R.id.button_switch_user)
        val resetCommunityButton: Button = view.findViewById(R.id.button_reset_community)

        resetProgressButton.setOnClickListener {
            demoManager.resetUserProgress()
            Toast.makeText(context, getString(R.string.progress_reset), Toast.LENGTH_SHORT).show()
        }

        switchUserButton.setOnClickListener {
            val selectedId = switchUserGroup.checkedRadioButtonId

            if (selectedId != -1) {
                val radioButton = view.findViewById<RadioButton>(selectedId)
                val userType = when (radioButton.text.toString()) {
                    getString(R.string.user_type_beginner) -> "beginner"
                    getString(R.string.user_type_intermediate) -> "intermediate"
                    getString(R.string.user_type_advanced) -> "advanced"
                    else -> "beginner"
                }

                demoManager.switchDemoUser(userType) { success ->
                    if (success) {
                        Toast.makeText(context,
                            getString(R.string.switched_user, userType),
                            Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        Toast.makeText(context,
                            getString(R.string.failed_to_switch),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context,
                    getString(R.string.select_user_type),
                    Toast.LENGTH_SHORT).show()
            }
        }

        resetCommunityButton.setOnClickListener {
            demoManager.resetCommunityContent()
            Toast.makeText(context,
                getString(R.string.community_reset),
                Toast.LENGTH_SHORT).show()
        }

        builder.setView(view)
            .setTitle(getString(R.string.demo_mode))
            .setNegativeButton(getString(R.string.close)) { _, _ ->
                dismiss()
            }

        return builder.create()
    }
}
package com.example.user.chatapp

import android.os.Bundle
import android.text.Editable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_change_user_name.*

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ChangeUserNameFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class ChangeUserNameFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_user_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val userId = arguments!!.getString("user_id")
        val userName = arguments!!.getString("user_name")

        et_change_user_name.setText(userName)

        btn_change_user_name_cancel.setOnClickListener { dismiss() }
        btn_change_user_name_save.setOnClickListener { save(userId!!) }
    }

    private fun save(uId : String) {
        val uReference = FirebaseDatabase.getInstance().getReference("Users").child(uId)
        uReference.child("name").setValue(et_change_user_name.text.toString()).addOnSuccessListener {
            dismiss()
            Toast.makeText(context, "Name changed to\n ${et_change_user_name.text.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(): ChangeUserNameFragment {
            return ChangeUserNameFragment()
        }
    }
}
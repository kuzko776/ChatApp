package com.example.user.chatapp

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.user.chatapp.assist.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*


class ProfileFragment : DialogFragment() {

    private val imageRequest = 1
    private var imageUri: Uri? = null
    private var storageReference: StorageReference? = null
    private lateinit var user : User

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val uId = arguments!!.getString("userId")!!
        storageReference = FirebaseStorage.getInstance().getReference("uploads")

        if (uId == FirebaseAuth.getInstance().currentUser!!.uid) {
            fab_profile_change_image.visibility = View.VISIBLE
            profile_change_name.visibility = View.VISIBLE

            fab_profile_change_image.setOnClickListener { pickImage() }
            profile_change_name.setOnClickListener { changeName() }
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users").child(uId)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                user = dataSnapshot.getValue(User::class.java)!!

                profile_username.text = user.name

                if (user.imageURL == "default")
                    profile_image.setImageResource(R.drawable.ic_default_user_img)
                else
                    Glide.with(requireActivity()).load(user.imageURL).into(profile_image)
            }
        })
    }



    private fun changeName() {
        val fragmentManager = requireFragmentManager()
        val changeUserNameFragment = ChangeUserNameFragment.newInstance()
        val bundle = Bundle()
        bundle.putString("user_id", user.id)
        bundle.putString("user_name", user.name)
        changeUserNameFragment.arguments = bundle
        changeUserNameFragment.show(fragmentManager, "change_user_name_manager")
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, imageRequest)
    }

    private fun performCrop(picUri: Uri?) {
        CropImage.activity(picUri).setFixAspectRatio(true)
            .start(requireContext(), this)
    }

    private fun uploadImage(uri: Uri) {
        Toast.makeText(context, "Uploading", Toast.LENGTH_SHORT).show()

        val path = "images/${UUID.randomUUID()}"
        val ref = storageReference!!.child(path)
        val uploadTask = ref.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uReference = FirebaseDatabase.getInstance().getReference("Users").child(user.id)
                uReference.child("imageURL").setValue(task.result.toString())
                //val downloadUri = task.result
            } else {
                Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imageRequest && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            performCrop(imageUri)
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ){
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                uploadImage(result.uri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(context, "Crop Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
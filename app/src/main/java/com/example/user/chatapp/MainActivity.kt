package com.example.user.chatapp

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.user.chatapp.assist.User
import com.example.user.chatapp.assist.UserAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_profile.*


class MainActivity : AppCompatActivity() {

    private var users = ArrayList<User>()
    private var contactNumbers = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppThemeNoActionBar)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_activity_toolbar_root)
        supportActionBar!!.setDisplayShowTitleEnabled(false)


        if ( FirebaseAuth.getInstance().currentUser == null ){
            startActivity(Intent(this,LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            overridePendingTransition(0, 0)
        }else{
            //init current user data
            val uId = FirebaseAuth.getInstance().currentUser!!.uid
            val cUserReference = FirebaseDatabase.getInstance().getReference("Users").child(uId)
            cUserReference.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)
                    if (user!!.imageURL == "default")
                        main_activity_toolbar_image.setImageResource(R.drawable.ic_default_user_img)
                    else
                        Glide.with(applicationContext).load(user.imageURL).into(main_activity_toolbar_image)
                }

            })
            FirebaseMessaging.getInstance().subscribeToTopic(uId)
            readUsers()
        }
        main_activity_refresh_btn.setOnClickListener { readUsers() }


        main_activity_toolbar_image.setOnClickListener {openProfile()}
    }

    private fun openProfile() {
        val fragmentManager = supportFragmentManager
        val profileFragment = ProfileFragment.newInstance()
        val bundle = Bundle()
        bundle.putString("userId",FirebaseAuth.getInstance().currentUser!!.uid)
        profileFragment.arguments = bundle
        profileFragment.show(fragmentManager,"profile_manager")
    }


    private fun readUsers() {
        val cUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.keepSynced(true)


        if (requestContactPermission())
            reference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    getContactNumbers()
                    users.clear()

                    for (i in p0.children) {
                        val user = i.getValue(User::class.java)

                        if (user!!.id != cUser!!.uid && user.phone in contactNumbers)
                            users.add(user)

                        main_activity_refresh_btn.visibility = View.GONE
                    }
                    main_recycler_view.adapter = UserAdapter(users, this@MainActivity)
                }
            })
        else
            main_activity_refresh_btn.visibility = View.VISIBLE
    }

    private fun getContactNumbers() {
        val phones: Cursor? = contentResolver.query(
            Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (phones!!.moveToNext()) {
            val phoneNumber: String = phones.getString(phones.getColumnIndex(Phone.NUMBER))
            contactNumbers.add(phoneNumber.replace(" ",""))
        }
        phones.close()
    }

    private fun requestContactPermission() : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.READ_CONTACTS
                    )
                ) {
                    val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
                    builder.setTitle("Read Contacts permission")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setMessage("Please enable access to contacts. this app depend on your contacts to show your friends.")
                    builder.setOnDismissListener {
                        requestPermissions(
                            arrayOf(android.Manifest.permission.READ_CONTACTS),
                            1
                        )
                    }
                    builder.show()
                } else {
                    requestPermissions(
                        this, arrayOf(android.Manifest.permission.READ_CONTACTS),
                        1
                    )
                }
            } else {
                return true
            }
        } else {
            return true
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }
}
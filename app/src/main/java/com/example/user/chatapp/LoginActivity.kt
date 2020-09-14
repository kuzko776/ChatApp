package com.example.user.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fab_send_sms_code.setOnClickListener {
            val phoneNo = "+${login_country_code.text}${login_phone_number.text}"
            val intent = Intent(this, VerifyActivity::class.java)
            intent.putExtra("phone_number", phoneNo)
            intent.putExtra("user_name", login_user_name.text.toString())
            println(phoneNo)
            startActivity(intent)
        }
    }
}
package com.example.user.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_verify.*
import java.util.concurrent.TimeUnit


class VerifyActivity : AppCompatActivity() {

    private lateinit var verificationCodeBySystem: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        sendNewCode()
    }

    private fun sendNewCode() {

        val phoneNo = intent.getStringExtra("phone_number")!!
        tv_enter_code_message.text = "Enter verify code sent to \n$phoneNo"

        sendVerificationCodeToUser(phoneNo)



        fab_verify_code.text = "Verify"
        fab_verify_code.setOnClickListener {
            val code = et_verify_code.text
            if (code.isEmpty()) {
                et_verify_code.error = "Code cannot be empty"
                et_verify_code.requestFocus()
            }
            else
                verifyCode(code.toString())
        }

    }

    private fun sendVerificationCodeToUser(phoneNo: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNo,  // Phone number to verify
            10,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            TaskExecutors.MAIN_THREAD,  // Activity (for callback binding)
            mCallbacks
        )
    }

    private val mCallbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(code: PhoneAuthCredential?) {
                code?.smsCode?.let { verifyCode(it) }
            }

            override fun onVerificationFailed(exception: FirebaseException?) {
                Toast.makeText(this@VerifyActivity, "Failed, make sure you're connected to the internet", Toast.LENGTH_LONG).show()
                fab_verify_code.text = "Resend Code"
                fab_verify_code.setOnClickListener {
                    sendNewCode()
                }
            }

            override fun onCodeSent(p0: String?, p1: ForceResendingToken?) {
                super.onCodeSent(p0, p1)
                if (p0 != null) {
                    verificationCodeBySystem = p0

                    Toast.makeText(this@VerifyActivity, "Code Sent", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String?) {
                super.onCodeAutoRetrievalTimeOut(p0)
                fab_verify_code.text = "Resend Code"
                fab_verify_code.setOnClickListener {
                    sendNewCode()
                }
            }
        }

    private fun verifyCode(codeByUser: String) {
        verifing_code_progressbar.visibility = View.VISIBLE
        val credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser)
        signInTheUserByCredentials(credential)
    }

    private fun signInTheUserByCredentials(credential: PhoneAuthCredential) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this@VerifyActivity) { task ->
                if (task.isSuccessful) {

                    val userId =  firebaseAuth.currentUser!!.uid
                    val userName = intent.getStringExtra("user_name")?:""
                    val phone = intent.getStringExtra("phone_number")?:""
                    val reference = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

                    val hashMap = HashMap<String,String>()
                    hashMap["id"] = userId
                    hashMap["name"] = userName
                    hashMap["phone"] = phone
                    hashMap["imageURL"] = "default"

                    reference.setValue(hashMap).addOnCompleteListener {
                        if (task.isSuccessful) {
                            Toast.makeText(this@VerifyActivity, "Your Account has been created successfully!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(applicationContext, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
                verifing_code_progressbar.visibility = View.GONE
            }.addOnFailureListener {
                val errorMessage :String = if (it.message!!.contains("The sms verification code used to create the phone auth credential is invalid."))
                    "Wrong Code"
                else
                    "Error"
                et_verify_code.error = errorMessage
                Toast.makeText(this@VerifyActivity, errorMessage, Toast.LENGTH_SHORT).show()
                verifing_code_progressbar.visibility = View.GONE
            }
    }
}

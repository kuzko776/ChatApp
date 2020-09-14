package com.example.user.chatapp

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.user.chatapp.assist.ChatAdapter
import com.example.user.chatapp.assist.Message
import com.example.user.chatapp.assist.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.android.synthetic.main.activity_chat.*


class ChatActivity : AppCompatActivity() {

    private var oUser : String = ""
    private var cUser : String = ""
    private var cUserName : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initBar()
        readMessages()

        if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL)
            chat_fab_send.rotation = 180f

        chat_fab_send.setOnClickListener {
            sendMessage(chat_et_new_message.text.toString())
            chat_et_new_message.text.clear()
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users").child(cUser)
        reference.addListenerForSingleValueEvent( object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                cUserName = user!!.name
            }
        })
    }
    private fun initBar(){
        cUser = FirebaseAuth.getInstance().currentUser!!.uid
        oUser = intent.getStringExtra("user_id")

        val reference = FirebaseDatabase.getInstance().getReference("Users").child(oUser)
        reference.addValueEventListener( object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                title = user!!.name
            }
        })
    }

    private fun sendMessage(message:String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats").child(getChatId())

        val hashMap = HashMap<String,String>()
        hashMap["sender"] = cUser
        hashMap["receiver"] = oUser
        hashMap["message"] = message
        hashMap["status"] = "0"
        hashMap["date"] = System.currentTimeMillis().toString()
        
        reference.push().setValue(hashMap)
    }



    private fun getChatId(): String {
        return if (cUser < oUser)
            cUser + oUser
        else
            oUser + cUser
    }

    private fun readMessages(){

        val reference = FirebaseDatabase.getInstance().reference.child("Chats").child(getChatId())
        reference.keepSynced(true)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val messages = ArrayList<Message>()
                for (snapshot in dataSnapshot.children) {
                    val chat: Message = snapshot.getValue<Message>(Message::class.java)!!
                    messages.add(chat)
                }
                chat_activity_recycler.adapter = ChatAdapter(messages)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onPause() {
        super.onPause()

        currentUser("none")
    }

    override fun onResume() {
        super.onResume()
        currentUser(oUser)
    }

    private fun currentUser(cUser: String) {
        val editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
        editor.putString("current_user", cUser).apply()
    }
}
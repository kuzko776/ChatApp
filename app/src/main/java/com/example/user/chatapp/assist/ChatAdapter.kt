package com.example.user.chatapp.assist

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.user.chatapp.R
import com.example.user.chatapp.R.layout
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.chat_item_left.view.chat_item_msg
import kotlinx.android.synthetic.main.chat_item_right.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatAdapter(private val messages: ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val msgTypeLeft = 0
    private val msgTypeRight = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == msgTypeRight)
            RightViewHolder(parent.inflate(layout.chat_item_right, false))

        else LeftViewHolder(parent.inflate(layout.chat_item_left, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat: Message = messages[position]

        if (getItemViewType(position) == msgTypeRight){
            holder as RightViewHolder

            holder.msgView.text = chat.message
            holder.status.setImageResource(
                if (chat.status == "0")
                    R.drawable.ic_message_not_sent
                else
                    R.drawable.ic_message_sent
            )
            holder.time.text = formatTime(chat.date)
        }
        else{
            holder as LeftViewHolder
            holder.msgView.text = chat.message
        }
    }

    private fun formatTime(date: String): CharSequence? {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = date.toLong()
        val format = SimpleDateFormat("h:mm a")
        return format.format(calendar.time)
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        val fUser = FirebaseAuth.getInstance().currentUser
        return if (messages[position].sender == fUser!!.uid) {
            msgTypeRight
        } else {
            msgTypeLeft
        }
    }

    class RightViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val msgView : TextView = view.chat_item_msg
        val status : ImageView = view.chat_item_status
        val time : TextView = view.chat_item_time
    }

    class LeftViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val  msgView : TextView = view.findViewById(R.id.chat_item_msg)
    }
}

package com.example.user.chatapp.assist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.user.chatapp.ChatActivity
import com.example.user.chatapp.MainActivity
import com.example.user.chatapp.ProfileFragment
import com.example.user.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.user_list_item.view.*


class UserAdapter(private val users: ArrayList<User>, var context: Context) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflatedView = parent.inflate(R.layout.user_list_item, false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.userName.text = user.name

        if (user.imageURL == "default")
            holder.userImage.setImageResource(R.drawable.ic_default_user_img)
        else
            Glide.with(context).load(user.imageURL).into(holder.userImage)

    }

    override fun getItemCount()=users.size

    inner class ViewHolder (view : View) : RecyclerView.ViewHolder(view){
        var userName: TextView = view.user_list_item_name
        val userImage : ImageView = view.user_list_item_image

        init {
            view.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("user_id", users[adapterPosition].id)
                context.startActivity(intent)
            }

            userImage.setOnClickListener {
                val fragmentManager = (context as MainActivity).supportFragmentManager
                val profileFragment = ProfileFragment.newInstance()
                val bundle = Bundle()
                bundle.putString("userId", users[adapterPosition].id)
                profileFragment.arguments = bundle
                profileFragment.show(fragmentManager, "profile_manager")
            }
        }
    }

}
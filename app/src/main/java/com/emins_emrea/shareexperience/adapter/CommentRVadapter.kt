package com.emins_emrea.shareexperience.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.emins_emrea.shareexperience.R
import com.emins_emrea.shareexperience.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentRVadapter(
    private val myContext: Context?,
    private val commentList: ArrayList<Comment>
) :
    RecyclerView.Adapter<CommentRVadapter.CardViewObjectHolder>() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    init {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    class CardViewObjectHolder : RecyclerView.ViewHolder {
        var card_comment: CardView? = null
        var textViewUsermail: TextView? = null
        var textViewComment: TextView? = null


        constructor(itemView: View) : super(itemView) {
            card_comment = itemView.findViewById(R.id.card_comment)
            textViewUsermail = itemView.findViewById(R.id.textViewUsermail)
            textViewComment = itemView.findViewById(R.id.textViewComment)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewObjectHolder {
        val itemView =
            LayoutInflater.from(myContext).inflate(R.layout.comment_card, parent, false)

        return CardViewObjectHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewObjectHolder, position: Int) {
        holder.textViewUsermail?.text = commentList.get(position).userEmail
        holder.textViewComment?.text = commentList.get(position).comment

    }

    override fun getItemCount(): Int {
        return commentList.size
    }


}
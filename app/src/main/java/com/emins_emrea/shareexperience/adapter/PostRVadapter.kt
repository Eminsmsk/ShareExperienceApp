package com.emins_emrea.shareexperience.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emins_emrea.shareexperience.R
import com.emins_emrea.shareexperience.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.experience_card.view.*

class PostRVadapter(
    private val myContext: Context?,
    private val postList: ArrayList<Post>,
    private val fragment: Fragment
) :
    RecyclerView.Adapter<PostRVadapter.CardViewObjectHolder>() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    init {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    class CardViewObjectHolder : RecyclerView.ViewHolder {
        var card_experience: CardView? = null
        var textViewExperienceTitle: TextView? = null
        var textViewExperienceCategories: TextView? = null
        var textViewExperienceDetails: TextView? = null
        var textViewUsername: TextView? = null
        var imageViewExperience: ImageView? = null
        var buttonLike: Button? = null
        var buttonRemove: Button? = null

        constructor(itemView: View) : super(itemView) {
            card_experience = itemView.findViewById(R.id.card_comment)
            textViewExperienceTitle = itemView.findViewById(R.id.textViewExperienceTitle)
            textViewExperienceCategories = itemView.findViewById(R.id.textViewCategories)
            textViewExperienceDetails = itemView.findViewById(R.id.textViewExperienceDetails)
            textViewUsername = itemView.findViewById(R.id.textViewUsername)
            imageViewExperience = itemView.findViewById(R.id.imageViewExperience)
            buttonLike = itemView.findViewById(R.id.buttonLike)
            buttonRemove = itemView.findViewById(R.id.buttonRemove)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewObjectHolder {
        val itemView =
            LayoutInflater.from(myContext).inflate(R.layout.experience_card, parent, false)
        if (!(fragment is MyPageFragment))
            itemView.buttonRemove.visibility = View.INVISIBLE
        return CardViewObjectHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewObjectHolder, position: Int) {
        holder.textViewExperienceTitle?.text = postList.get(position).title
        val categories = postList.get(position).categories
        var str = "#"
        for (i in categories) {
            str += i + " #"
        }

        str = str.subSequence(0, str.length - 1).toString()
        holder.textViewExperienceCategories?.text = str
        if (postList.get(position).experienceDetail.length > 120)
            holder.textViewExperienceDetails?.text =
                postList.get(position).experienceDetail.subSequence(0, 120).toString() + "..."
        else
            holder.textViewExperienceDetails?.text = postList.get(position).experienceDetail
        holder.textViewUsername?.text = postList.get(position).userEmail


        Glide.with(myContext!!).load(postList.get(position).imageUrl)
            .into(holder.imageViewExperience!!)
        if(postList.get(position).likes.contains(auth.currentUser!!.email))
            holder.buttonLike!!.setBackgroundResource(R.drawable.ic_baseline_favorite_24)
        else
            holder.buttonLike!!.setBackgroundResource(R.drawable.ic_baseline_favorite_border_24)

        holder.card_experience?.setOnClickListener {
            val intent: Intent = Intent(myContext, PostDetailsActivity::class.java)
            intent.putExtra("chosen", postList.get(position))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            myContext.startActivity(intent)
        }

        holder.buttonLike!!.text = postList.get(position).likes.size.toString()

        holder.buttonLike!!.setOnClickListener {
            if (postList.isNotEmpty()) {
                var docID: String = ""
                val postsRef = db.collection("ExperiencePost")
                    .whereEqualTo("experienceTitle", postList.get(position).title.toString())
                    .whereEqualTo("usermail", postList.get(position).userEmail)
                postsRef.get().addOnSuccessListener { documents ->
                    for (v in documents) {
                        docID = v.id
                    }
                    if (!postList.get(position).likes.contains(auth.currentUser?.email)) {
                        // Atomically add a new region to the "regions" array field.
                        val postRef = db.collection("ExperiencePost").document(docID)
                        postRef.update(
                            "likes",
                            FieldValue.arrayUnion(auth.currentUser?.email)
                        ).addOnSuccessListener {
                            postRef.update("likesCount", postList.get(position).likes.size + 1)
                                .addOnSuccessListener {
                                    holder.buttonLike!!.text =
                                        (postList.get(position).likes.size + 1).toString()
                                    refreshData()
                                }

                        }


                    } else {
                        // Atomically remove a region from the "regions" array field.
                        val postRef = db.collection("ExperiencePost").document(docID)
                        postRef.update(
                            "likes",
                            FieldValue.arrayRemove(auth.currentUser?.email)
                        ).addOnSuccessListener {
                            postRef.update("likesCount", postList.get(position).likes.size - 1)
                                .addOnSuccessListener {
                                    holder.buttonLike!!.text =
                                        (postList.get(position).likes.size - 1).toString()
                                    refreshData()
                                }

                        }

                    }

                }


            }

        }
        holder.buttonRemove?.setOnClickListener {
            if (postList.isNotEmpty()) {
                var docID: String = ""
                val postsRef = db.collection("ExperiencePost")
                    .whereEqualTo("experienceTitle", postList.get(position).title.toString())
                    .whereEqualTo("usermail", postList.get(position).userEmail)
                postsRef.get().addOnSuccessListener { documents ->
                    for (v in documents) {
                        docID = v.id
                    }
                    db.collection("ExperiencePost").document(docID)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(myContext, "Post is deleted", Toast.LENGTH_SHORT).show()
                            refreshData()
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()

                        }

                }
            }

        }
    }

    override fun getItemCount(): Int {

        return postList.size
    }

    private fun refreshData() {


        if (postList.isNotEmpty())
            postList.clear()
        //when likes count is updated
        if (fragment is PopularPageFragment) {

            val postRefPopular =
                db.collection("ExperiencePost").orderBy("likesCount", Query.Direction.DESCENDING)
            postRefPopular.get().addOnSuccessListener { value ->
                for (v in value) {
                    postList.add(
                        Post(
                            v.get("usermail").toString(),
                            v.get("experienceTitle").toString(),
                            v.get("experienceDetails").toString(),
                            v.get("imageurl").toString(),
                            v.get("categories") as ArrayList<String>,
                            v.get("likes") as ArrayList<String>, v.get("likesCount") as Long
                        )
                    )
                }
                notifyDataSetChanged()
            }
        } else if (fragment is MainPageFragment) {
            var postRef = db.collection("ExperiencePost").orderBy("date", Query.Direction.DESCENDING)
            postRef.get().addOnSuccessListener { value ->
                for (v in value) {
                    postList.add(
                        Post(
                            v.get("usermail").toString(),
                            v.get("experienceTitle").toString(),
                            v.get("experienceDetails").toString(),
                            v.get("imageurl").toString(),
                            v.get("categories") as ArrayList<String>,
                            v.get("likes") as ArrayList<String>, v.get("likesCount") as Long
                        )
                    )
                }
                notifyDataSetChanged()
            }


        }
        //when item is removed
        else {
            var postRef =
                db.collection("ExperiencePost").whereEqualTo("usermail", auth.currentUser?.email)
            postRef.get().addOnSuccessListener { value ->
                for (v in value) {
                    postList.add(
                        Post(
                            v.get("usermail").toString(),
                            v.get("experienceTitle").toString(),
                            v.get("experienceDetails").toString(),
                            v.get("imageurl").toString(),
                            v.get("categories") as ArrayList<String>,
                            v.get("likes") as ArrayList<String>, v.get("likesCount") as Long
                        )
                    )
                }
                notifyDataSetChanged()
            }
        }
    }

}
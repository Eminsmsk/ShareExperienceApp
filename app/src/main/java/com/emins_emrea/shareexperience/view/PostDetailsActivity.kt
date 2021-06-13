package com.emins_emrea.shareexperience.view

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.emins_emrea.shareexperience.R
import com.emins_emrea.shareexperience.model.Comment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_post_details.*


class PostDetailsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var post: Post
    private lateinit var commentList: ArrayList<Comment>
    private var flag = true
    private lateinit var recyclerAdapter: CommentRVadapter
    private lateinit var commentsRV: RecyclerView
    private var docID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)
        val toolbar: Toolbar = findViewById(R.id.toolbarDetails)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        commentList = ArrayList<Comment>()



        post = intent.getSerializableExtra("chosen") as Post
        textViewDetailsAExperienceTitle.text = post.title

        val categories = post.categories
        var str = "#"
        for (i in categories) {
            str += i + " #"
        }



        str = str.subSequence(0, str.length - 1).toString()
        textViewDetailsAExperienceCategories.text = str

        textViewDetailsAExperienceDetails.text = post.experienceDetail
        textViewDetailsAExperienceDetails.setMovementMethod(ScrollingMovementMethod())
        textViewDetailsAUsername.text = post.userEmail
        Glide.with(applicationContext).load(post.imageUrl)
            .into(imageViewDetailsAExperience)


        val postsRef = db.collection("ExperiencePost")
            .whereEqualTo("experienceTitle", post.title)
            .whereEqualTo("usermail", post.userEmail)
        postsRef.get().addOnSuccessListener { documents ->
            for (v in documents) {
                docID = v.id
            }
            loadComments()
        }


        recyclerAdapter = CommentRVadapter(this, commentList)
        commentsRV = findViewById(R.id.commentsRV)
        commentsRV.layoutManager = LinearLayoutManager(this)
        commentsRV.adapter = recyclerAdapter

        val swipeRefresh: SwipeRefreshLayout = findViewById(R.id.swipeRefreshDetails)
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.setRefreshing(false)
            loadComments()
        }
        swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )


    }

    fun addComment(view: View) {
        val commentText = editTextAddComment.text.toString()
        if (commentText.isNullOrBlank()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
        } else {

            val commentHashMap = hashMapOf<String, Any>()
            commentHashMap.put("usermail", auth.currentUser!!.email.toString())
            commentHashMap.put("comment", commentText)
            val date = Timestamp.now()
            commentHashMap.put("date", date)
            db.collection("ExperiencePost").document(docID).collection("Comments")
                .add(commentHashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Comment is added", Toast.LENGTH_SHORT).show()
                        editTextAddComment.text.clear()
                        loadComments()
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }

        }
    }


    private fun loadComments() {
        if (!commentList.isEmpty()) {
            commentList.clear()
            recyclerAdapter.notifyDataSetChanged()
        }

        db.collection("ExperiencePost").document(docID).collection("Comments")
            .orderBy("date", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { value ->
                if (value != null) {
                    for (v in value.documents) {
                        commentList.add(
                            Comment(
                                v.get("usermail").toString(), v.get("comment").toString()
                            )
                        )
                    }
                    if (flag) {
                        recyclerAdapter = CommentRVadapter(this, commentList)
                        commentsRV.adapter = recyclerAdapter
                        flag = false
                    } else {
                        recyclerAdapter.notifyDataSetChanged()
                    }

                }


            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
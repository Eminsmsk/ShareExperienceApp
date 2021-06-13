package com.emins_emrea.shareexperience.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.emins_emrea.shareexperience.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main_page.*


class MyPageFragment(private val filter: HashMap<String, String>) : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var experiencePosts: ArrayList<Post>
    private var flag = true
    private lateinit var recyclerAdapter: PostRVadapter
    private lateinit var myPageRV: RecyclerView
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_page, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        experiencePosts = ArrayList<Post>()
        auth = FirebaseAuth.getInstance()
        recyclerAdapter = PostRVadapter(activity?.applicationContext, experiencePosts, this)
        myPageRV = view.findViewById(R.id.myPageRV)
        myPageRV.layoutManager = LinearLayoutManager(context)
        myPageRV.adapter = recyclerAdapter
        getPosts()

        val swipeRefresh: SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshMyPage)
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.setRefreshing(false)
            getPosts()
        }
        swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )


    }

    private fun getPosts() {
        if (!experiencePosts.isEmpty()) {
            experiencePosts.clear()
            recyclerAdapter.notifyDataSetChanged()
        }

        if (filter.get("categoryFilter").isNullOrEmpty()) {
            db.collection("ExperiencePost").whereEqualTo(
                "usermail",
                auth.currentUser?.email
            ).orderBy(
                "date",
                Query.Direction.DESCENDING
            ).get().addOnSuccessListener { value ->
                if (value != null) {
                    for (v in value.documents) {
                        experiencePosts.add(
                            Post(
                                v.get("usermail").toString(),
                                v.get("experienceTitle").toString(),
                                v.get("experienceDetails").toString(),
                                v.get("imageurl").toString(),
                                v.get("categories") as ArrayList<String>,
                                v.get("likes") as ArrayList<String>,
                                v.get("likesCount") as Long
                            )
                        )
                    }
                    var tempList: ArrayList<Post> = ArrayList<Post>()
                    tempList.addAll(experiencePosts)
                    if (!filter.get("titleFilter").isNullOrEmpty()) {
                        for (v in experiencePosts) {
                            if (!v.title.toString().toLowerCase()
                                    .contains(filter.get("titleFilter").toString().toLowerCase())
                            ) {
                                tempList.remove(v)
                            }
                        }
                        experiencePosts.clear()
                        experiencePosts.addAll(tempList)
                    }

                    if (flag) {
                        recyclerAdapter =
                            PostRVadapter(activity?.applicationContext, experiencePosts, this)
                        myPageRV.adapter = recyclerAdapter
                        flag = false
                    } else {
                        //recyclerAdapter.notifyItemRangeChanged(0,wordList.size)
                        recyclerAdapter.notifyDataSetChanged()

                    }
                }

            }.addOnFailureListener { exception ->
                println(exception.stackTrace)
            }
        } else {

            var categoryList: List<String>? = null
            if (!(filter.get("categoryFilter").isNullOrEmpty())) {
                categoryList = filter.getValue("categoryFilter").trim().split(" ")
                val postsRef = db.collection("ExperiencePost")
                    .whereEqualTo("usermail", auth.currentUser?.email)
                    .whereArrayContainsAny("categories", categoryList).orderBy(
                        "date",
                        Query.Direction.DESCENDING
                    )
                postsRef.get()
                    .addOnSuccessListener { documents ->
                        for (v in documents) {
                            experiencePosts.add(
                                Post(
                                    v.get("usermail").toString(),
                                    v.get("experienceTitle").toString(),
                                    v.get("experienceDetails").toString(),
                                    v.get("imageurl").toString(),
                                    v.get("categories") as ArrayList<String>,
                                    v.get("likes") as ArrayList<String>,
                                    v.get("likesCount") as Long
                                )
                            )
                        }
                        if (flag) {
                            recyclerAdapter =
                                PostRVadapter(activity?.applicationContext, experiencePosts, this)
                            myPageRV.adapter = recyclerAdapter
                            flag = false
                        } else {
                            //recyclerAdapter.notifyItemRangeChanged(0,wordList.size)
                            recyclerAdapter.notifyDataSetChanged()

                        }

                    }

                    .addOnFailureListener { exception ->
                        println(exception.stackTrace)
                    }
            }
        }
    }
}

package com.emins_emrea.shareexperience.view

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.CheckBox
import android.widget.Scroller
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.emins_emrea.shareexperience.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.royrodriguez.transitionbutton.TransitionButton
import kotlinx.android.synthetic.main.activity_share.*
import java.util.*

class ShareActivity : AppCompatActivity() {

    var selected_image: Uri? = null
    var image_bitmap: Bitmap? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private val defaultImage: String =
        "https://firebasestorage.googleapis.com/v0/b/shareexperienceapp.appspot.com/o/images%2Fbef2ac3a-8f3f-4825-8a1d-684842e665f3.jpg?alt=media&token=7e0f1a00-bf10-4de8-a540-de60c1b50f3e"
    private lateinit var btn_share: TransitionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        editTextShareInput.setScroller(Scroller(applicationContext))
        editTextShareInput.isVerticalScrollBarEnabled = true
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        btn_share = findViewById(R.id.btn_share)

    }

    fun shareExperience(view: View) {

        val uuid = UUID.randomUUID()
        val imageName = "${uuid}.jpg"

        val reference = storage.reference

        val imageReference = reference.child("images").child(imageName)



        if (et_experience_title.text.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "Add a title", Toast.LENGTH_LONG).show()
        } else {
            btn_share.startAnimation()
            if (selected_image != null) {
                imageReference.putFile(selected_image!!).addOnSuccessListener { taskSnapshot ->
                    val loadedImageReference =
                        FirebaseStorage.getInstance().reference.child("images").child(imageName)
                    loadedImageReference.downloadUrl.addOnSuccessListener { uri ->

                        loadOnFirebase(uri.toString())

                    }.addOnFailureListener { exception ->
                        btn_share.stopAnimation(
                            TransitionButton.StopAnimationStyle.SHAKE,
                            null
                        )
                        Toast.makeText(
                            applicationContext,
                            exception.localizedMessage,
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }

            } else {
                loadOnFirebase(defaultImage)
            }
        }
    }

    fun selectImage(view: View) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                123
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 456)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 123) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 456)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 456 && resultCode == RESULT_OK && data != null) {

            selected_image = data.data

            if (selected_image != null) {

                if (Build.VERSION.SDK_INT >= 28) {

                    val source = ImageDecoder.createSource(this.contentResolver, selected_image!!)
                    image_bitmap = ImageDecoder.decodeBitmap(source)
                    iv_share.setImageBitmap(image_bitmap)


                } else {
                    image_bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selected_image)
                    iv_share.setImageBitmap(image_bitmap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadOnFirebase(imageLink: String) {
        val downloadUrl = imageLink
        val currentUserMail = auth.currentUser!!.email.toString()
        val experienceTitle = et_experience_title.text.toString()
        val experienceDetails = editTextShareInput.text.toString()
        var categories: String = ""
        if (checkBoxWorking.isChecked)
            categories += "Working "

        if (checkBoxGaming.isChecked)
            categories += "Gaming "

        if (checkBoxTravelling.isChecked)
            categories += "Travelling "

        if (checkBoxCooking.isChecked)
            categories += "Cooking "

        if (categories == "") {
            categories = "Other"
        }
        categories = categories.trim()
        var array = categories.split(" ")
        val date = Timestamp.now()


        val postHashMap = hashMapOf<String, Any>()
        postHashMap.put("imageurl", downloadUrl)
        postHashMap.put("usermail", currentUserMail)
        postHashMap.put("experienceTitle", experienceTitle)
        postHashMap.put("experienceDetails", experienceDetails)
        postHashMap.put("categories", array)
        postHashMap.put("date", date)
        postHashMap.put("likes", ArrayList<String>())
        postHashMap.put("likesCount", 0)

        database.collection("ExperiencePost").add(postHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    btn_share.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND,
                        TransitionButton.OnAnimationStopEndListener {
                            Toast.makeText(this, "Shared", Toast.LENGTH_SHORT).show()
                            finish()
                        })

                }
            }.addOnFailureListener { exception ->
                btn_share.stopAnimation(
                    TransitionButton.StopAnimationStyle.SHAKE,
                    null
                )
                Toast.makeText(
                    applicationContext,
                    exception.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }

    }
}
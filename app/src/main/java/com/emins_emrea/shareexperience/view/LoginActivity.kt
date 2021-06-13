package com.emins_emrea.shareexperience.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emins_emrea.shareexperience.R
import com.google.firebase.auth.FirebaseAuth
import com.royrodriguez.transitionbutton.TransitionButton
import com.royrodriguez.transitionbutton.TransitionButton.OnAnimationStopEndListener
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var transitionButtonLogin: TransitionButton
    private lateinit var transitionButtonRegister: TransitionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        transitionButtonLogin = findViewById(R.id.transitionButtonLogin)
        transitionButtonRegister = findViewById(R.id.transitionButtonRegister)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun login(view: View) {
        val email = et_user_name.text.toString()
        val password = et_password.text.toString()
        if (!(email.isNullOrBlank() or password.isNullOrBlank())) {
            transitionButtonLogin.startAnimation()
            auth.signInWithEmailAndPassword(
                et_user_name.text.toString(),
                et_password.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    transitionButtonLogin.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND,
                        OnAnimationStopEndListener {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            startActivity(intent)
                            Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
                            finish()
                        })
                }
            }.addOnFailureListener {
                transitionButtonLogin.stopAnimation(
                    TransitionButton.StopAnimationStyle.SHAKE,
                    null
                )
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        } else
            Toast.makeText(this, "Fill in fields", Toast.LENGTH_SHORT).show()
    }

    fun register(view: View) {

        val email = et_user_name.text.toString()
        val password = et_password.text.toString()
        if (!(email.isNullOrBlank() or password.isNullOrBlank())) {
            transitionButtonRegister.startAnimation()
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    transitionButtonRegister.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND,
                        OnAnimationStopEndListener {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            startActivity(intent)
                            Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
                            finish()
                        })
                }
            }.addOnFailureListener {
                transitionButtonRegister.stopAnimation(
                    TransitionButton.StopAnimationStyle.SHAKE,
                    null
                )
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        } else
            Toast.makeText(this, "Fill in fields", Toast.LENGTH_SHORT).show()
    }

}
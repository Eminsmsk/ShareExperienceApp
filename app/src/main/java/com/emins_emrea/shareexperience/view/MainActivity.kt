package com.emins_emrea.shareexperience.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.emins_emrea.shareexperience.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.alertdialog_design.*
import kotlinx.android.synthetic.main.alertdialog_design.view.*
import kotlinx.android.synthetic.main.navdrawer_header.view.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragment: Fragment
    private lateinit var auth: FirebaseAuth
    private var mAlertDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        val drawer_layout: DrawerLayout = findViewById(R.id.drawer_layout)
        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            (R.string.openDrawer),
            (R.string.closeDrawer)
        ) {

        }
        drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        drawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()


        nav_view.setNavigationItemSelectedListener(this)

        fragment = MainPageFragment(HashMap())
        supportFragmentManager.beginTransaction().replace(R.id.fragmentHolder, fragment).commit()
        toolbar.subtitle = fragment.javaClass.simpleName.removeSuffix("PageFragment") + " Page"


        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById(R.id.header_username)
        navUsername.text = auth.currentUser?.email


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_page -> fragment = MainPageFragment(HashMap())
            R.id.my_page -> fragment = MyPageFragment(HashMap())
            R.id.popular_page -> fragment = PopularPageFragment(HashMap())
            R.id.exit -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

        supportFragmentManager.beginTransaction().replace(R.id.fragmentHolder, fragment).commit()
        toolbar.subtitle = fragment.javaClass.simpleName.removeSuffix("PageFragment") + " Page"
        drawer_layout.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val mDialogView =
                    LayoutInflater.from(this).inflate(R.layout.alertdialog_design, null)
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Add filter")
                builder.setView(mDialogView)
                builder.setPositiveButton("OK") { dialog, which ->
                    var categories: String = ""
                    if (mDialogView.chip_Working.isChecked)
                        categories += "Working "
                    if (mDialogView.chip_Gaming.isChecked)
                        categories += "Gaming "
                    if (mDialogView.chip_Travelling.isChecked)
                        categories += "Travelling "
                    if (mDialogView.chip_Cooking.isChecked)
                        categories += "Cooking "
                    if (mDialogView.chip_Other.isChecked)
                        categories += "Other"
                    if (fragment is MainPageFragment) {
                        fragment = MainPageFragment(
                            hashMapOf<String, String>(
                                "titleFilter" to mDialogView.editTextFilterTitle.text.toString(),
                                "categoryFilter" to categories
                            )
                        )
                    }
                    if (fragment is MyPageFragment) {
                        fragment = MyPageFragment(
                            hashMapOf<String, String>(
                                "titleFilter" to mDialogView.editTextFilterTitle.text.toString(),
                                "categoryFilter" to categories
                            )
                        )
                    }
                    if (fragment is PopularPageFragment) {
                        fragment = PopularPageFragment(
                            hashMapOf<String, String>(
                                "titleFilter" to mDialogView.editTextFilterTitle.text.toString(),
                                "categoryFilter" to categories
                            )
                        )
                    }
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentHolder, fragment)
                        .commit()
                    toolbar.subtitle =
                        fragment.javaClass.simpleName.removeSuffix("PageFragment") + " Page"


                }
                builder.setNegativeButton("CANCEL") { dialog, which ->
                    Toast.makeText(
                        this,
                        "CANCEL", Toast.LENGTH_SHORT
                    ).show()
                }
                mAlertDialog = builder.create()
                if (mAlertDialog != null)
                    mAlertDialog!!.show()


            }


        }


        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        super.onBackPressed()

    }
}
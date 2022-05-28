package com.example.wechat.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.wechat.R
import com.example.wechat.Util.eventbus_events.CallbackManagerEvent
import com.example.wechat.Util.eventbus_events.ConnectionChangeEvent
import com.example.wechat.Util.eventbus_events.KeyboardEvent
import com.example.wechat.databinding.ActivityMainBinding
import com.facebook.CallbackManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {

    var isActivityRecreated = false
    private lateinit var callbackManager: CallbackManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //register to event bus to receive callbacks
        EventBus.getDefault().register(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        sharedViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        //change title text color of toolbar to white
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(applicationContext, R.color.white))

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        var navController: NavController = navHostFragment.navController

        //setup toolbar with navigation
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.chatFragment, R.id.homeFragment))
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
    }

    // Show snack bar whenever the connection state changes
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onConnectionChangeEvent(event: ConnectionChangeEvent): Unit {
        if (!isActivityRecreated) {//to not show toast on configuration changes
            Snackbar.make(binding.coordinator, event.message, Snackbar.LENGTH_LONG).show()
        }
    }

    //facebook fragment will pass callbackManager to activity to continue FB login
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: CallbackManagerEvent) {
        callbackManager = event.callbackManager
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onKeyboardEvent(event: KeyboardEvent) {
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.toolbar.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
        Process.killProcess(Process.myPid())
    }
}
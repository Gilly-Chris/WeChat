package com.example.wechat.ui.fbLoginFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.wechat.R
import com.example.wechat.Util.AuthUtil
import com.example.wechat.Util.ErrorMessage
import com.example.wechat.Util.eventbus_events.CallbackManagerEvent
import com.example.wechat.databinding.FacebookLoginFragmentBinding
import com.example.wechat.ui.main.MainActivity
import com.example.wechat.ui.main.MainViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginResult
import org.greenrobot.eventbus.EventBus

class FacebookLoginFragment : Fragment() {

    private lateinit var callbackManager: CallbackManager

    private lateinit var binding: FacebookLoginFragmentBinding

    companion object {
        fun newInstance() = FacebookLoginFragment()
    }

    private lateinit var viewModel: FacebookLoginViewModel
    private lateinit var sharedViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.facebook_login_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[FacebookLoginViewModel::class.java]
        sharedViewModel = ViewModelProvider(activity!!)[MainViewModel::class.java]

        // Initialize Facebook callbackManager used in Login button
        callbackManager = CallbackManager.Factory.create()
        //pass callback manager to activity to continue FB login
        EventBus.getDefault().post(CallbackManagerEvent(callbackManager))
        binding.FBloginButton.loginBehavior = LoginBehavior.WEB_ONLY
        binding.FBloginButton.setPermissions("email", "public_profile")
        binding.FBloginButton.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onCancel() {}

            override fun onError(error: FacebookException) {
                ErrorMessage.errorMessage = error.message
            }

            override fun onSuccess(result: LoginResult) {
                viewModel.handleFacebookAccessToken(
                    AuthUtil.firebaseAuthInstance,
                    result.accessToken
                ).observe(this@FacebookLoginFragment, { firebaseUser ->
//                    facebook login was successful
                    viewModel.isUserAlreadyStoredInFirestore(firebaseUser.uid)
                        .observe(this@FacebookLoginFragment, Observer { isUserStoredInFirestore ->
//                            if the user doesn't exist in firestore, store user
                            if(!isUserStoredInFirestore){
                                viewModel.storeFacebookUserInFirebase().observe(
                                    this@FacebookLoginFragment,
                                    Observer { isStoredSuccessfully ->
                                        if (isStoredSuccessfully) {
                                            navigateToHome()
                                        }
                                    }
                                )
                            }else {
                                //fb user already stored in firestore, just navigate to home
                                navigateToHome()
                            }
                        })
                })
            }

        })
    }

    private fun navigateToHome() {
        try {
           startActivity(Intent(requireActivity(), MainActivity::class.java))
        } catch (e: Exception) {
            println("FacebookLoginFragment.navigateToHome:${e.message}")
        }
    }
}
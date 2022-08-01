package com.example.wechat.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.wechat.R
import com.example.wechat.Util.AuthUtil
import com.example.wechat.Util.ErrorMessage
import com.example.wechat.Util.LoadState
import com.example.wechat.Util.eventbus_events.KeyboardEvent
import com.example.wechat.databinding.LoginFragmentBinding
import com.example.wechat.ui.main.MainActivity
import com.example.wechat.ui.signup.SignupActivity
import com.google.android.material.textfield.TextInputEditText
import org.greenrobot.eventbus.EventBus

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginFragmentBinding
    private lateinit var viewModel: LoginViewModel

    companion object {
        fun newInstance() = LoginActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.login_fragment)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        //check if user has previously logged in
        if(AuthUtil.firebaseAuthInstance.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        initializeUi()
    }

    private fun initializeUi(){
        // Navigate to signup fragment
        binding.gotoSignUpFragmentTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        //Report text change to viewModel and Observe if email format is correct
        binding.emailEditText.afterTextChanged { email ->
            viewModel.isEmailFormatCorrect(email).observe(this, { isEmailFormatCorrect ->
                if (!isEmailFormatCorrect) {//email format is not correct
                    binding.email.error = getString(R.string.wrong_email_format)
                } else {
                    binding.email.isErrorEnabled = false
                }
            })
        }

        //password length must be at least 6 characters
        binding.passwordEditText.afterTextChanged {
            if (it.length < 6) {
                binding.password.error = getString(R.string.password_size)
            } else {
                binding.password.isErrorEnabled = false
            }
        }

        //handle login click
        binding.loginButton.setOnClickListener {
            login()
        }

        //hide issue layout on x icon click
        binding.issueLayout.cancelImage.setOnClickListener {
            binding.issueLayout.root.visibility = View.GONE
        }
        //login on keyboard done click when focus is on passwordEditText
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            login()
            true
        }
    }

    private fun login() {
        EventBus.getDefault().post(KeyboardEvent())
        if (binding.email.error != null || binding.password.error != null || binding.email.editText!!.text.isEmpty() || binding.password.editText!!.text.isEmpty()) {
            //name or password doesn't match format
            Toast.makeText(this, "Check email and password then retry.", Toast.LENGTH_LONG)
                .show()
        } else {
            //All fields are correct we can login
            viewModel.login(
                AuthUtil.firebaseAuthInstance,
                binding.email.editText!!.text.toString(),
                binding.password.editText!!.text.toString()
            ).observe(this, { loadState ->
                when(loadState) {
                    LoadState.SUCCESS -> {   //triggered when login with email and password is successful
//                        this@LoginFragment.findNavController()
//                            .navigate(R.id.action_loginFragment_to_homeFragment)
                        Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                        viewModel.doneNavigating()
                    }
                    LoadState.LOADING -> {
                        binding.loadingLayout.visibility = View.VISIBLE
                        binding.issueLayout.root.visibility = View.GONE
                    }
                    LoadState.FAILURE -> {
                        binding.loadingLayout.visibility = View.GONE
                        binding.issueLayout.root.visibility = View.VISIBLE
                        binding.issueLayout.textViewIssue.text = ErrorMessage.errorMessage
                    }
                }
            })

        }
    }

    /**
     * Extension function to simplify setting an afterTextChanged action to EditText components.
     */
    private fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

    }
}
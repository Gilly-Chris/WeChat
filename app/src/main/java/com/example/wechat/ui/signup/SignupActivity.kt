package com.example.wechat.ui.signup

import android.content.Intent
import android.os.Bundle
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
import com.example.wechat.databinding.SignupFragmentBinding
import com.example.wechat.ui.main.MainActivity
import org.greenrobot.eventbus.EventBus
import java.util.regex.Matcher
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: SignupFragmentBinding
    private lateinit var pattern: Pattern

    companion object {
        fun newInstance() = SignupActivity()
    }

    private lateinit var viewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.signup_fragment)
        initializeUI()
    }

    private fun initializeUI(){
        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]
        //regex pattern to check email format
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$"
        pattern = Pattern.compile(emailRegex)

        //handle register click
        binding.registerButton.setOnClickListener {
            signUp()
        }

        //hide issue layout on x icon click
        binding.issueLayout.cancelImage.setOnClickListener {
            binding.issueLayout.root.visibility = View.GONE
        }

        //show proper loading/error ui
        viewModel.loadingState.observe(this, {
            when(it) {
                LoadState.LOADING -> {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.issueLayout.root.visibility = View.GONE
                }
                LoadState.SUCCESS -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issueLayout.root.visibility = View.GONE
                }
                LoadState.FAILURE -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issueLayout.root.visibility = View.VISIBLE
                    binding.issueLayout.textViewIssue.text = ErrorMessage.errorMessage
                }
            }
        })

        //sign up on keyboard done click when focus is on passwordEditText
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            signUp()
            true
        }
    }

    private fun signUp() {
        EventBus.getDefault().post(KeyboardEvent())
        binding.userName.isErrorEnabled = false
        binding.email.isErrorEnabled = false
        binding.password.isErrorEnabled = false

        if (binding.userName.editText!!.text.length < 4) {
            binding.userName.error = "User name should be at least 4 characters"
            return
        }

        //check if email is empty or wrong format
        if (binding.email.editText!!.text.isNotEmpty()) {
            val matcher: Matcher = pattern.matcher(binding.email.editText!!.text)
            if (!matcher.matches()) {
                binding.email.error = "Email format isn't correct."
                return
            }
        } else if (binding.email.editText!!.text.isEmpty()) {
            binding.email.error = "Email field can't be empty."
            return
        }

        if (binding.password.editText!!.text.length < 6) {
            binding.password.error = "Password should be at least 6 characters"
            return
        }

        //email and pass are matching requirements now we can register to firebase auth
        viewModel.registerEmail(
            AuthUtil.firebaseAuthInstance,
            binding.email.editText!!.text.toString(),
            binding.password.editText!!.text.toString(),
            binding.userName.editText!!.text.toString()
        )

        viewModel.navigateToHomeMutableLiveData.observe(this, { navigateToHome ->
            if (navigateToHome != null && navigateToHome) {
//                this@SignupFragment.findNavController()
//                    .navigate(R.id.action_signupFragment_to_homeFragment)
                viewModel.loadingState.value = LoadState.SUCCESS
                Toast.makeText(this, "Sign up successful", Toast.LENGTH_LONG).show()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                viewModel.doneNavigating()
                finish()
            }
        })
    }
}
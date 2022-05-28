package com.example.wechat.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wechat.Util.ErrorMessage
import com.example.wechat.Util.LoadState
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Matcher
import java.util.regex.Pattern

class LoginViewModel : ViewModel() {
    private val loadingState = MutableLiveData<LoadState?>()

    private val emailMatch = MutableLiveData<Boolean>()
    private val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$"

    fun isEmailFormatCorrect(it: String) : LiveData<Boolean> {
        val pattern: Pattern = Pattern.compile(emailRegex)
        val matcher: Matcher = pattern.matcher(it)
        emailMatch.value = matcher.matches()
        return emailMatch
    }

    fun login(auth: FirebaseAuth, email: String, password: String) : MutableLiveData<LoadState?> {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            loadingState.value = LoadState.SUCCESS
        }.addOnFailureListener {
            ErrorMessage.errorMessage = it.message
            loadingState.value = LoadState.FAILURE
        }
        return loadingState
    }

    fun doneNavigating() {
        loadingState.value = null
    }
}
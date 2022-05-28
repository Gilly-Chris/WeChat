package com.example.wechat.ui.signup

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wechat.Util.ErrorMessage
import com.example.wechat.Util.FirestoreUtil
import com.example.wechat.Util.LoadState
import com.google.firebase.auth.FirebaseAuth
import com.example.wechat.data.model.User

class SignupViewModel : ViewModel(){
    val navigateToHomeMutableLiveData = MutableLiveData<Boolean?>()
    val loadingState = MutableLiveData<LoadState>()

    fun registerEmail(
        auth: FirebaseAuth,
        email: String,
        password: String,
        username: String
    ){
        loadingState.value = LoadState.LOADING

        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            storeUserInFirestore(User(it.user?.uid, username, email))
        }.addOnFailureListener {
            ErrorMessage.errorMessage = it.message
            loadingState.value = LoadState.FAILURE
        }
    }

    @SuppressLint("RestrictedApi")
    fun storeUserInFirestore(user: User){
        val db = FirestoreUtil.firestoreInstance
        user.uid?.let { uid ->
            db.collection("users").document(uid).set(user).addOnSuccessListener {
                navigateToHomeMutableLiveData.value = true
            }.addOnFailureListener {
                loadingState.value = LoadState.FAILURE
                ErrorMessage.errorMessage = it.message
            }
        }
    }

    fun doneNavigating() {
        navigateToHomeMutableLiveData.value = false
    }
}
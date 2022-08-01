package com.example.wechat.ui.findUser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wechat.Util.AuthUtil
import com.example.wechat.Util.FirestoreUtil
import com.example.wechat.data.model.User
import com.example.wechat.ui.incoming_requests.FRIENDS
import com.google.firebase.firestore.EventListener

class FindUserViewModel : ViewModel(){

    private val userDocumentsMutableLiveData = MutableLiveData<MutableList<User?>?>()

    fun loadUsers(): MutableLiveData<MutableList<User?>?> {
        val docRef = FirestoreUtil.firestoreInstance.collection("users")
        docRef.get().addOnSuccessListener { querySnapshot ->
//            add users except currently logged in user
            val result = mutableListOf<User?>()
            for(document in querySnapshot.documents) {
                if (document.get("uid").toString() != AuthUtil.getAuthId()) {
                    val user = document.toObject(User::class.java)
                    result.add(user)
                }
            }

//            exclude friends of currently logged in user
            docRef.whereArrayContains(FRIENDS, AuthUtil.getAuthId())
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException == null) {
                        val documents = querySnapshot?.documents
                        if (documents != null) {
                            for (document in documents) {
                                val user = document.toObject(User::class.java)
                                result.remove(user)
                            }
                            userDocumentsMutableLiveData.value = result
                        }
                    } else {
                        userDocumentsMutableLiveData.value = null
                    }
                }
        }.addOnFailureListener {
            userDocumentsMutableLiveData.value = null
            }

        return userDocumentsMutableLiveData
    }
}
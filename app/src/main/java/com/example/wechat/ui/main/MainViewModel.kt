package com.example.wechat.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wechat.data.model.User
import com.example.wechat.Util.FirestoreUtil
import com.google.firebase.firestore.CollectionReference

class MainViewModel : ViewModel() {
    private var friendsListMutableLiveData =
        MutableLiveData<List<User>?>()
    private var usersCollectionRef : CollectionReference =
        FirestoreUtil.firestoreInstance.collection("users")

    fun loadFriends(loggedUser: User): MutableLiveData<List<User>?> {
        val friendIds = loggedUser.friends
        if(!friendIds.isNullOrEmpty()){
            val mFriendList = mutableListOf<User>()
            for(friendId in friendIds){
                usersCollectionRef.document(friendId).get()
                    .addOnSuccessListener { friendUser ->
                        val friend =
                            friendUser.toObject(User::class.java)
                        friend?.let { user -> mFriendList.add(user) }
                        friendsListMutableLiveData.value = mFriendList
                    }
            }
        }else {
            friendsListMutableLiveData.value = null
        }

        return friendsListMutableLiveData
    }
}
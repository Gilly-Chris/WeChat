package com.example.wechat.service

import com.example.wechat.Util.AuthUtil
import com.example.wechat.Util.FirestoreUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {

        fun getInstanceId(): Unit {
            FirebaseInstallations.getInstance().id
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        println("MyFirebaseMessagingService.getInstanceId:${task.exception}")
                        return@OnCompleteListener
                    }
//                    Get new instance id token
                    val token = task.result
                    println("MyFirebaseMessagingService.s:${token}")
                    if (token != null) {
                        addTokenToUserDocument(token)
                    }
                })
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("MyFirebaseMessagingService.onNewToken:${token}")
        addTokenToUserDocument(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            //pushing to github
            println("MyFirebaseMessagingService.onMessageReceived:${remoteMessage.data}")
        }
    }
}

private fun addTokenToUserDocument(token: String) {
    val loggedUserID = AuthUtil.firebaseAuthInstance.currentUser?.uid
    if (loggedUserID != null) {
        FirestoreUtil.firestoreInstance.collection("users").document(loggedUserID)
            .update("token", token)
    }
}
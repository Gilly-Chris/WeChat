package com.example.wechat.Util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object FirestoreUtil {

    val firestoreInstance: FirebaseFirestore by lazy {
        val firebaseFirestore = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .build()

        firebaseFirestore.firestoreSettings = settings
        firebaseFirestore
    }
}
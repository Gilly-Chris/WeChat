package com.example.wechat.Util

import com.google.firebase.storage.FirebaseStorage

object StorageUtil {
    val storageInstance: FirebaseStorage by lazy {
        println("StorageUtil.:")
        FirebaseStorage.getInstance()
    }
}
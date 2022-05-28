package com.example.wechat.Util

class ErrorMessage {
    companion object {
        var errorMessage : String? = "Something went wrong"
    }
}

enum class LoadState {
    SUCCESS, FAILURE, LOADING
}
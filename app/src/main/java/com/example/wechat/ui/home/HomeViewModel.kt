package com.example.wechat.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wechat.data.model.ChatParticipant
import com.example.wechat.data.model.User
import com.example.wechat.Util.AuthUtil
import com.example.wechat.Util.FirestoreUtil
import com.google.firebase.firestore.Query

class HomeViewModel : ViewModel() {

    var calledBefore = false
    init {
        getUserData()
    }

    private val chatParticipantList: MutableList<ChatParticipant> by lazy { mutableListOf() }
    private val chatParticipantsListMutableLiveData =
        MutableLiveData<MutableList<ChatParticipant>?>()
    val loggedUserMutableLiveData = MutableLiveData<User>()

    fun getChats(loggedUser: User): LiveData<MutableList<ChatParticipant>?> {
        if (calledBefore) {
            return chatParticipantsListMutableLiveData
        }
        calledBefore = true
        var loggedUserId = loggedUser.uid.toString()

        val query: Query = FirestoreUtil.firestoreInstance.collection("messages")
            .whereArrayContains("chat_members", loggedUserId)

        query.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException == null){
                chatParticipantList.clear()

                if(!querySnapshot?.documents.isNullOrEmpty()){
                    querySnapshot?.documents?.forEach { messageDocument ->
                        val chatParticipant = ChatParticipant()
                        //get last message & last message sender
                        val messagesList = messageDocument.get("messages") as List<HashMap<String, Any>>?
                        val lastMessage = messagesList?.get(messagesList.size - 1)

//                      get last message depending on type
                        val lastMessageType = lastMessage?.get("type") as Double?
                        chatParticipant.lastMessage = lastMessage?.get("text") as String?
                        chatParticipant.lastMessageType = lastMessageType
                        chatParticipant.lastMessageDate =
                            lastMessage?.get("created_at") as HashMap<String, Double>?
                        println("HomeViewModel.getChats:${chatParticipant.lastMessageDate?.get("seconds")}")
                        val lastMessageOwnerId = lastMessage?.get("from") as String?

//                        verify if last message was sent by logged user, and set loggedUser accordingly
                        chatParticipant.isLoggedUser = (lastMessageOwnerId == loggedUserId)

                        if(lastMessageOwnerId == loggedUserId) {
                            val recipient = lastMessage?.get("to") as String?
                            if (recipient != null) {
                                FirestoreUtil.firestoreInstance.collection("users")
                                    .document(recipient).get()
                                    .addOnSuccessListener {
                                        FirestoreUtil.firestoreInstance.collection("users")
                                            .document(recipient).get().addOnSuccessListener {
                                                val participant = it.toObject(User::class.java)
                                                chatParticipant.particpant = participant
                                                chatParticipantList.add(chatParticipant)
                                                chatParticipantsListMutableLiveData.value =
                                                    chatParticipantList
                                            }.addOnFailureListener {

                                            }
                                    }
                            } else {
                                val sender = lastMessage?.get("from") as String?
                                if (sender != null) {
                                    FirestoreUtil.firestoreInstance.collection("users")
                                        .document(sender).get()
                                        .addOnSuccessListener {
                                            FirestoreUtil.firestoreInstance.collection("users")
                                                .document(sender).get().addOnSuccessListener {
                                                    val participant = it.toObject(User::class.java)
                                                    chatParticipant.particpant = participant
                                                    chatParticipantList.add(chatParticipant)
                                                    chatParticipantsListMutableLiveData.value =
                                                        chatParticipantList

                                                }.addOnFailureListener {

                                                }
                                        }
                                }
                            }
                        }
                    }
                } else {
//                    no chats available
                    chatParticipantsListMutableLiveData.value = null
                }
            } else {
//                there was an error
                println("HomeViewModel.getChats:${firebaseFirestoreException.message}")
                chatParticipantsListMutableLiveData.value = null
            }
        }

        return chatParticipantsListMutableLiveData
    }

    private fun getUserData() {
        FirestoreUtil.firestoreInstance.collection("users").document(AuthUtil.getAuthId())
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException == null) {
                    val loggedUser = documentSnapshot?.toObject(User::class.java)
                    loggedUserMutableLiveData.value = loggedUser!!
                } else {
                    println("HomeViewModel.getUserData:${firebaseFirestoreException.message}")
                }
            }
    }
}
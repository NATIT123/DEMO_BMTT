package com.example.demo.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo.database.DemoDatabase
import com.example.demo.models.User
import com.example.demo.models.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DemoViewModel(private val demoDatabase: DemoDatabase) : ViewModel() {
    private var userListLiveData = demoDatabase.demoDAO().getListUser();
    private var userLiveData = MutableLiveData<User>();
    private var userEmailLiveData = MutableLiveData<User>();
    private var listVideoLiveData = MutableLiveData<List<Video>>()


    fun observerListVideo(): LiveData<List<Video>> {
        return listVideoLiveData
    }


    fun getListVideo(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val video = demoDatabase.demoDAO().getListVideo(userId)
            listVideoLiveData.postValue(video)
        }
    }


    fun checkIsExist(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = demoDatabase.demoDAO().getUserByEmailAndPassword(email, password)
            userLiveData.postValue(user)
        }
    }

    fun observerUser(email: String, password: String): MutableLiveData<User> {
        viewModelScope.launch(Dispatchers.IO) {
            val user = demoDatabase.demoDAO()
                .getUserByEmailAndPassword(email, password)
            withContext(Dispatchers.Main) {
                userLiveData.value = user
            }
        }
        return userLiveData
    }

    suspend fun addUser(user: User): Long {
        return withContext(Dispatchers.IO) {
            demoDatabase.demoDAO().upsert(user)
        }
    }


    fun addVideo(video: Video) {
        viewModelScope.launch {
            demoDatabase.demoDAO().upsertVideo(video)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            demoDatabase.demoDAO().updateUser(user.fullName, user.image, user.email)
        }
    }

    fun updateVideo(id: Long, name: String, encryptedFilePath: String) {
        viewModelScope.launch {
            demoDatabase.demoDAO().updateVideo(id, name, encryptedFilePath)
        }
    }

    fun deleteVideo(id: Long) {
        viewModelScope.launch {
            demoDatabase.demoDAO().deleteVideo(id)
        }
    }

    fun changePassword(password: String) {
        viewModelScope.launch {
            demoDatabase.demoDAO().changePassword(password)
        }
    }

    fun checkEmailIsExist(email: String) {
        userEmailLiveData.postValue(demoDatabase.demoDAO().checkEmailIsExist(email))
    }

    fun observerUserEmail(): MutableLiveData<User> {
        return userEmailLiveData
    }
}
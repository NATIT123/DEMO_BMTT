package com.example.demo.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo.database.DemoDatabase
import com.example.demo.models.User
import com.example.demo.models.Video
import com.example.demo.models.Video_Shared
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DemoViewModel(private val demoDatabase: DemoDatabase) : ViewModel() {
    private var userLiveData = MutableLiveData<User>();
    private var userEmailLiveData = MutableLiveData<User>();
    private var listVideoLiveData = MutableLiveData<List<Video>>()
    private var listVideoSharedLiveData = MutableLiveData<List<Video>>()


    fun observerListVideo(): LiveData<List<Video>> {
        return listVideoLiveData
    }


    fun getListVideo(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val video = demoDatabase.demoDAO().getListVideo(userId)
            listVideoLiveData.postValue(video)
        }
    }

    fun observerListSharedVideo(userId: Long): LiveData<List<Video>> {
        viewModelScope.launch(Dispatchers.IO) {
            val video = demoDatabase.demoDAO().getListVideoShared(userId)
            listVideoSharedLiveData.postValue(video)
        }
        return listVideoSharedLiveData
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
            withContext(Dispatchers.IO) {
                demoDatabase.demoDAO().upsertVideo(video)
            }
        }
    }

    fun addVideoShared(videoShared: Video_Shared) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                demoDatabase.demoDAO().upsertVideoShared(videoShared)
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                demoDatabase.demoDAO().updateUser(user.fullName, user.image, user.email)
            }
        }
    }

    fun updateVideo(id: Long, name: String, encryptedFilePath: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                demoDatabase.demoDAO().updateVideo(id, name, encryptedFilePath)
            }

        }
    }

    fun deleteVideo(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                demoDatabase.demoDAO().deleteVideo(id)
            }
        }
    }

    fun changePassword(password: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                demoDatabase.demoDAO().changePassword(password)
            }
        }
    }

    fun checkEmailIsExist(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = demoDatabase.demoDAO().checkEmailIsExist(email)
            userEmailLiveData.postValue(user)
        }
    }

    fun observerUserEmail(): MutableLiveData<User> {
        return userEmailLiveData
    }
}
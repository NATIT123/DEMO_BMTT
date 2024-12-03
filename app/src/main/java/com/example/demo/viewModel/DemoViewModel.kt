package com.example.demo.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo.database.DemoDatabase
import com.example.demo.models.User
import kotlinx.coroutines.launch


class DemoViewModel(private val demoDatabase: DemoDatabase) : ViewModel() {
    private var userListLiveData = demoDatabase.demoDAO().getListUser();
    private var userLiveData = MutableLiveData<User>();
    private var userEmailLiveData = MutableLiveData<User>();


    fun checkIsExist(email: String, password: String) {
        userLiveData.postValue(demoDatabase.demoDAO().getUserByEmailAndPassword(email, password))
    }

    fun observerUser(): MutableLiveData<User> {
        return userLiveData
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            demoDatabase.demoDAO().upsert(user);
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            demoDatabase.demoDAO().updateUser(user.fullName, user.image, user.email)
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
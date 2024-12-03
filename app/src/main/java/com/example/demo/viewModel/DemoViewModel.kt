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


    fun checkIsExist(email: String, password: String) {
        userLiveData.postValue(demoDatabase.demoDAO().getUserByEmailAndPassword(email, password))
    }

    fun observerUser(): MutableLiveData<User> {
        return userLiveData
    }

    fun addUser(user: User): Long {
        var id = 0L;
        viewModelScope.launch {
            id = demoDatabase.demoDAO().upsert(user);
        }
        return id;
    }
}
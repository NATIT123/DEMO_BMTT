package com.example.demo.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demo.database.DemoDatabase

class DemoViewModelFactory(private val demoDatabase:DemoDatabase):
    ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DemoViewModel(demoDatabase) as T
        }
}
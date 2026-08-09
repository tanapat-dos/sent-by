package com.reelshelf.app

import android.app.Application
import com.reelshelf.app.di.AppContainer

class ReelShelfApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

val Application.reelShelfContainer: AppContainer
    get() = (this as ReelShelfApp).container

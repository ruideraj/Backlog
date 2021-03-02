package com.ruideraj.backlog

import android.app.Application
import com.ruideraj.backlog.injection.AppComponent
import com.ruideraj.backlog.injection.DaggerAppComponent

class BacklogApp : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder().build()
    }

}
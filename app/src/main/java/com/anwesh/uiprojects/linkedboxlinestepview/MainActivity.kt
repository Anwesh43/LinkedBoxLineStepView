package com.anwesh.uiprojects.linkedboxlinestepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.boxlinestepview.BoxLineStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BoxLineStepView.create(this)
    }
}

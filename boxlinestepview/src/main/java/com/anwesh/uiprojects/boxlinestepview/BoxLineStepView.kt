package com.anwesh.uiprojects.boxlinestepview

/**
 * Created by anweshmishra on 09/11/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.app.Activity
import android.content.Context
import android.util.Log

val nodes : Int = 5

val INNER_LINES : Int = 3

val OUTER_LINES : Int = 4

val scGap : Float = 0.05f

fun Int.getInverse() : Float = 1f/this

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(this - i * n.getInverse(), 0f))

fun Float.scaleFactor() : Float = Math.floor(this / 0.5).toFloat()

fun Float.updateScale(dir : Float) : Float = dir * scGap * (scaleFactor() / OUTER_LINES + (1f - scaleFactor()) / INNER_LINES)

class BoxLineStepView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            val k : Float = scale.updateScale(dir)
            scale += k
            Log.d("scale Factor is ", "$k")
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (this.dir == 0f) {
                this.dir = 1f - 2 * this.prevScale
                cb()
            }
        }
    }
}
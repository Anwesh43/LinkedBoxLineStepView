package com.anwesh.uiprojects.boxlinestepview

/**
 * Created by anweshmishra on 09/11/18.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.content.Context
import android.util.Log

val nodes : Int = 5

val INNER_LINES : Int = 3

val OUTER_LINES : Int = 4

val scGap : Float = 0.05f

fun Int.getInverse() : Float = 1f/this

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(this - i * n.getInverse(), 0f)) * n

fun Float.scaleFactor() : Float = Math.floor(this / 0.5).toFloat()

fun Float.updateScale(dir : Float) : Float = dir * scGap * (scaleFactor() / OUTER_LINES + (1f - scaleFactor()) / INNER_LINES)

fun Canvas.drawBLSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val size : Float = gap / 3
    val hGap : Float = size / (INNER_LINES + 1)
    val deg : Float = 360f / OUTER_LINES
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#311B92")
    save()
    translate(gap * (i + 1), h/2)
    for (j in 0..INNER_LINES - 1) {
        val sc : Float = sc1.divideScale(j, INNER_LINES)
        save()
        translate(-size, -size + (hGap) * (j + 1))
        drawLine(0f, 0f, 2 * size * sc, 0f, paint)
        restore()
    }
    for (j in 0..OUTER_LINES - 1) {
        val sc : Float = sc2.divideScale(j, OUTER_LINES)
        save()
        rotate(deg * j)
        translate(size, size)
        drawLine(0f, 0f, -2 * size * sc, 0f, paint)
        restore()
    }
    restore()
}

class BoxLineStepView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BLSNode(var i : Int, val state : State = State()) {

        private var next : BLSNode? = null

        private var prev : BLSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BLSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBLSNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BLSNode {
            var curr : BLSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BoxLineStep(var i : Int) {

        private var curr : BLSNode = BLSNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BoxLineStepView) {

        private var bls : BoxLineStep = BoxLineStep(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            bls.draw(canvas, paint)
            animator.animate {
                bls.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bls.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BoxLineStepView {
            val view : BoxLineStepView = BoxLineStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
package com.example.open_autoglm_android.ui.floating

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

/**
 * 悬浮窗触摸监听器
 * 实现悬浮窗拖动功能
 */
class FloatingTouchListener(
    private var layoutParams: WindowManager.LayoutParams?,
    private val windowManager: WindowManager
) : View.OnTouchListener {
    
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录初始位置
                layoutParams?.let { params ->
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                }
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                layoutParams?.let { params ->
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    // 移动阈值，避免误触
                    if (kotlin.math.abs(deltaX) > 10 || kotlin.math.abs(deltaY) > 10) {
                        isDragging = true
                        params.x = initialX + deltaX.toInt()
                        params.y = initialY + deltaY.toInt()
                        windowManager.updateViewLayout(view, params)
                    }
                }
                return true
            }
            
            MotionEvent.ACTION_UP -> {
                // 如果没有拖动，则返回false以便触发点击事件
                return !isDragging
            }
        }
        return false
    }
}
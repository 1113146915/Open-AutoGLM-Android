package com.example.open_autoglm_android.ui.floating

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.open_autoglm_android.R

/**
 * 悬浮窗视图组件
 * 显示任务状态和停止按钮
 */
class FloatingWindowView(
    private val context: Context
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isShowing = false
    
    // UI组件
    private var statusIndicator: View? = null
    private var stopButton: ImageView? = null
    
    // 回调接口
    var onStopClicked: (() -> Unit)? = null
    
    /**
     * 任务状态枚举
     */
    enum class TaskStatus {
        THINKING,    // 思考中
        SUCCESS,     // 成功
        FAILED       // 失败
    }
    
    /**
     * 显示悬浮窗
     */
    fun show() {
        if (isShowing) return
        
        try {
            // 创建悬浮窗视图
            floatingView = LayoutInflater.from(context).inflate(R.layout.floating_window, null)
            
            // 初始化UI组件
            initViews()
            
            // 设置布局参数
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                x = 0
                y = 0
            }
            
            // 添加到窗口管理器
            windowManager.addView(floatingView, layoutParams)
            isShowing = true
            
            // 设置点击事件
            setupClickListeners()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 隐藏悬浮窗
     */
    fun hide() {
        if (!isShowing || floatingView == null) return
        
        try {
            windowManager.removeView(floatingView)
            floatingView = null
            isShowing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 更新任务状态
     */
    fun updateStatus(status: TaskStatus, message: String = "") {
        when (status) {
            TaskStatus.THINKING -> {
                // 思考中状态：蓝色
                statusIndicator?.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_blue_dark)
            }
            TaskStatus.SUCCESS -> {
                // 成功状态：绿色
                statusIndicator?.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_green_dark)
            }
            TaskStatus.FAILED -> {
                // 失败状态：红色
                statusIndicator?.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_red_dark)
            }
        }
    }
    
    /**
     * 检查是否正在显示
     */
    fun isShowing(): Boolean = isShowing
    
    /**
     * 初始化视图组件
     */
    private fun initViews() {
        floatingView?.let { view ->
            statusIndicator = view.findViewById(R.id.status_indicator)
            stopButton = view.findViewById(R.id.stop_button)
        }
    }
    
    /**
     * 设置点击事件
     */
    private fun setupClickListeners() {
        stopButton?.setOnClickListener {
            onStopClicked?.invoke()
        }
        
        // 设置整个悬浮窗可拖动
        floatingView?.setOnTouchListener(FloatingTouchListener(layoutParams, windowManager))
    }
}
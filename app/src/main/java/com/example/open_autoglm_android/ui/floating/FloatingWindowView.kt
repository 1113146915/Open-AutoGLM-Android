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
    private var statusIcon: ImageView? = null
    private var statusText: TextView? = null
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
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 200
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
                statusIcon?.setImageResource(R.drawable.ic_thinking)
                statusText?.text = "思考中..."
                statusText?.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            }
            TaskStatus.SUCCESS -> {
                statusIcon?.setImageResource(R.drawable.ic_success)
                statusText?.text = if (message.isEmpty()) "任务完成" else message
                statusText?.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            }
            TaskStatus.FAILED -> {
                statusIcon?.setImageResource(R.drawable.ic_error)
                statusText?.text = if (message.isEmpty()) "任务失败" else message
                statusText?.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
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
            statusIcon = view.findViewById(R.id.status_icon)
            statusText = view.findViewById(R.id.status_text)
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
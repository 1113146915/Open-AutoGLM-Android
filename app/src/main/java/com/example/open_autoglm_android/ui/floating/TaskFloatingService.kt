package com.example.open_autoglm_android.ui.floating

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.Context
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 任务悬浮窗服务
 * 负责显示和管理悬浮窗
 */
class TaskFloatingService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var floatingWindowView: FloatingWindowView? = null
    private var stateObserverJob: Job? = null
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "task_floating_service"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        // 初始化悬浮窗
        floatingWindowView = FloatingWindowView(this)
        
        // 设置停止按钮点击事件
        floatingWindowView?.onStopClicked = {
            stopCurrentTask()
        }
        
        // 监听任务状态变化
        observeTaskState()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 隐藏悬浮窗
        floatingWindowView?.hide()
        
        // 取消状态监听
        stateObserverJob?.cancel()
        
        // 取消协程作用域
        serviceScope.cancel()
        
        // 通知管理器服务已停止
        TaskStatusManager.onServiceStopped()
    }
    
    /**
     * 监听任务状态变化
     */
    private fun observeTaskState() {
        stateObserverJob = TaskStatusManager.taskState
            .onEach { state ->
                when (state) {
                    is TaskStatusManager.TaskState.Idle -> {
                        // 空闲状态，隐藏悬浮窗
                        floatingWindowView?.hide()
                        stopSelf()
                    }
                    is TaskStatusManager.TaskState.Thinking -> {
                        // 思考中状态，显示悬浮窗
                        floatingWindowView?.show()
                        floatingWindowView?.updateStatus(
                            FloatingWindowView.TaskStatus.THINKING,
                            state.message
                        )
                    }
                    is TaskStatusManager.TaskState.Success -> {
                        // 成功状态
                        floatingWindowView?.updateStatus(
                            FloatingWindowView.TaskStatus.SUCCESS,
                            state.message
                        )
                        
                        // 3秒后自动隐藏
                        serviceScope.launch {
                            kotlinx.coroutines.delay(3000)
                            if (TaskStatusManager.taskState.value is TaskStatusManager.TaskState.Success) {
                                TaskStatusManager.stopTask()
                            }
                        }
                    }
                    is TaskStatusManager.TaskState.Failed -> {
                        // 失败状态
                        floatingWindowView?.updateStatus(
                            FloatingWindowView.TaskStatus.FAILED,
                            state.message
                        )
                        
                        // 5秒后自动隐藏
                        serviceScope.launch {
                            kotlinx.coroutines.delay(5000)
                            if (TaskStatusManager.taskState.value is TaskStatusManager.TaskState.Failed) {
                                TaskStatusManager.stopTask()
                            }
                        }
                    }
                }
            }
            .launchIn(serviceScope)
    }
    
    /**
     * 停止当前任务
     */
    private fun stopCurrentTask() {
        // 发送广播通知ChatViewModel停止任务
        val stopIntent = Intent("com.example.open_autoglm_android.STOP_TASK")
        sendBroadcast(stopIntent)
        
        // 通知TaskStatusManager停止任务
        TaskStatusManager.stopTask()
        
        Log.d("TaskFloatingService", "发送停止任务广播")
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "任务悬浮窗服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示任务执行状态的悬浮窗服务，用于实时显示AI任务进度和提供停止功能"
                setShowBadge(false)
                // 对于 specialUse 类型，需要设置说明
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setSound(null, null)
                }
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建前台服务通知
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("AutoGLM 任务执行中")
            .setContentText("正在执行自动化任务")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}
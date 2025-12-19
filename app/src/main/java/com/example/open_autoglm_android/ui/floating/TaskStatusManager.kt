package com.example.open_autoglm_android.ui.floating

import android.content.Context
import android.content.Intent
import android.content.ComponentName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 任务状态管理器
 * 单例模式，管理任务状态和悬浮窗显示
 */
object TaskStatusManager {
    
    // 任务状态
    private val _taskState = MutableStateFlow<TaskState>(TaskState.Idle)
    val taskState: StateFlow<TaskState> = _taskState
    
    // 悬浮窗服务是否正在运行
    private var isServiceRunning = false
    
    // 当前任务ID（用于区分不同任务）
    private var currentTaskId: String? = null
    
    /**
     * 任务状态枚举
     */
    sealed class TaskState {
        object Idle : TaskState()
        data class Thinking(val taskId: String, val message: String = "思考中...") : TaskState()
        data class Success(val taskId: String, val message: String = "任务完成") : TaskState()
        data class Failed(val taskId: String, val message: String = "任务失败") : TaskState()
    }
    
    /**
     * 开始任务
     * @param taskId 任务ID
     * @param context 上下文，用于启动悬浮窗服务
     */
    fun startTask(taskId: String, context: Context? = null) {
        currentTaskId = taskId
        _taskState.value = TaskState.Thinking(taskId)
        
        // 启动悬浮窗服务
        context?.let {
            if (!isServiceRunning) {
                val intent = Intent(it, TaskFloatingService::class.java)
                it.startForegroundService(intent)
                isServiceRunning = true
            }
            
            // 返回主页
            returnToHome(it)
        }
    }
    
    /**
     * 返回主页
     */
    private fun returnToHome(context: Context) {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 更新任务为成功状态
     */
    fun completeTask(message: String = "任务完成") {
        currentTaskId?.let { taskId ->
            _taskState.value = TaskState.Success(taskId, message)
        }
    }
    
    /**
     * 更新任务为失败状态
     */
    fun failTask(message: String = "任务失败") {
        currentTaskId?.let { taskId ->
            _taskState.value = TaskState.Failed(taskId, message)
        }
    }
    
    /**
     * 停止当前任务
     */
    fun stopTask() {
        currentTaskId = null
        _taskState.value = TaskState.Idle
    }
    
    /**
     * 获取当前任务ID
     */
    fun getCurrentTaskId(): String? = currentTaskId
    
    /**
     * 检查是否有正在执行的任务
     */
    fun hasActiveTask(): Boolean = currentTaskId != null
    
    /**
     * 标记服务已停止
     */
    fun onServiceStopped() {
        isServiceRunning = false
        currentTaskId = null
        _taskState.value = TaskState.Idle
    }
}
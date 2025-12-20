package com.example.open_autoglm_android.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.open_autoglm_android.data.model.Workflow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
/**
 * 工作流数据仓库
 * 负责工作流数据的本地存储和管理
 * 使用单例模式确保数据一致性
 */
class WorkflowRepository private constructor(
    private val context: Context,
    private val gson: Gson
) {
    companion object {
        private const val PREFS_NAME = "workflow_prefs"
        private const val KEY_WORKFLOWS = "workflows"
        
        @Volatile
        private var INSTANCE: WorkflowRepository? = null
        
        /**
         * 获取单例实例
         * 
         * @param context 应用上下文
         * @param gson Gson实例
         * @return WorkflowRepository单例实例
         */
        fun getInstance(context: Context, gson: Gson): WorkflowRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkflowRepository(
                    context.applicationContext,
                    gson
                ).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // 工作流列表的 StateFlow
    private val _workflows = MutableStateFlow<List<Workflow>>(emptyList())
    val workflows: StateFlow<List<Workflow>> = _workflows.asStateFlow()
    
    init {
        loadWorkflows()
    }
    
    /**
     * 从本地存储加载工作流列表
     */
    private fun loadWorkflows() {
        try {
            val workflowsJson = sharedPreferences.getString(KEY_WORKFLOWS, null)
            if (!workflowsJson.isNullOrBlank()) {
                val type = object : TypeToken<List<Workflow>>() {}.type
                val workflowList: List<Workflow> = gson.fromJson(workflowsJson, type)
                _workflows.value = workflowList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _workflows.value = emptyList()
        }
    }
    
    /**
     * 保存工作流列表到本地存储
     */
    private fun saveWorkflows() {
        try {
            val workflowsJson = gson.toJson(_workflows.value)
            sharedPreferences.edit()
                .putString(KEY_WORKFLOWS, workflowsJson)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 添加新的工作流
     */
    fun addWorkflow(workflow: Workflow) {
        val currentList = _workflows.value.toMutableList()
        currentList.add(workflow)
        _workflows.value = currentList
        saveWorkflows()
    }
    
    /**
     * 更新现有工作流
     */
    fun updateWorkflow(workflow: Workflow) {
        val currentList = _workflows.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == workflow.id }
        if (index != -1) {
            currentList[index] = workflow.copy(updatedTime = System.currentTimeMillis())
            _workflows.value = currentList
            saveWorkflows()
        }
    }
    
    /**
     * 删除工作流
     * 
     * @param workflowId 要删除的工作流ID
     */
    suspend fun deleteWorkflow(workflowId: String) {
        try {
            val currentList = _workflows.value.toMutableList()
            currentList.removeAll { it.id == workflowId }
            _workflows.value = currentList
            saveWorkflows()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 根据ID获取工作流
     */
    fun getWorkflowById(workflowId: String): Workflow? {
        return _workflows.value.find { it.id == workflowId }
    }
    
    /**
     * 清空所有工作流
     */
    fun clearAllWorkflows() {
        _workflows.value = emptyList()
        saveWorkflows()
    }
}
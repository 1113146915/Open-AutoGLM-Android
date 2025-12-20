package com.example.open_autoglm_android.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.open_autoglm_android.data.model.Workflow
import com.example.open_autoglm_android.data.model.WorkflowTemplate
import com.example.open_autoglm_android.data.model.WorkflowTemplates
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
     * 处理向后兼容性：支持旧版本和新版本数据格式
     */
    private fun loadWorkflows() {
        try {
            val workflowsJson = sharedPreferences.getString(KEY_WORKFLOWS, null)
            if (!workflowsJson.isNullOrBlank()) {
                // 尝试解析新格式
                try {
                    val type = object : TypeToken<List<Workflow>>() {}.type
                    val workflowList: List<Workflow> = gson.fromJson(workflowsJson, type)
                    _workflows.value = workflowList
                } catch (e: Exception) {
                    // 如果新格式解析失败，尝试解析旧格式并转换
                    loadLegacyWorkflows(workflowsJson)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _workflows.value = emptyList()
        }
    }
    
    /**
     * 加载旧版本工作流格式并转换为新格式
     */
    private fun loadLegacyWorkflows(workflowsJson: String) {
        try {
            // 尝试解析旧格式数据结构
            val legacyType = object : TypeToken<List<LegacyWorkflow>>() {}.type
            val legacyWorkflows: List<LegacyWorkflow> = gson.fromJson(workflowsJson, legacyType)
            
            // 转换为新格式
            val convertedWorkflows = legacyWorkflows.map { legacy ->
                Workflow.fromTraditionalFormat(
                    legacy.id,
                    legacy.title,
                    legacy.steps
                )
            }
            
            _workflows.value = convertedWorkflows
            
            // 保存转换后的新格式
            saveWorkflows()
            
        } catch (e: Exception) {
            e.printStackTrace()
            _workflows.value = emptyList()
        }
    }
    
    /**
     * 旧版本工作流数据模型（仅用于向后兼容）
     */
    private data class LegacyWorkflow(
        val id: String,
        val title: String,
        val steps: String,
        val createdTime: Long = System.currentTimeMillis(),
        val updatedTime: Long = System.currentTimeMillis()
    )
    
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
    
    /**
     * 从模板创建工作流
     * 
     * @param template 模板对象
     * @param customTitle 自定义标题
     * @return 创建的工作流
     */
    fun createFromTemplate(template: WorkflowTemplate, customTitle: String? = null): Workflow {
        val workflow = Workflow(
            id = "workflow_${System.currentTimeMillis()}",
            title = customTitle ?: template.name,
            description = template.description,
            steps = template.steps,
            tags = template.tags
        )
        addWorkflow(workflow)
        return workflow
    }
    
    /**
     * 获取所有可用模板
     */
    fun getAllTemplates(): List<WorkflowTemplate> {
        return WorkflowTemplates.getAllTemplates()
    }
    
    /**
     * 根据分类获取模板
     */
    fun getTemplatesByCategory(category: String): List<WorkflowTemplate> {
        return WorkflowTemplates.getTemplatesByCategory(category)
    }
    
    /**
     * 根据标签搜索模板
     */
    fun searchTemplatesByTag(tag: String): List<WorkflowTemplate> {
        return WorkflowTemplates.searchTemplatesByTag(tag)
    }
    
    /**
     * 根据ID获取模板
     */
    fun getTemplateById(id: String): WorkflowTemplate? {
        return WorkflowTemplates.getTemplateById(id)
    }
    
    /**
     * 兼容性方法：从传统格式创建工作流
     * 用于向后兼容现有的简单文本格式工作流
     */
    fun createFromTraditionalFormat(id: String, title: String, stepsText: String): Workflow {
        val workflow = Workflow.fromTraditionalFormat(id, title, stepsText)
        addWorkflow(workflow)
        return workflow
    }
}
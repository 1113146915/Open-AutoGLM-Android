package com.example.open_autoglm_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.open_autoglm_android.data.model.Workflow
import com.example.open_autoglm_android.data.model.WorkflowTemplate
import com.example.open_autoglm_android.data.model.WorkflowStep

import com.example.open_autoglm_android.data.model.StepParameters
import com.example.open_autoglm_android.data.repository.WorkflowRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 工作流管理界面的UI状态
 */
data class WorkflowManagementUiState(
    val workflows: List<Workflow> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedWorkflow: Workflow? = null,
    val isEditingWorkflow: Boolean = false,
    val saveSuccess: Boolean = false
)

/**
 * 工作流编辑界面的UI状态
 */
data class WorkflowEditUiState(
    val title: String = "",
    val description: String = "",
    val steps: List<WorkflowStep> = emptyList(),
    val tags: List<String> = emptyList(),
    val isEditing: Boolean = false,
    val workflowId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 工作流管理ViewModel
 * 负责工作流的增删改查和状态管理
 */
class WorkflowViewModel(
    application: Application,
    private val workflowRepository: WorkflowRepository
) : AndroidViewModel(application) {
    
    // 管理界面状态
    private val _managementUiState = MutableStateFlow(WorkflowManagementUiState())
    val managementUiState: StateFlow<WorkflowManagementUiState> = _managementUiState.asStateFlow()
    
    // 编辑界面状态
    private val _editUiState = MutableStateFlow(WorkflowEditUiState())
    val editUiState: StateFlow<WorkflowEditUiState> = _editUiState.asStateFlow()
    
    // 模板状态
    private val _templates = MutableStateFlow<List<WorkflowTemplate>>(emptyList())
    val templates: StateFlow<List<WorkflowTemplate>> = _templates.asStateFlow()
    
    init {
        loadWorkflows()
    }
    
    /**
     * 加载工作流列表
     */
    private fun loadWorkflows() {
        viewModelScope.launch {
            try {
                _managementUiState.value = _managementUiState.value.copy(isLoading = true, error = null)
                workflowRepository.workflows.collect { workflows ->
                    _managementUiState.value = _managementUiState.value.copy(
                        workflows = workflows,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _managementUiState.value = _managementUiState.value.copy(
                    isLoading = false,
                    error = "加载工作流失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 开始新增工作流
     */
    fun startAddWorkflow() {
        _editUiState.value = WorkflowEditUiState()
        _managementUiState.value = _managementUiState.value.copy(
            isEditingWorkflow = true,
            selectedWorkflow = null
        )
    }
    
    /**
     * 开始编辑工作流
     */
    fun startEditWorkflow(workflow: Workflow) {
        _editUiState.value = WorkflowEditUiState(
            title = workflow.title,
            description = workflow.description,
            steps = workflow.steps,
            tags = workflow.tags,
            isEditing = true,
            workflowId = workflow.id
        )
        _managementUiState.value = _managementUiState.value.copy(
            isEditingWorkflow = true,
            selectedWorkflow = workflow
        )
    }
    
    /**
     * 根据ID开始编辑工作流
     */
    fun startEditWorkflowById(workflowId: String) {
        val workflowList = workflowRepository.workflows.value
        val workflow = workflowList.find { it.id == workflowId }
        if (workflow != null) {
            startEditWorkflow(workflow)
        }
    }
    
    /**
     * 结束编辑工作流
     */
    fun endEditWorkflow() {
        _editUiState.value = WorkflowEditUiState()
        _managementUiState.value = _managementUiState.value.copy(
            isEditingWorkflow = false,
            selectedWorkflow = null,
            saveSuccess = false
        )
    }
    
    /**
     * 更新编辑中的工作流标题
     */
    fun updateEditTitle(title: String) {
        _editUiState.value = _editUiState.value.copy(title = title)
    }
    
    /**
     * 更新编辑中的工作流描述
     */
    fun updateEditDescription(description: String) {
        _editUiState.value = _editUiState.value.copy(description = description)
    }
    
    /**
     * 更新工作流步骤
     */
    fun updateEditSteps(steps: List<WorkflowStep>) {
        _editUiState.value = _editUiState.value.copy(steps = steps)
    }
    
    /**
     * 更新工作流标签
     */
    fun updateEditTags(tags: List<String>) {
        _editUiState.value = _editUiState.value.copy(tags = tags)
    }
    
    /**
     * 更新工作流标题（保持向后兼容）
     */
    fun updateWorkflowTitle(title: String) {
        updateEditTitle(title)
    }
    
    /**
     * 更新工作流步骤（保持向后兼容）
     */
    fun updateWorkflowSteps(steps: String) {
        // 将字符串格式转换为结构化步骤
        val structuredSteps = steps.split("\n")
            .filter { it.isNotBlank() }
            .mapIndexed { index, stepText ->
                WorkflowStep(
                    id = "step_$index",
                    name = stepText.trim(),
                    description = "",
                    parameters = StepParameters(waitTime = 1000)
                )
            }
        _editUiState.value = _editUiState.value.copy(steps = structuredSteps)
    }
    
    /**
     * 保存工作流
     */
    fun saveWorkflow() {
        val currentState = _editUiState.value
        
        if (currentState.title.isBlank()) {
            _editUiState.value = currentState.copy(error = "请输入工作流标题")
            return
        }
        
        if (currentState.steps.isEmpty()) {
            _editUiState.value = currentState.copy(error = "请添加至少一个工作流步骤")
            return
        }
        
        viewModelScope.launch {
            try {
                _editUiState.value = currentState.copy(isLoading = true, error = null)
                
                if (currentState.isEditing && currentState.workflowId != null) {
                    // 更新现有工作流
                    val existingWorkflow = workflowRepository.getWorkflowById(currentState.workflowId)
                    if (existingWorkflow != null) {
                        val updatedWorkflow = existingWorkflow.copy(
                            title = currentState.title,
                            description = currentState.description,
                            steps = currentState.steps,
                            tags = currentState.tags,
                            updatedTime = System.currentTimeMillis()
                        )
                        workflowRepository.updateWorkflow(updatedWorkflow)
                    }
                } else {
                    // 创建新工作流
                    val newWorkflow = Workflow(
                        id = System.currentTimeMillis().toString(),
                        title = currentState.title,
                        description = currentState.description,
                        steps = currentState.steps,
                        tags = currentState.tags
                    )
                    workflowRepository.addWorkflow(newWorkflow)
                }
                
                _editUiState.value = WorkflowEditUiState()
                _managementUiState.value = _managementUiState.value.copy(
                    isEditingWorkflow = false,
                    selectedWorkflow = null,
                    saveSuccess = true
                )
                
            } catch (e: Exception) {
                _editUiState.value = currentState.copy(
                    isLoading = false,
                    error = "保存失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 删除工作流
     */
    fun deleteWorkflow(workflowId: String) {
        viewModelScope.launch {
            try {
                workflowRepository.deleteWorkflow(workflowId)
            } catch (e: Exception) {
                _managementUiState.value = _managementUiState.value.copy(
                    error = "删除失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearManagementError() {
        _managementUiState.value = _managementUiState.value.copy(error = null)
    }
    
    /**
     * 清除编辑界面错误信息
     */
    fun clearEditError() {
        _editUiState.value = _editUiState.value.copy(error = null)
    }
    
    /**
     * 清除保存成功状态
     */
    fun clearSaveSuccess() {
        _managementUiState.value = _managementUiState.value.copy(saveSuccess = false)
    }
    
    /**
     * 加载模板列表
     */
    fun loadTemplates() {
        viewModelScope.launch {
            try {
                val templateList = workflowRepository.getAllTemplates()
                _templates.value = templateList
            } catch (e: Exception) {
                _managementUiState.value = _managementUiState.value.copy(
                    error = "加载模板失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 从模板创建工作流
     */
    fun createFromTemplate(template: WorkflowTemplate): Workflow {
        return workflowRepository.createFromTemplate(template)
    }
    
    /**
     * 创建空工作流
     */
    fun createEmptyWorkflow(): Workflow {
        val workflow = Workflow(
            id = "workflow_${System.currentTimeMillis()}",
            title = "新工作流",
            description = "",
            steps = listOf(
                WorkflowStep(
                    id = "step_1",
                    name = "步骤1",
                    description = "请编辑此步骤",
                    parameters = StepParameters()
                )
            ),
            tags = emptyList()
        )
        workflowRepository.addWorkflow(workflow)
        return workflow
    }
    
    /**
     * 获取工作流列表（用于Compose）
     */
    val workflows = workflowRepository.workflows
    
    /**
     * 更新工作流描述
     */
    fun updateWorkflowDescription(description: String) {
        _editUiState.value = _editUiState.value.copy(description = description)
    }
    
    /**
     * 更新工作流标签
     */
    fun updateWorkflowTags(tags: List<String>) {
        _editUiState.value = _editUiState.value.copy(tags = tags)
    }
    
    /**
     * 更新结构化步骤
     */
    fun updateStructuredSteps(steps: List<WorkflowStep>) {
        _editUiState.value = _editUiState.value.copy(steps = steps)
    }
    
    /**
     * 保存增强版工作流
     */
    fun saveEnhancedWorkflow() {
        val currentState = _editUiState.value
        
        if (currentState.title.isBlank()) {
            _editUiState.value = currentState.copy(error = "请输入工作流标题")
            return
        }
        
        viewModelScope.launch {
            try {
                _editUiState.value = currentState.copy(isLoading = true, error = null)
                
                if (currentState.isEditing && currentState.workflowId != null) {
                    // 更新现有工作流
                    val existingWorkflow = workflowRepository.getWorkflowById(currentState.workflowId)
                    if (existingWorkflow != null) {
                        val updatedWorkflow = existingWorkflow.copy(
                            title = currentState.title,
                            description = currentState.description,
                            steps = currentState.steps,
                            tags = currentState.tags,
                            updatedTime = System.currentTimeMillis()
                        )
                        workflowRepository.updateWorkflow(updatedWorkflow)
                    }
                } else {
                    // 创建新工作流
                    val newWorkflow = Workflow(
                        id = System.currentTimeMillis().toString(),
                        title = currentState.title,
                        description = currentState.description,
                        steps = currentState.steps,
                        tags = currentState.tags
                    )
                    workflowRepository.addWorkflow(newWorkflow)
                }
                
                _editUiState.value = WorkflowEditUiState()
                _managementUiState.value = _managementUiState.value.copy(
                    isEditingWorkflow = false,
                    selectedWorkflow = null,
                    saveSuccess = true
                )
                
            } catch (e: Exception) {
                _editUiState.value = currentState.copy(
                    isLoading = false,
                    error = "保存失败: ${e.message}"
                )
            }
        }
    }
}
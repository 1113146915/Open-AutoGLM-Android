package com.example.open_autoglm_android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.open_autoglm_android.data.model.Workflow
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
    val steps: String = "",
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
            steps = workflow.steps,
            isEditing = true,
            workflowId = workflow.id
        )
        _managementUiState.value = _managementUiState.value.copy(
            isEditingWorkflow = true,
            selectedWorkflow = workflow
        )
    }
    
    /**
     * 结束编辑工作流
     */
    fun stopEditWorkflow() {
        _editUiState.value = WorkflowEditUiState()
        _managementUiState.value = _managementUiState.value.copy(
            isEditingWorkflow = false,
            selectedWorkflow = null,
            saveSuccess = false
        )
    }
    
    /**
     * 更新工作流标题
     */
    fun updateWorkflowTitle(title: String) {
        _editUiState.value = _editUiState.value.copy(title = title)
    }
    
    /**
     * 更新工作流步骤
     */
    fun updateWorkflowSteps(steps: String) {
        _editUiState.value = _editUiState.value.copy(steps = steps)
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
        
        if (currentState.steps.isBlank()) {
            _editUiState.value = currentState.copy(error = "请输入工作流步骤")
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
                            steps = currentState.steps,
                            updatedTime = System.currentTimeMillis()
                        )
                        workflowRepository.updateWorkflow(updatedWorkflow)
                    }
                } else {
                    // 创建新工作流
                    val newWorkflow = Workflow(
                        id = System.currentTimeMillis().toString(),
                        title = currentState.title,
                        steps = currentState.steps
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
}
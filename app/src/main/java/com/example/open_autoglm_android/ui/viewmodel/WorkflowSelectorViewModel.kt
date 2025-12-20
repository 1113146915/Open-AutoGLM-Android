package com.example.open_autoglm_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.open_autoglm_android.data.model.Workflow
import com.example.open_autoglm_android.data.repository.WorkflowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 工作流选择器的UI状态
 */
data class WorkflowSelectorUiState(
    val workflows: List<Workflow> = emptyList(),
    val isShowing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 工作流选择器ViewModel
 * 负责在会话界面中选择工作流
 */
class WorkflowSelectorViewModel(
    private val workflowRepository: WorkflowRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WorkflowSelectorUiState())
    val uiState: StateFlow<WorkflowSelectorUiState> = _uiState.asStateFlow()
    
    init {
        loadWorkflows()
    }
    
    /**
     * 加载工作流列表
     */
    private fun loadWorkflows() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                workflowRepository.workflows.collect { workflows ->
                    _uiState.value = _uiState.value.copy(
                        workflows = workflows,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载工作流失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 显示工作流选择器
     */
    fun showWorkflowSelector() {
        _uiState.value = _uiState.value.copy(isShowing = true)
    }
    
    /**
     * 隐藏工作流选择器
     */
    fun hideWorkflowSelector() {
        _uiState.value = _uiState.value.copy(isShowing = false)
    }
    
    /**
     * 切换工作流选择器显示状态
     */
    fun toggleWorkflowSelector() {
        _uiState.value = _uiState.value.copy(isShowing = !_uiState.value.isShowing)
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
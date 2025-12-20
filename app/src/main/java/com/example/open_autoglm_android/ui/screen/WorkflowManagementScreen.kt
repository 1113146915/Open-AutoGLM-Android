package com.example.open_autoglm_android.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.open_autoglm_android.data.model.WorkflowStep
import com.example.open_autoglm_android.data.model.StepParameters

import com.example.open_autoglm_android.data.model.Workflow
import com.example.open_autoglm_android.ui.viewmodel.WorkflowViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 工作流管理页面
 * 显示工作流列表，支持增加、修改、删除操作
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowManagementScreen(
    viewModel: WorkflowViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val managementUiState by viewModel.managementUiState.collectAsStateWithLifecycle()
    val editUiState by viewModel.editUiState.collectAsStateWithLifecycle()
    
    if (managementUiState.isEditingWorkflow) {
        // 显示编辑界面
        WorkflowEditScreen(
            uiState = editUiState,
            onBackClick = { viewModel.endEditWorkflow() },
            onSaveClick = { viewModel.saveWorkflow() },
            onTitleChange = { viewModel.updateWorkflowTitle(it) },
            onStepsChange = { viewModel.updateEditSteps(it) },
            onClearError = { viewModel.clearEditError() }
        )
    } else {
        // 显示列表界面
        WorkflowListScreen(
            uiState = managementUiState,
            onBackClick = onBackClick,
            onAddClick = { viewModel.startAddWorkflow() },
            onEditClick = { workflow -> viewModel.startEditWorkflow(workflow) },
            onDeleteClick = { workflowId -> viewModel.deleteWorkflow(workflowId) },
            onClearError = { viewModel.clearManagementError() },
            onClearSaveSuccess = { viewModel.clearSaveSuccess() },
            modifier = modifier
        )
    }
}

/**
 * 工作流列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkflowListScreen(
    uiState: com.example.open_autoglm_android.ui.viewmodel.WorkflowManagementUiState,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Workflow) -> Unit,
    onDeleteClick: (String) -> Unit,
    onClearError: () -> Unit,
    onClearSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
                Text(
                    text = "工作流管理",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加工作流"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 成功提示
        if (uiState.saveSuccess) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "工作流保存成功",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    TextButton(onClick = onClearSaveSuccess) {
                        Text("确定")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 错误提示
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(onClick = onClearError) {
                        Text("关闭")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 工作流列表
        if (uiState.workflows.isEmpty()) {
            // 空状态
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂无工作流",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右上角的 + 按钮创建第一个工作流",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // 工作流列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.workflows) { workflow ->
                    WorkflowItem(
                        workflow = workflow,
                        onEditClick = { onEditClick(workflow) },
                        onDeleteClick = { onDeleteClick(workflow.id) }
                    )
                }
            }
        }
    }
}

/**
 * 工作流项目组件
 */
@Composable
private fun WorkflowItem(
    workflow: Workflow,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = workflow.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workflow.generateStepsDescription(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "更新于 ${dateFormat.format(Date(workflow.updatedTime))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * 工作流编辑界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkflowEditScreen(
    uiState: com.example.open_autoglm_android.ui.viewmodel.WorkflowEditUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onStepsChange: (List<WorkflowStep>) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
                Text(
                    text = if (uiState.isEditing) "编辑工作流" else "新建工作流",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = onSaveClick,
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("保存")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 错误提示
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(onClick = onClearError) {
                        Text("关闭")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 标题输入
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text("工作流标题 *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("输入工作流标题") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 步骤输入（文本模式）
        OutlinedTextField(
            value = uiState.steps.joinToString("\n") { it.name },
            onValueChange = { stepsText ->
                // 将文本转换为步骤列表
                val stepsList = stepsText.split("\n")
                    .filter { it.isNotBlank() }
                    .mapIndexed { index, stepText ->
                        WorkflowStep(
                            id = "step_$index",
                            name = stepText.trim(),
                            description = "",
                            parameters = StepParameters(waitTime = 1000)
                        )
                    }
                onStepsChange(stepsList)
            },
            label = { Text("工作流步骤 *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("输入工作流的具体步骤描述...") },
            maxLines = 10
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 说明文字
        Text(
            text = "说明：\n" +
                    "• 工作流标题用于快速识别和选择\n" +
                    "• 工作流步骤将作为任务描述发送给AI\n" +
                    "• 请详细描述每个步骤，以获得更好的执行效果",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
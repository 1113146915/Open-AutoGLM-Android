package com.example.open_autoglm_android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.open_autoglm_android.data.model.Workflow
import com.example.open_autoglm_android.ui.viewmodel.WorkflowSelectorViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 工作流选择器组件
 * 在会话界面显示可选的工作流列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowSelector(
    viewModel: WorkflowSelectorViewModel,
    onWorkflowSelected: (Workflow) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.isShowing) {
        Dialog(onDismissRequest = { viewModel.hideWorkflowSelector() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "工作流",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "选择工作流",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        IconButton(onClick = { viewModel.hideWorkflowSelector() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭"
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
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
                                TextButton(onClick = { viewModel.clearError() }) {
                                    Text("关闭")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 工作流列表
                    if (uiState.workflows.isEmpty()) {
                        // 空状态
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "暂无可用工作流",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "请先在设置中创建工作流",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // 工作流列表
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(uiState.workflows) { workflow ->
                                WorkflowSelectorItem(
                                    workflow = workflow,
                                    onSelected = {
                                        onWorkflowSelected(workflow)
                                        viewModel.hideWorkflowSelector()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 工作流选择器项目组件
 */
@Composable
private fun WorkflowSelectorItem(
    workflow: Workflow,
    onSelected: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelected,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                text = workflow.steps,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "更新于 ${dateFormat.format(Date(workflow.updatedTime))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
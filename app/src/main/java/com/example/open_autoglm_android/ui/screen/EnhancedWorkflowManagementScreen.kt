package com.example.open_autoglm_android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.open_autoglm_android.ui.components.StructuredWorkflowEditor
import com.example.open_autoglm_android.ui.components.TagManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.open_autoglm_android.data.model.Workflow
import com.example.open_autoglm_android.data.model.WorkflowTemplate
import com.example.open_autoglm_android.ui.viewmodel.WorkflowViewModel
import kotlinx.coroutines.launch

/**
 * 增强的工作流管理界面
 * 支持模板选择、步骤编辑和智能优化
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedWorkflowManagementScreen(
    onBackClick: () -> Unit,
    onEditWorkflow: (String) -> Unit,
    onExecuteWorkflow: (String) -> Unit,
    viewModel: WorkflowViewModel
) {
    var showTemplateDialog by remember { mutableStateOf(false) }
    var selectedWorkflow by remember { mutableStateOf<Workflow?>(null) }
    
    val workflows by viewModel.workflows.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val editUiState by viewModel.editUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // 加载模板数据
    LaunchedEffect(Unit) {
        viewModel.loadTemplates()
    }
    
    // 如果正在编辑，显示编辑界面
    if (editUiState.isEditing) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (editUiState.isEditing) "编辑工作流" else "新建工作流") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.endEditWorkflow() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // 基本信息区域 - 使用Card包装
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 工作流名称
                            OutlinedTextField(
                                value = editUiState.title,
                                onValueChange = { viewModel.updateEditTitle(it) },
                                label = { Text("工作流名称 *") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = editUiState.error != null
                            )
                            
                            // 工作流描述
                            OutlinedTextField(
                                value = editUiState.description,
                                onValueChange = { viewModel.updateEditDescription(it) },
                                label = { Text("工作流描述") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                            
                            // 标签管理
                            TagManager(
                                tags = editUiState.tags,
                                onTagsChanged = { viewModel.updateEditTags(it) },
                                placeholder = "添加工作流标签..."
                            )
                        }
                    }
                }
                
                item {
                    // 步骤编辑区域 - 根据内容自适应高度
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // 结构化工作流步骤编辑器
                            StructuredWorkflowEditor(
                                steps = editUiState.steps,
                                onStepsChanged = { viewModel.updateEditSteps(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                item {
                    // 保存按钮区域
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Button(
                                onClick = { 
                                    viewModel.saveWorkflow()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("保存工作流")
                            }
                            
                            // 显示错误信息
                            editUiState.error?.let { error ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // 显示工作流列表
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部标题栏
            TopAppBar(
                title = { 
                    Text(
                        "工作流管理",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 从模板创建按钮
                    IconButton(
                        onClick = { showTemplateDialog = true }
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = "从模板创建")
                    }
                    
                    // 新建工作流按钮
                    IconButton(
                        onClick = { 
                            val newWorkflow = viewModel.createEmptyWorkflow()
                            onEditWorkflow(newWorkflow.id)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "新建工作流")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            
            // 工作流列表
            if (workflows.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Apps,
                            modifier = Modifier.size(64.dp),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "暂无工作流",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "点击右上角模板按钮快速创建",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Button(
                            onClick = { showTemplateDialog = true },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("从模板创建")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(workflows) { workflow ->
                        WorkflowCard(
                            workflow = workflow,
                            onEdit = { onEditWorkflow(workflow.id) },
                            onExecute = { onExecuteWorkflow(workflow.id) },
                            onDelete = { 
                                coroutineScope.launch {
                                    viewModel.deleteWorkflow(workflow.id)
                                }
                            }
                        )
                    }
                }
            }
        }
        }
    }
    
    // 模板选择对话框
    if (showTemplateDialog) {
        TemplateSelectionDialog(
            templates = templates,
            onTemplateSelected = { template ->
                showTemplateDialog = false
                val newWorkflow = viewModel.createFromTemplate(template)
                onEditWorkflow(newWorkflow.id)
            },
            onDismiss = { showTemplateDialog = false }
        )
    }
}

/**
 * 工作流卡片组件
 */
@Composable
private fun WorkflowCard(
    workflow: Workflow,
    onEdit: () -> Unit,
    onExecute: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题和操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workflow.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (workflow.description.isNotEmpty()) {
                        Text(
                            text = workflow.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    // 标签
                    if (workflow.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            workflow.tags.take(3).forEach { tag ->
                                Surface(
                                    modifier = Modifier,
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            if (workflow.tags.size > 3) {
                                Text(
                                    text = "+${workflow.tags.size - 3}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 操作按钮
                Row {
                    IconButton(onClick = onExecute) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "执行",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑"
                        )
                    }
                }
            }
            
            // 步骤信息
            if (workflow.steps.isNotEmpty()) {
                Text(
                    text = "步骤: ${workflow.steps.size}个",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // 时间信息
            Text(
                text = "更新时间: ${java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(workflow.updatedTime))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 模板选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateSelectionDialog(
    templates: List<WorkflowTemplate>,
    onTemplateSelected: (WorkflowTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>("全部") }
    val categories = remember {
        listOf("全部") + templates.map { it.category }.distinct()
    }
    
    val filteredTemplates = if (selectedCategory == "全部") {
        templates
    } else {
        templates.filter { it.category == selectedCategory }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择工作流模板",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // 分类筛选
                LazyRow(
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        val category = categories[index]
                        FilterChip(
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            selected = selectedCategory == category
                        )
                    }
                }
                
                // 模板列表
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTemplates.size) { index ->
                        val template = filteredTemplates[index]
                        TemplateCard(
                            template = template,
                            onSelect = { onTemplateSelected(template) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 模板卡片组件
 */
@Composable
private fun TemplateCard(
    template: WorkflowTemplate,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分类: ${template.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "${template.steps.size} 步骤",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (template.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    template.tags.take(2).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
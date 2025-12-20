package com.example.open_autoglm_android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.open_autoglm_android.data.model.StepCondition
import com.example.open_autoglm_android.data.model.WorkflowStep

/**
 * 条件判断编辑器
 * 支持复杂的条件逻辑配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionEditor(
    step: WorkflowStep,
    onStepChanged: (WorkflowStep) -> Unit,
    modifier: Modifier = Modifier
) {
    var hasCondition by remember { mutableStateOf(step.condition != null) }
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 条件开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "执行条件",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = hasCondition,
                        onCheckedChange = { enabled ->
                            hasCondition = enabled
                            if (!enabled) {
                                onStepChanged(step.copy(condition = null))
                            } else {
                                // 创建默认条件
                                val defaultCondition = StepCondition(
                                    type = "element_exists",
                                    target = "继续",
                                    operator = "equals",
                                    onTrue = "继续执行",
                                    onFalse = "跳过此步骤"
                                )
                                onStepChanged(step.copy(condition = defaultCondition))
                            }
                        }
                    )
                    
                    if (hasCondition) {
                        IconButton(
                            onClick = { expanded = !expanded }
                        ) {
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "收起" else "展开"
                            )
                        }
                    }
                }
            }
            
            if (hasCondition && expanded && step.condition != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    ConditionDetailEditor(
                        condition = step.condition!!,
                        onConditionChanged = { newCondition ->
                            onStepChanged(step.copy(condition = newCondition))
                        }
                    )
                }
            }
        }
    }
}

/**
 * 条件详细编辑器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionDetailEditor(
    condition: StepCondition,
    onConditionChanged: (StepCondition) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 条件类型选择
        var conditionTypeExpanded by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = conditionTypeExpanded,
            onExpandedChange = { conditionTypeExpanded = !conditionTypeExpanded }
        ) {
            OutlinedTextField(
                value = condition.type,
                onValueChange = { },
                readOnly = true,
                label = { Text("条件类型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionTypeExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = conditionTypeExpanded,
                onDismissRequest = { conditionTypeExpanded = false }
            ) {
                listOf("element_exists", "text_contains", "app_active", "network_connected").forEach { conditionType ->
                    DropdownMenuItem(
                        text = { Text(
                            when (conditionType) {
                                "element_exists" -> "元素存在"
                                "text_contains" -> "文本包含"
                                "app_active" -> "应用活跃"
                                "network_connected" -> "网络连接"
                                else -> conditionType
                            }
                        ) },
                        onClick = {
                            onConditionChanged(condition.copy(type = conditionType))
                            conditionTypeExpanded = false
                        }
                    )
                }
            }
        }
        
        // 目标文本
        OutlinedTextField(
            value = condition.target,
            onValueChange = { 
                onConditionChanged(condition.copy(target = it))
            },
            label = { Text("目标文本") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("要检查的文本内容") }
        )
        
        // 条件操作符
        var operatorExpanded by remember { mutableStateOf(false) }
        val operators = listOf("equals", "contains", "startsWith", "endsWith", "not_equals", "not_contains")
        
        ExposedDropdownMenuBox(
            expanded = operatorExpanded,
            onExpandedChange = { operatorExpanded = !operatorExpanded }
        ) {
            OutlinedTextField(
                value = condition.operator,
                onValueChange = { },
                readOnly = true,
                label = { Text("条件操作符") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operatorExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = operatorExpanded,
                onDismissRequest = { operatorExpanded = false }
            ) {
                operators.forEach { operator ->
                    DropdownMenuItem(
                        text = { 
                            Text(when (operator) {
                                "equals" -> "等于"
                                "contains" -> "包含"
                                "startsWith" -> "开始于"
                                "endsWith" -> "结束于"
                                "not_equals" -> "不等于"
                                "not_contains" -> "不包含"
                                else -> operator
                            })
                        },
                        onClick = {
                            onConditionChanged(condition.copy(operator = operator))
                            operatorExpanded = false
                        }
                    )
                }
            }
        }
        
        // 预期值（可选）
        OutlinedTextField(
            value = condition.expectedValue,
            onValueChange = { 
                onConditionChanged(condition.copy(expectedValue = it))
            },
            label = { Text("预期值（可选）") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("与目标文本比较的值，留空则与目标文本自身比较") }
        )
        
        // 成功时执行
        OutlinedTextField(
            value = condition.onTrue,
            onValueChange = { 
                onConditionChanged(condition.copy(onTrue = it))
            },
            label = { Text("条件满足时执行") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例如：继续执行下一步骤") }
        )
        
        // 失败时执行
        OutlinedTextField(
            value = condition.onFalse,
            onValueChange = { 
                onConditionChanged(condition.copy(onFalse = it))
            },
            label = { Text("条件不满足时执行") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("例如：跳过此步骤或重试") }
        )
        
        // 高级选项
        AdvancedConditionOptions(
            condition = condition,
            onConditionChanged = onConditionChanged
        )
    }
}

/**
 * 高级条件选项
 */
@Composable
private fun AdvancedConditionOptions(
    condition: StepCondition,
    onConditionChanged: (StepCondition) -> Unit
) {
    var showAdvanced by remember { mutableStateOf(false) }
    
    Column {
        TextButton(
            onClick = { showAdvanced = !showAdvanced }
        ) {
            Text(
                text = if (showAdvanced) "隐藏高级选项" else "显示高级选项",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        if (showAdvanced) {
            // 超时时间
            OutlinedTextField(
                value = condition.timeout?.toString() ?: "",
                onValueChange = { 
                    val timeout = it.toIntOrNull()
                    onConditionChanged(condition.copy(timeout = timeout))
                },
                label = { Text("超时时间(秒)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("条件检查的最大等待时间") }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 重试次数
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("重试次数: ${condition.retryCount}")
                
                Row {
                    IconButton(
                        onClick = {
                            if (condition.retryCount > 0) {
                                onConditionChanged(condition.copy(retryCount = condition.retryCount - 1))
                            }
                        },
                        enabled = condition.retryCount > 0
                    ) {
                        Text("-")
                    }
                    
                    Text(
                        text = condition.retryCount.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    IconButton(
                        onClick = {
                            onConditionChanged(condition.copy(retryCount = condition.retryCount + 1))
                        }
                    ) {
                        Text("+")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 重试间隔
            OutlinedTextField(
                value = condition.retryDelay?.toString() ?: "",
                onValueChange = { 
                    val delay = it.toLongOrNull()
                    onConditionChanged(condition.copy(retryDelay = delay))
                },
                label = { Text("重试间隔(毫秒)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("每次重试之间的等待时间") }
            )
        }
    }
}

/**
 * 条件预览组件
 */
@Composable
fun ConditionPreview(
    condition: StepCondition?,
    modifier: Modifier = Modifier
) {
    if (condition != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "执行条件",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val conditionText = buildString {
                    append("如果 ")
                    append(condition.target.ifEmpty { "目标" })
                    append(" ")
                    append(
                        when (condition.operator) {
                            "equals" -> "等于"
                            "contains" -> "包含"
                            "startsWith" -> "开始于"
                            "endsWith" -> "结束于"
                            "not_equals" -> "不等于"
                            "not_contains" -> "不包含"
                            else -> condition.operator
                        }
                    )
                    condition.expectedValue?.let { 
                        append(" '$it'") 
                    }
                    append("，则")
                    append(condition.onTrue ?: "继续执行")
                    append("，否则")
                    append(condition.onFalse ?: "跳过")
                }
                
                Text(
                    text = conditionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
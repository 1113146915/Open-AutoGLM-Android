package com.example.open_autoglm_android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.open_autoglm_android.data.model.StepParameters
import com.example.open_autoglm_android.data.model.WorkflowStep

/**
 * 简化版工作流编辑器
 * 只保留步骤名称和描述字段
 */
@Composable
fun StructuredWorkflowEditor(
    steps: List<WorkflowStep>,
    onStepsChanged: (List<WorkflowStep>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 步骤列表头部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "工作流步骤",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    val newStep = WorkflowStep(
                        id = "step_${System.currentTimeMillis()}",
                        name = "新步骤 ${steps.size + 1}",
                        description = "",
                        parameters = StepParameters()
                    )
                    onStepsChanged(steps + newStep)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加步骤")
            }
        }
        
        // 步骤列表
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, step ->
                SimpleStepEditorCard(
                    stepIndex = index + 1,
                    step = step,
                    onStepChanged = { updatedStep ->
                        val updatedSteps = steps.map { 
                            if (it.id == step.id) updatedStep else it 
                        }
                        onStepsChanged(updatedSteps)
                    },
                    onStepDeleted = {
                        onStepsChanged(steps.filter { it.id != step.id })
                    },
                    onAddStep = { insertIndex ->
                        val newStep = WorkflowStep(
                            id = "step_${System.currentTimeMillis()}",
                            name = "新步骤 ${steps.size + 1}",
                            description = "",
                            parameters = StepParameters()
                        )
                        val updatedSteps = steps.toMutableList()
                        updatedSteps.add(insertIndex, newStep)
                        onStepsChanged(updatedSteps)
                    }
                )
            }
        }
    }
}

/**
 * 简化版步骤编辑卡片
 * 只包含步骤名称、描述和基本操作
 */
@Composable
private fun SimpleStepEditorCard(
    stepIndex: Int,
    step: WorkflowStep,
    onStepChanged: (WorkflowStep) -> Unit,
    onStepDeleted: () -> Unit,
    onAddStep: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 步骤头部：包含操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "步骤 $stepIndex",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 启用开关
                    Switch(
                        checked = step.isEnabled,
                        onCheckedChange = { 
                            onStepChanged(step.copy(isEnabled = it)) 
                        },
                        modifier = Modifier.scale(0.8f)
                    )
                    
                    // 删除按钮
                    IconButton(
                        onClick = onStepDeleted,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除步骤",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 步骤名称
            OutlinedTextField(
                value = step.name,
                onValueChange = { onStepChanged(step.copy(name = it)) },
                label = { Text("步骤名称 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 步骤描述
            OutlinedTextField(
                value = step.description,
                onValueChange = { onStepChanged(step.copy(description = it)) },
                label = { Text("步骤描述") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 添加步骤按钮
            Button(
                onClick = { 
                    onAddStep(stepIndex) // 在当前步骤后插入新步骤
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "添加步骤",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
package com.example.open_autoglm_android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 标签管理器
 * 支持添加、删除和选择标签
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManager(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "添加标签..."
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // 标签列表
        if (tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                items(tags) { tag ->
                    TagChip(
                        tag = tag,
                        onTagRemoved = {
                            onTagsChanged(tags.filter { it != tag })
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 添加标签按钮
        OutlinedButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(placeholder)
        }
    }
    
    // 添加标签对话框
    if (showAddDialog) {
        AddTagDialog(
            existingTags = tags,
            onTagAdded = { newTag ->
                onTagsChanged(tags + newTag)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

/**
 * 标签芯片组件
 */
@Composable
private fun TagChip(
    tag: String,
    onTagRemoved: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onTagRemoved,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "删除标签",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * 添加标签对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTagDialog(
    existingTags: List<String>,
    onTagAdded: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var tagText by remember { mutableStateOf("") }
    val suggestedTags = remember {
        listOf(
            "日常任务", "工作流程", "自动化", "考勤打卡", "应用启动",
            "数据处理", "文件操作", "网络操作", "系统设置", "测试脚本"
        ).filterNot { it in existingTags }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加标签") },
        text = {
            Column {
                // 输入框
                OutlinedTextField(
                    value = tagText,
                    onValueChange = { tagText = it },
                    label = { Text("标签名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 推荐标签
                if (suggestedTags.isNotEmpty()) {
                    Text(
                        text = "推荐标签：",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suggestedTags) { suggestedTag ->
                            FilterChip(
                                selected = false,
                                onClick = { 
                                    tagText = suggestedTag 
                                },
                                label = { 
                                    Text(
                                        text = suggestedTag,
                                        fontSize = 12.sp
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (tagText.isNotBlank() && tagText !in existingTags) {
                        onTagAdded(tagText.trim())
                    }
                },
                enabled = tagText.isNotBlank() && tagText !in existingTags
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 标签选择器（用于筛选）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelector(
    availableTags: List<String>,
    selectedTags: List<String>,
    onTagsSelected: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    maxSelection: Int = 3
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(availableTags) { tag ->
            FilterChip(
                selected = tag in selectedTags,
                onClick = {
                    if (tag in selectedTags) {
                        onTagsSelected(selectedTags - tag)
                    } else if (selectedTags.size < maxSelection) {
                        onTagsSelected(selectedTags + tag)
                    }
                },
                label = { 
                    Text(
                        text = tag,
                        fontSize = 12.sp
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * 标签显示组件（只读）
 */
@Composable
fun TagDisplay(
    tags: List<String>,
    modifier: Modifier = Modifier,
    maxTags: Int = 3
) {
    if (tags.isNotEmpty()) {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tags.take(maxTags)) { tag ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 如果标签数量超过显示限制，显示数量提示
            if (tags.size > maxTags) {
                item {
                    Text(
                        text = "+${tags.size - maxTags}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
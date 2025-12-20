package com.example.open_autoglm_android.data.model

/**
 * 工作流数据模型
 * 包含工作流的标题和步骤描述
 */
data class Workflow(
    val id: String,
    val title: String,
    val steps: String,
    val createdTime: Long = System.currentTimeMillis(),
    val updatedTime: Long = System.currentTimeMillis()
)
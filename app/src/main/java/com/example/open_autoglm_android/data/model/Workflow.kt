package com.example.open_autoglm_android.data.model

/**
 * 工作流步骤条件
 */
data class StepCondition(
    val type: String,           // 条件类型：element_exists, text_contains, app_active等
    val target: String,          // 检查目标：元素坐标、文本内容、应用名等
    val operator: String = "equals", // 操作符：equals, contains, not_equals等
    val expectedValue: String = "",  // 期望值
    val onTrue: String = "",    // 条件满足时执行的动作
    val onFalse: String = "",   // 条件不满足时执行的动作
    val timeout: Int? = null,    // 超时时间（秒）
    val retryCount: Int = 0,    // 重试次数
    val retryDelay: Long? = null // 重试间隔（毫秒）
)

/**
 * 工作流步骤参数
 * 简化版本，只保留必要的基础参数
 */
data class StepParameters(
    val waitTime: Int = 1000        // 操作后等待时间（毫秒）
)

/**
 * 工作流步骤
 */
data class WorkflowStep(
    val id: String,
    val name: String,               // 步骤名称
    val description: String,         // 步骤描述
    val parameters: StepParameters, // 步骤参数
    val condition: StepCondition? = null,    // 执行条件
    val onSuccess: String? = null,   // 成功后跳转的步骤ID
    val onFailure: String? = null,   // 失败后跳转的步骤ID
    val isOptional: Boolean = false, // 是否为可选步骤
    val isEnabled: Boolean = true    // 是否启用
)

/**
 * 工作流数据模型
 * 支持结构化步骤和条件分支
 */
data class Workflow(
    val id: String,
    val title: String,
    val description: String = "",    // 工作流描述
    val steps: List<WorkflowStep>,   // 结构化步骤列表
    val tags: List<String> = emptyList(), // 标签分类
    val createdTime: Long = System.currentTimeMillis(),
    val updatedTime: Long = System.currentTimeMillis()
) {
    /**
     * 生成传统格式的步骤描述（向后兼容）
     */
    fun generateStepsDescription(): String {
        return steps.joinToString("\n") { step ->
            "${step.name}${if (step.description.isNotEmpty()) " - ${step.description}" else ""}"
        }
    }
    
    /**
     * 从传统格式创建工作流（兼容性）
     */
    companion object {
        fun fromTraditionalFormat(id: String, title: String, stepsText: String): Workflow {
            val steps = stepsText.split("\n").mapIndexed { index, line ->
                WorkflowStep(
                    id = "step_${index + 1}",
                    name = "步骤${index + 1}",
                    description = line,
                    parameters = StepParameters(),
                    condition = null
                )
            }
            return Workflow(id, title, steps = steps)
        }
    }
}

/**
 * 工作流模板
 */
data class WorkflowTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val steps: List<WorkflowStep>,
    val tags: List<String> = emptyList()
)
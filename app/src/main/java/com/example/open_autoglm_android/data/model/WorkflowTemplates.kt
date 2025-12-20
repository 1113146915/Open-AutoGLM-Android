package com.example.open_autoglm_android.data.model

/**
 * 工作流模板预设
 * 包含常用的工作流模板，便于快速创建工作流
 * 简化版本，只保留步骤名称和描述
 */
object WorkflowTemplates {
    
    /**
     * 考勤打卡工作流模板
     * 基于用户提供的实际需求
     */
    val ATTENDANCE_WORKFLOW = WorkflowTemplate(
        id = "attendance_check",
        name = "考勤打卡流程",
        description = "完整的考勤打卡流程，包括虚拟定位、虚拟机操作和灯塔打卡",
        category = "考勤管理",
        tags = listOf("考勤", "打卡", "虚拟定位", "灯塔"),
        steps = listOf(
            WorkflowStep(
                id = "step_1",
                name = "打开虚拟定位",
                description = "打开虚拟定位应用",
                parameters = StepParameters(waitTime = 2000)
            ),
            WorkflowStep(
                id = "step_2",
                name = "点击历史位置",
                description = "点击历史位置选项",
                parameters = StepParameters(waitTime = 1000)
            ),
            WorkflowStep(
                id = "step_3",
                name = "选择金地威新",
                description = "点击历史位置下的金地威新",
                parameters = StepParameters(waitTime = 1000)
            ),
            WorkflowStep(
                id = "step_4",
                name = "启动模拟",
                description = "点击启动模拟，如果看到停止模拟则不操作直接进入下一步",
                parameters = StepParameters(waitTime = 2000),
                condition = StepCondition(
                    type = "text_contains",
                    target = "停止模拟",
                    operator = "not_contains"
                ),
                onFailure = "step_5" // 如果看到停止模拟，直接跳到下一步
            ),
            WorkflowStep(
                id = "step_5",
                name = "忽略提示",
                description = "忽略任何提示信息",
                parameters = StepParameters(waitTime = 2000)
            ),
            WorkflowStep(
                id = "step_6",
                name = "返回桌面",
                description = "返回到桌面",
                parameters = StepParameters(waitTime = 1000)
            ),
            WorkflowStep(
                id = "step_7",
                name = "打开虚拟机",
                description = "打开虚拟机应用",
                parameters = StepParameters(waitTime = 3000)
            ),
            WorkflowStep(
                id = "step_8",
                name = "进入第一个虚拟机",
                description = "进入第一个虚拟机，必须先点击一次虚拟机",
                parameters = StepParameters(waitTime = 2000)
            ),
            WorkflowStep(
                id = "step_9",
                name = "关闭开窗广告",
                description = "关闭开窗广告，关闭按钮一般在方形图片的右上角",
                parameters = StepParameters(waitTime = 1000),
                condition = StepCondition(
                    type = "element_exists",
                    target = "关闭按钮",
                    operator = "exists"
                ),
                isOptional = true // 可选步骤，如果没有广告则跳过
            ),
            WorkflowStep(
                id = "step_10",
                name = "检查虚拟机状态",
                description = "检查界面下方是否有虚拟机三个字的按钮，如果有就强制点击一次虚拟机中间区域",
                parameters = StepParameters(),
                condition = StepCondition(
                    type = "text_contains",
                    target = "虚拟机",
                    operator = "contains"
                ),
                onSuccess = "step_10_1" // 如果有虚拟机按钮，点击中间区域
            ),
            WorkflowStep(
                id = "step_10_1",
                name = "点击虚拟机中间",
                description = "点击虚拟机中间区域",
                parameters = StepParameters(waitTime = 1000),
                onSuccess = "step_11" // 点击后继续下一步
            ),
            WorkflowStep(
                id = "step_11",
                name = "打开灯塔",
                description = "打开灯塔应用",
                parameters = StepParameters(waitTime = 3000)
            ),
            WorkflowStep(
                id = "step_12",
                name = "点击工作台",
                description = "点击工作台选项",
                parameters = StepParameters(waitTime = 2000)
            ),
            WorkflowStep(
                id = "step_13",
                name = "外员考勤",
                description = "点击外员考勤",
                parameters = StepParameters(waitTime = 2000)
            ),
            WorkflowStep(
                id = "step_14",
                name = "查看考勤范围",
                description = "查看考勤范围，如果在考勤范围内则点击下方打卡按钮",
                parameters = StepParameters(),
                condition = StepCondition(
                    type = "text_contains",
                    target = "考勤范围内",
                    operator = "contains"
                ),
                onSuccess = "step_15", // 在范围内，去打卡
                onFailure = "step_16"  // 不在范围内，重试
            ),
            WorkflowStep(
                id = "step_15",
                name = "打卡",
                description = "点击下方打卡按钮，忽略点击打卡按钮后的任何提示信息。注意即时看到已经打卡也要重新打卡。不要因为任何原因不点击打卡。",
                parameters = StepParameters(waitTime = 2000)
            ),
            WorkflowStep(
                id = "step_16",
                name = "返回工作台",
                description = "返回到工作台重新操作一次",
                parameters = StepParameters(waitTime = 1000),
                onSuccess = "step_12" // 返回后重新从工作台开始
            ),
            WorkflowStep(
                id = "step_17",
                name = "任务结束",
                description = "重新操作一次仍然不在范围内则任务结束",
                parameters = StepParameters()
            )
        )
    )
    
    /**
     * 应用启动流程模板
     */
    val APP_LAUNCH_TEMPLATE = WorkflowTemplate(
        id = "app_launch",
        name = "应用启动流程",
        description = "标准的应用启动和导航流程",
        category = "应用操作",
        tags = listOf("启动", "导航", "基础操作"),
        steps = listOf(
            WorkflowStep(
                id = "step_1",
                name = "启动应用",
                description = "启动目标应用",
                parameters = StepParameters(waitTime = 2000)
            ),
            WorkflowStep(
                id = "step_2",
                name = "检查应用状态",
                description = "检查应用是否正确启动",
                parameters = StepParameters(),
                condition = StepCondition(
                    type = "app_active",
                    target = "目标应用",
                    operator = "equals"
                ),
                onSuccess = "step_3",
                onFailure = "step_1" // 重试启动
            ),
            WorkflowStep(
                id = "step_3",
                name = "完成启动",
                description = "应用启动完成",
                parameters = StepParameters()
            )
        )
    )
    
    /**
     * 获取所有模板
     */
    fun getAllTemplates(): List<WorkflowTemplate> {
        return listOf(ATTENDANCE_WORKFLOW, APP_LAUNCH_TEMPLATE)
    }
    
    /**
     * 根据分类获取模板
     */
    fun getTemplatesByCategory(category: String): List<WorkflowTemplate> {
        return getAllTemplates().filter { it.category == category }
    }
    
    /**
     * 根据标签搜索模板
     */
    fun searchTemplatesByTag(tag: String): List<WorkflowTemplate> {
        return getAllTemplates().filter { 
            it.tags.any { t -> t.contains(tag, ignoreCase = true) } 
        }
    }
    
    /**
     * 根据ID获取模板
     */
    fun getTemplateById(id: String): WorkflowTemplate? {
        return getAllTemplates().find { it.id == id }
    }
}
<template>
  <div class="page-stack">
    <section class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <component :is="item.icon" class="metric-icon" />
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.trend }}</small>
      </div>
    </section>

    <section class="work-surface">
      <div class="section-title">
        <h2>待处理工单</h2>
        <el-button type="primary" :icon="Plus">新建工单</el-button>
      </div>
      <el-table :data="tickets" height="360">
        <el-table-column prop="no" label="工单编号" width="140" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="priority" label="优先级" width="110">
          <template #default="{ row }">
            <el-tag :type="row.priorityType">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="assignee" label="处理人" width="120" />
        <el-table-column prop="deadline" label="SLA 截止" width="180" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { Clock, Finished, Plus, Tickets, Warning } from '@element-plus/icons-vue'

const metrics = [
  { label: '今日新增', value: '18', trend: '较昨日 +4', icon: Tickets },
  { label: '处理中', value: '42', trend: '12 个即将超时', icon: Clock },
  { label: '已关闭', value: '126', trend: '本周累计', icon: Finished },
  { label: '超时提醒', value: '5', trend: '需主管关注', icon: Warning }
]

const tickets = [
  { no: 'TF20260630001', title: '研发区网络间歇性中断', priority: '紧急', priorityType: 'danger', status: '待接单', assignee: '李运维', deadline: '2026-06-30 11:30' },
  { no: 'TF20260630002', title: '新员工账号权限开通', priority: '中', priorityType: 'warning', status: '处理中', assignee: '周工程师', deadline: '2026-06-30 18:00' },
  { no: 'TF20260630003', title: '会议室投屏设备无信号', priority: '低', priorityType: 'info', status: '待确认', assignee: '王工', deadline: '2026-07-01 12:00' }
]
</script>

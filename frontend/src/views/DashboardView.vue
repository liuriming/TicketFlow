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
        <el-button type="primary" :icon="Plus" @click="$router.push('/tickets/list')">新建工单</el-button>
      </div>
      <el-table v-loading="loading" :data="tickets" height="360">
        <el-table-column prop="ticketNo" label="工单编号" width="160" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column label="优先级" width="110">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column label="处理人" width="120">
          <template #default="{ row }">{{ row.assigneeName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="resolveDeadline" label="SLA 截止" width="180" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Clock, Finished, Plus, Tickets, Warning } from '@element-plus/icons-vue'
import { getDashboardSummary } from '../api/report'
import { pageTickets } from '../api/ticket'

const loading = ref(false)
const summary = ref({
  todayCreatedCount: 0,
  processingCount: 0,
  closedCount: 0,
  overdueCount: 0
})
const tickets = ref([])

const metrics = computed(() => [
  { label: '今日新增', value: summary.value.todayCreatedCount, trend: '当前数据范围', icon: Tickets },
  { label: '待处理', value: summary.value.processingCount, trend: '待分派/待接单/处理中/待确认', icon: Clock },
  { label: '已关闭', value: summary.value.closedCount, trend: '累计关闭', icon: Finished },
  { label: '超时提醒', value: summary.value.overdueCount, trend: '按处理截止统计', icon: Warning }
])

const statusMap = {
  PENDING_ASSIGN: '待分派',
  PENDING_ACCEPT: '待接单',
  PROCESSING: '处理中',
  PENDING_CONFIRM: '待确认',
  CLOSED: '已关闭',
  REJECTED: '已驳回',
  CANCELED: '已取消'
}

const priorityMap = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  URGENT: '紧急'
}

onMounted(async () => {
  loading.value = true
  try {
    const [summaryData, ticketPage] = await Promise.all([
      getDashboardSummary(),
      pageTickets({ pageNo: 1, pageSize: 8 })
    ])
    summary.value = summaryData
    tickets.value = ticketPage.records || []
  } finally {
    loading.value = false
  }
})

function statusText(value) {
  return statusMap[value] || value || '-'
}

function priorityText(value) {
  return priorityMap[value] || value || '-'
}

function priorityType(value) {
  if (value === 'URGENT') return 'danger'
  if (value === 'HIGH') return 'warning'
  if (value === 'LOW') return 'info'
  return 'success'
}
</script>

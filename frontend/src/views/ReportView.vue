<template>
  <div class="page-stack">
    <section class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <component :is="item.icon" class="metric-icon" />
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.sub }}</small>
      </div>
    </section>
    <section class="work-surface split-surface">
      <div>
        <h2>分类分布</h2>
        <div class="bar-list">
          <div v-for="item in categoryBars" :key="item.name">
            <span>{{ item.name }}</span>
            <el-progress :percentage="item.value" :stroke-width="12" />
          </div>
        </div>
      </div>
      <div>
        <h2>人员工作量</h2>
        <el-table v-loading="loading" :data="workload" height="300">
          <el-table-column prop="assigneeId" label="处理人 ID" />
          <el-table-column prop="processingCount" label="处理中" />
          <el-table-column prop="closedCount" label="已关闭" />
          <el-table-column prop="overdueCount" label="超时" />
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Histogram, Stopwatch, Tickets, TrendCharts } from '@element-plus/icons-vue'
import { getReportOverview, getWorkload } from '../api/report'

const loading = ref(false)
const overview = ref({
  ticketCount: 0,
  overdueRate: 0,
  averageResolveHours: 0,
  activeAssigneeCount: 0
})
const workload = ref([])

const metrics = computed(() => [
  { label: '工单总数', value: overview.value.ticketCount, sub: '当前数据范围', icon: Tickets },
  { label: '超时率', value: `${overview.value.overdueRate}%`, sub: '按处理截止统计', icon: Stopwatch },
  { label: '平均处理', value: `${overview.value.averageResolveHours}h`, sub: '关闭工单平均值', icon: Histogram },
  { label: '活跃处理人', value: overview.value.activeAssigneeCount, sub: '已分派工单人员', icon: TrendCharts }
])

const categoryBars = [
  { name: '网络故障', value: 42 },
  { name: '账号权限', value: 28 },
  { name: '硬件维修', value: 18 },
  { name: '服务器问题', value: 12 }
]

onMounted(async () => {
  loading.value = true
  try {
    overview.value = await getReportOverview()
    workload.value = await getWorkload()
  } finally {
    loading.value = false
  }
})
</script>

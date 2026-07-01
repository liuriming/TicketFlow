<template>
  <section class="work-surface audit-log">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索请求地址或操作人" clearable @keyup.enter="loadLogs" />
      <el-button :icon="Search" @click="loadLogs">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="logs" height="560">
      <el-table-column prop="operatorName" label="操作人" width="140">
        <template #default="{ row }">{{ row.operatorName || `用户 ${row.operatorId || '-'}` }}</template>
      </el-table-column>
      <el-table-column label="方法" width="90">
        <template #default="{ row }">
          <el-tag :type="methodType(row.requestMethod)">{{ row.requestMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUri" label="请求地址" min-width="260" show-overflow-tooltip />
      <el-table-column prop="queryString" label="查询参数" min-width="180" show-overflow-tooltip />
      <el-table-column prop="clientIp" label="客户端 IP" width="140" />
      <el-table-column label="结果" width="90">
        <template #default="{ row }">
          <el-tag :type="row.success === 1 ? 'success' : 'danger'">{{ row.success === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="耗时" width="100">
        <template #default="{ row }">{{ row.durationMs || 0 }} ms</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="操作时间" width="180" />
    </el-table>

    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadLogs"
        @current-change="loadLogs"
      />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { pageOperationLogs } from '../api/audit'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: ''
})

onMounted(loadLogs)

async function loadLogs() {
  loading.value = true
  try {
    const result = await pageOperationLogs({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined
    })
    logs.value = result.records || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

function methodType(method) {
  if (method === 'DELETE') return 'danger'
  if (method === 'PUT') return 'warning'
  return 'primary'
}
</script>

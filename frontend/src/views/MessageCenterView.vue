<template>
  <section class="work-surface message-center">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索标题或内容" clearable @keyup.enter="loadMessages" />
      <el-select v-model="query.readFlag" placeholder="已读状态" clearable>
        <el-option label="未读" :value="0" />
        <el-option label="已读" :value="1" />
      </el-select>
      <el-select v-model="query.businessType" placeholder="消息类型" clearable>
        <el-option label="工单通知" value="TICKET" />
        <el-option label="SLA 提醒" value="SLA" />
      </el-select>
      <el-button :icon="Search" @click="loadMessages">查询</el-button>
      <el-button type="primary" :icon="Check" @click="markAllRead">全部已读</el-button>
    </div>

    <el-table v-loading="loading" :data="messages" height="560">
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.readFlag === 0 ? 'warning' : 'info'">{{ row.readFlag === 0 ? '未读' : '已读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="级别" width="90">
        <template #default="{ row }">
          <el-tag :type="levelType(row.level)">{{ row.level || 'INFO' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="businessType" label="业务类型" width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="170">
        <template #default="{ row }">
          <el-button v-if="row.readFlag === 0" link type="primary" @click="markRead(row)">已读</el-button>
          <el-button v-if="canOpenTicket(row)" link type="primary" @click="openTicket(row)">工单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadMessages"
        @current-change="loadMessages"
      />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, Search } from '@element-plus/icons-vue'
import { markAllMessagesRead, markMessageRead, pageMessages } from '../api/message'

const router = useRouter()
const loading = ref(false)
const messages = ref([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  readFlag: '',
  businessType: ''
})

onMounted(loadMessages)

async function loadMessages() {
  loading.value = true
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      readFlag: query.readFlag === '' ? undefined : query.readFlag,
      businessType: normalizeBusinessType(query.businessType)
    }
    const result = await pageMessages(params)
    messages.value = result.records || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

async function markRead(row) {
  await markMessageRead(row.id)
  row.readFlag = 1
  ElMessage.success('消息已读')
}

async function markAllRead() {
  await markAllMessagesRead({
    keyword: query.keyword || undefined,
    readFlag: query.readFlag === '' ? undefined : query.readFlag,
    businessType: normalizeBusinessType(query.businessType)
  })
  ElMessage.success('消息已全部标记为已读')
  await loadMessages()
}

function openTicket(row) {
  router.push({ path: '/tickets/list', query: { ticketId: row.businessId } })
}

function canOpenTicket(row) {
  return row.businessId && (row.businessType?.startsWith('TICKET') || row.businessType?.startsWith('SLA'))
}

function levelType(level) {
  if (level === 'ERROR') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'info'
}

function normalizeBusinessType(value) {
  if (!value) return undefined
  return value
}
</script>

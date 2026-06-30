<template>
  <section class="work-surface">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索工单编号、标题" clearable @keyup.enter="loadTickets" />
      <el-select v-model="status" placeholder="状态" clearable>
        <el-option label="待分派" value="PENDING_ASSIGN" />
        <el-option label="待接单" value="PENDING_ACCEPT" />
        <el-option label="处理中" value="PROCESSING" />
        <el-option label="待确认" value="PENDING_CONFIRM" />
      </el-select>
      <el-button :icon="Search" @click="loadTickets">查询</el-button>
      <el-button type="primary" :icon="Plus" @click="createDialogVisible = true">创建工单</el-button>
    </div>
    <el-table v-loading="loading" :data="tickets" height="520">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="ticketNo" label="工单编号" width="170" />
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column label="分类" width="130">
        <template #default="{ row }">{{ row.categoryName || `分类 ${row.categoryId}` }}</template>
      </el-table-column>
      <el-table-column label="优先级" width="110">
        <template #default="{ row }">
          <el-tag :type="priorityType(row.priority)">{{ priorityText(row.priority) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="提交人" width="120">
        <template #default="{ row }">{{ row.creatorName || `用户 ${row.creatorId}` }}</template>
      </el-table-column>
      <el-table-column label="处理人" width="120">
        <template #default="{ row }">{{ row.assigneeName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="resolveDeadline" label="处理截止" width="180" />
      <el-table-column label="操作" fixed="right" width="360">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="hasAction(row, 'ACCEPT')" link type="primary" @click="quickAction(row, 'accept')">接单</el-button>
          <el-button v-if="hasAction(row, 'PROCESS')" link type="primary" @click="openProcess(row)">处理</el-button>
          <el-button v-if="hasAction(row, 'TRANSFER')" link type="primary" @click="openTransfer(row)">转派</el-button>
          <el-button v-if="hasAction(row, 'CONFIRM_CLOSE')" link type="success" @click="quickAction(row, 'close')">关闭</el-button>
          <el-button v-if="hasAction(row, 'REJECT')" link type="warning" @click="quickAction(row, 'reject')">驳回</el-button>
          <el-button v-if="hasAction(row, 'CANCEL')" link type="danger" @click="quickAction(row, 'cancel')">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-row">
      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadTickets"
        @current-change="loadTickets"
      />
    </div>

    <el-dialog v-model="createDialogVisible" title="创建工单" width="560px">
      <el-form :model="createForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="请输入工单标题" />
        </el-form-item>
        <el-form-item label="工单分类">
          <el-select v-model="createForm.categoryId" filterable>
            <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="createForm.priority">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="5" placeholder="请描述问题现象、影响范围和期望结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="processDialogVisible" title="提交处理结果" width="520px">
      <el-input v-model="processResult" type="textarea" :rows="5" placeholder="请输入处理结果" />
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitProcess">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transferDialogVisible" title="转派工单" width="520px">
      <el-form :model="transferForm" label-width="90px">
        <el-form-item label="处理人">
          <el-select v-model="transferForm.assigneeId" filterable>
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.realName}（${user.deptName || '未分配部门'}）`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转派原因">
          <el-input v-model="transferForm.reason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTransfer">提交</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" :title="detailTitle" size="620px" @closed="resetDetail">
      <div v-loading="detailLoading" class="ticket-detail" v-if="ticketDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单编号">{{ ticketDetail.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(ticketDetail.status) }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ ticketDetail.categoryName || `分类 ${ticketDetail.categoryId}` }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ priorityText(ticketDetail.priority) }}</el-descriptions-item>
          <el-descriptions-item label="提交人">{{ ticketDetail.creatorName || `用户 ${ticketDetail.creatorId}` }}</el-descriptions-item>
          <el-descriptions-item label="提交部门">{{ ticketDetail.creatorDeptName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ ticketDetail.assigneeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="响应截止">{{ ticketDetail.responseDeadline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理截止">{{ ticketDetail.resolveDeadline || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <h2>问题描述</h2>
          <p>{{ ticketDetail.description || '-' }}</p>
        </div>

        <div class="detail-block">
          <h2>流转记录</h2>
          <el-timeline>
            <el-timeline-item
              v-for="record in ticketDetail.flowRecords || []"
              :key="record.id"
              :timestamp="record.createdAt"
            >
              {{ statusText(record.fromStatus) }} → {{ statusText(record.toStatus) }}
              <small>{{ record.remark || '-' }}</small>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="detail-block">
          <h2>评论</h2>
          <div class="comment-list">
            <div v-for="comment in ticketDetail.comments || []" :key="comment.id" class="comment-item">
              <strong>用户 {{ comment.userId }}</strong>
              <span>{{ comment.createdAt }}</span>
              <p>{{ comment.content }}</p>
            </div>
            <el-empty v-if="!ticketDetail.comments?.length" description="暂无评论" :image-size="72" />
          </div>
          <el-input v-model="commentForm.content" type="textarea" :rows="3" placeholder="补充处理过程或沟通记录" />
          <div class="comment-actions">
            <el-checkbox v-model="commentForm.internalOnly">内部备注</el-checkbox>
            <el-button type="primary" :loading="submitting" @click="submitComment">发表评论</el-button>
          </div>
        </div>

        <div class="detail-block">
          <div class="detail-title-line">
            <h2>附件</h2>
            <el-upload v-if="hasDetailAction('UPLOAD_ATTACHMENT')" :http-request="handleAttachmentUpload" :show-file-list="false">
              <el-button :icon="Upload" size="small">上传附件</el-button>
            </el-upload>
          </div>
          <el-table :data="attachments" size="small">
            <el-table-column prop="originalName" label="文件名" min-width="180" />
            <el-table-column label="大小" width="100">
              <template #default="{ row }">{{ fileSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上传时间" width="170" />
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
                <el-button link type="danger" @click="removeFile(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload } from '@element-plus/icons-vue'
import { deleteAttachment, downloadAttachment, listAttachments, uploadAttachment } from '../api/attachment'
import { useRoute } from 'vue-router'
import { listCategories } from '../api/rule'
import { listUserOptions } from '../api/system'
import {
  acceptTicket,
  addTicketComment,
  cancelTicket,
  confirmCloseTicket,
  createTicket,
  getTicketDetail,
  pageTickets,
  processTicket,
  rejectTicket,
  transferTicket
} from '../api/ticket'

const route = useRoute()
const status = ref('')
const tickets = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const currentTicket = ref(null)
const ticketDetail = ref(null)
const detailVisible = ref(false)
const detailLoading = ref(false)
const attachments = ref([])
const createDialogVisible = ref(false)
const processDialogVisible = ref(false)
const transferDialogVisible = ref(false)
const processResult = ref('')
const detailTitle = computed(() => ticketDetail.value ? `${ticketDetail.value.ticketNo} · ${ticketDetail.value.title}` : '工单详情')
const categories = ref([])
const userOptions = ref([])

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: ''
})

const createForm = reactive({
  title: '',
  description: '',
  categoryId: 1,
  priority: 'MEDIUM'
})

const transferForm = reactive({
  assigneeId: 1,
  reason: ''
})

const commentForm = reactive({
  content: '',
  internalOnly: false
})

const statusMap = {
  CREATED: '已创建',
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

watch(status, () => {
  query.pageNo = 1
  loadTickets()
})

watch(
  () => route.query.ticketId,
  async (ticketId) => {
    if (ticketId) {
      await openDetailById(ticketId)
    }
  }
)

onMounted(async () => {
  await loadOptions()
  await loadTickets()
  if (route.query.ticketId) {
    await openDetailById(route.query.ticketId)
  }
})

async function loadOptions() {
  const [categoryRows, userRows] = await Promise.all([
    listCategories().catch(() => []),
    listUserOptions().catch(() => [])
  ])
  categories.value = categoryRows || []
  userOptions.value = userRows || []
  if (categories.value.length && !createForm.categoryId) {
    createForm.categoryId = categories.value[0].id
  }
}

async function loadTickets() {
  loading.value = true
  try {
    const result = await pageTickets({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: status.value || undefined,
      keyword: query.keyword || undefined
    })
    tickets.value = result.records || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  submitting.value = true
  try {
    await createTicket(createForm)
    ElMessage.success('工单已创建')
    createDialogVisible.value = false
    await loadTickets()
  } finally {
    submitting.value = false
  }
}

function openProcess(row) {
  currentTicket.value = row
  processResult.value = ''
  processDialogVisible.value = true
}

async function submitProcess() {
  submitting.value = true
  try {
    await processTicket(currentTicket.value.id, processResult.value)
    ElMessage.success('处理结果已提交')
    processDialogVisible.value = false
    await loadTickets()
  } finally {
    submitting.value = false
  }
}

function openTransfer(row) {
  currentTicket.value = row
  transferForm.assigneeId = row.assigneeId || 1
  transferForm.reason = ''
  transferDialogVisible.value = true
}

async function submitTransfer() {
  submitting.value = true
  try {
    await transferTicket(currentTicket.value.id, { ...transferForm })
    ElMessage.success('工单已转派')
    transferDialogVisible.value = false
    await loadTickets()
  } finally {
    submitting.value = false
  }
}

async function quickAction(row, action) {
  const handlers = {
    accept: () => acceptTicket(row.id),
    close: (remark) => confirmCloseTicket(row.id, remark),
    reject: (remark) => rejectTicket(row.id, remark),
    cancel: (remark) => cancelTicket(row.id, remark)
  }
  const promptActions = ['close', 'reject', 'cancel']
  let remark = ''
  if (promptActions.includes(action)) {
    const result = await ElMessageBox.prompt('请输入操作说明', '工单操作', {
      confirmButtonText: '提交',
      cancelButtonText: '取消'
    }).catch(() => null)
    if (!result) return
    remark = result.value
  }
  await handlers[action](remark)
  ElMessage.success('操作已提交')
  await loadTickets()
}

async function openDetail(row) {
  currentTicket.value = row
  detailVisible.value = true
  await refreshDetail(row.id)
}

async function openDetailById(id) {
  currentTicket.value = { id: Number(id) }
  detailVisible.value = true
  await refreshDetail(Number(id))
}

async function refreshDetail(id = currentTicket.value?.id) {
  if (!id) return
  detailLoading.value = true
  try {
    ticketDetail.value = await getTicketDetail(id)
    await loadAttachments(id)
  } finally {
    detailLoading.value = false
  }
}

async function loadAttachments(ticketId) {
  attachments.value = await listAttachments({ businessType: 'TICKET', businessId: ticketId })
}

async function submitComment() {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    ticketDetail.value = await addTicketComment(ticketDetail.value.id, {
      content: commentForm.content,
      internalOnly: commentForm.internalOnly
    })
    commentForm.content = ''
    commentForm.internalOnly = false
    ElMessage.success('评论已发布')
  } finally {
    submitting.value = false
  }
}

async function handleAttachmentUpload(option) {
  try {
    await uploadAttachment(option.file, 'TICKET', ticketDetail.value.id)
    option.onSuccess()
    ElMessage.success('附件已上传')
    await loadAttachments(ticketDetail.value.id)
  } catch (error) {
    option.onError(error)
  }
}

async function downloadFile(file) {
  const blob = await downloadAttachment(file.id)
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = file.originalName || 'attachment'
  link.click()
  URL.revokeObjectURL(url)
}

async function removeFile(file) {
  const confirmed = await ElMessageBox.confirm(`确认删除附件“${file.originalName}”？`, '删除附件', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(() => true).catch(() => false)
  if (!confirmed) return
  await deleteAttachment(file.id)
  ElMessage.success('附件已删除')
  await loadAttachments(ticketDetail.value.id)
}

function resetDetail() {
  ticketDetail.value = null
  attachments.value = []
  commentForm.content = ''
  commentForm.internalOnly = false
}

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

function hasAction(row, action) {
  return (row.allowedActions || []).includes(action)
}

function hasDetailAction(action) {
  return (ticketDetail.value?.allowedActions || []).includes(action)
}

function fileSize(value) {
  if (!value) return '0 B'
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}
</script>

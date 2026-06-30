<template>
  <section class="work-surface">
    <div class="toolbar">
      <el-segmented v-model="mode" :options="['工单分类', '派单规则', 'SLA 规则']" />
      <el-button type="primary" :icon="Plus" @click="openCreate">新增</el-button>
    </div>
    <el-table v-if="mode === '工单分类'" v-loading="loading" :data="categories" height="520">
      <el-table-column prop="categoryName" label="分类名称" min-width="180" />
      <el-table-column prop="categoryCode" label="分类编码" width="160" />
      <el-table-column prop="parentId" label="父分类" width="120" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="操作" fixed="right" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else-if="mode === '派单规则'" v-loading="loading" :data="dispatchRules" height="520">
      <el-table-column prop="categoryId" label="分类 ID" width="120" />
      <el-table-column prop="deptId" label="部门 ID" width="120" />
      <el-table-column prop="skillCode" label="技能/范围" min-width="160" />
      <el-table-column prop="assigneeId" label="处理人 ID" width="130" />
      <el-table-column prop="priority" label="优先级" width="120" />
      <el-table-column prop="enabled" label="启用" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled === 1" disabled />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else v-loading="loading" :data="slaRules" height="520">
      <el-table-column prop="priority" label="优先级" width="150" />
      <el-table-column prop="responseMinutes" label="响应时限（分钟）" width="180" />
      <el-table-column prop="resolveMinutes" label="处理时限（分钟）" width="180" />
      <el-table-column prop="enabled" label="启用" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled === 1" disabled />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form v-if="mode === '工单分类'" :model="categoryForm" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.categoryName" />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="categoryForm.categoryCode" />
        </el-form-item>
        <el-form-item label="父分类 ID">
          <el-input-number v-model="categoryForm.parentId" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <el-form v-else-if="mode === '派单规则'" :model="dispatchForm" label-width="110px">
        <el-form-item label="分类 ID">
          <el-input-number v-model="dispatchForm.categoryId" :min="1" />
        </el-form-item>
        <el-form-item label="部门 ID">
          <el-input-number v-model="dispatchForm.deptId" :min="1" />
        </el-form-item>
        <el-form-item label="技能编码">
          <el-input v-model="dispatchForm.skillCode" />
        </el-form-item>
        <el-form-item label="处理人 ID">
          <el-input-number v-model="dispatchForm.assigneeId" :min="1" />
        </el-form-item>
        <el-form-item label="规则优先级">
          <el-input-number v-model="dispatchForm.priority" :min="0" />
        </el-form-item>
      </el-form>
      <el-form v-else :model="slaForm" label-width="130px">
        <el-form-item label="优先级">
          <el-select v-model="slaForm.priority">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="响应时限">
          <el-input-number v-model="slaForm.responseMinutes" :min="1" />
        </el-form-item>
        <el-form-item label="处理时限">
          <el-input-number v-model="slaForm.resolveMinutes" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import {
  listCategories,
  listDispatchRules,
  listSlaRules,
  saveCategory,
  saveDispatchRule,
  saveSlaRule
} from '../api/rule'

const route = useRoute()
const mode = ref(resolveMode(route.path))
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const categories = ref([])
const dispatchRules = ref([])
const slaRules = ref([])

const categoryForm = reactive({
  parentId: 0,
  categoryName: '',
  categoryCode: '',
  sortOrder: 0
})

const dispatchForm = reactive({
  categoryId: 1,
  deptId: 2,
  skillCode: 'NETWORK',
  assigneeId: 1,
  priority: 10
})

const slaForm = reactive({
  priority: 'MEDIUM',
  responseMinutes: 120,
  resolveMinutes: 1440
})

const dialogTitle = computed(() => `${editingId.value ? '编辑' : '新增'}${mode.value}`)

watch(mode, () => {
  dialogVisible.value = false
  load()
})

watch(
  () => route.path,
  (path) => {
    const nextMode = resolveMode(path)
    if (nextMode !== mode.value) {
      mode.value = nextMode
    }
  }
)

onMounted(load)

async function load() {
  loading.value = true
  try {
    if (mode.value === '工单分类') {
      categories.value = await listCategories()
    } else if (mode.value === '派单规则') {
      dispatchRules.value = await listDispatchRules()
    } else {
      slaRules.value = await listSlaRules()
    }
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  if (mode.value === '工单分类') {
    Object.assign(categoryForm, {
      parentId: row.parentId ?? 0,
      categoryName: row.categoryName,
      categoryCode: row.categoryCode,
      sortOrder: row.sortOrder ?? 0
    })
  } else if (mode.value === '派单规则') {
    Object.assign(dispatchForm, {
      categoryId: row.categoryId,
      deptId: row.deptId,
      skillCode: row.skillCode,
      assigneeId: row.assigneeId,
      priority: row.priority ?? 0
    })
  } else {
    Object.assign(slaForm, {
      priority: row.priority,
      responseMinutes: row.responseMinutes,
      resolveMinutes: row.resolveMinutes
    })
  }
  dialogVisible.value = true
}

async function submit() {
  submitting.value = true
  try {
    if (mode.value === '工单分类') {
      await saveCategory({ ...categoryForm }, editingId.value)
    } else if (mode.value === '派单规则') {
      await saveDispatchRule({ ...dispatchForm }, editingId.value)
    } else {
      await saveSlaRule({ ...slaForm }, editingId.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  Object.assign(categoryForm, { parentId: 0, categoryName: '', categoryCode: '', sortOrder: 0 })
  Object.assign(dispatchForm, { categoryId: 1, deptId: 2, skillCode: 'NETWORK', assigneeId: 1, priority: 10 })
  Object.assign(slaForm, { priority: 'MEDIUM', responseMinutes: 120, resolveMinutes: 1440 })
}

function resolveMode(path) {
  if (path.includes('/tickets/categories')) return '工单分类'
  if (path.includes('/rules/sla')) return 'SLA 规则'
  return '派单规则'
}
</script>

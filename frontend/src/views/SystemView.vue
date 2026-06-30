<template>
  <section class="work-surface">
    <div class="toolbar">
      <el-segmented v-model="mode" :options="modeOptions" />
      <el-input v-model="keyword" placeholder="输入名称、编码或账号搜索" clearable />
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增</el-button>
    </div>

    <el-table v-if="mode === '用户管理'" v-loading="loading" :data="filteredRows" height="520">
      <el-table-column prop="username" label="登录账号" width="160" />
      <el-table-column prop="realName" label="用户姓名" width="150" />
      <el-table-column label="所属部门" min-width="160">
        <template #default="{ row }">{{ deptName(row.deptId) }}</template>
      </el-table-column>
      <el-table-column label="角色" min-width="220">
        <template #default="{ row }">
          <el-tag v-for="roleId in row.roleIds || []" :key="roleId" class="tag-gap" type="success">
            {{ roleName(roleId) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 'ENABLED'"
            @change="(value) => toggleUserStatus(row, value)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="190">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="openResetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else-if="mode === '角色管理'" v-loading="loading" :data="filteredRows" height="520">
      <el-table-column prop="roleName" label="角色名称" min-width="180" />
      <el-table-column prop="roleCode" label="角色编码" width="180" />
      <el-table-column label="数据范围" width="170">
        <template #default="{ row }">{{ dataScopeText(row.dataScope) }}</template>
      </el-table-column>
      <el-table-column label="授权菜单数" width="120">
        <template #default="{ row }">{{ row.menuIds?.length || 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled === 1" @change="(value) => toggleEnabled(row, value)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else-if="mode === '菜单管理'" v-loading="loading" :data="filteredRows" height="520">
      <el-table-column prop="menuName" label="菜单名称" min-width="170" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="path" label="路由路径" min-width="190" />
      <el-table-column prop="permission" label="权限标识" min-width="190" />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="启用" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.visible === 1" @change="(value) => toggleEnabled(row, value)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-else v-loading="loading" :data="filteredRows" height="520">
      <el-table-column prop="deptName" label="部门名称" min-width="180" />
      <el-table-column prop="parentId" label="上级部门 ID" width="130" />
      <el-table-column prop="path" label="部门路径" min-width="180" />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled === 1" @change="(value) => toggleEnabled(row, value)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="mode === '用户管理'" class="pagination-row">
      <el-pagination
        v-model:current-page="page.pageNo"
        v-model:page-size="page.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="load"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form v-if="mode === '用户管理'" :model="userForm" label-width="100px">
        <el-form-item label="登录账号">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input v-model="userForm.password" type="password" placeholder="编辑时为空表示不修改" show-password />
        </el-form-item>
        <el-form-item label="用户姓名">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="userForm.deptId" filterable>
            <el-option v-for="dept in depts" :key="dept.id" :label="dept.deptName" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定角色">
          <el-select v-model="userForm.roleIds" multiple filterable>
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
      </el-form>

      <el-form v-else-if="mode === '角色管理'" :model="roleForm" label-width="100px">
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="roleForm.roleCode" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="roleForm.dataScope">
            <el-option label="本人" value="SELF" />
            <el-option label="本部门" value="DEPT" />
            <el-option label="本部门及下级" value="DEPT_AND_CHILD" />
            <el-option label="全部数据" value="ALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="授权菜单">
          <el-select v-model="roleForm.menuIds" multiple filterable>
            <el-option v-for="menu in menus" :key="menu.id" :label="menu.menuName" :value="menu.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-form v-else-if="mode === '菜单管理'" :model="menuForm" label-width="100px">
        <el-form-item label="上级菜单">
          <el-select v-model="menuForm.parentId" filterable>
            <el-option label="根菜单" :value="0" />
            <el-option v-for="menu in menus" :key="menu.id" :label="menu.menuName" :value="menu.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input v-model="menuForm.menuName" />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-select v-model="menuForm.type">
            <el-option label="目录" value="CATALOG" />
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="menuForm.path" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="menuForm.component" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="menuForm.icon" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="menuForm.permission" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="menuForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="显示">
          <el-switch v-model="menuForm.visible" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>

      <el-form v-else :model="deptForm" label-width="100px">
        <el-form-item label="上级部门">
          <el-select v-model="deptForm.parentId" filterable>
            <el-option label="根部门" :value="0" />
            <el-option v-for="dept in depts" :key="dept.id" :label="dept.deptName" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称">
          <el-input v-model="deptForm.deptName" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="deptForm.sortOrder" :min="0" />
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getDept,
  getMenu,
  getRole,
  getUser,
  listDepts,
  listMenus,
  listRoles,
  pageUsers,
  resetUserPassword,
  saveDept,
  saveMenu,
  saveRole,
  saveUser,
  updateDeptEnabled,
  updateMenuEnabled,
  updateRoleEnabled,
  updateUserStatus
} from '../api/system'

const route = useRoute()
const router = useRouter()
const modeOptions = ['用户管理', '角色管理', '菜单管理', '部门管理']
const modePathMap = {
  用户管理: '/system/users',
  角色管理: '/system/roles',
  菜单管理: '/system/menus',
  部门管理: '/system/depts'
}
const mode = ref(resolveMode(route.path))
const keyword = ref('')
const rows = ref([])
const roles = ref([])
const depts = ref([])
const menus = ref([])
const total = ref(0)
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)

const page = reactive({
  pageNo: 1,
  pageSize: 10
})

const userForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  deptId: null,
  roleIds: []
})

const roleForm = reactive({
  roleName: '',
  roleCode: '',
  dataScope: 'SELF',
  menuIds: []
})

const menuForm = reactive({
  parentId: 0,
  menuName: '',
  type: 'MENU',
  path: '',
  component: '',
  icon: 'Document',
  permission: '',
  sortOrder: 0,
  visible: 1
})

const deptForm = reactive({
  parentId: 0,
  deptName: '',
  sortOrder: 0
})

const dialogTitle = computed(() => `${editingId.value ? '编辑' : '新增'}${mode.value}`)
const filteredRows = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return rows.value
  return rows.value.filter((row) => JSON.stringify(row).toLowerCase().includes(text))
})

watch(mode, async (next) => {
  dialogVisible.value = false
  page.pageNo = 1
  if (route.path !== modePathMap[next]) {
    await router.push(modePathMap[next])
  }
  await load()
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

onMounted(async () => {
  await loadOptions()
  await load()
})

async function loadOptions() {
  const [roleRows, deptRows, menuRows] = await Promise.all([listRoles(), listDepts(), listMenus()])
  roles.value = roleRows || []
  depts.value = deptRows || []
  menus.value = menuRows || []
}

async function load() {
  loading.value = true
  try {
    if (mode.value === '用户管理') {
      const result = await pageUsers({ pageNo: page.pageNo, pageSize: page.pageSize })
      rows.value = result.records || []
      total.value = result.total || 0
    } else if (mode.value === '角色管理') {
      rows.value = await listRoles()
      roles.value = rows.value
    } else if (mode.value === '菜单管理') {
      rows.value = await listMenus()
      menus.value = rows.value
    } else {
      rows.value = await listDepts()
      depts.value = rows.value
    }
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetForms()
  dialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  resetForms()
  if (mode.value === '用户管理') {
    const detail = await getUser(row.id)
    Object.assign(userForm, { ...detail, password: '', roleIds: detail.roleIds || [] })
  } else if (mode.value === '角色管理') {
    const detail = await getRole(row.id)
    Object.assign(roleForm, { ...detail, menuIds: detail.menuIds || [] })
  } else if (mode.value === '菜单管理') {
    const detail = await getMenu(row.id)
    Object.assign(menuForm, {
      parentId: detail.parentId ?? 0,
      menuName: detail.menuName,
      type: detail.type || 'MENU',
      path: detail.path || '',
      component: detail.component || '',
      icon: detail.icon || 'Document',
      permission: detail.permission || '',
      sortOrder: detail.sortOrder ?? 0,
      visible: detail.visible ?? 1
    })
  } else {
    const detail = await getDept(row.id)
    Object.assign(deptForm, {
      parentId: detail.parentId ?? 0,
      deptName: detail.deptName,
      sortOrder: detail.sortOrder ?? 0
    })
  }
  dialogVisible.value = true
}

async function submit() {
  submitting.value = true
  try {
    if (mode.value === '用户管理') {
      await saveUser({ ...userForm }, editingId.value)
      await loadOptions()
    } else if (mode.value === '角色管理') {
      await saveRole({ ...roleForm }, editingId.value)
      await loadOptions()
    } else if (mode.value === '菜单管理') {
      await saveMenu({ ...menuForm }, editingId.value)
      await loadOptions()
    } else {
      await saveDept({ ...deptForm }, editingId.value)
      await loadOptions()
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function toggleUserStatus(row, value) {
  await updateUserStatus(row.id, value ? 'ENABLED' : 'DISABLED')
  ElMessage.success(value ? '用户已启用' : '用户已停用')
  await load()
}

async function toggleEnabled(row, value) {
  const enabled = value ? 1 : 0
  if (mode.value === '角色管理') {
    await updateRoleEnabled(row.id, enabled)
  } else if (mode.value === '菜单管理') {
    await updateMenuEnabled(row.id, enabled)
  } else if (mode.value === '部门管理') {
    await updateDeptEnabled(row.id, enabled)
  }
  ElMessage.success(enabled ? '已启用' : '已停用')
  await loadOptions()
  await load()
}

async function openResetPassword(row) {
  const result = await ElMessageBox.prompt(`请输入“${row.realName}”的新密码`, '重置密码', {
    confirmButtonText: '重置',
    cancelButtonText: '取消',
    inputType: 'password',
    inputPattern: /^.{6,}$/,
    inputErrorMessage: '密码至少 6 位'
  }).catch(() => null)
  if (!result) return
  await resetUserPassword(row.id, result.value)
  ElMessage.success('密码已重置')
}

function resetForms() {
  Object.assign(userForm, {
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    deptId: depts.value[0]?.id || null,
    roleIds: []
  })
  Object.assign(roleForm, { roleName: '', roleCode: '', dataScope: 'SELF', menuIds: [] })
  Object.assign(menuForm, {
    parentId: 0,
    menuName: '',
    type: 'MENU',
    path: '',
    component: '',
    icon: 'Document',
    permission: '',
    sortOrder: 0,
    visible: 1
  })
  Object.assign(deptForm, { parentId: 0, deptName: '', sortOrder: 0 })
}

function resolveMode(path) {
  if (path.includes('/system/roles')) return '角色管理'
  if (path.includes('/system/menus')) return '菜单管理'
  if (path.includes('/system/depts')) return '部门管理'
  return '用户管理'
}

function deptName(id) {
  return depts.value.find((dept) => dept.id === id)?.deptName || '-'
}

function roleName(id) {
  return roles.value.find((role) => role.id === id)?.roleName || `角色 ${id}`
}

function userStatusText(status) {
  return status === 'ENABLED' ? '启用' : '停用'
}

function dataScopeText(scope) {
  const texts = {
    SELF: '本人',
    DEPT: '本部门',
    DEPT_AND_CHILD: '本部门及下级',
    ALL: '全部数据'
  }
  return texts[scope] || scope || '-'
}
</script>

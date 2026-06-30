<template>
  <el-container class="app-shell">
    <el-aside class="sidebar" width="232px">
      <div class="brand">
        <div class="brand-mark">TF</div>
        <div>
          <strong>TicketFlow</strong>
          <span>IT 运维工单</span>
        </div>
      </div>
      <el-menu :default-active="$route.path" router class="side-menu">
        <template v-for="item in menuItems" :key="item.id || item.path">
          <el-sub-menu v-if="item.children?.length" :index="item.path || String(item.id)">
            <template #title>
              <el-icon><component :is="iconComponent(item.icon)" /></el-icon>
              <span>{{ item.name }}</span>
            </template>
            <el-menu-item v-for="child in item.children" :key="child.id || child.path" :index="child.path">
              <el-icon><component :is="iconComponent(child.icon)" /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">
            <el-icon><component :is="iconComponent(item.icon)" /></el-icon>
            <span>{{ item.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ today }}</p>
        </div>
        <div class="top-actions">
          <el-popover placement="bottom-end" width="360" trigger="click" @show="loadMessages">
            <template #reference>
              <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
                <el-button :icon="Bell" circle />
              </el-badge>
            </template>
            <div class="message-popover">
              <div class="message-head">
                <strong>站内信</strong>
                <el-button link type="primary" @click="loadMessages">刷新</el-button>
              </div>
              <el-scrollbar height="320px">
                <button
                  v-for="message in messages"
                  :key="message.id"
                  class="message-item"
                  :class="{ unread: message.readFlag === 0 }"
                  @click="handleMessageClick(message)"
                >
                  <span>{{ message.title }}</span>
                  <small>{{ message.content }}</small>
                  <em>{{ message.createdAt || '-' }}</em>
                </button>
                <el-empty v-if="messages.length === 0" description="暂无消息" :image-size="72" />
              </el-scrollbar>
            </div>
          </el-popover>
          <el-dropdown>
            <button class="user-chip">
              <el-icon><UserFilled /></el-icon>
              <span>{{ auth.user?.realName || '管理员' }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Avatar,
  Bell,
  Connection,
  Document,
  Folder,
  Menu as MenuIcon,
  Monitor,
  OfficeBuilding,
  SetUp,
  Setting,
  Tickets,
  Timer,
  TrendCharts,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { fetchUnreadCount, markMessageRead, pageMessages } from '../api/message'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const unreadCount = ref(0)
const messages = ref([])

const pageTitle = computed(() => route.meta.title || '工作台')
const today = new Intl.DateTimeFormat('zh-CN', { dateStyle: 'full' }).format(new Date())
const fallbackMenus = [
  { id: 'dashboard', name: '工作台', path: '/dashboard', icon: 'Monitor', children: [] },
  {
    id: 'tickets',
    name: '工单管理',
    path: '/tickets',
    icon: 'Tickets',
    children: [
      { id: 'ticket-list', name: '工单列表', path: '/tickets/list', icon: 'Document' },
      { id: 'ticket-category', name: '工单分类', path: '/tickets/categories', icon: 'Folder' }
    ]
  },
  {
    id: 'rules',
    name: '规则配置',
    path: '/rules',
    icon: 'SetUp',
    children: [
      { id: 'dispatch-rule', name: '派单规则', path: '/rules/dispatch', icon: 'Connection' },
      { id: 'sla-rule', name: 'SLA 规则', path: '/rules/sla', icon: 'Timer' }
    ]
  },
  {
    id: 'system',
    name: '系统管理',
    path: '/system',
    icon: 'Setting',
    children: [
      { id: 'system-users', name: '用户管理', path: '/system/users', icon: 'User' },
      { id: 'system-roles', name: '角色管理', path: '/system/roles', icon: 'Avatar' },
      { id: 'system-menus', name: '菜单管理', path: '/system/menus', icon: 'Menu' },
      { id: 'system-depts', name: '部门管理', path: '/system/depts', icon: 'OfficeBuilding' }
    ]
  },
  { id: 'reports', name: '统计看板', path: '/reports', icon: 'TrendCharts', children: [] }
]

const iconMap = {
  Avatar,
  Connection,
  Document,
  Folder,
  Menu: MenuIcon,
  Monitor,
  OfficeBuilding,
  SetUp,
  Setting,
  Tickets,
  Timer,
  TrendCharts,
  User
}

const menuItems = computed(() => (auth.routes?.length ? auth.routes : fallbackMenus))

onMounted(() => {
  if (auth.token && (!auth.user || !auth.routes.length)) {
    auth.loadCurrentUser().catch(() => {})
  }
  loadUnreadCount()
})

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}

function iconComponent(name) {
  return iconMap[name] || Document
}

async function loadUnreadCount() {
  const result = await fetchUnreadCount().catch(() => ({ count: 0 }))
  unreadCount.value = result.count || 0
}

async function loadMessages() {
  const result = await pageMessages({ pageNo: 1, pageSize: 8 }).catch(() => ({ records: [] }))
  messages.value = result.records || []
  await loadUnreadCount()
}

async function handleMessageClick(message) {
  if (message.readFlag === 0) {
    await markMessageRead(message.id)
    message.readFlag = 1
    await loadUnreadCount()
  }
  if (message.businessType === 'TICKET' && message.businessId) {
    router.push({ path: '/tickets/list', query: { ticketId: message.businessId } })
  }
}
</script>

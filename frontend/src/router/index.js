import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        meta: { title: '工作台', icon: 'Monitor' },
        component: () => import('../views/DashboardView.vue')
      },
      {
        path: 'tickets/list',
        name: 'ticket-list',
        meta: { title: '工单列表', icon: 'Document' },
        component: () => import('../views/TicketListView.vue')
      },
      {
        path: 'tickets/categories',
        name: 'ticket-category',
        meta: { title: '工单分类', icon: 'Folder' },
        component: () => import('../views/RuleView.vue')
      },
      {
        path: 'rules/dispatch',
        name: 'dispatch-rule',
        meta: { title: '派单规则', icon: 'Connection' },
        component: () => import('../views/RuleView.vue')
      },
      {
        path: 'rules/sla',
        name: 'sla-rule',
        meta: { title: 'SLA 规则', icon: 'Timer' },
        component: () => import('../views/RuleView.vue')
      },
      {
        path: 'system/users',
        name: 'system-users',
        meta: { title: '用户管理', icon: 'User' },
        component: () => import('../views/SystemView.vue')
      },
      {
        path: 'system/roles',
        name: 'system-roles',
        meta: { title: '角色管理', icon: 'Avatar' },
        component: () => import('../views/SystemView.vue')
      },
      {
        path: 'system/menus',
        name: 'system-menus',
        meta: { title: '菜单管理', icon: 'Menu' },
        component: () => import('../views/SystemView.vue')
      },
      {
        path: 'system/depts',
        name: 'system-depts',
        meta: { title: '部门管理', icon: 'OfficeBuilding' },
        component: () => import('../views/SystemView.vue')
      },
      {
        path: 'reports',
        name: 'reports',
        meta: { title: '统计看板', icon: 'TrendCharts' },
        component: () => import('../views/ReportView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.token) {
    return '/login'
  }
  if (to.path === '/login' && auth.token) {
    return '/dashboard'
  }
  return true
})

export default router

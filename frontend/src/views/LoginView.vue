<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-copy">
        <div class="brand-line">
          <span>TicketFlow</span>
          <strong>企业 IT 运维工单</strong>
        </div>
        <h1>统一受理、派单、处理和验收</h1>
        <div class="signal-grid">
          <div>
            <b>响应</b>
            <span>SLA</span>
          </div>
          <div>
            <b>协作</b>
            <span>RBAC</span>
          </div>
          <div>
            <b>追踪</b>
            <span>日志</span>
          </div>
        </div>
      </div>
      <el-form class="login-form" :model="form" label-position="top" @submit.prevent>
        <h2>登录后台</h2>
        <el-form-item label="账号">
          <el-input v-model="form.username" size="large" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" size="large" type="password" placeholder="123456" show-password />
        </el-form-item>
        <el-button class="login-button" type="primary" size="large" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
})

async function submit() {
  loading.value = true
  try {
    await auth.login(form)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="work-surface">
    <div class="empty-state">
      <el-icon><FolderOpened /></el-icon>
      <h2>{{ title }}</h2>
      <el-button type="primary" :icon="Plus">新增</el-button>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const title = computed(() => route.meta.title || '配置管理')
</script>

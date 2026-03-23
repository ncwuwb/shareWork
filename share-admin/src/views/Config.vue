<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const list = ref([])

async function load() {
  try {
    const res = await http.get('/admin/config/list')
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

async function save(row) {
  try {
    await http.post('/admin/config', {
      configKey: row.configKey,
      configValue: row.configValue,
      description: row.description,
    })
    ElMessage.success('已保存')
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

onMounted(load)
</script>

<template>
  <el-card>
    <el-button @click="load">刷新</el-button>
    <el-table :data="list" class="mt">
      <el-table-column prop="configKey" label="键" width="200" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="值" width="280">
        <template #default="{ row }">
          <el-input v-model="row.configValue" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="save(row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.mt {
  margin-top: 12px;
}
</style>

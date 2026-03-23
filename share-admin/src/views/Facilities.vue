<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({ id: null, name: '', icon: '' })

async function load() {
  try {
    const res = await http.get('/admin/facilities')
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

onMounted(load)

function openCreate() {
  form.value = { id: null, name: '', icon: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    await http.post('/admin/facilities', form.value)
    ElMessage.success('已保存')
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

async function remove(row) {
  try {
    await http.delete('/admin/facilities/' + row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}
</script>

<template>
  <el-card>
    <el-button type="primary" @click="openCreate">新增</el-button>
    <el-table :data="list" class="mt">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="icon" label="图标" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="设施">
      <el-form :model="form">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.mt {
  margin-top: 12px;
}
</style>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const parentId = ref(0)
const list = ref([])
const form = ref({ id: null, parentId: 0, name: '', sort: 0 })
const visible = ref(false)

async function load() {
  try {
    const res = await http.get('/admin/depts/tree', { params: { parentId: parentId.value } })
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

watch(parentId, load, { immediate: true })

function addChild() {
  form.value = { id: null, parentId: parentId.value, name: '', sort: 0 }
  visible.value = true
}

function edit(row) {
  form.value = { ...row }
  visible.value = true
}

async function save() {
  try {
    await http.post('/admin/depts', form.value)
    ElMessage.success('已保存')
    visible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

async function remove(row) {
  try {
    await http.delete('/admin/depts/' + row.id)
    ElMessage.success('已删')
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

function enter(row) {
  parentId.value = row.id
}
</script>

<template>
  <el-card>
    <el-button @click="parentId = 0">顶级</el-button>
    <el-button type="primary" @click="addChild">新增部门</el-button>
    <el-table :data="list" class="mt">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="enter(row)">进入</el-button>
          <el-button link @click="edit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="部门">
      <el-form :model="form">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" /></el-form-item>
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

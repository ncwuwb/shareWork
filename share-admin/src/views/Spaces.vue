<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const parentId = ref(0)
const list = ref([])
const dialogVisible = ref(false)
const form = ref({
  id: null,
  parentId: 0,
  name: '',
  type: 4,
  centerLongitude: null,
  centerLatitude: null,
  bgImage: '',
  sort: 0,
})

async function load() {
  try {
    const res = await http.get('/admin/spaces/children', { params: { parentId: parentId.value } })
    list.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

watch(parentId, load, { immediate: true })

function enterChild(row) {
  parentId.value = row.id
}

function goUp() {
  parentId.value = 0
}

function openCreate() {
  form.value = {
    id: null,
    parentId: parentId.value,
    name: '',
    type: 4,
    centerLongitude: null,
    centerLatitude: null,
    bgImage: '',
    sort: 0,
  }
  dialogVisible.value = true
}

function openEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    await http.post('/admin/spaces', form.value)
    ElMessage.success('已保存')
    dialogVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function remove(row) {
  try {
    await http.delete('/admin/spaces/' + row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

function spaceTypeText(type) {
  const map = { 1: '园区', 2: '楼宇', 3: '楼层', 4: '办公区域' }
  return map[type] ?? '—'
}

async function onFileChange(file) {
  const fd = new FormData()
  fd.append('file', file.raw)
  try {
    const res = await http.post('/admin/spaces/upload-bg', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    form.value.bgImage = res.data
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  }
}
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-button @click="goUp" :disabled="parentId === 0">返回顶级</el-button>
      <el-button type="primary" @click="openCreate">新增节点</el-button>
    </div>
    <el-table :data="list" size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column label="类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag type="info" size="small" effect="plain">{{ spaceTypeText(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="enterChild(row)">进入</el-button>
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="空间" width="520">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option :value="1" label="1园区" />
            <el-option :value="2" label="2楼宇" />
            <el-option :value="3" label="3楼层" />
            <el-option :value="4" label="4办公区域" />
          </el-select>
        </el-form-item>
        <el-form-item label="中心经度"><el-input v-model="form.centerLongitude" /></el-form-item>
        <el-form-item label="中心纬度"><el-input v-model="form.centerLatitude" /></el-form-item>
        <el-form-item label="底图">
          <el-input v-model="form.bgImage" placeholder="/uploads/xxx" />
          <el-upload :auto-upload="false" :show-file-list="false" :on-change="onFileChange" style="margin-top: 8px">
            <el-button>上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
</style>

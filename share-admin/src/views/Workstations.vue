<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const spaceId = ref(null)
const areaOptions = ref([])
const table = ref([])
const batchVisible = ref(false)
const batch = ref({ spaceId: null, prefix: 'A-', startNum: 1, count: 10 })
const editVisible = ref(false)
const edit = ref({})

async function loadAreaOptions() {
  try {
    const res = await http.get('/admin/spaces/workstation-areas')
    areaOptions.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载办公区域失败')
    areaOptions.value = []
  }
}

function onSpaceChange(val) {
  if (val == null || val === '') {
    table.value = []
    return
  }
  load()
}

async function load() {
  if (spaceId.value == null || spaceId.value === '') return
  try {
    const res = await http.get('/admin/workstations/page', {
      params: { spaceId: spaceId.value, page: 1, size: 200 },
    })
    table.value = res.data.records || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

function openBatch() {
  batch.value = {
    spaceId: spaceId.value,
    prefix: 'A-',
    startNum: 1,
    count: 10,
  }
  batchVisible.value = true
}

async function doBatch() {
  if (batch.value.spaceId == null || batch.value.spaceId === '') {
    ElMessage.warning('请选择办公区域')
    return
  }
  try {
    await http.post('/admin/workstations/batch', batch.value)
    ElMessage.success('已生成')
    batchVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

function openEdit(row) {
  edit.value = { ...row }
  editVisible.value = true
}

async function saveEdit() {
  try {
    await http.put('/admin/workstations/' + edit.value.id, edit.value)
    ElMessage.success('已保存')
    editVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

function baseStatusText(v) {
  const map = { 0: '正常', 1: '维修', 2: '专属' }
  return map[v] ?? '—'
}

function baseStatusTagType(v) {
  if (v === 0) return 'success'
  if (v === 1) return 'warning'
  if (v === 2) return 'info'
  return 'info'
}

onMounted(() => {
  loadAreaOptions()
})
</script>

<template>
  <el-card>
    <el-space wrap alignment="center">
      <span class="field-label">办公区域</span>
      <el-select
        v-model="spaceId"
        placeholder="请选择办公区域（type=4）"
        filterable
        clearable
        style="width: min(520px, 92vw)"
        @change="onSpaceChange"
      >
        <el-option
          v-for="item in areaOptions"
          :key="item.id"
          :label="item.path"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" :disabled="spaceId == null" @click="load">刷新列表</el-button>
      <el-button :disabled="spaceId == null" @click="openBatch">批量生成</el-button>
    </el-space>
    <p v-if="!areaOptions.length" class="hint">暂无可用办公区域，请先在「空间管理」中创建 type=4 的节点。</p>

    <el-table :data="table" class="mt" size="small">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="code" label="编号" />
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="baseStatusTagType(row.baseStatus)" size="small" effect="plain">
            {{ baseStatusText(row.baseStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="coordX" label="X" width="70" />
      <el-table-column prop="coordY" label="Y" width="70" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑坐标/状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="batchVisible" title="批量生成工位" width="520">
      <el-form :model="batch" label-width="100">
        <el-form-item label="办公区域">
          <el-select
            v-model="batch.spaceId"
            placeholder="请选择"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in areaOptions"
              :key="item.id"
              :label="item.path"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="编号前缀"><el-input v-model="batch.prefix" placeholder="如 A-" /></el-form-item>
        <el-form-item label="起始号"><el-input-number v-model="batch.startNum" :min="1" /></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="batch.count" :min="1" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="doBatch">生成</el-button></template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="工位">
      <el-form :model="edit">
        <el-form-item label="编号"><el-input v-model="edit.code" /></el-form-item>
        <el-form-item label="X"><el-input-number v-model="edit.coordX" /></el-form-item>
        <el-form-item label="Y"><el-input-number v-model="edit.coordY" /></el-form-item>
        <el-form-item label="基础状态"><el-input-number v-model="edit.baseStatus" :min="0" :max="2" /></el-form-item>
        <el-form-item label="专属用户ID"><el-input v-model="edit.reservedUserId" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="saveEdit">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.mt {
  margin-top: 12px;
}
.field-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
}
.hint {
  margin-top: 10px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>

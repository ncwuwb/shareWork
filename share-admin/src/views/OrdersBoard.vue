<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const areaOptions = ref([])
const areaId = ref(null)
const day = ref(new Date().toISOString().slice(0, 10))
const board = ref([])

/** 与后端订单 status 一致 */
const ORDER_STATUS_LABEL = {
  0: '待签到',
  1: '使用中',
  2: '已完成',
  3: '已取消',
  4: '已违约',
}

const tableRows = computed(() => {
  const rows = []
  for (const row of board.value) {
    const segs = row.segments || []
    if (!segs.length) {
      rows.push({
        workstationId: row.workstationId,
        code: row.code,
        orderId: null,
        startTime: null,
        endTime: null,
        userLabel: '—',
        status: null,
        statusLabel: '—',
      })
    } else {
      for (const s of segs) {
        const st = s.status
        rows.push({
          workstationId: row.workstationId,
          code: row.code,
          orderId: s.orderId,
          startTime: s.startTime,
          endTime: s.endTime,
          userLabel: [s.userName, s.phone].filter(Boolean).join(' · ') || '—',
          status: st,
          statusLabel: ORDER_STATUS_LABEL[st] ?? `状态${st}`,
        })
      }
    }
  }
  return rows
})

async function loadAreaOptions() {
  try {
    const res = await http.get('/admin/spaces/workstation-areas')
    areaOptions.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载办公区域失败')
    areaOptions.value = []
  }
}

async function load() {
  if (areaId.value == null || areaId.value === '') {
    board.value = []
    return
  }
  try {
    const res = await http.get('/admin/orders/board', {
      params: { areaSpaceId: areaId.value, day: day.value },
    })
    board.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

async function forceCancel(orderId) {
  try {
    await http.post('/admin/orders/' + orderId + '/force-cancel')
    ElMessage.success('已取消')
    load()
  } catch (e) {
    ElMessage.error(e.message || '失败')
  }
}

function formatDt(v) {
  if (v == null || v === '') return '—'
  return String(v).replace('T', ' ')
}

function statusTagType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  if (status === 3) return ''
  if (status === 4) return 'danger'
  return 'info'
}

/** 后端对已完成(2)、已取消(3)不会执行强取消，界面不再展示按钮 */
function showForceCancel(row) {
  if (row.orderId == null) return false
  const st = row.status
  return st !== 2 && st !== 3
}

onMounted(() => {
  loadAreaOptions()
})
</script>

<template>
  <el-card shadow="never" class="board-card">
    <el-space wrap alignment="center" class="toolbar">
      <span class="field-label">办公区域</span>
      <el-select
        v-model="areaId"
        placeholder="请选择办公区域"
        filterable
        clearable
        style="width: min(480px, 100%)"
        @change="load"
      >
        <el-option v-for="item in areaOptions" :key="item.id" :label="item.path" :value="item.id" />
      </el-select>
      <el-date-picker v-model="day" type="date" value-format="YYYY-MM-DD" @change="load" />
      <el-button type="primary" :disabled="areaId == null" @click="load">加载看板</el-button>
    </el-space>

    <p v-if="!areaOptions.length" class="hint">暂无可用办公区域，请先在「空间管理」中维护 type=4 的节点。</p>
    <p v-else-if="areaId == null" class="hint">请选择办公区域后点击「加载看板」查看当日预约。</p>

    <el-table
      v-else
      :data="tableRows"
      border
      stripe
      class="board-table"
      empty-text="暂无工位或当日无预约记录"
      size="default"
    >
      <el-table-column prop="code" label="工位编号" width="120" fixed />
      <el-table-column label="预约时段" min-width="200">
        <template #default="{ row }">
          <span v-if="row.orderId == null">
            <span class="muted">当日无预约</span>
          </span>
          <span v-else>
            {{ formatDt(row.startTime) }} — {{ formatDt(row.endTime) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="userLabel" label="用户" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <template v-if="row.orderId == null">
            <span class="muted">—</span>
          </template>
          <el-tag v-else :type="statusTagType(row.status)" size="small" effect="plain">
            {{ row.statusLabel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="showForceCancel(row)"
            link
            type="danger"
            size="small"
            @click="forceCancel(row.orderId)"
          >
            强取消
          </el-button>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.board-card {
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 16px;
}

.field-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.board-table {
  width: 100%;
  margin-top: 4px;
}

.muted {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>

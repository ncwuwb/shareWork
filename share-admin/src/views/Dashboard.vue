<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'
import http from '@/api/http'

const summary = ref(null)

const donutRef = ref(null)
const barRef = ref(null)
const chartDonut = shallowRef(null)
const chartBar = shallowRef(null)

const areaOptions = ref([])
const boardAreaId = ref(null)
const boardDay = ref(new Date().toISOString().slice(0, 10))
const boardRows = ref([])
const dashboardReady = ref(false)

const chartText = '#94a3b8'
const chartLine = '#334155'
const accentColors = ['#38bdf8', '#a78bfa', '#f472b6', '#34d399', '#fbbf24']

const axisDark = {
  axisLine: { lineStyle: { color: chartLine } },
  axisLabel: { color: chartText },
  splitLine: { lineStyle: { color: chartLine, type: 'dashed' } },
}

function disposeAll() {
  chartDonut.value?.dispose()
  chartBar.value?.dispose()
  chartDonut.value = null
  chartBar.value = null
}

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
  try {
    const res = await http.get('/admin/dashboard/summary')
    summary.value = res.data
    // v-if="summary" 下图表容器下一帧才挂载，需 nextTick 后再 init ECharts
    await nextTick()
    renderDonut(res.data.hotRegions || [])
    renderBar(res.data.deptBreach || [])
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

function renderDonut(regions) {
  if (!donutRef.value) return
  if (!chartDonut.value) chartDonut.value = echarts.init(donutRef.value, undefined, { renderer: 'canvas' })
  const data = regions.map((r, i) => ({
    name: r.spaceName || '区域' + (r.spaceId ?? i),
    value: Number(r.cnt) || 0,
    itemStyle: { color: accentColors[i % accentColors.length] },
  }))
  chartDonut.value.setOption({
    backgroundColor: 'transparent',
    title: {
      text: '过去30天最受欢迎区域 Top3',
      left: 'center',
      top: 8,
      textStyle: { color: '#e2e8f0', fontSize: 14 },
    },
    tooltip: { trigger: 'item', backgroundColor: '#1e293b', borderColor: '#334155', textStyle: { color: '#e2e8f0' } },
    legend: { bottom: 8, textStyle: { color: chartText } },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '52%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#0f172a', borderWidth: 2 },
        label: { color: '#e2e8f0' },
        data: data.length ? data : [{ name: '暂无', value: 1, itemStyle: { color: '#334155' } }],
      },
    ],
  })
}

function renderBar(rows) {
  if (!barRef.value) return
  if (!chartBar.value) chartBar.value = echarts.init(barRef.value, undefined, { renderer: 'canvas' })
  const names = []
  const rates = []
  for (const r of rows) {
    const total = Number(r.total) || 0
    const breach = Number(r.breach) || 0
    names.push(r.deptName || '部门' + r.deptId)
    rates.push(total === 0 ? 0 : Math.round((breach * 1000) / total) / 10)
  }
  chartBar.value.setOption({
    backgroundColor: 'transparent',
    title: {
      text: '违约统计 · 部门违约占比(%)',
      left: 'center',
      top: 8,
      textStyle: { color: '#e2e8f0', fontSize: 14 },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1e293b',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0' },
    },
    grid: { left: 48, right: 16, top: 48, bottom: 32 },
    xAxis: { type: 'category', data: names.length ? names : ['暂无'], ...axisDark, axisLabel: { rotate: 28, color: chartText } },
    yAxis: { type: 'value', name: '%', nameTextStyle: { color: chartText }, ...axisDark },
    series: [
      {
        type: 'bar',
        data: rates.length ? rates : [0],
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#38bdf8' },
            { offset: 1, color: '#6366f1' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      },
    ],
  })
}

const boardColumns = computed(() => {
  const start = 8
  const end = 20
  const slots = []
  for (let i = start; i <= end; i += 2) {
    slots.push(i)
  }
  return slots
})

function parseDayMs(dayStr) {
  const [y, m, d] = dayStr.split('-').map(Number)
  return new Date(y, m - 1, d).getTime()
}

function parseDateTimeMs(value) {
  if (value == null || value === '') return NaN
  const raw = String(value).trim()
  const m = raw.match(/^(\d{4})-(\d{2})-(\d{2})(?:[ T](\d{2}):(\d{2})(?::(\d{2}))?)?$/)
  if (m) {
    const [, y, mo, d, h = '0', mi = '0', s = '0'] = m
    return new Date(Number(y), Number(mo) - 1, Number(d), Number(h), Number(mi), Number(s)).getTime()
  }
  const ts = Date.parse(raw.replace(' ', 'T'))
  return Number.isNaN(ts) ? NaN : ts
}

function formatClock(hour) {
  const total = Math.max(0, Math.min(24, Number(hour) || 0))
  const h = Math.floor(total)
  const m = Math.round((total - h) * 60)
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

function segmentToHours(seg, dayStr) {
  const day0 = parseDayMs(dayStr)
  const s = parseDateTimeMs(seg.startTime)
  const e = parseDateTimeMs(seg.endTime)
  if (Number.isNaN(s) || Number.isNaN(e)) return null
  let h0 = (s - day0) / 3600000
  let h1 = (e - day0) / 3600000
  h0 = Math.max(0, Math.min(24, h0))
  h1 = Math.max(0, Math.min(24, h1))
  if (h1 <= h0) h1 = Math.min(24, h0 + 0.5)
  return [h0, h1]
}

const statusMeta = {
  0: { label: '待签到', color: '#f59e0b' },
  1: { label: '进行中', color: '#22c55e' },
  2: { label: '已完成', color: '#64748b' },
  3: { label: '已取消', color: '#94a3b8' },
  4: { label: '已违约', color: '#ef4444' },
}

function getStatusMeta(status) {
  return statusMeta[Number(status)] || { label: '未知', color: '#38bdf8' }
}

function buildBoardRows() {
  return boardRows.value.map((row) => {
    const items = (row.segments || [])
      .map((seg, idx) => {
        const hours = segmentToHours(seg, boardDay.value)
        if (!hours) return null
        const [start, end] = hours
        const meta = getStatusMeta(seg.status)
        const timeLabel = `${formatClock(start)}-${formatClock(end)}`
        return {
          id: `${row.workstationId}-${seg.orderId ?? idx}`,
          start,
          end,
          label: seg.userName || seg.phone || '预约',
          timeLabel,
          status: Number(seg.status),
          color: meta.color,
          statusLabel: meta.label,
        }
      })
      .filter(Boolean)
      .sort((a, b) => a.start - b.start)
    return {
      ...row,
      items,
    }
  })
}

const visibleBoardRows = computed(() => buildBoardRows())
const hasBoardData = computed(() => visibleBoardRows.value.some((row) => row.items.length > 0))

function findCurrentLine() {
  const now = new Date()
  const dayMs = parseDayMs(boardDay.value)
  if (now >= dayMs && now < dayMs + 86400000) {
    return (now - dayMs) / 3600000
  }
  return null
}

async function loadBoard() {
  if (boardAreaId.value == null || boardAreaId.value === '') {
    boardRows.value = []
    localStorage.removeItem('admin_board_area_id')
    return
  }
  try {
    localStorage.setItem('admin_board_area_id', String(boardAreaId.value))
    const res = await http.get('/admin/orders/board', {
      params: { areaSpaceId: boardAreaId.value, day: boardDay.value },
    })
    boardRows.value = res.data || []
  } catch (e) {
    ElMessage.error(e.message || '看板加载失败')
  }
}

watch(
  [boardAreaId, boardDay],
  () => {
    if (dashboardReady.value && summary.value && boardAreaId.value) {
      loadBoard()
    }
  },
  { flush: 'post' },
)

async function exportSummary() {
  try {
    const res = await axios.get('/api/admin/dashboard/export-summary', {
      responseType: 'blob',
      headers: { Authorization: 'Bearer ' + localStorage.getItem('adminToken') },
    })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = 'dashboard-summary.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

function onResize() {
  chartDonut.value?.resize()
  chartBar.value?.resize()
  chartGantt.value?.resize()
}

onMounted(async () => {
  await loadAreaOptions()
  const stored = localStorage.getItem('admin_board_area_id')
  if (stored) {
    const n = Number(stored)
    if (!Number.isNaN(n) && areaOptions.value.some((o) => o.id === n)) {
      boardAreaId.value = n
    }
  }
  dashboardReady.value = true
  await load()
  if (boardAreaId.value != null && boardAreaId.value !== '') {
    await loadBoard()
  }
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  dashboardReady.value = false
  window.removeEventListener('resize', onResize)
  disposeAll()
})
</script>

<template>
  <div v-if="summary" class="dashboard">
    <el-row :gutter="16" class="row-main">
      <el-col :xs="24" :lg="16" class="col-left">
        <el-row :gutter="12" class="kpi-row">
          <el-col :span="8">
            <el-card class="admin-card kpi-card" shadow="never">
              <div class="kpi-label">今日预约总数</div>
              <div class="kpi-val">{{ summary.todayOrders }}</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="admin-card kpi-card" shadow="never">
              <div class="kpi-label">当前签到率</div>
              <div class="kpi-val">{{ summary.todaySignRate }}%</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="admin-card kpi-card" shadow="never">
              <div class="kpi-label">空置率</div>
              <div class="kpi-val">{{ summary.vacancyRate }}%</div>
            </el-card>
          </el-col>
        </el-row>
        <el-card class="admin-card chart-card mt" shadow="never">
          <div ref="donutRef" class="chart-box donut"></div>
        </el-card>
        <el-card class="admin-card chart-card mt" shadow="never">
          <div ref="barRef" class="chart-box bar"></div>
        </el-card>
        <div class="mt actions">
          <el-button type="primary" @click="exportSummary">导出汇总 Excel</el-button>
        </div>
      </el-col>
      <el-col :xs="24" :lg="8" class="col-right">
        <el-card class="admin-card gantt-card" shadow="never">
          <div class="gantt-toolbar">
            <el-select
              v-model="boardAreaId"
              placeholder="办公区域"
              filterable
              clearable
              size="small"
              style="width: min(260px, 100%)"
            >
              <el-option v-for="item in areaOptions" :key="item.id" :label="item.path" :value="item.id" />
            </el-select>
            <el-date-picker v-model="boardDay" type="date" value-format="YYYY-MM-DD" size="small" style="width: 140px" />
          </div>
          <p v-if="!boardAreaId" class="gantt-hint">请选择办公区域后会自动显示当日工位占用情况</p>
          <div v-else class="board-wrap">
            <div class="board-header">
              <span>时间</span>
              <span v-for="hour in boardColumns" :key="hour">{{ String(hour).padStart(2, '0') }}:00</span>
            </div>
            <div class="board-body">
              <div v-if="!visibleBoardRows.length" class="board-empty">暂无预约数据</div>
              <div v-for="row in visibleBoardRows" :key="row.workstationId" class="board-row">
                <div class="board-name" :title="row.code">{{ row.code }}</div>
                <div class="board-track">
                  <div class="board-grid">
                    <span v-for="hour in boardColumns" :key="hour" class="board-grid-line" :style="{ left: `${((hour - 8) / 12) * 100}%` }"></span>
                  </div>
                  <div
                    v-for="item in row.items"
                    :key="item.id"
                    class="board-segment"
                    :style="{
                      left: `${((item.start - 8) / 12) * 100}%`,
                      width: `${Math.max(((item.end - item.start) / 12) * 100, 2)}%`,
                      backgroundColor: item.color,
                    }"
                    :title="`${row.code} · ${item.label} · ${item.timeLabel} · ${item.statusLabel}`"
                  >
                    <span class="segment-text">{{ item.timeLabel }}</span>
<!--                    <span class="segment-name">{{ item.label }}</span>-->
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
  <div v-else class="loading">加载中…</div>
</template>

<style scoped>
.dashboard {
  min-height: calc(100vh - 120px);
}
.row-main {
  align-items: stretch;
}
.col-left {
  display: flex;
  flex-direction: column;
}
.col-right {
  display: flex;
  flex-direction: column;
}
.kpi-row {
  margin-bottom: 0;
}
.kpi-card {
  text-align: center;
  border-radius: 10px;
}
.kpi-label {
  color: #94a3b8;
  font-size: 13px;
}
.kpi-val {
  font-size: 28px;
  font-weight: 700;
  color: #f1f5f9;
  margin-top: 8px;
  font-variant-numeric: tabular-nums;
}
.chart-card {
  border-radius: 10px;
}
.mt {
  margin-top: 12px;
}
.chart-box {
  width: 100%;
}
.donut {
  height: 300px;
}
.bar {
  height: 320px;
}
.gantt-card {
  border-radius: 10px;
  min-height: 640px;
  display: flex;
  flex-direction: column;
}
.gantt-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}
.gantt-hint {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 8px;
}
.board-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 520px;
  flex: 1;
}
.board-header {
  display: grid;
  grid-template-columns: 92px repeat(7, 1fr);
  gap: 8px;
  align-items: center;
  padding: 0 4px 4px;
  color: #94a3b8;
  font-size: 12px;
}
.board-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.board-empty {
  color: #64748b;
  font-size: 13px;
  padding: 20px 4px;
}
.board-row {
  display: grid;
  grid-template-columns: 92px 1fr;
  gap: 8px;
  align-items: stretch;
}
.board-name {
  color: #e2e8f0;
  font-size: 12px;
  line-height: 18px;
  padding-top: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.board-track {
  position: relative;
  min-height: 34px;
  border-radius: 8px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.95), rgba(15, 23, 42, 0.8));
  border: 1px solid rgba(51, 65, 85, 0.9);
  overflow: hidden;
}
.board-grid {
  position: absolute;
  inset: 0;
}
.board-grid-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1px;
  background: rgba(51, 65, 85, 0.75);
}
.board-segment {
  position: absolute;
  top: 5px;
  bottom: 5px;
  border-radius: 6px;
  padding: 3px 8px;
  color: white;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.35);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.segment-text {
  font-size: 11px;
  font-weight: 600;
  line-height: 1.1;
}
.segment-name {
  font-size: 10px;
  line-height: 1.1;
  opacity: 0.92;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.actions {
  padding-bottom: 8px;
}
.loading {
  color: #94a3b8;
  padding: 24px;
}
</style>

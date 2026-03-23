<script setup>
import { nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts'
import http from '@/api/http'

const summary = ref(null)

const donutRef = ref(null)
const barRef = ref(null)
const ganttRef = ref(null)
const chartDonut = shallowRef(null)
const chartBar = shallowRef(null)
const chartGantt = shallowRef(null)

const areaOptions = ref([])
const boardAreaId = ref(null)
const boardDay = ref(new Date().toISOString().slice(0, 10))
const boardRows = ref([])

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
  chartGantt.value?.dispose()
  chartDonut.value = null
  chartBar.value = null
  chartGantt.value = null
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

function parseDayMs(dayStr) {
  const [y, m, d] = dayStr.split('-').map(Number)
  return new Date(y, m - 1, d).getTime()
}

function segmentToHours(seg, dayStr) {
  const day0 = parseDayMs(dayStr)
  const msDay = 86400000
  const s = new Date(String(seg.startTime).replace(' ', 'T')).getTime()
  const e = new Date(String(seg.endTime).replace(' ', 'T')).getTime()
  let h0 = (s - day0) / 3600000
  let h1 = (e - day0) / 3600000
  h0 = Math.max(0, Math.min(24, h0))
  h1 = Math.max(0, Math.min(24, h1))
  if (h1 <= h0) h1 = Math.min(24, h0 + 0.5)
  return [h0, h1]
}

const statusColor = {
  0: '#f59e0b',
  1: '#22c55e',
  2: '#64748b',
  4: '#ef4444',
}

function renderGantt() {
  if (!ganttRef.value) return
  if (!chartGantt.value) chartGantt.value = echarts.init(ganttRef.value, undefined, { renderer: 'canvas' })

  const categories = boardRows.value.map((r) => r.code)
  const customData = []
  boardRows.value.forEach((row, yi) => {
    for (const seg of row.segments || []) {
      const [h0, h1] = segmentToHours(seg, boardDay.value)
      const st = Number(seg.status)
      customData.push([h0, h1, yi, st, seg.userName || seg.phone || ''])
    }
  })

  const now = new Date()
  const dayMs = parseDayMs(boardDay.value)
  let curLine = null
  if (now >= dayMs && now < dayMs + 86400000) {
    curLine = (now - dayMs) / 3600000
  }

  const series = [
    {
      type: 'custom',
      renderItem(params, api) {
        const d = params.data
        if (!d || d.length < 3) return
        const yIndex = d[2]
        const start = api.coord([d[0], yIndex])
        const end = api.coord([d[1], yIndex])
        const h = api.size([0, 1])[1] * 0.55
        const w = Math.max(end[0] - start[0], 3)
        return {
          type: 'rect',
          shape: { x: start[0], y: start[1] - h / 2, width: w, height: h },
          style: { fill: statusColor[d[3]] ?? '#38bdf8', opacity: 0.92 },
        }
      },
      dimensions: ['h0', 'h1', 'y', 'status', 'name'],
      encode: { x: [0, 1], y: 2 },
      data: customData,
    },
  ]
  if (curLine != null && categories.length) {
    series.push({
      type: 'scatter',
      symbolSize: 0,
      data: [],
      markLine: {
        silent: true,
        symbol: 'none',
        lineStyle: { color: 'rgba(255,255,255,0.35)', width: 1 },
        label: { show: false },
        data: [{ xAxis: curLine }],
      },
    })
  }

  chartGantt.value.setOption({
    backgroundColor: 'transparent',
    title: {
      text: '预约看板',
      left: 'center',
      top: 4,
      textStyle: { color: '#e2e8f0', fontSize: 14 },
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: '#1e293b',
      borderColor: '#334155',
      textStyle: { color: '#e2e8f0' },
      formatter: (p) => {
        const d = p.data
        if (!d || !d.length) return ''
        return `${d[4] || '预约'}<br/>${Number(d[0]).toFixed(1)}时 - ${Number(d[1]).toFixed(1)}时`
      },
    },
    grid: { left: 56, right: 16, top: 40, bottom: 28 },
    xAxis: {
      type: 'value',
      min: 0,
      max: 24,
      interval: 2,
      axisLabel: { formatter: (v) => (v === 24 ? '24:00' : `${String(v).padStart(2, '0')}:00`), color: chartText },
      splitLine: { lineStyle: { color: chartLine, type: 'dashed' } },
    },
    yAxis: {
      type: 'category',
      data: categories.length ? categories : ['(无工位)'],
      inverse: true,
      axisLine: { lineStyle: { color: chartLine } },
      axisLabel: { color: chartText, fontSize: 11 },
    },
    series,
  })
}

async function loadBoard() {
  if (boardAreaId.value == null || boardAreaId.value === '') {
    boardRows.value = []
    renderGantt()
    localStorage.removeItem('admin_board_area_id')
    return
  }
  try {
    localStorage.setItem('admin_board_area_id', String(boardAreaId.value))
    const res = await http.get('/admin/orders/board', {
      params: { areaSpaceId: boardAreaId.value, day: boardDay.value },
    })
    boardRows.value = res.data || []
    renderGantt()
  } catch (e) {
    ElMessage.error(e.message || '看板加载失败')
  }
}

watch(boardDay, () => {
  if (boardAreaId.value) loadBoard()
})

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
  load()
  loadBoard()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
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
              @change="loadBoard"
            >
              <el-option v-for="item in areaOptions" :key="item.id" :label="item.path" :value="item.id" />
            </el-select>
            <el-date-picker v-model="boardDay" type="date" value-format="YYYY-MM-DD" size="small" style="width: 140px" />
            <el-button size="small" type="primary" @click="loadBoard">刷新</el-button>
          </div>
          <p v-if="!boardAreaId" class="gantt-hint">请选择办公区域后刷新，可显示当日工位占用甘特图</p>
          <div ref="ganttRef" class="chart-box gantt"></div>
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
.gantt {
  flex: 1;
  min-height: 520px;
}
.actions {
  padding-bottom: 8px;
}
.loading {
  color: #94a3b8;
  padding: 24px;
}
</style>

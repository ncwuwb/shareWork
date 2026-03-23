<template>
  <view class="sw-page hall-page">
    <view class="sw-shell">
      <view v-if="upcomingOrder" class="sw-hero hall-upcoming-hero">
        <view class="upcoming-banner">
          <view class="upcoming-body">
            <text class="upcoming-label">最近预约</text>
            <text class="upcoming-title">{{ upcomingOrder.orderNo || `订单 #${upcomingOrder.id}` }}</text>
            <text class="upcoming-time">{{ formatRange(upcomingOrder.startTime, upcomingOrder.endTime) }}</text>
            <view class="upcoming-actions">
              <button class="sw-secondary-button upcoming-btn" @click="goOrders">查看订单</button>
              <button class="sw-primary-button upcoming-btn" @click="goMessages">查看消息</button>
            </view>
          </view>
        </view>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">预约时间</text>
            <text class="sw-section-subtitle">支持全天、上午、下午和自定义时段，最短 1 小时。</text>
          </view>
          <view class="hall-time-summary-stack">
            <text class="hall-time-summary-line hall-time-summary-date">{{ timeSummaryDateLine }}</text>
            <text class="hall-time-summary-line hall-time-summary-time">{{ timeSummaryTimeLine }}</text>
          </view>
        </view>

        <view class="sw-chip-row">
          <view
            v-for="preset in TIME_PRESETS"
            :key="preset.key"
            :class="['sw-chip', currentPreset === preset.key ? 'sw-chip--active' : '']"
            @click="applyPreset(preset.key)"
          >
            {{ preset.label }}
          </view>
        </view>

        <view class="sw-field-grid time-grid">
          <view class="sw-field hall-date-field">
            <text class="sw-label">日期</text>
            <view class="hall-date-md-row">
              <picker mode="selector" :range="monthLabels" :value="monthPickIndex" @change="onMonthPickChange">
                <view class="sw-picker hall-date-md-picker">{{ monthLabels[monthPickIndex] }}</view>
              </picker>
              <picker mode="selector" :range="dayLabels" :value="dayPickIndex" @change="onDayPickChange">
                <view class="sw-picker hall-date-md-picker">{{ dayLabels[dayPickIndex] }}</view>
              </picker>
            </view>
            <text class="hall-date-year-hint">年份默认今年</text>
          </view>
          <view class="sw-field">
            <text class="sw-label">开始时间</text>
            <picker mode="selector" :range="startLabels" :value="startIndex" @change="onStartChange">
              <view class="sw-picker">{{ startLabels[startIndex] }}</view>
            </picker>
          </view>
          <view class="sw-field">
            <text class="sw-label">结束时间</text>
            <picker mode="selector" :range="endLabels" :value="endIndex" @change="onEndChange">
              <view class="sw-picker">{{ endLabels[endIndex] }}</view>
            </picker>
          </view>
        </view>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">请选择您的办公区域</text>
          </view>
        </view>

        <view v-if="spaceError" class="sw-empty">{{ spaceError }}</view>
        <view v-else class="space-stack">
          <view
            v-for="(level, levelIndex) in spaceLevels"
            :key="level.key"
            v-show="levelIndex === 0 || level.options.length"
            class="space-item"
          >
            <text class="sw-label">{{ level.title }}</text>
            <picker
              mode="selector"
              :range="level.options.map((item) => item.name)"
              :value="level.index"
              @change="pickSpace(levelIndex, $event)"
            >
              <view class="sw-picker">
                {{ level.options[level.index] ? level.options[level.index].name : level.placeholder }}
              </view>
            </picker>
          </view>
          <view class="path-card">
            <text class="path-label">当前区域</text>
            <text class="path-value">{{ activeAreaPath || '请选择到最深一级办公区域' }}</text>
          </view>
        </view>
      </view>

      <view class="sw-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">设施偏好</text>
            <text class="sw-section-subtitle">多选过滤，优先找到更适合你的工位环境。</text>
          </view>
          <text class="selection-count">{{ selectedFacilityIds.length }} 项</text>
        </view>

        <view v-if="!facilities.length" class="sw-empty">暂未配置设施标签，仍可按空间和时间进行查询。</view>
        <view v-else class="sw-chip-row">
          <view
            v-for="facility in facilities"
            :key="facility.id"
            :class="['sw-chip', selectedFacilityIds.includes(facility.id) ? 'sw-chip--active' : '']"
            @click="toggleFacility(facility.id)"
          >
            {{ facility.name }}
          </view>
        </view>
      </view>

      <view class="sw-card tools-card">
        <view class="legend-row">
          <view v-for="item in legendItems" :key="item.label" class="legend-item">
            <view class="legend-dot" :class="`legend-dot--${item.tone}`"></view>
            <text class="legend-label">{{ item.label }}</text>
          </view>
        </view>

        <view class="tools-row">
          <view class="sw-chip-row">
            <view
              :class="['sw-chip', viewMode === 'map' ? 'sw-chip--active' : '']"
              @click="viewMode = 'map'"
            >
              地图视图
            </view>
            <view
              :class="['sw-chip', viewMode === 'list' ? 'sw-chip--active' : '']"
              @click="viewMode = 'list'"
            >
              列表视图
            </view>
          </view>
          <button class="sw-primary-button tools-button" :disabled="loading" @click="searchSeats">
            {{ loading ? '查询中...' : '查询工位' }}
          </button>
        </view>
      </view>

      <view class="sw-card workspace-card">
        <view class="sw-section-head">
          <view>
            <text class="sw-section-title">工位分布</text>
            <text class="sw-section-subtitle">
              {{ activeAreaPath || '请先选择区域并执行查询' }}
            </text>
          </view>
          <text class="selection-count">{{ workstations.length }} 个工位</text>
        </view>

        <view v-if="loading" class="sw-empty">正在根据你的筛选条件刷新工位状态...</view>
        <view v-else-if="!workstations.length" class="sw-empty">
          还没有可展示的工位结果。先选择办公区域，再点击“查询工位”。
        </view>

        <view v-else-if="viewMode === 'map'" class="map-stage">
          <view class="map-toolbar">
            <text class="map-tip">双指缩放 / 拖拽查看全景 · 当前缩放 {{ mapScaleLabel }}</text>
            <view class="map-actions">
              <button class="sw-secondary-button map-action" @click="zoomOut">缩小</button>
              <button class="sw-secondary-button map-action" @click="resetMap">重置</button>
              <button class="sw-secondary-button map-action" @click="zoomIn">放大</button>
            </view>
          </view>

          <movable-area class="map-area">
            <movable-view
              class="map-canvas"
              direction="all"
              inertia
              out-of-bounds
              scale
              :scale-value="mapScale"
              :scale-min="0.8"
              :scale-max="2.4"
              :x="mapX"
              :y="mapY"
              :style="mapCanvasStyle"
              @scale="onMapScale"
              @change="onMapChange"
            >
              <view class="workspace-board" :style="boardStyle">
                <view class="board-grid"></view>
                <view
                  v-for="(seat, index) in workstations"
                  :key="seat.id"
                  :class="['seat-node', `seat-node--${getRuntimeStatusMeta(seat.runtimeStatus).tone}`]"
                  :style="seatStyle(seat, index)"
                  @click="openSeat(seat)"
                >
                  <text class="seat-code">{{ seat.code }}</text>
                  <text class="seat-state">{{ getRuntimeStatusMeta(seat.runtimeStatus).label }}</text>
                </view>
              </view>
            </movable-view>
          </movable-area>
        </view>

        <view v-else class="seat-list">
          <view
            v-for="seat in workstations"
            :key="seat.id"
            class="seat-card"
            @click="openSeat(seat)"
          >
            <view class="seat-card-top">
              <view>
                <text class="seat-card-code">{{ seat.code }}</text>
                <text class="seat-card-meta">工位 ID #{{ seat.id }}</text>
              </view>
              <text :class="['sw-pill', `sw-pill--${getRuntimeStatusMeta(seat.runtimeStatus).tone}`]">
                {{ getRuntimeStatusMeta(seat.runtimeStatus).label }}
              </text>
            </view>
            <text class="seat-card-note">{{ seatNote(seat) }}</text>
            <view class="seat-card-tags">
              <text
                v-for="tag in facilityNames(seat)"
                :key="`${seat.id}-${tag}`"
                class="mini-tag"
              >
                {{ tag }}
              </text>
              <text v-if="!facilityNames(seat).length" class="mini-tag mini-tag--empty">未配置设施</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="activeSeat" class="sw-overlay" @click="activeSeat = null">
      <view class="sw-sheet" @click.stop>
        <view class="sheet-top">
          <view>
            <text class="sheet-code">{{ activeSeat.code }}</text>
            <view class="sheet-time-block">
              <text class="sheet-subtitle">预约时段</text>
              <text class="sheet-time-line">{{ timeSummaryDateLine }}</text>
              <text class="sheet-time-line">{{ timeSummaryTimeLine }}</text>
            </view>
          </view>
          <text :class="['sw-pill', `sw-pill--${getRuntimeStatusMeta(activeSeat.runtimeStatus).tone}`]">
            {{ getRuntimeStatusMeta(activeSeat.runtimeStatus).label }}
          </text>
        </view>

        <view class="detail-block">
          <text class="detail-title">工位说明</text>
          <text class="detail-content">{{ seatNote(activeSeat) }}</text>
        </view>

        <view class="detail-block">
          <text class="detail-title">设施标签</text>
          <view class="sheet-tags">
            <text
              v-for="tag in facilityNames(activeSeat)"
              :key="`${activeSeat.id}-${tag}`"
              class="mini-tag"
            >
              {{ tag }}
            </text>
            <text v-if="!facilityNames(activeSeat).length" class="mini-tag mini-tag--empty">未配置设施</text>
          </view>
        </view>

        <view class="detail-block">
          <text class="detail-title">预约规则</text>
          <text class="detail-content">
            同一时段只能预约一个工位；开始前 1 小时外取消不扣分，签到需处于办公区有效范围内。
          </text>
        </view>

        <view class="sheet-actions">
          <button class="sw-secondary-button flex-button" @click="activeSeat = null">稍后再说</button>
          <button
            v-if="activeSeat.runtimeStatus === 0"
            class="sw-primary-button flex-button"
            @click="reserveSeat"
          >
            确认预约
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  HOUR_OPTIONS,
  TIME_PRESETS,
  buildDateTime,
  ensureLogin,
  formatDateKey,
  formatRange,
  getRuntimeStatusMeta,
  showError,
  toDate,
} from '@/utils/app.js'
import { request } from '@/utils/request.js'

const facilities = ref([])
const selectedFacilityIds = ref([])
const workstations = ref([])
const loading = ref(false)
const activeSeat = ref(null)
const viewMode = ref('map')
const upcomingOrder = ref(null)
const spaceError = ref('')
const initialized = ref(false)
const mapScale = ref(1)
const mapX = ref(0)
const mapY = ref(0)

const day = ref(formatDateKey())
/** 月、日选择器索引（年份固定为当前年） */
const monthPickIndex = ref(0)
const dayPickIndex = ref(0)
const startHour = ref(9)
const endHour = ref(18)
const currentPreset = ref('full')
const activeAreaId = ref(null)
const activeAreaPath = ref('')

const spaceLevels = ref(createSpaceLevels())

const legendItems = [
  { label: '可预约', tone: 'emerald' },
  { label: '已预约', tone: 'rose' },
  { label: '使用中', tone: 'amber' },
  { label: '不可用', tone: 'slate' },
]

const monthLabels = Array.from({ length: 12 }, (_, i) => `${i + 1}月`)

function bookingYear() {
  return new Date().getFullYear()
}

function daysInMonth(year, month1to12) {
  return new Date(year, month1to12, 0).getDate()
}

const dayLabels = computed(() => {
  const dim = daysInMonth(bookingYear(), monthPickIndex.value + 1)
  return Array.from({ length: dim }, (_, i) => `${i + 1}日`)
})

const startOptions = HOUR_OPTIONS.slice(0, HOUR_OPTIONS.length - 1)
const startLabels = startOptions.map((item) => item.label)
const startIndex = computed(() =>
  Math.max(
    0,
    startOptions.findIndex((item) => item.value === startHour.value),
  ),
)
const endOptions = computed(() => HOUR_OPTIONS.filter((item) => item.value > startHour.value))
const endLabels = computed(() => endOptions.value.map((item) => item.label))
const endIndex = computed(() =>
  Math.max(
    0,
    endOptions.value.findIndex((item) => item.value === endHour.value),
  ),
)

const facilityMap = computed(() => {
  const result = {}
  facilities.value.forEach((facility) => {
    result[facility.id] = facility.name
  })
  return result
})

const timeSummaryDateLine = computed(() => {
  const m = day.value.match(/^(\d{4})-(\d{2})-(\d{2})$/)
  return m ? `${m[2]}-${m[3]}` : '--'
})

const timeSummaryTimeLine = computed(() => {
  return `${String(startHour.value).padStart(2, '0')}:00 - ${String(endHour.value).padStart(2, '0')}:00`
})

/** 单行摘要（弹窗等场景） */
const timeSummary = computed(() => `${timeSummaryDateLine.value} ${timeSummaryTimeLine.value}`)

const boardHeight = computed(() => {
  const rows = Math.max(4, Math.ceil(workstations.value.length / 5))
  return rows * 124 + 120
})

const boardStyle = computed(() => `height:${boardHeight.value}px; width:980px;`)
const mapCanvasStyle = computed(() => `width:980px; height:${boardHeight.value}px;`)
const mapScaleLabel = computed(() => `${Math.round(mapScale.value * 100)}%`)

onShow(async () => {
  if (!ensureLogin()) {
    return
  }
  normalizeDayToCurrentYear()
  syncPickersFromDay()
  if (!initialized.value) {
    initialized.value = true
    await Promise.all([loadFacilities(), loadSpaceLevel(0, 0)])
  }
  await loadUpcomingOrder()
})

function createSpaceLevels() {
  return [
    { key: 'campus', title: '园区', placeholder: '请选择园区', options: [], index: 0 },
    { key: 'building', title: '楼宇', placeholder: '请选择楼宇', options: [], index: 0 },
    { key: 'floor', title: '楼层', placeholder: '请选择楼层', options: [], index: 0 },
    { key: 'area', title: '区域', placeholder: '请选择办公区域', options: [], index: 0 },
  ]
}

function applyPreset(key) {
  currentPreset.value = key
  if (key === 'custom') {
    return
  }
  const preset = TIME_PRESETS.find((item) => item.key === key)
  if (!preset) return
  startHour.value = preset.start
  endHour.value = preset.end
}

function commitDayFromPickers() {
  const y = bookingYear()
  const mm = String(monthPickIndex.value + 1).padStart(2, '0')
  const dd = String(dayPickIndex.value + 1).padStart(2, '0')
  day.value = `${y}-${mm}-${dd}`
}

function syncPickersFromDay() {
  const m = day.value.match(/^(\d{4})-(\d{2})-(\d{2})$/)
  if (!m) {
    day.value = formatDateKey()
    return syncPickersFromDay()
  }
  const y = bookingYear()
  let mm = Number(m[2])
  let dd = Number(m[3])
  if (mm < 1 || mm > 12) {
    mm = new Date().getMonth() + 1
  }
  const dim = daysInMonth(y, mm)
  dd = Math.min(Math.max(1, dd), dim)
  monthPickIndex.value = mm - 1
  dayPickIndex.value = dd - 1
  day.value = `${y}-${String(mm).padStart(2, '0')}-${String(dd).padStart(2, '0')}`
}

function normalizeDayToCurrentYear() {
  const m = day.value.match(/^(\d{4})-(\d{2})-(\d{2})$/)
  if (!m) {
    day.value = formatDateKey()
    return
  }
  const y = bookingYear()
  const mm = Number(m[2])
  const ddRaw = Number(m[3])
  const dim = daysInMonth(y, mm)
  const dd = Math.min(Math.max(1, ddRaw), dim)
  day.value = `${y}-${String(mm).padStart(2, '0')}-${String(dd).padStart(2, '0')}`
}

function onMonthPickChange(event) {
  monthPickIndex.value = Number(event.detail.value)
  const dim = daysInMonth(bookingYear(), monthPickIndex.value + 1)
  if (dayPickIndex.value >= dim) {
    dayPickIndex.value = dim - 1
  }
  commitDayFromPickers()
}

function onDayPickChange(event) {
  dayPickIndex.value = Number(event.detail.value)
  commitDayFromPickers()
}

syncPickersFromDay()

function onStartChange(event) {
  const option = startOptions[Number(event.detail.value)]
  if (!option) return
  startHour.value = option.value
  if (endHour.value <= startHour.value) {
    endHour.value = Math.min(23, startHour.value + 1)
  }
  currentPreset.value = 'custom'
}

function onEndChange(event) {
  const option = endOptions.value[Number(event.detail.value)]
  if (!option) return
  endHour.value = option.value
  currentPreset.value = 'custom'
}

async function loadFacilities() {
  try {
    const response = await request({ url: '/api/app/workstations/facilities' })
    facilities.value = response.data || []
  } catch (error) {
    facilities.value = []
  }
}

async function loadSpaceLevel(parentId, levelIndex) {
  try {
    spaceError.value = ''
    const response = await request({ url: `/api/app/spaces/children?parentId=${parentId}` })
    spaceLevels.value[levelIndex].options = (response.data || []).map((item) => ({
      id: item.id,
      name: item.name,
    }))
    spaceLevels.value[levelIndex].index = 0
  } catch (error) {
    spaceError.value = '空间层级加载失败，请检查网络或后端空间配置。'
  }
}

async function pickSpace(levelIndex, event) {
  const selectedIndex = Number(event.detail.value)
  const currentLevel = spaceLevels.value[levelIndex]
  currentLevel.index = selectedIndex

  for (let index = levelIndex + 1; index < spaceLevels.value.length; index += 1) {
    spaceLevels.value[index].options = []
    spaceLevels.value[index].index = 0
  }

  const selectedOption = currentLevel.options[selectedIndex]
  activeAreaId.value = null
  activeAreaPath.value = selectedPathUntil(levelIndex)
  if (!selectedOption || levelIndex === spaceLevels.value.length - 1) {
    activeAreaId.value = selectedOption ? selectedOption.id : null
    return
  }

  try {
    const response = await request({
      url: `/api/app/spaces/children?parentId=${selectedOption.id}`,
    })
    const children = (response.data || []).map((item) => ({
      id: item.id,
      name: item.name,
    }))
    if (!children.length) {
      activeAreaId.value = selectedOption.id
      return
    }
    spaceLevels.value[levelIndex + 1].options = children
  } catch (error) {
    showError(error, '下级空间加载失败')
  }
}

function selectedPathUntil(levelIndex) {
  return spaceLevels.value
    .slice(0, levelIndex + 1)
    .map((level) => level.options[level.index] && level.options[level.index].name)
    .filter(Boolean)
    .join(' / ')
}

function toggleFacility(id) {
  if (selectedFacilityIds.value.includes(id)) {
    selectedFacilityIds.value = selectedFacilityIds.value.filter((item) => item !== id)
    return
  }
  selectedFacilityIds.value = [...selectedFacilityIds.value, id]
}

async function loadUpcomingOrder() {
  try {
    const response = await request({ url: '/api/app/orders?page=1&size=20' })
    const records = (response.data && response.data.records) || []
    const target = records
      .filter((item) => item.status === 0 || item.status === 1)
      .sort((first, second) => {
        const firstTime = toDate(first.startTime)
        const secondTime = toDate(second.startTime)
        return (firstTime ? firstTime.getTime() : 0) - (secondTime ? secondTime.getTime() : 0)
      })[0]
    upcomingOrder.value = target || null
  } catch (error) {
    upcomingOrder.value = null
  }
}

async function searchSeats() {
  if (!activeAreaId.value) {
    showError({ message: '请先逐级选择到办公区域' }, '请先逐级选择到办公区域')
    return
  }
  loading.value = true
  activeSeat.value = null
  try {
    const params = [
      `spaceId=${activeAreaId.value}`,
      `startTime=${encodeURIComponent(buildDateTime(day.value, startHour.value))}`,
      `endTime=${encodeURIComponent(buildDateTime(day.value, endHour.value))}`,
    ]
    selectedFacilityIds.value.forEach((id) => {
      params.push(`facilityIds=${id}`)
    })
    const response = await request({
      url: `/api/app/workstations?${params.join('&')}`,
    })
    workstations.value = response.data || []
    resetMap()
  } catch (error) {
    showError(error, '工位查询失败')
  } finally {
    loading.value = false
  }
}

function goOrders() {
  uni.switchTab({ url: '/pages/orders/orders' })
}

function goMessages() {
  uni.switchTab({ url: '/pages/messages/messages' })
}

function openSeat(seat) {
  activeSeat.value = seat
}

function seatNote(seat) {
  if (seat.baseStatus === 1) return '维护锁定：该工位暂不开放预约。'
  if (seat.baseStatus === 2) return '专属保留：仅指定人员可在该时段使用。'
  return getRuntimeStatusMeta(seat.runtimeStatus).description
}

function facilityNames(seat) {
  return (seat.facilityIds || [])
    .map((id) => facilityMap.value[id])
    .filter(Boolean)
}

function seatStyle(seat, index) {
  const fallbackColumn = index % 5
  const fallbackRow = Math.floor(index / 5)
  const x = normalizeCoord(seat.coordX, 40 + fallbackColumn * 182, 840)
  const y = normalizeCoord(seat.coordY, 42 + fallbackRow * 124, Math.max(80, boardHeight.value - 100))
  return `left:${x}px; top:${y}px;`
}

function normalizeCoord(value, fallback, max) {
  const numeric = Number(value)
  if (!Number.isNaN(numeric) && numeric > 0) {
    return Math.max(20, Math.min(numeric, max))
  }
  return fallback
}

function onMapScale(event) {
  const nextScale = Number(event && event.detail && event.detail.scale)
  if (!Number.isNaN(nextScale) && nextScale > 0) {
    mapScale.value = Number(nextScale.toFixed(2))
  }
}

function onMapChange(event) {
  const detail = (event && event.detail) || {}
  if (typeof detail.x === 'number') {
    mapX.value = detail.x
  }
  if (typeof detail.y === 'number') {
    mapY.value = detail.y
  }
}

function zoomIn() {
  mapScale.value = Math.min(2.4, Number((mapScale.value + 0.2).toFixed(2)))
}

function zoomOut() {
  mapScale.value = Math.max(0.8, Number((mapScale.value - 0.2).toFixed(2)))
}

function resetMap() {
  mapScale.value = 1
  mapX.value = 0
  mapY.value = 0
}

async function reserveSeat() {
  if (!activeSeat.value) return

  const confirmed = await new Promise((resolve) => {
    uni.showModal({
      title: '确认预约',
      content: `工位：${activeSeat.value.code}\n时段：${timeSummary.value}\n开始前 1 小时内取消会扣分，是否继续？`,
      success: (result) => resolve(Boolean(result.confirm)),
      fail: () => resolve(false),
    })
  })

  if (!confirmed) return

  try {
    await request({
      url: '/api/app/orders',
      method: 'POST',
      data: {
        workstationId: activeSeat.value.id,
        startTime: buildDateTime(day.value, startHour.value),
        endTime: buildDateTime(day.value, endHour.value),
      },
    })
    uni.showToast({ title: '预约成功', icon: 'success' })
    activeSeat.value = null
    await Promise.all([searchSeats(), loadUpcomingOrder()])
  } catch (error) {
    showError(error, '预约失败，请稍后重试')
  }
}
</script>

<style scoped>
.hall-page {
  padding-bottom: 52rpx;
}

.hall-upcoming-hero {
  margin-bottom: 0;
}

.upcoming-banner {
  margin-top: 0;
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(15, 23, 42, 0.44);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.upcoming-body {
  width: 100%;
}

.upcoming-label,
.upcoming-title,
.upcoming-time {
  display: block;
}

.upcoming-label {
  font-size: 22rpx;
  color: var(--sw-text-muted);
}

.upcoming-title {
  margin-top: 10rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #f8fafc;
}

.upcoming-time {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--sw-text-muted);
}

.upcoming-actions {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  margin-top: 22rpx;
}

.upcoming-btn {
  flex: 1;
  min-height: 76rpx;
  font-size: 26rpx;
}

.selection-count {
  font-size: 22rpx;
  color: var(--sw-accent);
  font-weight: 700;
}

.hall-time-summary-stack {
  flex-shrink: 0;
  text-align: right;
}

.hall-time-summary-line {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: var(--sw-accent);
  line-height: 1.35;
}

.hall-time-summary-date {
  font-size: 26rpx;
}

.hall-time-summary-time {
  margin-top: 4rpx;
  font-size: 22rpx;
  font-weight: 600;
  color: var(--sw-text-muted);
}

.hall-date-md-row {
  display: flex;
  flex-direction: row;
  gap: 12rpx;
}

.hall-date-md-picker {
  flex: 1;
  min-width: 0;
  justify-content: center;
  text-align: center;
}

.hall-date-year-hint {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: var(--sw-text-soft);
}

.sheet-time-block {
  margin-top: 6rpx;
}

.sheet-time-line {
  display: block;
  font-size: 24rpx;
  color: var(--sw-text-muted);
  line-height: 1.4;
}

.time-grid {
  margin-top: 22rpx;
}

.space-stack {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.path-card {
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: rgba(15, 23, 42, 0.44);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.path-label {
  display: block;
  font-size: 22rpx;
  color: var(--sw-text-muted);
}

.path-value {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  line-height: 1.6;
  color: #f8fafc;
}

.tools-card {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.legend-row {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.legend-dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
}

.legend-dot--emerald {
  background: var(--sw-success);
}

.legend-dot--amber {
  background: var(--sw-warning);
}

.legend-dot--rose {
  background: var(--sw-danger);
}

.legend-dot--slate {
  background: #94a3b8;
}

.legend-label {
  font-size: 22rpx;
  color: var(--sw-text-muted);
}

.tools-row {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  align-items: center;
}

.tools-button {
  width: 208rpx;
  min-height: 84rpx;
  font-size: 24rpx;
}

.workspace-card {
  overflow: hidden;
}

.map-stage {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.map-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  align-items: center;
  flex-wrap: wrap;
}

.map-tip {
  font-size: 22rpx;
  color: var(--sw-text-muted);
}

.map-actions {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.map-action {
  min-height: 72rpx;
  padding: 0 24rpx;
  font-size: 22rpx;
  border-radius: 20rpx;
}

.map-area {
  width: 100%;
  height: 760rpx;
  border-radius: 28rpx;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.24);
}

.map-canvas {
  width: 980px;
}

.workspace-board {
  position: relative;
  border-radius: 28rpx;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.86), rgba(8, 17, 31, 0.92)),
    linear-gradient(90deg, rgba(56, 189, 248, 0.04), transparent 60%);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.board-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px);
  background-size: 64px 64px;
}

.seat-node {
  position: absolute;
  width: 148rpx;
  padding: 16rpx 14rpx;
  border-radius: 22rpx;
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 0 14rpx 36rpx rgba(2, 6, 23, 0.24);
}

.seat-node--emerald {
  background: rgba(52, 211, 153, 0.14);
}

.seat-node--amber {
  background: rgba(251, 191, 36, 0.16);
}

.seat-node--rose {
  background: rgba(251, 113, 133, 0.14);
}

.seat-node--slate {
  background: rgba(51, 65, 85, 0.64);
}

.seat-code,
.seat-state {
  display: block;
}

.seat-code {
  font-size: 26rpx;
  font-weight: 700;
  color: #f8fafc;
}

.seat-state {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--sw-text-muted);
}

.seat-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.seat-card {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(15, 23, 42, 0.44);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.seat-card-top {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  align-items: flex-start;
}

.seat-card-code,
.seat-card-meta,
.seat-card-note {
  display: block;
}

.seat-card-code {
  font-size: 30rpx;
  font-weight: 700;
  color: #f8fafc;
}

.seat-card-meta {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--sw-text-muted);
}

.seat-card-note {
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.65;
  color: var(--sw-text-muted);
}

.seat-card-tags,
.sheet-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.mini-tag {
  padding: 10rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(56, 189, 248, 0.12);
  color: var(--sw-accent);
  font-size: 22rpx;
}

.mini-tag--empty {
  background: rgba(148, 163, 184, 0.14);
  color: #cbd5e1;
}

.sheet-top {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  align-items: flex-start;
}

.sheet-code,
.sheet-subtitle,
.detail-title,
.detail-content {
  display: block;
}

.sheet-code {
  font-size: 36rpx;
  font-weight: 700;
  color: #f8fafc;
}

.sheet-subtitle {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: var(--sw-text-muted);
}

.detail-block {
  margin-top: 24rpx;
}

.detail-title {
  font-size: 24rpx;
  color: var(--sw-text-soft);
}

.detail-content {
  margin-top: 10rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: var(--sw-text);
}

.sheet-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 30rpx;
}

.flex-button {
  flex: 1;
}
</style>

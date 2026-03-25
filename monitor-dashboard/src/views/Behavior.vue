<template>
  <div class="behavior">
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="应用ID">
          <el-input v-model="appId" placeholder="请输入应用ID" />
        </el-form-item>
        <el-form-item label="行为类型">
          <el-select v-model="behaviorType" placeholder="请选择行为类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="点击" value="click" />
            <el-option label="滚动" value="scroll" />
            <el-option label="页面浏览" value="page_view" />
            <el-option label="曝光" value="exposure" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">点击次数</div>
          <div class="stat-value">{{ stats.click }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">滚动次数</div>
          <div class="stat-value">{{ stats.scroll }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">页面浏览</div>
          <div class="stat-value">{{ stats.pageView }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">曝光次数</div>
          <div class="stat-value">{{ stats.exposure }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <div ref="typeChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div ref="trendChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">行为列表</div>
      </template>
      <el-table :data="behaviors" stripe>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBehaviorTypeColor(row.type)">{{ getBehaviorTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventName" label="事件名称" width="150" />
        <el-table-column prop="url" label="页面URL" show-overflow-tooltip />
        <el-table-column prop="timestamp" label="时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.timestamp).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="text" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="行为详情" width="800px">
      <div v-if="currentBehavior" class="behavior-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="行为类型">{{ getBehaviorTypeText(currentBehavior.type) }}</el-descriptions-item>
          <el-descriptions-item label="事件名称">{{ currentBehavior.eventName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="页面URL">{{ currentBehavior.url }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ new Date(currentBehavior.timestamp).toLocaleString() }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentBehavior.elementInfo" class="info-section">
          <div class="info-title">元素信息:</div>
          <pre class="info-content">{{ JSON.stringify(currentBehavior.elementInfo, null, 2) }}</pre>
        </div>
        <div v-if="currentBehavior.eventData" class="info-section">
          <div class="info-title">事件数据:</div>
          <pre class="info-content">{{ JSON.stringify(currentBehavior.eventData, null, 2) }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const appId = ref('demo-app')
const behaviorType = ref('')
const stats = ref({
  click: 0,
  scroll: 0,
  pageView: 0,
  exposure: 0
})
const behaviors = ref([])
const detailVisible = ref(false)
const currentBehavior = ref(null)
const typeChart = ref(null)
const trendChart = ref(null)
let charts = []

const loadData = async () => {
  try {
    const params = { appId: appId.value }
    if (behaviorType.value) {
      params.type = behaviorType.value
    }

    const { data } = await axios.get('/api/query/behavior', { params })
    
    if (data.success) {
      behaviors.value = data.data
      processStats(data.data)
      updateCharts(data.data)
    }
  } catch (error) {
    console.error('Load data failed:', error)
  }
}

const processStats = (data) => {
  stats.value = {
    click: data.filter(b => b.type === 'click').length,
    scroll: data.filter(b => b.type === 'scroll').length,
    pageView: data.filter(b => b.type === 'page_view').length,
    exposure: data.filter(b => b.type === 'exposure').length
  }
}

const updateCharts = (data) => {
  const typeStats = {}
  data.forEach(item => {
    typeStats[item.type] = (typeStats[item.type] || 0) + 1
  })

  if (charts[0]) {
    charts[0].setOption({
      series: [{
        data: Object.keys(typeStats).map(type => ({
          value: typeStats[type],
          name: getBehaviorTypeText(type)
        }))
      }]
    })
  }

  if (charts[1]) {
    const timeStats = {}
    data.forEach(item => {
      const hour = new Date(item.timestamp).getHours()
      timeStats[hour] = (timeStats[hour] || 0) + 1
    })

    charts[1].setOption({
      xAxis: { data: Object.keys(timeStats).map(h => `${h}:00`) },
      series: [{
        data: Object.values(timeStats)
      }]
    })
  }
}

const initCharts = () => {
  const chart1 = echarts.init(typeChart.value)
  chart1.setOption({
    title: { text: '行为类型分布', left: 'center' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: []
    }]
  })
  charts.push(chart1)

  const chart2 = echarts.init(trendChart.value)
  chart2.setOption({
    title: { text: '行为趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      data: [],
      itemStyle: { color: '#409eff' }
    }]
  })
  charts.push(chart2)
}

const showDetail = (behavior) => {
  currentBehavior.value = behavior
  detailVisible.value = true
}

const getBehaviorTypeText = (type) => {
  const texts = {
    click: '点击',
    scroll: '滚动',
    page_view: '页面浏览',
    page_leave: '页面离开',
    track: '自定义事件',
    exposure: '曝光'
  }
  return texts[type] || type
}

const getBehaviorTypeColor = (type) => {
  const colors = {
    click: 'primary',
    scroll: 'success',
    page_view: 'info',
    exposure: 'warning'
  }
  return colors[type] || ''
}

onMounted(() => {
  initCharts()
  loadData()
})

onUnmounted(() => {
  charts.forEach(chart => chart.dispose())
})
</script>

<style scoped>
.behavior {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  padding: 20px;
  background: white;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.charts-row {
  margin-bottom: 20px;
}

.chart {
  width: 100%;
  height: 400px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.behavior-detail {
  padding: 20px;
}

.info-section {
  margin-top: 20px;
}

.info-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.info-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: #606266;
  overflow-x: auto;
}
</style>

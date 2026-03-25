<template>
  <div class="error">
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="应用ID">
          <el-input v-model="appId" placeholder="请输入应用ID" />
        </el-form-item>
        <el-form-item label="错误类型">
          <el-select v-model="errorType" placeholder="请选择错误类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="JS错误" value="jsError" />
            <el-option label="Promise错误" value="promiseError" />
            <el-option label="资源错误" value="resourceError" />
            <el-option label="Vue错误" value="vueError" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">总错误数</div>
          <div class="stat-value error">{{ stats.total }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">JS错误</div>
          <div class="stat-value">{{ stats.jsError }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">Promise错误</div>
          <div class="stat-value">{{ stats.promiseError }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card>
          <div ref="distributionChart" class="chart"></div>
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
        <div class="table-header">错误列表</div>
      </template>
      <el-table :data="errors" stripe>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getErrorTypeColor(row.type)">{{ getErrorTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="错误信息" show-overflow-tooltip />
        <el-table-column prop="source" label="文件" width="200" />
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

    <el-dialog v-model="detailVisible" title="错误详情" width="800px">
      <div v-if="currentError" class="error-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="错误类型">{{ getErrorTypeText(currentError.type) }}</el-descriptions-item>
          <el-descriptions-item label="错误信息">{{ currentError.message }}</el-descriptions-item>
          <el-descriptions-item label="文件">{{ currentError.source || '-' }}</el-descriptions-item>
          <el-descriptions-item label="行号">{{ currentError.lineno || '-' }}</el-descriptions-item>
          <el-descriptions-item label="列号">{{ currentError.colno || '-' }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ new Date(currentError.timestamp).toLocaleString() }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="currentError.stack" class="stack-section">
          <div class="stack-title">堆栈信息:</div>
          <pre class="stack-content">{{ currentError.stack }}</pre>
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
const errorType = ref('')
const stats = ref({
  total: 0,
  jsError: 0,
  promiseError: 0,
  resourceError: 0,
  vueError: 0
})
const errors = ref([])
const detailVisible = ref(false)
const currentError = ref(null)
const distributionChart = ref(null)
const trendChart = ref(null)
let charts = []

const loadData = async () => {
  try {
    const params = { appId: appId.value }
    if (errorType.value) {
      params.type = errorType.value
    }

    const { data } = await axios.get('/api/query/errors', { params })
    
    if (data.success) {
      errors.value = data.data
      processStats(data.data)
      updateCharts(data.data)
    }
  } catch (error) {
    console.error('Load data failed:', error)
  }
}

const processStats = (data) => {
  stats.value = {
    total: data.length,
    jsError: data.filter(e => e.type === 'jsError').length,
    promiseError: data.filter(e => e.type === 'promiseError').length,
    resourceError: data.filter(e => e.type === 'resourceError').length,
    vueError: data.filter(e => e.type === 'vueError').length
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
          name: getErrorTypeText(type)
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
  const distChart = echarts.init(distributionChart.value)
  distChart.setOption({
    title: { text: '错误类型分布', left: 'center' },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: []
    }]
  })
  charts.push(distChart)

  const trendChartInstance = echarts.init(trendChart.value)
  trendChartInstance.setOption({
    title: { text: '错误趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      data: [],
      itemStyle: { color: '#f56c6c' }
    }]
  })
  charts.push(trendChartInstance)
}

const showDetail = (error) => {
  currentError.value = error
  detailVisible.value = true
}

const getErrorTypeText = (type) => {
  const texts = {
    jsError: 'JS错误',
    promiseError: 'Promise错误',
    resourceError: '资源错误',
    vueError: 'Vue错误'
  }
  return texts[type] || type
}

const getErrorTypeColor = (type) => {
  const colors = {
    jsError: 'danger',
    promiseError: 'warning',
    resourceError: 'info',
    vueError: 'danger'
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
.error {
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

.stat-value.error {
  color: #f56c6c;
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

.error-detail {
  padding: 20px;
}

.stack-section {
  margin-top: 20px;
}

.stack-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.stack-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: #606266;
  overflow-x: auto;
}
</style>

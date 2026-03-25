<template>
  <div class="performance">
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="应用ID">
          <el-input v-model="appId" placeholder="请输入应用ID" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">平均FCP</div>
          <div class="stat-value">{{ stats.avgFCP }}ms</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">平均LCP</div>
          <div class="stat-value">{{ stats.avgLCP }}ms</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">平均FID</div>
          <div class="stat-value">{{ stats.avgFID }}ms</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">平均CLS</div>
          <div class="stat-value">{{ stats.avgCLS }}</div>
        </div>
      </el-col>
    </el-row>

    <el-card class="chart-card">
      <div ref="trendChart" class="chart"></div>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">慢资源列表</div>
      </template>
      <el-table :data="slowResources" stripe>
        <el-table-column prop="name" label="资源名称" />
        <el-table-column prop="resourceType" label="类型" />
        <el-table-column prop="duration" label="耗时(ms)" />
        <el-table-column prop="size" label="大小" />
        <el-table-column prop="timestamp" label="时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const appId = ref('demo-app')
const dateRange = ref([])
const stats = ref({
  avgFCP: 0,
  avgLCP: 0,
  avgFID: 0,
  avgCLS: 0
})
const slowResources = ref([])
const trendChart = ref(null)
let chart = null

const loadData = async () => {
  try {
    const params = { appId: appId.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const { data } = await axios.get('/api/query/performance', { params })
    
    if (data.success) {
      processData(data.data)
    }
  } catch (error) {
    console.error('Load data failed:', error)
  }
}

const processData = (data) => {
  const metrics = data.filter(item => item.subType === 'navigation')
  
  if (metrics.length > 0) {
    stats.value = {
      avgFCP: Math.round(metrics.reduce((sum, item) => sum + (item.metrics?.fcp || 0), 0) / metrics.length),
      avgLCP: Math.round(metrics.reduce((sum, item) => sum + (item.metrics?.lcp || 0), 0) / metrics.length),
      avgFID: Math.round(metrics.reduce((sum, item) => sum + (item.metrics?.fid || 0), 0) / metrics.length),
      avgCLS: (metrics.reduce((sum, item) => sum + (item.metrics?.cls || 0), 0) / metrics.length).toFixed(3)
    }
  }

  slowResources.value = data
    .filter(item => item.subType === 'resource' && item.duration > 2000)
    .slice(0, 20)

  updateTrendChart(data)
}

const updateTrendChart = (data) => {
  if (!chart) return

  const trendData = data.filter(item => item.subType === 'navigation')
  const xData = trendData.map(item => new Date(item.timestamp).toLocaleTimeString())
  
  chart.setOption({
    xAxis: { data: xData },
    series: [
      { data: trendData.map(item => item.metrics?.fcp || 0) },
      { data: trendData.map(item => item.metrics?.lcp || 0) },
      { data: trendData.map(item => item.metrics?.fid || 0) }
    ]
  })
}

const initChart = () => {
  chart = echarts.init(trendChart.value)
  
  const option = {
    title: { text: '性能趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['FCP', 'LCP', 'FID'], bottom: 0 },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value', name: '时间(ms)' },
    series: [
      { name: 'FCP', type: 'line', data: [], itemStyle: { color: '#409eff' } },
      { name: 'LCP', type: 'line', data: [], itemStyle: { color: '#67c23a' } },
      { name: 'FID', type: 'line', data: [], itemStyle: { color: '#e6a23c' } }
    ]
  }
  
  chart.setOption(option)
}

onMounted(() => {
  initChart()
  loadData()
})

onUnmounted(() => {
  if (chart) chart.dispose()
})
</script>

<style scoped>
.performance {
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

.chart-card {
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
</style>

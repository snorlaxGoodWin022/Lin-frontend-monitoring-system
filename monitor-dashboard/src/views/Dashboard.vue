<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">今日PV</div>
          <div class="stat-value">{{ stats.pv.toLocaleString() }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">今日UV</div>
          <div class="stat-value">{{ stats.uv.toLocaleString() }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">错误数</div>
          <div class="stat-value error">{{ stats.errorCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">平均加载</div>
          <div class="stat-value">{{ stats.avgLoadTime }}ms</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">性能趋势</div>
          <div ref="performanceChart" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">错误分布</div>
          <div ref="errorChart" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-title">PV/UV趋势</div>
          <div ref="pvUvChart" class="chart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const stats = ref({
  pv: 0,
  uv: 0,
  errorCount: 0,
  avgLoadTime: 0
})

const performanceChart = ref(null)
const errorChart = ref(null)
const pvUvChart = ref(null)
let charts = []
let timer = null

const loadStats = async () => {
  try {
    const { data } = await axios.get('/api/dashboard/stats', {
      params: { appId: 'demo-app' }
    })
    if (data.success) {
      stats.value = {
        pv: data.stats.page_view || 0,
        uv: data.stats.page_view || 0,
        errorCount: data.stats.error || 0,
        avgLoadTime: 0
      }
    }
  } catch (error) {
    console.error('Load stats failed:', error)
  }
}

const initPerformanceChart = () => {
  const chart = echarts.init(performanceChart.value)
  
  const option = {
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
  charts.push(chart)
}

const initErrorChart = () => {
  const chart = echarts.init(errorChart.value)
  
  const option = {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: []
    }]
  }
  
  chart.setOption(option)
  charts.push(chart)
}

const initPvUvChart = () => {
  const chart = echarts.init(pvUvChart.value)
  
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['PV', 'UV'], bottom: 0 },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value' },
    series: [
      { name: 'PV', type: 'line', data: [], itemStyle: { color: '#409eff' } },
      { name: 'UV', type: 'line', data: [], itemStyle: { color: '#67c23a' } }
    ]
  }
  
  chart.setOption(option)
  charts.push(chart)
}

onMounted(async () => {
  await loadStats()
  initPerformanceChart()
  initErrorChart()
  initPvUvChart()
  
  timer = setInterval(loadStats, 10000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  charts.forEach(chart => chart.dispose())
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  padding: 30px;
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
  font-size: 36px;
  font-weight: 700;
  color: #303133;
}

.stat-value.error {
  color: #f56c6c;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.chart {
  width: 100%;
  height: 400px;
}
</style>

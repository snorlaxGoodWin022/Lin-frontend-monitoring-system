const express = require('express')
const router = express.Router()
const Performance = require('../models/performance')
const Error = require('../models/error')
const Behavior = require('../models/behavior')
const { validateData, cleanData } = require('../utils/processor')
const redis = require('../utils/redis')

router.post('/', async (req, res) => {
  try {
    let data = req.body

    if (!Array.isArray(data)) {
      data = [data]
    }

    const validation = validateData(data)
    if (!validation.valid) {
      return res.status(400).json({ error: validation.message })
    }

    const cleanedData = cleanData(data)
    const categorizedData = categorizeData(cleanedData)

    await Promise.all([
      savePerformanceData(categorizedData.performance),
      saveErrorData(categorizedData.error),
      saveBehaviorData(categorizedData.behavior)
    ])

    await updateRealtimeStats(categorizedData)

    res.json({ success: true, count: data.length })
  } catch (error) {
    console.error('Report error:', error)
    res.status(500).json({ error: 'Internal server error' })
  }
})

function categorizeData(data) {
  const categorized = {
    performance: [],
    error: [],
    behavior: []
  }

  data.forEach(item => {
    if (item.type === 'performance' || item.type === 'api') {
      categorized.performance.push(item)
    } else if (item.type === 'error') {
      categorized.error.push(item)
    } else if (item.type === 'click' || item.type === 'track' || item.type === 'scroll' || item.type === 'page_view' || item.type === 'page_leave' || item.type === 'exposure') {
      categorized.behavior.push(item)
    }
  })

  return categorized
}

async function savePerformanceData(data) {
  if (data.length === 0) return
  await Performance.insertMany(data)
}

async function saveErrorData(data) {
  if (data.length === 0) return
  await Error.insertMany(data)
}

async function saveBehaviorData(data) {
  if (data.length === 0) return
  await Behavior.insertMany(data)
}

async function updateRealtimeStats(data) {
  const client = redis.getClient()
  
  for (const item of data) {
    const key = `stats:${item.appId}:${item.type}`
    await client.hIncrBy(key, 'count', 1)
    await client.expire(key, 3600)
  }
}

module.exports = router

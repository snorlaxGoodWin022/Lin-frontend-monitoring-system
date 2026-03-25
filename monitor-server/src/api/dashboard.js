const express = require('express')
const router = express.Router()
const Performance = require('../models/performance')
const Error = require('../models/error')
const Behavior = require('../models/behavior')
const redis = require('../utils/redis')

router.get('/stats', async (req, res) => {
  try {
    const { appId } = req.query
    const client = redis.getClient()

    const keys = await client.keys(`stats:${appId}:*`)
    
    const stats = {}
    for (const key of keys) {
      const count = await client.hGet(key, 'count')
      const type = key.split(':')[2]
      stats[type] = parseInt(count) || 0
    }

    res.json({ success: true, stats })
  } catch (error) {
    console.error('Dashboard error:', error)
    res.status(500).json({ error: 'Internal server error' })
  }
})

router.get('/performance-trend', async (req, res) => {
  try {
    const { appId, hours = 24 } = req.query
    const startTime = new Date(Date.now() - hours * 60 * 60 * 1000)

    const data = await Performance.aggregate([
      { $match: { appId, timestamp: { $gte: startTime } } },
      { $group: {
        _id: {
          $dateToString: { format: '%Y-%m-%d %H:00', date: '$timestamp' }
        },
        avgFCP: { $avg: '$metrics.fcp' },
        avgLCP: { $avg: '$metrics.lcp' },
        avgFID: { $avg: '$metrics.fid' },
        avgCLS: { $avg: '$metrics.cls' },
        count: { $sum: 1 }
      }},
      { $sort: { _id: 1 } }
    ])

    res.json({ success: true, data })
  } catch (error) {
    console.error('Dashboard error:', error)
    res.status(500).json({ error: 'Internal server error' })
  }
})

router.get('/error-distribution', async (req, res) => {
  try {
    const { appId, hours = 24 } = req.query
    const startTime = new Date(Date.now() - hours * 60 * 60 * 1000)

    const data = await Error.aggregate([
      { $match: { appId, timestamp: { $gte: startTime } } },
      { $group: {
        _id: '$type',
        count: { $sum: 1 }
      }},
      { $sort: { count: -1 } }
    ])

    res.json({ success: true, data })
  } catch (error) {
    console.error('Dashboard error:', error)
    res.status(500).json({ error: 'Internal server error' })
  }
})

router.get('/pv-uv', async (req, res) => {
  try {
    const { appId, hours = 24 } = req.query
    const startTime = new Date(Date.now() - hours * 60 * 60 * 1000)

    const pvData = await Behavior.aggregate([
      { $match: { appId, type: 'page_view', timestamp: { $gte: startTime } } },
      { $group: {
        _id: {
          $dateToString: { format: '%Y-%m-%d %H:00', date: '$timestamp' }
        },
        pv: { $sum: 1 }
      }},
      { $sort: { _id: 1 } }
    ])

    const uvData = await Behavior.aggregate([
      { $match: { appId, type: 'page_view', timestamp: { $gte: startTime } } },
      { $group: {
        _id: {
          $dateToString: { format: '%Y-%m-%d %H:00', date: '$timestamp' },
          userId: '$userId'
        }
      }},
      { $group: {
        _id: '$_id.0',
        uv: { $sum: 1 }
      }},
      { $sort: { _id: 1 } }
    ])

    res.json({ success: true, pvData, uvData })
  } catch (error) {
    console.error('Dashboard error:', error)
    res.status(500).json({ error: 'Internal server error' })
  }
})

module.exports = router

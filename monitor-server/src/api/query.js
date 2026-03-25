const express = require('express')
const router = express.Router()
const Performance = require('../models/performance')
const Error = require('../models/error')
const Behavior = require('../models/behavior')

router.get('/performance', async (req, res) => {
  try {
    const { appId, startTime, endTime, limit = 100 } = req.query
    
    const query = {}
    if (appId) query.appId = appId
    if (startTime || endTime) {
      query.timestamp = {}
      if (startTime) query.timestamp.$gte = new Date(startTime)
      if (endTime) query.timestamp.$lte = new Date(endTime)
    }

    const data = await Performance.find(query)
      .sort({ timestamp: -1 })
      .limit(parseInt(limit))

    res.json({ success: true, data })
  } catch (error) {
    console.error('Query error:', error)
    res.status(500).json({ error: 'Internal server error' })
  }
})

router.get('/errors', async (req, res) => {
  try {
    const { appId, type, startTime, endTime, limit = 100 } = req.query
    
    const query = {}
    if (appId) query.appId = appId
    if (type) query.type = type
    if (startTime || endTime) {
      query.timestamp = {}
      if (startTime) query.timestamp.$gte = new Date(startTime)
      if (endTime) query.timestamp.$lte = new Date(endTime)
    }

    const data = await Error.find(query)
      .sort({ timestamp: -1 })
      .limit(parseInt(limit))

    res.json({ success: true, data })
  } catch (error) {
    console.error('Query error:', error)
    res.status(500).json({ error: 'Internal server error' })
  }
})

router.get('/behavior', async (req, res) => {
  try {
    const { appId, type, startTime, endTime, limit = 100 } = req.query
    
    const query = {}
    if (appId) query.appId = appId
    if (type) query.type = type
    if (startTime || endTime) {
      query.timestamp = {}
      if (startTime) query.timestamp.$gte = new Date(startTime)
      if (endTime) query.timestamp.$lte = new Date(endTime)
    }

    const data = await Behavior.find(query)
      .sort({ timestamp: -1 })
      .limit(parseInt(limit))

    res.json({ success: true, data })
  } catch (error) {
    console.error('Query error:', error)
    res.status(500).json({ error: 'Internal server error' })
  }
})

module.exports = router

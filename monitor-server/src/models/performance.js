const mongoose = require('mongoose')

const performanceSchema = new mongoose.Schema({
  appId: { type: String, required: true, index: true },
  userId: String,
  sessionId: String,
  deviceId: String,
  subType: String,
  metrics: mongoose.Schema.Types.Mixed,
  value: Number,
  timestamp: { type: Date, default: Date.now, index: true },
  url: String,
  platform: String,
  browser: String
}, { timestamps: true })

performanceSchema.index({ appId: 1, timestamp: -1 })

module.exports = mongoose.model('Performance', performanceSchema)

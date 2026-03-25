const mongoose = require('mongoose')

const behaviorSchema = new mongoose.Schema({
  appId: { type: String, required: true, index: true },
  userId: String,
  sessionId: String,
  deviceId: String,
  type: String,
  eventName: String,
  eventData: mongoose.Schema.Types.Mixed,
  elementInfo: mongoose.Schema.Types.Mixed,
  depth: Number,
  maxDepth: Number,
  scrollY: Number,
  stayTime: Number,
  elementId: String,
  position: mongoose.Schema.Types.Mixed,
  timestamp: { type: Date, default: Date.now, index: true },
  url: String,
  path: String,
  title: String,
  referrer: String,
  platform: String,
  browser: String
}, { timestamps: true })

behaviorSchema.index({ appId: 1, type: 1, timestamp: -1 })

module.exports = mongoose.model('Behavior', behaviorSchema)

const mongoose = require('mongoose')

const errorSchema = new mongoose.Schema({
  appId: { type: String, required: true, index: true },
  userId: String,
  sessionId: String,
  deviceId: String,
  type: String,
  message: String,
  source: String,
  lineno: Number,
  colno: Number,
  stack: String,
  componentName: String,
  info: String,
  extra: mongoose.Schema.Types.Mixed,
  timestamp: { type: Date, default: Date.now, index: true },
  url: String,
  platform: String,
  browser: String
}, { timestamps: true })

errorSchema.index({ appId: 1, type: 1, timestamp: -1 })

module.exports = mongoose.model('Error', errorSchema)

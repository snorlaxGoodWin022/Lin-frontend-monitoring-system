const express = require('express')
const cors = require('cors')
const mongoose = require('mongoose')
const redis = require('redis')
require('dotenv').config()

const app = express()

app.use(cors())
app.use(express.json({ limit: '10mb' }))

mongoose.connect(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => {
  console.log('Connected to MongoDB')
}).catch(err => {
  console.error('MongoDB connection error:', err)
})

const redisClient = redis.createClient({
  url: process.env.REDIS_URI
})

redisClient.connect().then(() => {
  console.log('Connected to Redis')
}).catch(err => {
  console.error('Redis connection error:', err)
})

app.use('/api/report', require('./api/report'))
app.use('/api/query', require('./api/query'))
app.use('/api/dashboard', require('./api/dashboard'))

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: Date.now() })
})

const PORT = process.env.PORT || 3000

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`)
})

module.exports = app

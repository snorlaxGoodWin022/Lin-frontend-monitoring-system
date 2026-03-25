import { compress } from '../utils/compress'
import { CacheManager } from '../utils/cache'
import { getUserId, getSessionId, getDeviceId, getPlatform, getBrowser, getScreenResolution, getViewport } from '../utils/helpers'

export class ReportManager {
  constructor(config) {
    this.config = config
    this.queue = []
    this.cacheManager = new CacheManager()
    this.timer = null
    this.isReporting = false
    
    this.init()
  }

  init() {
    this.startScheduleReport()
    this.setupUnloadReport()
    this.recoverCache()
  }

  add(data, priority = 'normal') {
    const item = {
      ...data,
      priority,
      appId: this.config.appId,
      userId: this.config.userId || getUserId(),
      sessionId: getSessionId(),
      deviceId: getDeviceId(),
      platform: getPlatform(),
      browser: getBrowser(),
      screenResolution: getScreenResolution(),
      viewport: getViewport(),
      timestamp: Date.now(),
      url: window.location.href,
      userAgent: navigator.userAgent
    }

    this.queue.push(item)

    if (this.queue.length >= this.config.maxQueueSize) {
      this.report()
    }
  }

  async report() {
    if (this.queue.length === 0 || this.isReporting) return

    this.isReporting = true
    const data = [...this.queue]
    this.queue = []

    try {
      let payload = data
      
      if (this.config.compress) {
        payload = compress(data)
      }

      await this.send(payload)
      
      if (this.config.debug) {
        console.log('Report success:', data)
      }
    } catch (error) {
      console.error('Report failed:', error)
      this.cacheManager.save(data)
    } finally {
      this.isReporting = false
    }
  }

  async send(data) {
    if (navigator.sendBeacon) {
      const blob = new Blob([JSON.stringify(data)], { type: 'application/json' })
      const success = navigator.sendBeacon(this.config.apiUrl, blob)
      
      if (success) return
    }

    const response = await fetch(this.config.apiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
      keepalive: true
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
  }

  startScheduleReport() {
    this.timer = setInterval(() => {
      this.report()
    }, this.config.reportInterval)
  }

  setupUnloadReport() {
    window.addEventListener('beforeunload', () => {
      this.report()
    })

    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        this.report()
      }
    })
  }

  recoverCache() {
    const cached = this.cacheManager.load()
    if (cached && cached.length > 0) {
      this.queue.push(...cached)
      this.cacheManager.clear()
    }
  }

  destroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
    this.report()
  }
}

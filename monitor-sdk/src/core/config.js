export class Config {
  constructor(options = {}) {
    this.appId = options.appId
    this.apiUrl = options.apiUrl
    this.userId = options.userId
    this.enablePerformance = options.enablePerformance !== false
    this.enableError = options.enableError !== false
    this.enableBehavior = options.enableBehavior !== false
    this.enableExposure = options.enableExposure !== false
    this.reportInterval = options.reportInterval || 5000
    this.maxQueueSize = options.maxQueueSize || 10
    this.sampleRate = options.sampleRate || 1
    this.debug = options.debug || false
    this.compress = options.compress !== false
  }

  validate() {
    if (!this.appId) {
      throw new Error('appId is required')
    }
    if (!this.apiUrl) {
      throw new Error('apiUrl is required')
    }
    if (this.sampleRate < 0 || this.sampleRate > 1) {
      throw new Error('sampleRate must be between 0 and 1')
    }
    return true
  }

  shouldSample() {
    return Math.random() <= this.sampleRate
  }

  update(options) {
    Object.assign(this, options)
  }
}

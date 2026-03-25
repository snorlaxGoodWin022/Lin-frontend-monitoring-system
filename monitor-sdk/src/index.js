import { MonitorBase } from './core/base'

class MonitorSDK extends MonitorBase {
  constructor() {
    super()
    this.version = '1.0.0'
  }

  init(options) {
    super.init(options)
  }

  track(eventName, eventData) {
    super.track(eventName, eventData)
  }

  setUserId(userId) {
    super.setUserId(userId)
  }

  getVersion() {
    return this.version
  }
}

const monitor = new MonitorSDK()

export default monitor

if (typeof window !== 'undefined') {
  window.MonitorSDK = monitor
}

import { Config } from './config'
import { ReportManager } from './report'

export class MonitorBase {
  constructor() {
    this.config = null
    this.reportManager = null
    this.modules = {}
  }

  init(options) {
    this.config = new Config(options)
    this.config.validate()

    if (!this.config.shouldSample()) {
      console.log('Monitor SDK: Sampling skipped')
      return
    }

    this.reportManager = new ReportManager(this.config)
    this.initModules()
  }

  initModules() {
    const moduleMap = {
      performance: 'PerformanceMonitor',
      error: 'ErrorMonitor',
      behavior: 'BehaviorTracker',
      exposure: 'ExposureTracker'
    }

    const configMap = {
      performance: this.config.enablePerformance,
      error: this.config.enableError,
      behavior: this.config.enableBehavior,
      exposure: this.config.enableExposure
    }

    Object.keys(configMap).forEach(key => {
      if (configMap[key]) {
        this.loadModule(key, moduleMap[key])
      }
    })
  }

  loadModule(moduleName, className) {
    try {
      const module = require(`../modules/${moduleName}`)
      const ModuleClass = module.default || module[className]
      
      if (ModuleClass) {
        this.modules[moduleName] = new ModuleClass(this.config, this.reportManager)
        this.modules[moduleName].init()
      }
    } catch (error) {
      console.error(`Failed to load module: ${moduleName}`, error)
    }
  }

  getModule(moduleName) {
    return this.modules[moduleName]
  }

  track(eventName, eventData = {}) {
    this.reportManager.add({
      type: 'track',
      eventName,
      eventData
    })
  }

  setUserId(userId) {
    this.config.userId = userId
  }

  destroy() {
    Object.values(this.modules).forEach(module => {
      if (module.destroy) {
        module.destroy()
      }
    })
    
    if (this.reportManager) {
      this.reportManager.destroy()
    }
    
    this.modules = {}
  }
}

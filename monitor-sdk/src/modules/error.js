import { generateId } from '../utils/helpers'

export class ErrorMonitor {
  constructor(config, reportManager) {
    this.config = config
    this.reportManager = reportManager
    this.errors = []
    this.maxErrors = 100
    this.vueErrorHandler = null
  }

  init() {
    this.captureJSError()
    this.capturePromiseError()
    this.captureResourceError()
    this.captureVueError()
  }

  captureJSError() {
    window.onerror = (message, source, lineno, colno, error) => {
      const errorInfo = {
        type: 'jsError',
        message: String(message),
        source,
        lineno,
        colno,
        stack: error?.stack,
        timestamp: Date.now()
      }

      this.handleError(errorInfo)
      return true
    }
  }

  capturePromiseError() {
    window.addEventListener('unhandledrejection', (event) => {
      const errorInfo = {
        type: 'promiseError',
        message: event.reason?.message || String(event.reason),
        stack: event.reason?.stack,
        timestamp: Date.now()
      }

      this.handleError(errorInfo)
      event.preventDefault()
    })
  }

  captureResourceError() {
    window.addEventListener('error', (event) => {
      if (event.target !== window) {
        const target = event.target || event.srcElement
        
        const errorInfo = {
          type: 'resourceError',
          message: `${target.tagName} load error`,
          source: target.src || target.href,
          tagName: target.tagName,
          timestamp: Date.now()
        }

        this.handleError(errorInfo)
      }
    }, true)
  }

  captureVueError() {
    this.vueErrorHandler = (err, vm, info) => {
      const errorInfo = {
        type: 'vueError',
        message: err.message,
        stack: err.stack,
        componentName: vm?.$options?.name || 'Anonymous',
        propsData: vm?.$options?.propsData,
        info,
        timestamp: Date.now()
      }

      this.handleError(errorInfo)
    }
  }

  handleError(errorInfo) {
    errorInfo.id = generateId()

    if (this.isDuplicate(errorInfo)) {
      return
    }

    this.errors.unshift(errorInfo)

    if (this.errors.length > this.maxErrors) {
      this.errors.pop()
    }

    if (this.config.debug) {
      console.error('Error captured:', errorInfo)
    }

    this.reportManager.add({
      type: 'error',
      ...errorInfo
    })
  }

  isDuplicate(errorInfo) {
    const recentErrors = this.errors.slice(0, 10)
    
    return recentErrors.some(error => {
      return (
        error.type === errorInfo.type &&
        error.message === errorInfo.message &&
        error.source === errorInfo.source &&
        error.lineno === errorInfo.lineno &&
        error.colno === errorInfo.colno
      )
    })
  }

  captureError(error, extra = {}) {
    const errorInfo = {
      type: 'manualError',
      message: error.message || String(error),
      stack: error.stack,
      extra,
      timestamp: Date.now()
    }

    this.handleError(errorInfo)
  }

  getErrors() {
    return this.errors
  }

  getStatistics() {
    const stats = {
      total: this.errors.length,
      byType: {},
      bySource: {}
    }

    this.errors.forEach(error => {
      stats.byType[error.type] = (stats.byType[error.type] || 0) + 1

      if (error.source) {
        const source = this.getShortSource(error.source)
        stats.bySource[source] = (stats.bySource[source] || 0) + 1
      }
    })

    return stats
  }

  getShortSource(source) {
    if (!source) return 'unknown'
    return source.split('/').pop() || source
  }

  clear() {
    this.errors = []
  }

  getVueErrorHandler() {
    return this.vueErrorHandler
  }

  destroy() {
    window.onerror = null
  }
}

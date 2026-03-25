export class PerformanceMonitor {
  constructor(config, reportManager) {
    this.config = config
    this.reportManager = reportManager
    this.metrics = {}
    this.observers = []
  }

  init() {
    if (document.readyState === 'complete') {
      this.collectMetrics()
    } else {
      window.addEventListener('load', () => {
        this.collectMetrics()
      })
    }
  }

  collectMetrics() {
    this.collectNavigationTiming()
    this.collectWebVitals()
    this.observeLongTasks()
    this.collectResources()
    this.interceptFetch()
    this.interceptXHR()
  }

  collectNavigationTiming() {
    const timing = performance.timing
    
    this.metrics = {
      dns: timing.domainLookupEnd - timing.domainLookupStart,
      tcp: timing.connectEnd - timing.connectStart,
      ttfb: timing.responseStart - timing.navigationStart,
      domParse: timing.domInteractive - timing.domLoading,
      domReady: timing.domContentLoadedEventEnd - timing.navigationStart,
      loadComplete: timing.loadEventEnd - timing.navigationStart,
      firstPaint: timing.responseEnd - timing.fetchStart
    }

    this.reportManager.add({
      type: 'performance',
      subType: 'navigation',
      metrics: this.metrics
    })
  }

  collectWebVitals() {
    this.observeFCP()
    this.observeLCP()
    this.observeFID()
    this.observeCLS()
  }

  observeFCP() {
    if (!('PerformanceObserver' in window)) return

    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (entry.name === 'first-contentful-paint') {
          this.metrics.fcp = entry.startTime
          this.reportManager.add({
            type: 'performance',
            subType: 'fcp',
            value: entry.startTime
          })
          observer.disconnect()
        }
      }
    })

    observer.observe({ entryTypes: ['paint'] })
    this.observers.push(observer)
  }

  observeLCP() {
    if (!('PerformanceObserver' in window)) return

    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries()
      const lastEntry = entries[entries.length - 1]
      
      this.metrics.lcp = lastEntry.startTime
      this.reportManager.add({
        type: 'performance',
        subType: 'lcp',
        value: lastEntry.startTime,
        element: lastEntry.element?.tagName
      })
    })

    observer.observe({ entryTypes: ['largest-contentful-paint'] })
    this.observers.push(observer)
  }

  observeFID() {
    if (!('PerformanceObserver' in window)) return

    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        const fid = entry.processingStart - entry.startTime
        this.metrics.fid = fid
        this.reportManager.add({
          type: 'performance',
          subType: 'fid',
          value: fid,
          eventType: entry.name
        })
        observer.disconnect()
      }
    })

    observer.observe({ entryTypes: ['first-input'], buffered: true })
    this.observers.push(observer)
  }

  observeCLS() {
    if (!('PerformanceObserver' in window)) return

    let clsValue = 0
    
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (!entry.hadRecentInput) {
          clsValue += entry.value
          this.metrics.cls = clsValue
          this.reportManager.add({
            type: 'performance',
            subType: 'cls',
            value: clsValue
          })
        }
      }
    })

    observer.observe({ entryTypes: ['layout-shift'] })
    this.observers.push(observer)
  }

  observeLongTasks() {
    if (!('PerformanceObserver' in window)) return

    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        if (entry.duration > 50) {
          this.reportManager.add({
            type: 'performance',
            subType: 'longtask',
            duration: entry.duration,
            startTime: entry.startTime
          })
        }
      }
    })

    try {
      observer.observe({ entryTypes: ['longtask'] })
      this.observers.push(observer)
    } catch (e) {
      console.warn('Long Task API not supported')
    }
  }

  collectResources() {
    const resources = performance.getEntriesByType('resource')
    
    resources.forEach(resource => {
      const resourceData = {
        type: 'performance',
        subType: 'resource',
        name: resource.name,
        resourceType: this.getResourceType(resource),
        size: resource.transferSize,
        duration: resource.duration,
        startTime: resource.startTime,
        cached: resource.transferSize === 0
      }

      if (resource.duration > 2000) {
        resourceData.isSlow = true
      }

      if (resource.transferSize > 500 * 1024) {
        resourceData.isLarge = true
      }

      this.reportManager.add(resourceData)
    })
  }

  getResourceType(resource) {
    const initiatorType = resource.initiatorType
    
    if (initiatorType === 'img') return 'image'
    if (initiatorType === 'script') return 'script'
    if (initiatorType === 'link' || initiatorType === 'css') return 'stylesheet'
    if (initiatorType === 'xmlhttprequest' || initiatorType === 'fetch') return 'xhr'
    
    const url = resource.name
    if (/\.(jpg|jpeg|png|gif|webp|svg)$/i.test(url)) return 'image'
    if (/\.js$/i.test(url)) return 'script'
    if (/\.css$/i.test(url)) return 'stylesheet'
    if (/\.(woff|woff2|ttf|eot)$/i.test(url)) return 'font'
    
    return 'other'
  }

  interceptFetch() {
    const originalFetch = window.fetch
    const self = this

    window.fetch = function(...args) {
      const startTime = Date.now()
      const url = args[0]
      const options = args[1] || {}
      
      return originalFetch.apply(this, args)
        .then(response => {
          const endTime = Date.now()
          const duration = endTime - startTime
          
          self.reportManager.add({
            type: 'api',
            url: url,
            method: options.method || 'GET',
            status: response.status,
            success: response.ok,
            duration,
            isSlow: duration > 1000
          })
          
          return response
        })
        .catch(error => {
          const endTime = Date.now()
          
          self.reportManager.add({
            type: 'api',
            url: url,
            method: options.method || 'GET',
            success: false,
            error: error.message,
            duration: endTime - startTime
          })
          
          throw error
        })
    }
  }

  interceptXHR() {
    const self = this
    const originalOpen = XMLHttpRequest.prototype.open
    const originalSend = XMLHttpRequest.prototype.send

    XMLHttpRequest.prototype.open = function(method, url) {
      this._requestInfo = {
        url,
        method,
        startTime: null
      }
      return originalOpen.apply(this, arguments)
    }

    XMLHttpRequest.prototype.send = function() {
      if (this._requestInfo) {
        this._requestInfo.startTime = Date.now()
      }

      this.addEventListener('loadend', function() {
        if (!this._requestInfo) return
        
        const endTime = Date.now()
        const duration = endTime - this._requestInfo.startTime
        
        self.reportManager.add({
          type: 'api',
          url: this._requestInfo.url,
          method: this._requestInfo.method,
          status: this.status,
          success: this.status >= 200 && this.status < 300,
          duration,
          isSlow: duration > 1000
        })
      })

      return originalSend.apply(this, arguments)
    }
  }

  destroy() {
    this.observers.forEach(observer => observer.disconnect())
    this.observers = []
  }
}

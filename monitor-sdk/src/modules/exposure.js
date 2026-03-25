import { getXPath } from '../utils/helpers'

export class ExposureTracker {
  constructor(config, reportManager) {
    this.config = config
    this.reportManager = reportManager
    this.threshold = config.exposureThreshold || 0.5
    this.exposureTime = config.exposureTime || 1000
    this.observers = new Map()
    this.exposureTimers = new Map()
    this.exposedElements = new Set()
  }

  init() {
    this.observer = new IntersectionObserver(
      (entries) => this.handleIntersection(entries),
      {
        threshold: this.threshold,
        rootMargin: '0px'
      }
    )

    this.observeAutoTrackElements()
    this.observeDOMChanges()
  }

  handleIntersection(entries) {
    entries.forEach(entry => {
      const element = entry.target
      const elementId = this.getElementId(element)

      if (entry.isIntersecting) {
        this.startExposureTimer(element, elementId)
      } else {
        this.clearExposureTimer(elementId)
      }
    })
  }

  startExposureTimer(element, elementId) {
    if (this.exposedElements.has(elementId)) {
      return
    }

    const timer = setTimeout(() => {
      this.trackExposure(element)
      this.exposedElements.add(elementId)
      this.clearExposureTimer(elementId)
    }, this.exposureTime)

    this.exposureTimers.set(elementId, timer)
  }

  clearExposureTimer(elementId) {
    const timer = this.exposureTimers.get(elementId)
    if (timer) {
      clearTimeout(timer)
      this.exposureTimers.delete(elementId)
    }
  }

  trackExposure(element) {
    const exposureData = {
      type: 'exposure',
      elementId: this.getElementId(element),
      elementInfo: {
        tagName: element.tagName,
        id: element.id,
        className: element.className,
        text: element.innerText?.substring(0, 50),
        xpath: getXPath(element)
      },
      position: element.getBoundingClientRect(),
      ...this.getCustomData(element)
    }

    this.reportManager.add(exposureData)
  }

  observe(element) {
    if (element && !this.observers.has(element)) {
      this.observer.observe(element)
      this.observers.set(element, true)
    }
  }

  unobserve(element) {
    if (element && this.observers.has(element)) {
      this.observer.unobserve(element)
      this.observers.delete(element)
      
      const elementId = this.getElementId(element)
      this.clearExposureTimer(elementId)
    }
  }

  observeAutoTrackElements() {
    const elements = document.querySelectorAll('[data-track-exposure]')
    elements.forEach(element => {
      this.observe(element)
    })
  }

  observeDOMChanges() {
    const mutationObserver = new MutationObserver((mutations) => {
      mutations.forEach(mutation => {
        mutation.addedNodes.forEach(node => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            if (node.hasAttribute('data-track-exposure')) {
              this.observe(node)
            }
            
            const children = node.querySelectorAll('[data-track-exposure]')
            children.forEach(child => {
              this.observe(child)
            })
          }
        })
      })
    })

    mutationObserver.observe(document.body, {
      childList: true,
      subtree: true
    })
  }

  getElementId(element) {
    if (element.id) return element.id
    return getXPath(element)
  }

  getCustomData(element) {
    const customData = {}
    
    Array.from(element.attributes).forEach(attr => {
      if (attr.name.startsWith('data-track-')) {
        const key = attr.name.replace('data-track-', '')
        customData[key] = attr.value
      }
    })
    
    return customData
  }

  destroy() {
    this.observer.disconnect()
    this.exposureTimers.forEach(timer => clearTimeout(timer))
    this.exposureTimers.clear()
    this.observers.clear()
  }
}

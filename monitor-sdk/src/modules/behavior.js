import { getXPath, getElementText, getUserId, getSessionId } from '../utils/helpers'

export class BehaviorTracker {
  constructor(config, reportManager) {
    this.config = config
    this.reportManager = reportManager
    this.pageEnterTime = Date.now()
    this.maxScrollDepth = 0
  }

  init() {
    this.trackPageView()
    this.setupAutoTrack()
  }

  trackPageView() {
    this.reportManager.add({
      type: 'page_view',
      url: window.location.href,
      path: window.location.pathname,
      title: document.title,
      referrer: document.referrer
    })

    this.trackPageStay()
    this.trackScroll()
  }

  setupAutoTrack() {
    document.addEventListener('click', (e) => {
      this.trackClick(e)
    }, true)
  }

  trackClick(event) {
    const target = event.target
    const tagName = target.tagName.toLowerCase()
    
    const elementInfo = {
      tagName,
      id: target.id,
      className: target.className,
      text: getElementText(target),
      xpath: getXPath(target),
      position: {
        x: event.clientX,
        y: event.clientY
      }
    }

    if (tagName === 'button' || tagName === 'a') {
      elementInfo.buttonText = target.innerText
      if (tagName === 'a') {
        elementInfo.href = target.href
      }
    }

    this.reportManager.add({
      type: 'click',
      elementInfo
    })
  }

  trackPageStay() {
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        const stayTime = Date.now() - this.pageEnterTime
        this.reportManager.add({
          type: 'page_leave',
          url: window.location.href,
          stayTime
        })
      } else {
        this.pageEnterTime = Date.now()
      }
    })
  }

  trackScroll() {
    let scrollTimer = null
    
    window.addEventListener('scroll', () => {
      const scrollDepth = Math.round(
        (window.scrollY / (document.documentElement.scrollHeight - window.innerHeight)) * 100
      )
      
      this.maxScrollDepth = Math.max(this.maxScrollDepth, scrollDepth)
      
      clearTimeout(scrollTimer)
      scrollTimer = setTimeout(() => {
        this.reportManager.add({
          type: 'scroll',
          depth: scrollDepth,
          maxDepth: this.maxScrollDepth,
          scrollY: window.scrollY
        })
      }, 500)
    })
  }

  track(eventName, eventData = {}) {
    this.reportManager.add({
      type: 'track',
      eventName,
      eventData
    })
  }

  destroy() {
  }
}

export function generateId() {
  return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

export function getUserId() {
  let userId = localStorage.getItem('monitor_user_id')
  if (!userId) {
    userId = `user_${generateId()}`
    localStorage.setItem('monitor_user_id', userId)
  }
  return userId
}

export function getSessionId() {
  let sessionId = sessionStorage.getItem('monitor_session_id')
  if (!sessionId) {
    sessionId = `session_${generateId()}`
    sessionStorage.setItem('monitor_session_id', sessionId)
  }
  return sessionId
}

export function getDeviceId() {
  let deviceId = localStorage.getItem('monitor_device_id')
  if (!deviceId) {
    deviceId = `device_${generateId()}`
    localStorage.setItem('monitor_device_id', deviceId)
  }
  return deviceId
}

export function getPlatform() {
  const ua = navigator.userAgent
  if (/Android/i.test(ua)) return 'Android'
  if (/iPhone|iPad|iPod/i.test(ua)) return 'iOS'
  if (/Windows/i.test(ua)) return 'Windows'
  if (/Mac/i.test(ua)) return 'Mac'
  if (/Linux/i.test(ua)) return 'Linux'
  return 'Unknown'
}

export function getBrowser() {
  const ua = navigator.userAgent
  if (/Chrome/i.test(ua) && !/Edge/i.test(ua)) return 'Chrome'
  if (/Safari/i.test(ua) && !/Chrome/i.test(ua)) return 'Safari'
  if (/Firefox/i.test(ua)) return 'Firefox'
  if (/Edge/i.test(ua)) return 'Edge'
  if (/MSIE|Trident/i.test(ua)) return 'IE'
  return 'Unknown'
}

export function getScreenResolution() {
  return `${window.screen.width}x${window.screen.height}`
}

export function getViewport() {
  return `${window.innerWidth}x${window.innerHeight}`
}

export function getXPath(element) {
  if (element.id) {
    return `//*[@id="${element.id}"]`
  }
  
  const parts = []
  while (element && element.nodeType === Node.ELEMENT_NODE) {
    let index = 0
    let sibling = element.previousSibling
    
    while (sibling) {
      if (sibling.nodeType === Node.ELEMENT_NODE && sibling.nodeName === element.nodeName) {
        index++
      }
      sibling = sibling.previousSibling
    }
    
    const tagName = element.nodeName.toLowerCase()
    const pathIndex = index ? `[${index + 1}]` : ''
    parts.unshift(tagName + pathIndex)
    
    element = element.parentNode
  }
  
  return parts.length ? '/' + parts.join('/') : ''
}

export function getElementText(element) {
  const text = element.innerText || element.textContent || ''
  return text.trim().substring(0, 50)
}

export function formatTime(timestamp) {
  return new Date(timestamp).toISOString()
}

export function debounce(fn, delay) {
  let timer = null
  return function(...args) {
    clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

export function throttle(fn, delay) {
  let lastTime = 0
  return function(...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      fn.apply(this, args)
      lastTime = now
    }
  }
}

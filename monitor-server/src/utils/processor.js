function validateData(data) {
  if (!Array.isArray(data)) {
    return { valid: false, message: 'Data must be array' }
  }

  for (const item of data) {
    if (!item.appId) {
      return { valid: false, message: 'appId is required' }
    }
    if (!item.timestamp) {
      return { valid: false, message: 'timestamp is required' }
    }
  }

  return { valid: true }
}

function cleanData(data) {
  return data.map(item => {
    const cleaned = { ...item }
    
    if (cleaned.url) {
      cleaned.url = desensitizeUrl(cleaned.url)
    }

    if (cleaned.timestamp) {
      cleaned.timestamp = new Date(cleaned.timestamp).toISOString()
    }

    if (cleaned.userAgent) {
      cleaned.browser = extractBrowser(cleaned.userAgent)
      cleaned.os = extractOS(cleaned.userAgent)
      cleaned.device = extractDevice(cleaned.userAgent)
    }

    return cleaned
  })
}

function desensitizeUrl(url) {
  try {
    const urlObj = new URL(url)
    const sensitiveParams = ['token', 'password', 'key', 'secret']
    
    sensitiveParams.forEach(param => {
      urlObj.searchParams.delete(param)
    })
    
    return urlObj.toString()
  } catch (error) {
    return url
  }
}

function extractBrowser(ua) {
  if (/Chrome/i.test(ua)) return 'Chrome'
  if (/Safari/i.test(ua)) return 'Safari'
  if (/Firefox/i.test(ua)) return 'Firefox'
  if (/Edge/i.test(ua)) return 'Edge'
  return 'Unknown'
}

function extractOS(ua) {
  if (/Windows/i.test(ua)) return 'Windows'
  if (/Mac/i.test(ua)) return 'MacOS'
  if (/Linux/i.test(ua)) return 'Linux'
  if (/Android/i.test(ua)) return 'Android'
  if (/iOS/i.test(ua)) return 'iOS'
  return 'Unknown'
}

function extractDevice(ua) {
  if (/Mobile/i.test(ua)) return 'Mobile'
  if (/Tablet/i.test(ua)) return 'Tablet'
  return 'Desktop'
}

module.exports = {
  validateData,
  cleanData,
  desensitizeUrl,
  extractBrowser,
  extractOS,
  extractDevice
}

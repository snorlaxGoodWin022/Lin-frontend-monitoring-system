export class CacheManager {
  constructor(key = 'monitor_cache', maxSize = 100) {
    this.key = key
    this.maxSize = maxSize
  }

  save(data) {
    try {
      const cached = this.load() || []
      cached.push(...data)
      
      const limited = cached.slice(-this.maxSize)
      
      localStorage.setItem(this.key, JSON.stringify(limited))
      return true
    } catch (error) {
      console.error('Cache save failed:', error)
      return false
    }
  }

  load() {
    try {
      const cached = localStorage.getItem(this.key)
      return cached ? JSON.parse(cached) : []
    } catch (error) {
      console.error('Cache load failed:', error)
      return []
    }
  }

  clear() {
    try {
      localStorage.removeItem(this.key)
      return true
    } catch (error) {
      console.error('Cache clear failed:', error)
      return false
    }
  }

  size() {
    const cached = this.load()
    return cached.length
  }
}

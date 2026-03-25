import pako from 'pako'

export function compress(data) {
  try {
    const json = JSON.stringify(data)
    const compressed = pako.gzip(json)
    return compressed
  } catch (error) {
    console.error('Compress failed:', error)
    return JSON.stringify(data)
  }
}

export function decompress(data) {
  try {
    const decompressed = pako.ungzip(data, { to: 'string' })
    return JSON.parse(decompressed)
  } catch (error) {
    console.error('Decompress failed:', error)
    return null
  }
}

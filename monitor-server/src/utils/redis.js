const redis = require('redis')

let client = null

function getClient() {
  if (!client) {
    client = redis.createClient({
      url: process.env.REDIS_URI
    })
    client.connect()
  }
  return client
}

async function set(key, value, ttl = 3600) {
  const cli = getClient()
  await cli.set(key, JSON.stringify(value))
  if (ttl) {
    await cli.expire(key, ttl)
  }
}

async function get(key) {
  const cli = getClient()
  const value = await cli.get(key)
  return value ? JSON.parse(value) : null
}

async function del(key) {
  const cli = getClient()
  await cli.del(key)
}

async function hSet(key, field, value) {
  const cli = getClient()
  await cli.hSet(key, field, JSON.stringify(value))
}

async function hGet(key, field) {
  const cli = getClient()
  const value = await cli.hGet(key, field)
  return value ? JSON.parse(value) : null
}

async function hIncrBy(key, field, increment) {
  const cli = getClient()
  return await cli.hIncrBy(key, field, increment)
}

async function expire(key, ttl) {
  const cli = getClient()
  await cli.expire(key, ttl)
}

async function keys(pattern) {
  const cli = getClient()
  return await cli.keys(pattern)
}

module.exports = {
  getClient,
  set,
  get,
  del,
  hSet,
  hGet,
  hIncrBy,
  expire,
  keys
}

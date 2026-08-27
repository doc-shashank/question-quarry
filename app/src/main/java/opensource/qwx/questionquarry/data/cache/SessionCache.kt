package opensource.qwx.questionquarry.data.cache

import opensource.qwx.questionquarry.data.local.entity.Session

class SessionCache(private val maxSize: Int = 10) {
    private val cache = object : LinkedHashMap<Long, Session>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Session>?): Boolean {
            return size > maxSize
        }
    }

    fun get(id: Long): Session? {
        synchronized(cache) {
            return cache[id]
        }
    }

    fun put(session: Session) {
        synchronized(cache) {
            cache[session.id] = session
        }
    }

    fun remove(id: Long) {
        synchronized(cache) {
            cache.remove(id)
        }
    }

    fun clear() {
        synchronized(cache) {
            cache.clear()
        }
    }
}

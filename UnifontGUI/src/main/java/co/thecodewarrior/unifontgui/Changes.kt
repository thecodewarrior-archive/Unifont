package co.thecodewarrior.unifontgui

import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Changes {
    private val listeners = ConcurrentHashMap<IdentityHash<Any>, MutableSet<IdentityHash<ChangeListener>>>()

    fun listen(target: Any, listener: ChangeListener) {
        listeners.getOrPut(IdentityHash(target)) { Collections.newSetFromMap(ConcurrentHashMap()) }.add(IdentityHash(listener))
        cleanup()
    }

    fun unlisten(target: Any, listener: ChangeListener) {
        listeners[IdentityHash(target)]?.remove(IdentityHash(listener))
        cleanup()
    }

    fun submit(target: Any) {
        listeners[IdentityHash(target)]?.forEach {
            it.value.get()?.changeOccured(target)
        }
        cleanup()
    }

    private fun cleanup() {
        listeners.keys.filter { it.value.get() == null }.forEach {
            listeners.remove(it)
        }
        listeners.values.forEach { value ->
            value.removeIf { it.value.get() == null }
        }
    }
}

private class IdentityHash<T>(value: T) {
    val value = WeakReference(value)
    val hash = System.identityHashCode(value)

    override fun hashCode(): Int {
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdentityHash<*>) return false

        if (value.get() !== other.value.get()) return false

        return true
    }
}

interface ChangeListener {
    fun changeOccured(target: Any)

    fun listenTo(target: Any) {
        Changes.listen(target, this)
    }

    fun unlistenTo(target: Any) {
        Changes.unlisten(target, this)
    }
}

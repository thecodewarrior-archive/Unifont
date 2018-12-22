package co.thecodewarrior.unifontgui

import java.lang.ref.WeakReference
import java.util.Objects
import java.util.WeakHashMap

object Changes {
    private val listeners = mutableMapOf<IdentityHash, MutableList<WeakReference<ChangeListener>>>()

    fun listen(target: Any, listener: ChangeListener) {
        listeners.getOrPut(IdentityHash(target)) { mutableListOf() }.add(WeakReference(listener))
        cleanup()
    }

    fun unlisten(target: Any, listener: ChangeListener) {
        listeners[IdentityHash(target)]?.remove(WeakReference(listener))
        cleanup()
    }

    fun submit(target: Any) {
        listeners[IdentityHash(target)]?.forEach {
            it.get()?.changeOccured(target)
        }
        cleanup()
    }

    private fun cleanup() {
        listeners.keys.filter { it.value.get() == null }.forEach {
            listeners.remove(it)
        }
    }
}

private class IdentityHash(value: Any) {
    val value = WeakReference(value)
    val hash = System.identityHashCode(value)

    override fun hashCode(): Int {
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdentityHash) return false

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

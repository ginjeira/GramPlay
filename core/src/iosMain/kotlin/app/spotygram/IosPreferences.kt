package app.spotygram

import platform.Foundation.NSUserDefaults

/** NSUserDefaults-backed implementation of the shared preference contract. */
class IosPreferences(private val suiteName: String) : Preferences {
    private val defaults = NSUserDefaults(suiteName = suiteName) ?: NSUserDefaults.standardUserDefaults

    override fun contains(key: String): Boolean = defaults.objectForKey(key) != null

    override fun getString(key: String, default: String?): String? =
        defaults.stringForKey(key) ?: default

    override fun getBoolean(key: String, default: Boolean): Boolean =
        if (contains(key)) defaults.boolForKey(key) else default

    override fun getInt(key: String, default: Int): Int =
        if (contains(key)) defaults.integerForKey(key).toInt() else default

    override fun getLong(key: String, default: Long): Long =
        if (contains(key)) defaults.integerForKey(key) else default

    override fun getFloat(key: String, default: Float): Float =
        if (contains(key)) defaults.floatForKey(key) else default

    override fun edit(): Preferences.Editor = IosPreferencesEditor(defaults, suiteName)
}

private class IosPreferencesEditor(
    private val defaults: NSUserDefaults,
    private val suiteName: String,
) : Preferences.Editor {
    private val values = linkedMapOf<String, Any>()
    private val removed = linkedSetOf<String>()
    private var clearAll = false

    override fun putString(key: String, value: String?): Preferences.Editor =
        if (value == null) remove(key) else put(key, value)

    override fun putBoolean(key: String, value: Boolean): Preferences.Editor = put(key, value)

    override fun putInt(key: String, value: Int): Preferences.Editor = put(key, value)

    override fun putLong(key: String, value: Long): Preferences.Editor = put(key, value)

    override fun putFloat(key: String, value: Float): Preferences.Editor = put(key, value)

    override fun remove(key: String): Preferences.Editor {
        values.remove(key)
        removed.add(key)
        return this
    }

    override fun clear(): Preferences.Editor {
        values.clear()
        removed.clear()
        clearAll = true
        return this
    }

    override fun commit(): Boolean {
        apply()
        return true
    }

    override fun apply() {
        if (clearAll) defaults.removePersistentDomainForName(suiteName)
        removed.forEach(defaults::removeObjectForKey)
        values.forEach { (key, value) ->
            when (value) {
                is String -> defaults.setObject(value, forKey = key)
                is Boolean -> defaults.setBool(value, forKey = key)
                is Int -> defaults.setInteger(value.toLong(), forKey = key)
                is Long -> defaults.setInteger(value, forKey = key)
                is Float -> defaults.setFloat(value, forKey = key)
                else -> error("Unsupported preference value: ${value::class}")
            }
        }
    }

    private fun put(key: String, value: Any): Preferences.Editor {
        removed.remove(key)
        values[key] = value
        return this
    }
}

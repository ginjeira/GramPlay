package app.spotygram

interface TdClient {
    fun createClientId(): Int
    fun send(client: Int, request: String)
    fun receive(timeout: Double): String?
    fun execute(request: String): String?
}

interface Preferences {
    fun contains(key: String): Boolean
    fun getString(key: String, default: String?): String?
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getInt(key: String, default: Int): Int
    fun getLong(key: String, default: Long): Long
    fun getFloat(key: String, default: Float): Float
    fun edit(): Editor

    interface Editor {
        fun putString(key: String, value: String?): Editor
        fun putBoolean(key: String, value: Boolean): Editor
        fun putInt(key: String, value: Int): Editor
        fun putLong(key: String, value: Long): Editor
        fun putFloat(key: String, value: Float): Editor
        fun remove(key: String): Editor
        fun clear(): Editor
        fun commit(): Boolean
        fun apply()
    }
}

class Values {
    private val storage = linkedMapOf<String, Any?>()

    val keys: Set<String>
        get() = storage.keys

    val values: Collection<Any?>
        get() = storage.values

    fun put(key: String, value: String?) { storage[key] = value }
    fun put(key: String, value: Int) { storage[key] = value }
    fun put(key: String, value: Long) { storage[key] = value }

    fun forEach(action: (Map.Entry<String, Any?>) -> Unit) = storage.forEach(action)
}

interface SqlDatabase : AutoCloseable {
    fun execSQL(sql: String, arguments: Array<out Any?> = emptyArray())
    fun rawQuery(sql: String, arguments: Array<out String>?): SqlCursor
    fun update(table: String, values: Values, where: String, arguments: Array<out String>): Int
    fun delete(table: String, where: String?, arguments: Array<out String>?): Int
    fun insertOrThrow(table: String, nullColumnHack: String?, values: Values): Long
    fun insertWithOnConflict(table: String, nullColumnHack: String?, values: Values, conflict: Int): Long
    fun compileStatement(sql: String): SqlStatement
    fun beginTransaction()
    fun setTransactionSuccessful()
    fun endTransaction()

    companion object { const val CONFLICT_IGNORE = 4 }
}

interface SqlCursor : AutoCloseable {
    fun moveToFirst(): Boolean
    fun moveToNext(): Boolean
    fun getString(index: Int): String
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
}

interface SqlStatement : AutoCloseable {
    fun bindString(index: Int, value: String)
    fun bindLong(index: Int, value: Long)
    fun executeUpdateDelete(): Int
    fun executeInsert(): Long
}

inline fun <T> SqlDatabase.transaction(block: SqlDatabase.() -> T): T {
    beginTransaction()
    return try {
        val result = block()
        setTransactionSuccessful()
        result
    } finally { endTransaction() }
}

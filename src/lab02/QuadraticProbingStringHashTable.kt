package lab02

/**
 * @author TonTL
 * @version 2/21/2018
 */
class QuadraticProbingStringHashTable(private val capacity: Int) {

    private val table = Array<String?>(capacity, { null })

    fun insert(s: String): Boolean {
        val idx = hashIdx(s)
        var i = 0L
        var offset: Int
        do {
            offset = ((idx.toLong() + i * i) % capacity).toInt()
            if (offset < 0) return false
            i++
        } while (table[offset] != null)
        table[offset] = s
        return true
    }

    fun values() = table

    private fun hashIdx(s: String): Int {
        val hash = s.hashKeyPoly(37)
        val idx = hash % capacity
        return if (idx < 0) idx + capacity else idx
    }
}
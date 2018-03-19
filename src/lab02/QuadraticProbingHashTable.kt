package lab02

/**
 * @author TonTL
 * @version 2/21/2018
 */
class QuadraticProbingHashTable<in K, out V>(var capacity: Int,
                                             private val hash: (K) -> Int,
                                             private val generator: (K) -> V,
                                             private val extractor: (V) -> K) {

    internal var table = Array<Any?>(capacity, { null })

    private fun put(v: V) {
        val k = extractor(v)
        var idx = hashIdx(k).toLong()
        var i = 0L
        var offset: Int
        while (true) {
            offset = ((idx + i * i) % table.size).toInt()
            if (offset < 0) {
                resizeTable()
                i = 0
                idx = hashIdx(k).toLong()
                continue
            }
            i++
            val value = table[offset]
            @Suppress("UNCHECKED_CAST")
            if (value == null) {
                table[offset] = v
                return
            }
        }
    }

    fun getOrCompute(k: K): V {
        var idx = hashIdx(k).toLong()
        var i = 0L
        var offset: Int
        while (true) {
            offset = ((idx + i * i) % table.size).toInt()
            if (offset < 0) {
                resizeTable()
                i = 0
                idx = hashIdx(k).toLong()
                continue
            }
            i++
            val value = table[offset]
            @Suppress("UNCHECKED_CAST")
            if (value == null) {
                val v = generator(k)
                table[offset] = v
                return v
            } else if (extractor(value as V) == k) {
                return value
            }
        }
    }

    private fun resizeTable() {
        val old = table
        capacity *= 2
        table = Array(capacity, { null })
        old.filter { it != null }
                .forEach {
                    @Suppress("UNCHECKED_CAST")
                    put(it!! as V)
                }
    }

    private fun hashIdx(k: K): Int {
        val hash = hash(k)
        val i = hash % capacity
        return if (i < 0) i + capacity else i
    }
}
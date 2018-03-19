package lab02

import java.util.*

/**
 * @author TonTL
 * @version 2/21/2018
 */
class SeperateChainingHashTable<in K, V>(private val capacity: Int,
                                         private val hash: (K) -> Int,
                                         private val generator: (K) -> V,
                                         private val extractor: (V) -> K) {

    val table = Array<MutableList<V>>(capacity, { LinkedList() })

    fun get(k: K): Optional<V> {
        val list = table[hashIdx(k)]
        val find = list.find { k == extractor(it) }
        return Optional.ofNullable(find)
    }

    fun getOrCompute(k: K): V {
        val list = table[hashIdx(k)]
        val find = list.find { k == extractor(it) }
        return if (find != null) {
            find
        } else {
            val v = generator(k)
            list.add(v)
            v
        }
    }

    private fun hashIdx(k: K): Int {
        val h = hash(k)
        val idx = h % capacity
        return if (idx >= 0) idx else idx + capacity
    }
}
package lab02

import java.util.*

/**
 * @author TonTL
 * @version 2/21/2018
 */
class SeperateChainingStringHashTable(val capacity: Int) {

    val table = Array<MutableList<String>>(capacity, { LinkedList() })

    fun insert(s: String) {
        table[hashIdx(s)].add(s)
    }

    fun contains(s: String) : Boolean {
        return table[hashIdx(s)].contains(s)
    }

    private fun hashIdx(s: String): Int {
        val hash = s.hashKeyPoly(37)
        val idx = hash % capacity
        return if (idx >= 0) idx else idx + capacity
    }
}
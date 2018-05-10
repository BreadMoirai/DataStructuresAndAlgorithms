package lab06

import javafx.event.ActionEvent
import javafx.scene.control.TextArea
import util.inputDialog
import util.mst
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.IntStream
import kotlin.system.measureNanoTime

class Controller {
    lateinit var input: TextArea
    lateinit var output: TextArea

    fun mpss(actionEvent: ActionEvent) {
        val a = input.text.split(Regex("(,\\s*)|(\\s+)")).map { it.trim() }.map { it.toInt() }.toIntArray()

        output.text = "NAIVE: ${naive(a)}\nDIV & CONQ: ${smart(a)}"
    }

    fun time(actionEvent: ActionEvent) {
        val await = inputDialog<NAB>().showAndWait()
        if (await.isPresent.not()) return
        val (n, a, b) = await.get()
        val arr = IntArray(n) { ThreadLocalRandom.current().nextInt(a, b) }
        output.text = "N = $n\nA = $a\nB = $b\nNAIVE: " + mst(measureNanoTime { naive(arr) }) +
                "\nDIV & CONQ: " + mst(measureNanoTime { smart(arr) })
    }

    fun naive(a: IntArray): Int {
        var minSum = Int.MAX_VALUE
        val n = a.size
        for (i in 0 until n) {
            var localSum = 0
            for (j in i until n) {
                localSum += a[j]
                if (localSum in 1 until minSum)
                    minSum = localSum
            }
        }
        return minSum
    }

    fun smart(a: IntArray): Int {
        return mpss(a)
    }

    fun mpss(a: IntArray, left: Int = 0, right: Int = a.size - 1): Int {
        if (right == left)
            return a[left]

        if (right == left + 1)
            return smallestOf(a[left], a[right], a[left] + a[right])

        val mid = (left + right) / 2

        val mssLeft = mpss(a, left, mid)
        val mssRight = mpss(a, mid + 1, right)
        val mssMid = mpssMiddle(a, left, mid, right)

        return smallestOf(mssLeft, mssMid, mssRight)
    }

    private fun smallestOf(vararg i: Int): Int {
        return IntStream.of(*i).filter { it > 0 }.min().orElse(Int.MAX_VALUE)
    }

    private fun mpssMiddle(a: IntArray, left: Int, mid: Int, right: Int): Int {
        val leftSum = IntArray(mid - left + 1)
        leftSum[0] = a[mid]
        for (i in 1..(mid - left)) {
            leftSum[i] = leftSum[i - 1] + a[mid - i]
        }
        leftSum.sort()
        val rightSum = IntArray(right - mid)
        rightSum[0] = a[mid + 1]
        for (i in 1..(right - mid - 1)) {
            rightSum[i] = rightSum[i - 1] + a[mid + 1 + i]
        }
        rightSum.sort()
        var minSum = Int.MAX_VALUE
        var i = 0
        var j = rightSum.size - 1
        while (i in 0 until leftSum.size && j in 0 until rightSum.size) {
            val localSum = leftSum[i] + rightSum[j]
            if (localSum in 1..(minSum - 1)) {
                minSum = localSum
            }
            if (localSum <= 0) {
                i++
            } else {
                j--
            }
        }
        return minSum
    }

    fun findEditDist(actionEvent: ActionEvent) {
        val wait = inputDialog<PQ>().showAndWait()
        if (wait.isPresent.not()) {
            return
        }
        val (p, q) = wait.get()
        val d = Array(p.length + 1) { IntArray(q.length + 1) }
        for (i in 0 .. p.length) {
            for (j in 0 .. q.length) {
                d[i][j] = when {
                    i == 0 -> j
                    j == 0 -> i
                    else -> minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + if (p[i-1] != q[j-1]) 1 else 0)
                }
            }
        }
        val sb = StringBuilder()
        var i = p.length
        var j = q.length
        do {
            val x = d[i][j]
            sb.append( when (x - 1) {
                d[i - 1][j - 1] -> "${p[--i]} -> ${q[--j]}\n"
                d[i - 1][j] -> "- ${p[--i]}\n"
                d[i][j - 1] -> "+ ${q[--j]}\n"
                else -> when (x) {
                    d[i - 1][j - 1] -> "${p[--i]} == ${q[--j]}\n"
                    else -> "ERR"
                }
            })
        } while (i > 0 && j > 0)
        output.text = sb.toString()
    }
}

data class NAB(val n: Int, val a: Int, val b: Int)

data class PQ(val p: String, val q: String)
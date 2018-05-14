package lab05

import javafx.event.ActionEvent
import javafx.scene.control.Alert
import javafx.scene.control.TextArea
import util.inputDialog
import util.mst
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min
import kotlin.system.measureNanoTime

class Controller {
    val rand = Random()
    lateinit var input: TextArea
    lateinit var output: TextArea

    fun profit(actionEvent: ActionEvent) {
        val lines = input.text.split("\n").map { it.split(Regex(",\\s*")) }
        if (lines.size != 3) {
            Alert(Alert.AlertType.ERROR, "WE NEED 3 LINES")
            return
        }
        val length = lines[0].size
        if (!lines.all { it.size == length }) {
            Alert(Alert.AlertType.ERROR, "UNEQUAL NUMBER OF ELEMENTS IN EACH LINE")
            return
        }
        val tasks = mutableListOf<UnitTask>()
        for (i in 0 until length) {
            tasks += UnitTask(lines[0][i], lines[1][i].toInt(), lines[2][i].toInt())
        }
        output.text = "NAIVE: ${naive(tasks)}\nDISJOINT-SET: ${disJoint(tasks)}"
    }

    fun time(actionEvent: ActionEvent) {
        val awit = inputDialog<NABPQ>().showAndWait()
        if (awit.isPresent.not()) return
        val (n, a, b, p, q) = awit.get()
        val arr = List(n) { i -> UnitTask(i.toString(), if (a == b) a else ThreadLocalRandom.current().nextInt(a, b), if (p == q) p else ThreadLocalRandom.current().nextInt(p, q)) }
        output.text = "NAIVE: " + mst(measureNanoTime { naive(arr) }) +
                "\nDISJOINT-SET: " + mst(measureNanoTime { disJoint(arr) })
    }

    fun naive(tasks: List<UnitTask>): Int {
        val max = tasks.maxBy { it.deadline }!!.deadline
        val sched = Array<UnitTask?>(max) { null }
        for (task in tasks.sortedByDescending { it.profit }) {
            for (i in task.deadline - 1 downTo 0) {
                if (sched[i] == null) {
                    sched[i] = task
                    break
                }
            }
        }
        return sched.filter { it != null }.sumBy { it!!.profit }
    }

    fun disJoint(tasks: List<UnitTask>): Int {
        val output = mutableListOf<UnitTask>()
        val max = tasks.maxBy { it.deadline }!!.deadline
        val array = Array(max + 1) { i -> TaskSet(i, if (i != 0) arrayOf(i) else emptyArray()) }
        for (task in tasks.sortedByDescending { it.profit }) {
            val set = array[task.deadline]
            if (set.time <= 0) continue
            output += task
            for (i in set.time - 1 downTo 0) {
                val other = array[i]
                if (other !== set) {
                    if (set.list.size >= other.list.size) {
                        set.merge(other)
                        for (ptr in other.list) {
                            array[ptr] = set
                        }
                    } else {
                        other.merge(set)
                        for (ptr in set.list) {
                            array[ptr] = other
                        }
                    }
                }
            }
        }
        return output.sumBy { it.profit }
    }
}

data class UnitTask(val name: String, val deadline: Int, val profit: Int)

class TaskSet(var time: Int, members: Array<Int>) {
    val list = members.toHashSet()

    operator fun plusAssign(o: Int) {
        list += o
    }

    fun merge(other: TaskSet): TaskSet {
        list += other.list
        time = min(time, other.time)
        return this
    }

}

data class NABPQ(val n: Int, val a: Int, val b: Int, val p: Int, val q: Int)
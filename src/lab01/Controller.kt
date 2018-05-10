/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
@file:Suppress("UNUSED_PARAMETER")

package lab01

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import util.mst
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.math.max
import kotlin.system.measureNanoTime

class Controller {
    @FXML
    var input: TextArea? = null
    @FXML
    var result: TextArea? = null
    @FXML
    var n: TextField? = null
    @FXML
    var a1: CheckBox? = null
    @FXML
    var a2: CheckBox? = null
    @FXML
    var a3: CheckBox? = null
    @FXML
    var a4: CheckBox? = null
    @FXML
    var resLabel: Label? = null

    private var runLabel = SimpleStringProperty()

    private val r = Random()

    private var inputArr: IntArray? = null

    private var predictFor: Int? = null

    @FXML
    var button: Button? = null

    @FXML
    fun initialize() {
        result!!.isDisable = true
        input!!.textProperty().addListener { _, _, newValue ->
            if (newValue != "Input not shown")
                inputArr = null
        }
        resLabel!!.textProperty().bind(runLabel)
    }

    fun generateNumbers(actionEvent: ActionEvent?) {
        val narr = this.n!!.text.split(",\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (narr.isEmpty()) {
            Alert(Alert.AlertType.ERROR, "Please specify an integer", ButtonType.OK)
            return
        }
        val n1 = Integer.parseInt(narr[0])

        predictFor = if (narr.size == 1) null else Integer.parseInt(narr[1])

        val arr = IntArray(n1, { r.nextInt(100) - 50 })

        if (n1 < 1_000_000) {
            val sj = StringJoiner(", ")
            for (i in arr.indices) {
                sj.add(arr[i].toString())
            }
            input!!.text = sj.toString()
        } else {
            input!!.text = "Input not shown"
        }
        inputArr = arr
    }

    fun findMMS(actionEvent: ActionEvent) {
        runLabel.set("Running...")
        button!!.isDisable = true
        result!!.isDisable = false

        if (inputArr == null) {
            try {
                inputArr = readInput()
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        }
        result!!.text = null

        val futures = ArrayList<CompletableFuture<Void>>(4)

        if (a1!!.isSelected) {
            futures.add(computeResults(1, inputArr!!, predictFor)!!)
        }
        if (a2!!.isSelected) {
            futures.add(computeResults(2, inputArr!!, predictFor)!!)
        }
        if (a3!!.isSelected) {
            futures.add(computeResults(3, inputArr!!, predictFor)!!)
        }
        if (a4!!.isSelected) {
            futures.add(computeResults(4, inputArr!!, predictFor)!!)
        }

        CompletableFuture.allOf(*futures.toTypedArray()).thenRun {
            Platform.runLater {
                button!!.isDisable = false
                runLabel.set("")
            }
        }
    }

    private fun readInput(): IntArray? {
        if (input!!.text.isEmpty()) {
            generateNumbers(null)
        }
        val text = input!!.text
        val split = text.split(Regex(",\\s*"), 0)
        return split.map { Integer.parseInt(it) }.toIntArray()
    }

    inner class ComputationResult(
            private val name: String,
            private val subsequencesum: Int,
            private val n: Int,
            private val elapsedTime: Long,
            private val predict: Int?,
            private val predictedTime: Long? = if (predict != null) getPredicted(name, n, elapsedTime, predict) else null) {

        override fun toString(): String {
            return "${"%-10s".format(name).replace(' ', '_')}: Result=$subsequencesum, runningTime[$n]=${mst(elapsedTime)} ${if (predict == null) "" else ", predictedTime[$predict]=${mst(predictedTime!!)} "}"
        }
    }


    fun computeResults(level: Int, a: IntArray, predict: Int?): CompletableFuture<Void>? {
        val name: String
        val func: (IntArray) -> Int
        when (level) {
            1 -> {
                func = this::freshman
                name = "Freshman"
            }
            2 -> {
                func = this::softmore
                name = "Sophmore"
            }
            3 -> {
                func = { junior(it) }
                name = "Junior"
            }
            4 -> {
                func = this::senior
                name = "Senior"
            }
            else -> {
                throw IndexOutOfBoundsException()
            }
        }

//        val begin = Instant.now()

//        val handler = EventHandler<ActionEvent> { runLabel.set("Running $name... ${mst(Duration.between(begin, Instant.now()).toNanos())}") }
//        if (timeline != null) {
//            timeline!!.stop()
//        }
//        timeline = Timeline(60.0,
//                KeyFrame(javafx.util.Duration.ZERO, handler),
//                KeyFrame(javafx.util.Duration.ONE))
//        timeline!!.cycleCount = Timeline.INDEFINITE
//        timeline!!.play()


        val runComputation = { a: IntArray, predict: Int? ->
            var result: Int? = null
            val elapsedTime = measureNanoTime {
                result = func(a)
            }
            ComputationResult(name, result!!, a.size, elapsedTime, predict)
        }
        var async = CompletableFuture.supplyAsync { runComputation(a, predict).toString() }
        if (predict != null) {
            async = async.thenApply {
                "$it\n${runComputation(IntArray(predict, { r.nextInt(100) - 50 }), null)}"
            }
        }
        return async.thenAccept { synchronized(result!!, { if (result!!.text != null) result!!.text += "$it\n" else result!!.text = "$it\n" }) }
    }


    fun freshman(a: IntArray): Int {
        val n = a.size - 1
        var sumMax = 0

        for (i in 0..n) {
            for (j in i..n) {
                var sum = 0
                for (k in i..j) {
                    sum += a[k]
                }
                if (sum > sumMax)
                    sumMax = sum
            }
        }
        return sumMax
    }

    fun softmore(a: IntArray): Int {
        val n = a.size - 1
        var sumMax = 0

        for (i in 0..n) {
            var sum = 0
            for (j in i..n) {
                sum += a[j]
                if (sum > sumMax) {
                    sumMax = sum
                }
            }
        }
        return sumMax
    }

    fun junior(a: IntArray, left: Int = 0, right: Int = a.size - 1): Int {
        if (right == left)
            return a[left]

        if (right == left + 1)
            return max(max(a[left], a[right]), a[left] + a[right])

        val mid = (left + right) / 2

        val mssLeft = junior(a, left, mid)
        val mssRight = junior(a, mid + 1, right)
        val mssMid = juniorMiddle(a, left, mid, right)

        return max(max(mssLeft, mssMid), mssRight)
    }

    fun juniorMiddle(a: IntArray, left: Int, mid: Int, right: Int): Int {
        var max_left_sum = Int.MIN_VALUE
        var sum = 0
        var i: Int = mid
        while (i >= left) {
            sum += a[i]
            if (sum > max_left_sum)
                max_left_sum = sum
            i--
        }

        var max_right_sum = Int.MIN_VALUE
        sum = 0
        i = mid + 1
        while (i <= right) {
            sum += a[i]
            if (sum > max_right_sum)
                max_right_sum = sum
            i++
        }
        return max_left_sum + max_right_sum
    }

    fun senior(a: IntArray): Int {
        var sumMax = 0
        var sum = 0
        for (i in a) {
            sum += i
            if (sum > sumMax)
                sumMax = sum
            else if (sum < 0)
                sum = 0

        }
        return sumMax
    }

    private fun getPredicted(name: String, n: Int, t: Long, predN: Int): Long {
        return when (name) {
            "Freshman" -> t / Math.pow(n.toDouble(), 3.0) * Math.pow(predN.toDouble(), 3.0) //n3
            "Sophmore" -> t / Math.pow(n.toDouble(), 2.0) * Math.pow(predN.toDouble(), 1.0) //n2
            "Junior" -> t / (n * Math.log(n.toDouble())) * (predN * Math.log(predN.toDouble())) //n log n
            "Senior" -> t.toDouble() / n * predN // n
            else -> 0.0
        }.toLong()
    }


}
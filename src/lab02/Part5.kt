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
package lab02

import javafx.fxml.FXML
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import util.mst
import java.io.BufferedReader
import java.io.FileReader
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.math.ln
import kotlin.system.measureNanoTime

class Part5 {

    lateinit var output: TextArea
    lateinit var choice: ChoiceBox<String>
    lateinit var time: TextField
    var selected = 0
    var tokens: List<String>? = null

    private fun updateTable() {
        if (tokens == null) return
        val tokens = this.tokens!!
        when (selected) {
            0 -> {
                var list: MutableList<Token>? = null
                val timeElapsed = measureNanoTime {
                    list = ArrayList()
                    for (token in tokens) {
                        val find = list!!.find { it.key == token }
                        if (find == null) {
                            list!!.add(Token(token, 1))
                        } else {
                            find.inc()
                        }
                    }
                }
                val collect = list!!.stream()
                        .sorted { o1, o2 -> o2.count - o1.count }
                        .map {
                            "${it.count}\t| ${it.key}"
                        }
                        .collect(Collectors.joining("\n"))
                output.text = collect
                time.text = mst(timeElapsed)
            }
            1 -> {
                var table: SeperateChainingHashTable<String, Token>? = null
                val timeElapsed = measureNanoTime {
                    table = SeperateChainingHashTable(
                            (tokens.size / ln(tokens.size.toDouble()).toInt()),
                            { it.hashKeyPoly(37) },
                            { Token(it, 0) },
                            { it.key }
                    )
                    for (token in tokens) {
                        table!!.getOrCompute(token).inc()
                    }
                }

                val collect = Arrays.stream(table!!.table)
                        .flatMap { it.stream() }
                        .sorted { o1, o2 -> o2.count - o1.count }
                        .map {
                            "${it.count}\t| ${it.key}"
                        }
                        .collect(Collectors.joining("\n"))
                output.text = collect
                time.text = mst(timeElapsed)
            }
            2 -> {
                var table: QuadraticProbingHashTable<String, Token>? = null
                val timeElapsed = measureNanoTime {
                    table = QuadraticProbingHashTable(
                            tokens.size * 2,
                            { it.hashKeyPoly(37) },
                            { Token(it, 0) },
                            { it.key })
                    for (token in tokens) {
                        table!!.getOrCompute(token).inc()
                    }
                }
                val collect = Arrays.stream(table!!.table)
                        .filter(Objects::nonNull)
                        .map { it!! }
                        .map { it as Token }
                        .sorted { o1, o2 -> o2.count - o1.count }
                        .map {
                            "${it.count}\t| ${it.key}"
                        }
                        .collect(Collectors.joining("\n"))
                output.text = collect
                time.text = mst(timeElapsed)
            }
        }
    }

    @FXML
    fun initialize() {
        choice.items.addAll("List", "Separate-Chaining Table", "Quadratic-Probing Table")
        choice.selectionModel.select(0)
        choice.selectionModel.selectedIndexProperty().addListener { _, _, v ->
            selected = v.toInt()
            updateTable()
        }
    }

    fun onDragDone(dragEvent: DragEvent) {
        val dragboard = dragEvent.dragboard
        val file = dragboard.files[0]
        BufferedReader(FileReader(file)).useLines {
            tokens = it.toList().cleanLines()
        }
        updateTable()
    }

    fun onDragOver(dragEvent: DragEvent) {
        val dragboard = dragEvent.dragboard
        if (dragboard.hasFiles()) {
            val files = dragboard.files
            if (files.size != 1)
                return
            val file = files[0]
            if (file.extension == "txt" || file.extension.isEmpty()) {
                dragEvent.acceptTransferModes(TransferMode.LINK)
            }
        }
    }

    data class Token(val key: String, var count: Int) {
        fun inc() = count++
    }

}

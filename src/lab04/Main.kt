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
package lab04

import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import util.inputDialog
import java.util.*

class Main : Application() {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        val root = BorderPane()
        primaryStage.title = "Hello World"
        val graph = GraphStage()
        root.center = graph
        graph.prefHeight = 480.0
        graph.prefWidth = 600.0
        graph.background = Background(BackgroundFill(Color.WHITESMOKE, null, null))
        addRightButtons(root, graph)
        val scene = Scene(root, 750.0, 480.0)
        scene.stylesheets.add(javaClass.getResource("style.css").toExternalForm())
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun addRightButtons(root: BorderPane, graph: GraphStage) {
        val output = TextArea()
        output.isEditable = false
        output.prefWidth = 100.0
        val b1 = Button("Distance")
        b1.setOnAction {
            graph.setOnNodeClick { node ->
                val distance = getDistance(node, graph)
                output.text = distance.toList().sortedWith(kotlin.Comparator { o1, o2 ->
                    when {
                        o1.second == o2.second -> 0
                        o1.second == -1 -> Int.MAX_VALUE
                        else -> o1.second - o2.second
                    }
                }).joinToString("\n",
                        "DISTANCE\nFROM [${node.name}]\n") { "${it.first.name}  |  ${if (it.second >= 0) it.second.toString() else "∞"}" }
            }
        }
        val b2 = Button("Get K-Subsets")
        b2.setOnAction {
            outputSubsetsNK(output)
        }
        val b3 = Button("Get ith K-Subset")
        b3.setOnAction {
            outputSubsetNKAI(output)
        }
        val b4 = Button("Waikabe")
        b4.setOnAction {
            for (i in 1..3) for (j in 1..4) {
                val c = Character.toString('A' + graph.nodes.size)
                val element = GraphNode(c, j * 100.0, i * 100.0, GraphStage.nodeSize, Color.SLATEBLUE, graph)
                graph += element
                if (i == 3 && j == 4) {
                    val c1 = Character.toString('A' + graph.nodes.size)
                    val element1 = GraphNode(c1, j * 100.0 + 100.0, i * 100.0, GraphStage.nodeSize, Color.SLATEBLUE,
                            graph)
                    graph += element1
                }
            }
            val nodeMap = mutableMapOf<String, GraphNode>()
            graph.nodes.associateByTo(nodeMap, { it.name })
            val createEdge = { a: String, b: String ->
                GraphEdge(nodeMap[a]!!, nodeMap[b]!!, graph)
            }
            val pipes = listOf("A" to "F", "A" to "H", "B" to "M", "D" to "L", "F" to "E", "F" to "G", "G" to "A",
                    "G" to "J", "G" to "L", "H" to "C", "H" to "I", "J" to "B", "J" to "D", "J" to "M",
                    "K" to "C", "K" to "D", "K" to "H", "K" to "J", "K" to "M", "L" to "C", "L" to "I")
            for (pipe in pipes) {
                createEdge(pipe.first, pipe.second)
            }
        }
        val b5 = Button("Waikabe B to E")
        b5.setOnAction {
            val nodeMap = mutableMapOf<String, GraphNode>()
            graph.nodes.associateByTo(nodeMap, { it.name })
            val createEdge = { a: String, b: String ->
                val edge = GraphEdge(nodeMap[a]!!, nodeMap[b]!!, graph)
                edge.mark()
                edge
            }
            val pipes = listOf('M' to 'I', 'E' to 'M', 'H' to 'L', 'J' to 'K', 'J' to 'I',
                    'I' to 'D', 'E' to 'A', 'I' to 'M', 'D' to 'I', 'G' to 'C', 'K' to 'L',
                    'F' to 'D', 'A' to 'B', 'H' to 'K', 'E' to 'F', 'G' to 'H', 'H' to 'G', 'E' to 'H',
                    'L' to 'M', 'I' to 'B', 'H' to 'J', 'E' to 'D', 'E' to 'B', 'A' to 'C',
                    'G' to 'D', 'C' to 'J', 'J' to 'L', 'H' to 'M', 'E' to 'L', 'E' to 'J')
            val waikabe = listOf('A' to 'F', 'A' to 'H', 'B' to 'M', 'D' to 'L', 'F' to 'E', 'F' to 'G', 'G' to 'A',
                    'G' to 'J', 'G' to 'L', 'H' to 'C', 'H' to 'I', 'J' to 'B', 'J' to 'D', 'J' to 'M',
                    'K' to 'C', 'K' to 'D', 'K' to 'H', 'K' to 'J', 'K' to 'M', 'L' to 'C', 'L' to 'I')
            val map = mutableMapOf<Char, MutableList<Char>>()
            for (i in 'A'..'M') {
                map[i] = mutableListOf()
            }
            for (pipe in waikabe) {
                map[pipe.first]!! += pipe.second
            }

            outer@ for (k in 1 until pipes.size) {
                for (subset in getKSubsets(k, pipes.size - 1)) {
                    val newPipes = subset.map { pipes[it] }
                    for (pipe in newPipes) {
                        if (pipe.second in map[pipe.first]!!) {
                            println("duplicate pipe")
                        }
                        map[pipe.first]!! += pipe.second
                    }
                    val visited = mutableSetOf<Char>()
                    val queue = ArrayDeque<Char>(listOf('B'))
                    while (queue.isNotEmpty()) {
                        val pop = queue.pop()
                        visited += pop
                        map[pop]!!.filter { it !in visited }.forEach { queue += it }
                    }
                    if ('E' in visited) {
                        for (pipe in newPipes) {
                            createEdge(pipe.first.toString(), pipe.second.toString())
                            output.text = newPipes.joinToString("\n") { "(${it.first}, ${it.second})" }
                        }
                        break@outer
                    } else {
                        for (pipe in newPipes) {
                            map[pipe.first]!! -= pipe.second
                        }
                    }
                }
            }
        }
        val b6 = Button("Clear")
        b6.setOnAction {
            graph.clear()
        }
        val pane = VBox(10.0, b1, b2, b3, b4, b5, output, b6)
        pane.padding = Insets(40.0, 10.0, 80.0, 10.0)
        pane.alignment = Pos.CENTER
        root.right = pane
    }

    private fun outputSubsetsNK(output: TextArea) {
        val opt = inputDialog<NK>().showAndWait()
        if (!opt.isPresent) {
            return
        }
        val (n, k) = opt.get()
        val list = getKSubsets(k, n)
        output.text = list.joinToString("\n") { it.joinToString(",", "{", "}") }
    }

    private fun getKSubsets(k: Int, n: Int): MutableList<IntArray> {
        val array = (0 until k).toList().toIntArray()
        val list = mutableListOf<IntArray>()
        do {
            list += array.copyOf()
        } while (nextCombination(array, k, n))
        return list
    }

    private fun outputSubsetNKAI(output: TextArea) {
        val opt = inputDialog<NKAI>().showAndWait()
        if (!opt.isPresent) {
            return
        }
        val (n, k, a, i) = opt.get()
        val array = (0 until k).toList().toIntArray()
        val sb = StringBuilder()
        for (j in 1..i) if (!nextCombination(array, k, n)) {
            Alert(Alert.AlertType.ERROR, "${i}th $k-Subset does not exist")
            return
        }

        sb.append(array.map { it + a }.joinToString(", ", "{", "}")).append('\n')

        output.text = sb.toString()
    }

    private fun nextCombination(array: IntArray, k: Int, maxS: Int): Boolean {
        for (j in k - 1 downTo 0) if (array[j] + (k - j) <= maxS) {
            var v = array[j] + 1
            for (r in j until k) {
                array[r] = v
                v++
            }
            return true
        }
        return false
    }

    private fun getDistance(node: GraphNode, graph: GraphStage): MutableMap<GraphNode, Int> {
        graph.setOnNodeClickNoAction()
        val sb = StringBuilder()
        sb.append("Distance From\n  Node [").append(node.name).append("]\n")
        val queue = ArrayDeque<GraphNode>()
        val nextLevel = mutableSetOf<GraphNode>()
        node.edges.map { it.b }.toCollection(queue)
        val visited = mutableMapOf(node to 0)
        var i = 1
        do {
            queue.addAll(nextLevel)
            nextLevel.clear()
            while (queue.isNotEmpty()) {
                val pop = queue.pop()
                pop.edges.map { it.b }.stream().filter { !visited.contains(it) }.forEach { nextLevel.add(it) }
                visited[pop] = i
            }
            i += 1
        } while (nextLevel.isNotEmpty())
        graph.nodes.filter { visited.contains(it).not() }.forEach { visited[it] = -1 }
        return visited
    }

}

data class NK(val n: Int, val k: Int)
data class NKAI(val n: Int, val k: Int, val a: Int, val i: Int)

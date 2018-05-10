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
            outputDistance(graph, output)
        }
        val b2 = Button("Get K-Subsets")
        b2.setOnAction {
            outputSubsetsNK(output)
        }
        val b3 = Button("Get ith K-Subset")
        b3.setOnAction {
            outputSubsetNKAI(output)
        }
        val pane = VBox(10.0, b1, b2, b3, output)
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
        val array = (0 until k).toList().toIntArray()
        val sb = StringBuilder()
        do {
            sb.append(array.joinToString(", ", "{", "}")).append('\n')
        } while (nextCombination(array, k, n))
        output.text = sb.toString()
    }

    private fun outputSubsetNKAI(output: TextArea) {
        val opt = inputDialog<NKAI>().showAndWait()
        if (!opt.isPresent) {
            return
        }
        val (n, k, a, i) = opt.get()
        val array = (0 until k).toList().toIntArray()
        val sb = StringBuilder()
        for (j in 1..i)
            if (!nextCombination(array, k, n)) {
                Alert(Alert.AlertType.ERROR, "${i}th $k-Subset does not exist")
                return
            }

        sb.append(array.map { it + a }.joinToString(", ", "{", "}")).append('\n')

        output.text = sb.toString()
    }

    private fun nextCombination(array: IntArray, k: Int, maxS: Int): Boolean {
        for (j in k - 1 downTo 0)
            if (array[j] + (k - j) <= maxS) {
                var v = array[j] + 1
                for (r in j until k) {
                    array[r] = v
                    v++
                }
                return true
            }
        return false
    }

    private fun outputDistance(graph: GraphStage, output: TextArea) {
        graph.setOnNodeClick { node ->
            graph.setOnNodeClickNoAction()
            val sb = StringBuilder()
            sb.append("Distance From\n  Node [")
                    .append(node.name)
                    .append("]\n")
            val visited = mutableSetOf(node)
            val queue = ArrayDeque<GraphNode>()
            val nextLevel = mutableSetOf<GraphNode>()
            node.connections.toCollection(queue)
            var i = 1
            do {
                queue.addAll(nextLevel)
                nextLevel.clear()
                while (queue.isNotEmpty()) {
                    val pop = queue.pop()
                    pop.connections.stream().filter { !visited.contains(it) }
                            .forEach { nextLevel.add(it) }
                    sb.append(pop.name).append(" | ").append(i).append('\n')
                    visited.add(pop)
                }
                i += 1
            } while (nextLevel.isNotEmpty())
            for (notVisited in graph.nodes.minus(visited)) {
                sb.append(notVisited.name).append(" | ").append('∞').append('\n')
            }
            output.text = sb.toString()
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}

data class NK(val n: Int, val k: Int)
data class NKAI(val n: Int, val k: Int, val a: Int, val i: Int)

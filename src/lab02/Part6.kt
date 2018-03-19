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

import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

class Part6 {
    lateinit var folderA: TextField
    lateinit var folderB: TextField
    lateinit var outputA: TextArea
    lateinit var outputB: TextArea
    private lateinit var documentsA: List<File>
    private lateinit var documentsB: List<File>

    private fun updateTable() {
        if (this::documentsB.isInitialized) {
            var tokenCount = 0
            val tokensA = ArrayList<MutableList<String>>()
            for (i in 0..500) {
                val element = readFile(documentsA[i])
                tokensA.add(element.toMutableList())
                tokenCount += element.size
            }
            val tokensB = ArrayList<MutableList<String>>()
            for (i in 0..500) {
                val element = readFile(documentsB[i])
                tokensB.add(element.toMutableList())
                tokenCount += element.size
            }
            val table = SeperateChainingHashTable<String, Token>(
                    (tokenCount / ln(tokenCount.toDouble())).toInt(),
                    { it.hashKeyPoly(37) },
                    { Token(it, 0, 0) },
                    { it.token })
            tokensA.forEach {
                while (it.isNotEmpty()) {
                    val s = it.removeAt(0)
                    it.removeIf { it == s }
                    table.getOrCompute(s).incA()
                }
            }
            tokensB.forEach {
                while (it.isNotEmpty()) {
                    val s = it.removeAt(0)
                    it.removeIf { it == s }
                    table.getOrCompute(s).incB()
                }
            }

            val collect = Arrays.stream(table.table)
                    .flatMap { it.stream() }
                    .map { Discriminator(it.token, it.correlation()) }
                    .filter { it.correlation != Double.NaN }
                    .filter { abs(it.correlation) > 0.0001 }
                    .collect(Collectors.partitioningBy { it.correlation > 0 })
            val comparator = Comparator.comparingDouble<Discriminator>({ it.correlation })
            outputA.text = collect[true]!!
                    .stream()
                    .sorted(comparator.reversed())
                    .map { it.toString() }
                    .collect(Collectors.joining("\n"))
            outputB.text = collect[false]!!
                    .stream()
                    .sorted(comparator)
                    .map { it.toString() }
                    .collect(Collectors.joining("\n"))
        }
    }

    fun onDragA(dragEvent: DragEvent) {
        val folder = dragEvent.dragboard.files[0]
        val filesA = folder.listFiles()
        val list = ArrayList<File>()
        list.addAll(filesA)
        list.shuffle()
        documentsA = ArrayList(list)
        folderA.text = "${folder.name} (${documentsA.size})"
        updateTable()
    }

    fun onDragB(dragEvent: DragEvent) {
        val folder = dragEvent.dragboard.files[0]
        val filesB = folder.listFiles()
        val list = ArrayList<File>()
        list.addAll(filesB)
        list.shuffle()
        documentsB = ArrayList(list)
        folderB.text = "${folder.name} (${documentsB.size})"
        updateTable()
    }

    fun onDragOverA(dragEvent: DragEvent) {
        acceptDrag(dragEvent)
    }

    fun onDragOverB(dragEvent: DragEvent) {
        if (this::documentsA.isInitialized)
            acceptDrag(dragEvent)
    }

    private fun acceptDrag(dragEvent: DragEvent) {
        val dragboard = dragEvent.dragboard
        if (dragboard.hasFiles()) {
            val files = dragboard.files
            if (files.size != 1)
                return
            val file = files[0]
            if (file.isDirectory) {
                dragEvent.acceptTransferModes(TransferMode.LINK)
            }
        }
    }

    private fun readFile(file: File): List<String> {
        var tokens: List<String>? = null
        BufferedReader(FileReader(file)).useLines {
            tokens = it.toList().cleanLines()
        }
        return tokens!!
    }

    data class Token(val token: String,
                     var a: Int = 0,
                     var b: Int = 0) {
        fun incA() = a++
        fun incB() = b++

        fun correlation() = (a - b) / (sqrt(a.toDouble() + b) * sqrt(1000.0 - a - b))
    }

    data class Discriminator(val token: String,
                             val correlation: Double) {

        override fun toString() = String.format("%.4f\t|\t%s", abs(correlation), token)
    }

}


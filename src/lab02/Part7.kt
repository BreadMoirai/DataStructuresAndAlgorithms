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

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.util.converter.DoubleStringConverter
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

class Part7 {
    lateinit var folderA: TextField
    lateinit var folderB: TextField
    lateinit var thresholdField: TextField
    lateinit var output: TextArea
    private lateinit var documentsA: List<File>
    private lateinit var documentsB: List<File>
    var threshold: Double = 0.0

    @FXML
    fun initialize() {
        thresholdField.textFormatter = TextFormatter(DoubleStringConverter())
        thresholdField.textProperty().addListener { _, _, newValue ->
            threshold = newValue.toDoubleOrNull() ?: 0.0
        }
    }

    private fun updateTable() {
        Platform.runLater {
            output.text = ""
        }
        if (this::documentsB.isInitialized) {
            var tokenCount = 0
            val tokensA = ArrayList<MutableList<String>>()

            appendOutput("Reading 500 documents from ${folderA.text}...\n")
            for (i in 0..499) {
                val element = readFile(documentsA[i])
                tokensA.add(element.toMutableList())
                tokenCount += element.size
            }
            val tokensB = ArrayList<MutableList<String>>()
            appendOutput("Reading 500 documents from ${folderB.text}...\n")
            for (i in 0..499) {
                val element = readFile(documentsB[i])
                tokensB.add(element.toMutableList())
                tokenCount += element.size
            }
            appendOutput("Creating table...\n")
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
            appendOutput("\nTesting ${folderA.text} documents...\n")
            var successes = 0
            for (i in 500..999) {
                appendOutput(String.format("Testing_\"%-15s\"_", documentsA[i].name)
                        .replace(' ', '.')
                        .replace('_', ' '))
                val tokens = readFile(documentsA[i])
                val sum = tokens.stream()
                        .mapToDouble { table.get(it).map { it.correlation() }.orElse(0.0) }
                        .filter { abs(it) > threshold }
                        .sum()
                if (sum > 0.00005) {
                    appendOutput(String.format("SUCCESS: Score=%.2f\n", sum))
                    successes++
                } else if (sum < -0.00005) {
                    appendOutput(String.format("FAILURE: Score=%.2f\n", sum))
                } else {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        successes++
                        appendOutput("SUCCESS: Score=NONE\n")
                    } else {
                        appendOutput("FAILURE: Score=NONE\n")
                    }
                }
            }
            appendOutput("\nTesting ${folderB.text} documents...\n")
            for (i in 500..999) {
                appendOutput(String.format("Testing_\"%-15s\"_", documentsB[i].name)
                        .replace(' ', '.')
                        .replace('_', ' '))
                val tokens = readFile(documentsB[i])
                val sum = tokens.stream()
                        .mapToDouble { table.get(it).map { it.correlation() }.orElse(0.0) }
                        .filter { abs(it) > threshold }
                        .sum()
                if (sum < -0.0001) {
                    appendOutput(String.format("SUCCESS: Score=%.2f\n", abs(sum)))
                    successes++
                } else if (sum > 0.0001) {
                    appendOutput(String.format("FAILURE: Score=%.2f\n", abs(sum)))
                } else {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        successes++
                        appendOutput("SUCCESS: Score=NONE\n")
                    } else {
                        appendOutput("FAILURE: Score=NONE\n")
                    }
                }
            }
            appendOutput("\n\nRESULTS:\n\t\tDocuments Successfully classified: $successes\n\n")
        }
        Platform.runLater {
            output.scrollTop = Double.MAX_VALUE
        }
    }

    private fun appendOutput(s: String) {
        Platform.runLater {
            output.text += s
            output.scrollTop = Double.MAX_VALUE
        }
    }

    fun onDragA(dragEvent: DragEvent) {
        val folder = dragEvent.dragboard.files[0]
        val filesA = folder.listFiles()
        val list = ArrayList<File>()
        list.addAll(filesA)
        list.shuffle()
        documentsA = ArrayList(list)
        folderA.text = folder.name
        CompletableFuture.runAsync { updateTable() }
    }

    fun onDragB(dragEvent: DragEvent) {
        val folder = dragEvent.dragboard.files[0]
        val filesB = folder.listFiles()
        val list = ArrayList<File>()
        list.addAll(filesB)
        list.shuffle()
        documentsB = ArrayList(list)
        folderB.text = folder.name
        CompletableFuture.runAsync { updateTable() }
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

}


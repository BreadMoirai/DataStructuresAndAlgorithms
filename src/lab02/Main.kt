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

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

fun String.hashKeyPoly(x: Int): Int {
    var sum = 0
    for (b in this.toByteArray()) {
        sum = sum * x + b.toInt()
    }
    return sum
}

fun List<String>.cleanLines(): List<String> {
    val list = mutableListOf<String>()
    this.forEach { line ->
        val split = line.split(' ')
        split.stream()
                .map { it.replace('>', ' ') }
                .map { it.replace('|', ' ') }
                .map { it.replace(',', ' ') }
                .map { it.replace('.', ' ') }
                .map { it.replace('\"', ' ') }
                .map { it.replace('?', ' ') }
                .map { it.replace('!', ' ') }
                .map { it.replace('-', ' ') }
                .map { it.replace("'", "") }
                .map { it.trim() }
                .map { it.toLowerCase() }
                .filter { it.matches(Regex("[a-z]+")) }
                .forEach { list.add(it) }
    }
    return list
}

class Main : Application() {

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("gui.fxml"))
        primaryStage.title = "Hello World"
        val scene = Scene(root, 570.0, 400.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}

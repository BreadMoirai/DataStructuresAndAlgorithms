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
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.util.converter.IntegerStringConverter
import java.util.stream.Collectors

class Part2 {
    lateinit var size: TextField
    lateinit var input: TextArea
    lateinit var output: TextArea

    @FXML
    fun initialize() {
        size.text = "size"
        size.textFormatter = TextFormatter(IntegerStringConverter(), 1)
        input.textProperty().addListener{ _, _, text ->
            compute(text)
        }
    }

    private fun compute(text: String) {
        output.clear()
        if (text.isEmpty()) return
        val z = size.toInt()
        val table = SeperateChainingStringHashTable(z)
        val tokens = input.text.split(' ', ',', '\n').toMutableList()
        tokens.removeIf(String::isEmpty)
        tokens.forEach(table::insert)
        table.table.forEachIndexed { index, list ->
            output.appendText("[$index] : ${list.stream().collect(Collectors.joining(", "))}\n")
        }
    }

    fun TextField.toInt() = Integer.parseInt(size.text)

}

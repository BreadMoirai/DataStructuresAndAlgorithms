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
import javafx.scene.control.*
import javafx.util.converter.IntegerStringConverter

class Part3 {
    lateinit var size: TextField
    lateinit var input: TextArea
    lateinit var output: TextArea

    @FXML
    fun initialize() {
        size.text = "size"
        size.textFormatter = TextFormatter(IntegerStringConverter(), 1)
        input.textProperty().addListener { _, old, text ->
            if (!compute(text)) {
                input.text = old
            }
        }
    }

    private fun compute(text: String) : Boolean {
        output.clear()
        if (text.isEmpty()) return true
        val z = size.toInt()
        val table = QuadraticProbingStringHashTable(z)
        val tokens = input.text.split(' ', ',', '\n').toMutableList()
        tokens.removeIf(String::isEmpty)
        tokens.forEach{ value ->
            if (!table.insert(value)) {
                Alert(Alert.AlertType.ERROR, "Could not insert element", ButtonType.OK).show()
                return false
            }
        }
        table.values().forEachIndexed { index,s ->
            output.appendText("[$index] : ${s ?: ""}\n")
        }
        return true
    }

    fun TextField.toInt() = Integer.parseInt(this.text)
}

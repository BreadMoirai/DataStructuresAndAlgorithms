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
package lab03

import javafx.event.ActionEvent
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import java.util.*

class Controller {

    fun onAction(actionEvent: ActionEvent) {
        val text = (actionEvent.source as TextField).text
        try {
            val tree = ParseTree(text)
            ParseTreeStage(tree, "text").show()
        } catch (e: InputMismatchException) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.headerText = "Failed to parse equation"
            alert.contentText = text
            alert.showAndWait()
        }
    }



}
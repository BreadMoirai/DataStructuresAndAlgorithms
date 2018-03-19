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
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import java.nio.file.Files

class Part4 {

    lateinit var output: TextArea

    fun onDragDone(dragEvent: DragEvent) {
        val dragboard = dragEvent.dragboard
        val file = dragboard.files[0]
        val lines = Files.readAllLines(file.toPath())
        val tokens = lines.cleanLines()
        val content = tokens.joinToString(" ")
        output.text = content
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

}

package lab03

import com.sun.javafx.charts.Legend
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextFormatter
import javafx.scene.control.TextInputDialog
import javafx.scene.control.Tooltip
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeType
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import javafx.util.converter.IntegerStringConverter
import javax.swing.GroupLayout

private val nodeTip = Tooltip("Evaluate This Node")

class ParseTreeStage(tree: ParseTree, title: String) : Stage() {

    private val d = 800.0

    init {
        this.title = title
        this.scene = Scene(createNodeGroup(tree), d, d)
    }

    private fun createNodeGroup(tree: ParseTree): Group {
        val graphNodes = mutableListOf<Node>()

        val treeH = tree.root.getHeight()
        val secH = d / treeH
        val top = secH / 2

        var parentPoints = arrayOfNulls<Point>(0)
        var nodes: Array<ParseTree.Node?> = arrayOf(tree.root)
        var points: Array<Point?>
        var children: Array<ParseTree.Node?>

        for (h in 0..treeH) {
            points = arrayOfNulls(nodes.size)
            children = arrayOfNulls(nodes.size * 2)
            for (i in nodes.indices) {
                val node = nodes[i] ?: continue

                val secW = d / nodes.size
                val left = secW / 2
                children[i * 2] = node.left
                children[i * 2 + 1] = node.right
                val x = SimpleDoubleProperty(left + secW * i)
                val y = SimpleDoubleProperty(top + secH * h)
                val p = Point(x, y)
                points[i] = p

                graphNodes.add(DragNode(node, p))
                graphNodes.add(TreeNode(node, p))
                if (parentPoints.isNotEmpty()) graphNodes.add(BoundLine(parentPoints[i / 2]!!, p))
            }
            parentPoints = points
            nodes = children
        }
        graphNodes.add(ParseTreeLegend())
        return Group(graphNodes)
    }

    class ParseTreeLegend : TilePane(Orientation.VERTICAL) {
        init {
            children.add(HBox(makeCircle(Color.CORAL), makeLabel("Operation")))
            children.add(HBox(makeCircle(Color.MEDIUMAQUAMARINE), makeLabel("Integer")))
            children.add(HBox(makeCircle(Color.CORNFLOWERBLUE), makeLabel("Variable")))
            children.add(HBox(makeLine(Color.DARKGREEN), makeLabel("Left")))
            children.add(HBox(makeLine(Color.CADETBLUE), makeLabel("Right")))
        }

        private fun makeLabel(s: String): Node {
            val label = Text(s)
            label.translateY = label.layoutBounds.height / 4
            label.translateX = 5.0
            return label
        }

        private fun makeLine(color: Color): Node {
            val box = HBox(3.0)
            box.alignment = Pos.CENTER
            for (i in 0..2) {
                val line = Rectangle(6.0, 3.0)
                line.fill = color.deriveColor(0.0, 1.0, 1.0, 0.5)
                box.children.add(line)
            }
            return box
        }

        private fun makeCircle(color: Color) : Circle {
            val circle = Circle(10.0)
            circle.fill = color.deriveColor(1.0, 1.0, 1.0, 0.5)
            circle.stroke = color
            circle.strokeWidth = 2.0
            circle.strokeType = StrokeType.OUTSIDE
            return circle
        }
    }

    data class Point(val x: DoubleProperty, val y: DoubleProperty)

    class DragNode(node: ParseTree.Node, p: Point) : Circle(p.x.get(), p.y.get(), 25.0) {
        init {
            val color = when (node.value) {
                is ParseTree.Variable  -> Color.CORNFLOWERBLUE
                is ParseTree.Operation -> Color.CORAL
                is Int                 -> Color.MEDIUMAQUAMARINE
                else                   -> Color.BLACK
            }
            fill = color.deriveColor(1.0, 1.0, 1.0, 0.5)
            stroke = color
            strokeWidth = 2.0
            strokeType = StrokeType.OUTSIDE
            p.x.bind(centerXProperty())
            p.y.bind(centerYProperty())
            enableDrag()
        }

        fun enableDrag() {
            val dragDelta = DragNode.Delta()
            setOnMousePressed { event ->
                // record a delta distance for the drag and drop operation.
                dragDelta.x = centerX - event.x
                dragDelta.y = centerY - event.y
                scene.cursor = Cursor.MOVE
            }
            setOnMouseReleased { event ->
                scene.cursor = Cursor.HAND
            }
            setOnMouseDragged { event ->
                val newX = event.x + dragDelta.x
                if (newX > 0 && newX < scene.width) {
                    centerX = newX
                }
                val newY = event.y + dragDelta.y
                if (newY > 0 && newY < scene.height) {
                    centerY = newY
                }
            }
            setOnMouseEntered { event ->
                if (!event.isPrimaryButtonDown) scene.cursor = Cursor.HAND
            }
            setOnMouseExited { event ->
                if (!event.isPrimaryButtonDown) scene.cursor = Cursor.DEFAULT
            }
        }

        data class Delta(var x: Double = 0.0, var y: Double = 0.0)
    }

    class TreeNode(private val node: ParseTree.Node, p: Point) : Text(p.x.get(), p.y.get(), node.value.toString()) {

        init {
            val value = node.value
            if (value is ParseTree.Variable) {
                textProperty().bind(value.string)
                textProperty().addListener { _, _, _ ->
                    translateY = layoutBounds.height / 4
                    translateX = layoutBounds.width / -2
                }
            }
            font = Font("Comic Sans", 24.0)
            xProperty().bind(p.x)
            yProperty().bind(p.y)
            translateY = layoutBounds.height / 4
            translateX = layoutBounds.width / -2
            textAlignment = TextAlignment.CENTER
            Tooltip.install(this, nodeTip)
            enableAction()
        }

        private fun enableAction() {
            setOnMouseClicked {
                if (node.value is ParseTree.Variable) {
                    val dialog = TextInputDialog()
                    dialog.headerText = "Enter Value for ${node.value.c}"
                    dialog.editor.textFormatter = TextFormatter(IntegerStringConverter())
                    dialog.showAndWait().map { it.toIntOrNull() }.ifPresent {
                        node.value.value = it
                    }
                } else {
                    try {
                        val alert = Alert(Alert.AlertType.INFORMATION, node.evaluate().toString())
                        alert.headerText = "Result"
                        alert.show()
                    } catch (e: ParseTree.Node.VariableNotSetException) {
                        val alert = Alert(Alert.AlertType.ERROR)
                        alert.headerText = "A variable has not been set: ${e.message}"
                        alert.showAndWait()
                    }
                }
            }
            setOnMouseEntered { event ->
                if (!event.isPrimaryButtonDown) scene.cursor = Cursor.CROSSHAIR
            }
            setOnMouseExited { event ->
                if (!event.isPrimaryButtonDown) scene.cursor = Cursor.DEFAULT
            }
        }
    }

    class BoundLine(start: Point, end: Point) : Line() {
        init {
            startXProperty().bind(start.x)
            startYProperty().bind(start.y)
            endXProperty().bind(end.x)
            endYProperty().bind(end.y)
            strokeWidth = 2.0
            stroke = if (start.x.get() - end.x.get() > 0) {
                Color.DARKGREEN.deriveColor(0.0, 1.0, 1.0, 0.5)
            } else {
                Color.CADETBLUE.deriveColor(0.0, 1.0, 1.0, 0.5)
            }
            strokeLineCap = StrokeLineCap.BUTT

            strokeDashArray.setAll(10.0, 5.0)
            isMouseTransparent = true
        }
    }

}

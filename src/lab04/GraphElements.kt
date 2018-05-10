package lab04

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.scene.shape.ArcTo
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.StrokeLineCap
import javafx.scene.shape.StrokeType
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment

/**
 * @author TonTL
 * @version 5/3/2018
 */
data class Delta(var x: Double = 0.0, var y: Double = 0.0)

class Point(x: Double = 0.0, y: Double = 0.0) {
    private val xProperty = SimpleDoubleProperty(x)
    private val yProperty = SimpleDoubleProperty(y)
    var x: Double
        get() = xProperty.value
        set(value) = xProperty.set(value)
    var y: Double
        get() = yProperty.value
        set(value) = yProperty.set(value)

    fun bindX(property: DoubleProperty) = property.bind(xProperty)
    fun bindY(property: DoubleProperty) = property.bind(yProperty)
    fun addListener(function: (Double, Double) -> Unit) {
        xProperty.addListener({ _, _, _ -> function.invoke(x, y) })
        yProperty.addListener({ _, _, _ -> function.invoke(x, y) })
    }
}

class GraphNode(val name: String, x: Double, y: Double, s: Double, c: Color, val graph: GraphStage) : Circle(x, y, s) {
    val point = Point()
    val edges = mutableSetOf<GraphEdge>()
    val components: List<Node>
        get() = listOf(this, core, core.text)

    private val core = Core(name, point)

    private val colorStroke = c

    private val colorFill: Color
        get() = colorStroke.deriveColor(1.0, 1.0, 1.0, 0.5)

    init {
        point.bindX(centerXProperty())
        point.bindY(centerYProperty())
        point.x = x
        point.y = y
        fill = colorFill
        stroke = colorStroke
        strokeWidth = 2.0
        strokeType = StrokeType.OUTSIDE
        enableDrag()
        setOnMouseClicked { graph.onNodeClick(this) }
    }

    private fun enableDrag() {
        val dragDelta = Delta()
        setOnMousePressed { event ->
            // record a delta distance for the drag and drop operation.
            dragDelta.x = centerX - event.x
            dragDelta.y = centerY - event.y
            scene.cursor = Cursor.MOVE
        }
        setOnMouseReleased {
            scene.cursor = Cursor.HAND
        }
        setOnMouseDragged { event ->
            val newX = event.x + dragDelta.x
            if (newX > 0 && newX < scene.width) {
                point.x = newX
            }
            val newY = event.y + dragDelta.y
            if (newY > 0 && newY < scene.height) {
                point.y = newY
            }
        }
        setOnMouseEntered { event ->
            if (!event.isPrimaryButtonDown) scene.cursor = Cursor.HAND

        }
        setOnMouseExited { event ->
            if (!event.isPrimaryButtonDown) scene.cursor = Cursor.DEFAULT
        }
    }

    inner class Core(textValue: String, point: Point) : Circle(point.x, point.y, this@GraphNode.radius / 2) {

        private val node: GraphNode
            get() = this@GraphNode
        val text = Text(centerX, centerY, textValue)

        init {
            point.bindX(centerXProperty())
            point.bindY(centerYProperty())
            with(text) {
                point.bindX(xProperty())
                point.bindY(yProperty())
                font = Font("Comic Sans", 30.0)
                translateY = layoutBounds.height / 4
                translateX = layoutBounds.width / -2
                textAlignment = TextAlignment.CENTER
                isMouseTransparent = true
            }
            fill = Color.TRANSPARENT
            stroke = Color.TRANSPARENT
            enableAction()
        }

        fun setColor(stroke: Color, fill: Color) {
            this@GraphNode.stroke = stroke
            this@GraphNode.fill = fill
        }

        private fun enableAction() {
            setOnMouseEntered { event ->
                if (!event.isPrimaryButtonDown) scene.cursor = Cursor.CROSSHAIR
            }
            setOnMouseExited { event ->
                if (!event.isPrimaryButtonDown) scene.cursor = Cursor.DEFAULT
            }
            var endPoint: Point? = null
            var lineDrag: BoundLine? = null
            setOnMousePressed { event ->

            }
            setOnMouseReleased { event ->
                if (event.isStillSincePress) {
                    graph.onNodeClick(this@GraphNode)
                    event.consume()
                }
                isMouseTransparent = false
                endPoint = null
                val lineDrag1 = lineDrag
                if (lineDrag1 != null) {
                    graph -= lineDrag1
                }
                scene.cursor = Cursor.HAND
            }
            setOnMouseDragged { event ->
                if (lineDrag == null) {
                    isMouseTransparent = true
                    val point1 = Point(event.x, event.y)
                    endPoint = point1
                    val boundLine = BoundLine(point, point1)
                    lineDrag = boundLine
                    graph += boundLine
                }
                val p = endPoint
                if (p != null) {
                    p.x = event.x
                    p.y = event.y
                }
            }
            setOnDragDetected {
                startFullDrag()
            }
            setOnMouseDragOver { event ->
                if (event.gestureSource !== this && event.gestureSource is GraphNode.Core) {
                    val deriveColor = (this@GraphNode.stroke as Color).deriveColor(10.0, 1.0, 1.0, 1.0)
                    val deriveFill = deriveColor.deriveColor(1.0, 1.0, 1.0, 0.5)
                    setColor(deriveColor, deriveFill)
                    (event.gestureSource as Core).setColor(deriveColor, deriveFill)
                }
            }
            setOnMouseDragExited { event ->
                setColor(colorStroke, colorFill)
                if (event.gestureSource is Core) (event.gestureSource as Core).setColor(colorStroke, colorFill)
            }
            setOnMouseDragReleased { event ->
                if (event.gestureSource is Core && event.gestureSource !== this) {
                    val a = (event.gestureSource as Core).node
                    GraphEdge(a, this@GraphNode, graph)
                }
            }
        }
    }
}

class GraphEdge(val a: GraphNode, val b: GraphNode, val graph: GraphStage) {

    private val arc = BoundArc(a.point, b.point)

    val components: Collection<Node>
        get() = listOf(arc)

    init {
        if (!graph.edges.contains(this)) {
            graph += this
            a.edges += this
        } else {
            println("DUPED")
        }
    }

    fun remove() {
        a.edges -= this
        graph -= this
    }

    fun mark() {
        arc.mark()
    }

    fun unmark() {
        arc.unmark()
    }

    override fun equals(other: Any?): Boolean {
        return other is GraphEdge && other.a === a && other.b === b
    }

    override fun hashCode(): Int {
        var result = a.hashCode()
        result = 31 * result + b.hashCode()
        return result
    }
}

class BoundLine(val start: Point, val end: Point) : Line() {

    private val grad
        get() = LinearGradient(start.x, start.y, end.x, end.y, false, CycleMethod.NO_CYCLE,
                               Stop(0.0, Color.RED.deriveColor(1.0, 1.0, 1.0, 0.7)), Stop(.5, Color.DARKSLATEGREY),
                               Stop(1.0, Color.SPRINGGREEN))

    init {
        start.bindX(startXProperty())
        start.bindY(startYProperty())
        end.bindX(endXProperty())
        end.bindY(endYProperty())
        strokeWidth = 3.0
        stroke = grad
        strokeLineCap = StrokeLineCap.SQUARE
        isMouseTransparent = true
    }

    fun mark() {
        stroke = LinearGradient(start.x, start.y, end.x, end.y, false, CycleMethod.NO_CYCLE,
                                Stop(0.0, Color.DEEPSKYBLUE.deriveColor(1.0, 1.0, 1.0, 0.7)),
                                Stop(.5, Color.DARKSLATEGREY), Stop(1.0, Color.SPRINGGREEN))
    }

    fun unmark() {
        stroke = grad
    }
}

class BoundArc(val start: Point, val end: Point) : Path() {

    private var isMark = false
    private val grad
        get() = if (isMark) LinearGradient(start.x, start.y, end.x, end.y, false, CycleMethod.NO_CYCLE,
                                           Stop(0.0, Color.HOTPINK), Stop(1.0, Color.GOLD))
        else LinearGradient(start.x, start.y, end.x, end.y, false, CycleMethod.NO_CYCLE,
                            Stop(0.0, Color.RED.deriveColor(1.0, 1.0, 1.0, 0.7)), Stop(.5, Color.DARKSLATEGREY),
                            Stop(1.0, Color.SPRINGGREEN))

    init {
        start.addListener{ _, _ -> stroke = grad }
        end.addListener{ _, _ -> stroke = grad }
        isMouseTransparent = true
        strokeWidth = 3.0
        stroke = Color.FIREBRICK.deriveColor(1.0, 1.0, 1.0, 0.86)
        val moveTo = MoveTo()
        start.bindX(moveTo.xProperty())
        start.bindY(moveTo.yProperty())
        elements += moveTo
        val arc = ArcTo()
        end.bindX(arc.xProperty())
        end.bindY(arc.yProperty())
        arc.radiusX = 900.0
        arc.radiusY = 900.0
        elements += arc
        stroke = grad
    }

    fun mark() {
        isMark = true
        stroke = grad
    }

    fun unmark() {
        isMark = false
        stroke = grad
    }

}
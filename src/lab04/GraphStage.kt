package lab04

import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import kotlin.math.pow
import kotlin.math.sqrt

class GraphStage : Region() {

    val nodes = ArrayList<GraphNode>()
    val edges = ArrayList<GraphEdge>()
    private val order: Int
        get() = nodes.size
    private val size: Int
        get() = edges.size

    init {
        setOnMouseClicked { event -> onClick(event) }
    }

    operator fun plusAssign(node: GraphNode) {
        nodes += node
        children += node.components
    }

    operator fun plusAssign(edge: GraphEdge) {
        edges += edge
        children.addAll(0, edge.components)
    }

    operator fun minusAssign(edge: GraphEdge) {
        edges -= edge
        children.removeAll(edge.components)
    }

    operator fun plusAssign(node: Node) {
        children.add(node)
    }

    operator fun minusAssign(node: Node) {
        children.remove(node)
    }

    private fun onClick(event: MouseEvent) {
        if (event.isStillSincePress) {
            if (nodes.parallelStream().anyMatch { node ->
                        sqrt((node.centerY - event.y).pow(2) + (node.centerX - event.x).pow(2)) < node.radius
                    }) {
                return
            }
            val c = Character.toString('A' + nodes.size)
            val element = GraphNode(c, event.x, event.y, nodeSize, Color.SLATEBLUE, this)
            this += element
        }
    }

    private var action: ((GraphNode) -> Unit) = noAction

    fun setOnNodeClick(action: (GraphNode) -> Unit) {
        this.action = action
    }

    fun setOnNodeClickNoAction() {
        setOnNodeClick(noAction)
    }

    fun onNodeClick(node: GraphNode) {
        action.invoke(node)
    }

    fun clear() {
        nodes.clear()
        edges.clear()
        children.clear()
    }

    companion object {
        val noAction: (GraphNode) -> Unit = {}
        const val nodeSize = 25.0
    }

}
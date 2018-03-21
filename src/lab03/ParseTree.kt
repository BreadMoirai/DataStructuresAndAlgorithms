package lab03

import javafx.beans.property.SimpleStringProperty
import java.util.*
import kotlin.math.max

class ParseTree(text: String) {

    val root = try {
        parseTree(splitTokens(text))
    } catch (e: Exception) {
        throw InputMismatchException(text)
    }

    data class Node(val value: Any, var left: Node? = null, var right: Node? = null) {

        fun evaluate(): Int {
            return when (value) {
                is Int -> value
                is Operation -> value.evaluate(left!!, right!!)
                is Variable -> {
                    return value.value ?: throw VariableNotSetException(value.c.toString())
                }
                else -> throw RuntimeException()
            }
        }

        class VariableNotSetException(s: String) : RuntimeException(s)

        fun getHeight(): Int {
            return 1 + max(left?.getHeight() ?: 0, right?.getHeight() ?: 0)
        }

    }

    class Variable(val c: Char) {
        var value: Int? = null
            set(value) {
                field = value
                string.value = "$c=${value ?: '?'}"
            }
        val string = SimpleStringProperty("$c=?")
        override fun toString(): String {
            return string.valueSafe
        }
    }

    abstract class Operation : Comparable<Operation> {

        fun evaluate(left: Node, right: Node): Int {
            return operate(left.evaluate(), right.evaluate())
        }

        abstract fun operate(x: Int, y: Int): Int

    }

    class Add : Operation() {

        override fun compareTo(other: Operation): Int {
            return when (other) {
                is Subtract, is Add -> 0
                else -> -1
            }
        }

        override fun operate(x: Int, y: Int): Int = x + y

        override fun toString(): String = "+"
    }

    class Subtract : Operation() {

        override fun compareTo(other: Operation): Int {
            return when (other) {
                is Add, is Subtract -> 0
                else -> -1
            }
        }

        override fun operate(x: Int, y: Int): Int = x - y

        override fun toString(): String = "-"
    }

    class Multiply : Operation() {

        override fun compareTo(other: Operation): Int {
            return when (other) {
                is Exponent -> -1
                is Divide, is Multiply -> 0
                else -> 1
            }
        }

        override fun operate(x: Int, y: Int): Int = x * y

        override fun toString(): String = "*"
    }

    class Divide : Operation() {

        override fun compareTo(other: Operation): Int {
            return when (other) {
                is Exponent -> -1
                is Multiply, is Divide -> 0
                else -> +1
            }
        }

        override fun operate(x: Int, y: Int): Int = x / y

        override fun toString(): String = "/"
    }

    class Exponent : Operation() {

        override fun compareTo(other: Operation): Int {
            return if (other is Exponent) 0 else 1
        }

        override fun operate(x: Int, y: Int): Int {
            var r = 1
            for (i in 1..y) {
                r *= x
            }
            return r
        }

        override fun toString(): String = "^"

    }

    private fun parseTree(tokens: Array<Any>, left: Int = 0, right: Int = tokens.lastIndex): Node {
        if (left == right) {
            val value = tokens[left]
            assert(value is Variable || value is Int)
            return Node(value)
        }

        var inPar = 0
        var opIdx = -1
        var op: Operation? = null
        for (i in left..right) {
            val token = tokens[i]
            when (token) {
                '(' -> inPar++
                ')' -> inPar--
                is Operation -> {
                    if (inPar == 0) {
                        if (op?.compareTo(token) ?: 1 >= 0) {
                            op = token
                            opIdx = i
                        }

                    }
                }
            }
        }
        return if (op != null) {
            val root = Node(tokens[opIdx])
            root.left = parseTree(tokens, left, opIdx - 1)
            root.right = parseTree(tokens, opIdx + 1, right)
            root
        } else {
            assert(tokens[left] == '(')
            assert(tokens[right] == ')')
            parseTree(tokens, left + 1, right - 1)
        }
    }

    private fun splitTokens(text: String): Array<Any> {
        val list = ArrayList<Any>()
        var i = 0

        w@ while (i < text.length) {
            val c = text[i]
            when {
                c.isWhitespace() -> {
                }
                c.isDigit() -> {
                    var j = i + 1
                    while (j < text.length && text[j].isDigit()) {
                        j++
                    }
                    list.add(text.substring(i, j).toInt())
                    i = j - 1
                }
                c == '+' -> list.add(Add())
                c == '-' -> list.add(Subtract())
                c == '*' -> list.add(Multiply())
                c == '/' -> list.add(Divide())
                c == '^' -> list.add(Exponent())
                c == '(' -> list.add('(')
                c == ')' -> list.add(')')
                else -> {
                    list.add(list.find { it is Variable && it.c == c } ?: Variable(c))
                }
            }
            i++
        }
        return list.toTypedArray()
    }

}

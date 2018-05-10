package util

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.util.Callback
import javafx.util.StringConverter
import javafx.util.converter.DoubleStringConverter
import javafx.util.converter.IntegerStringConverter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor

inline fun <reified R : Any> inputDialog(): InputDialog<R> {
    return InputDialog(R::class)
}

inline fun <reified R> KParameter.isType(): Boolean {
    return this.type.isSubtypeOf(R::class.createType())
}

class InputField(prop: KParameter) : TextField() {
    val name = prop.name!!
    val label = Label(name.replace("([A-Z])", " $0").capitalize())

    init {
        textFormatterProperty().set(when {
            prop.isType<String>() -> TextFormatter<String>(object : StringConverter<String>() {
                override fun toString(`object`: String?): String {
                    return `object` ?: ""
                }

                override fun fromString(string: String?): String {
                    return string ?: ""
                }

            })
            prop.isType<Int>() -> TextFormatter(IntegerStringConverter())
            prop.isType<Double>() -> TextFormatter(DoubleStringConverter())
            else -> {
                throw UnsupportedOperationException("No Input configured for type ${prop.type}")
            }
        })
    }

    fun addTo(pane: GridPane, pos: Int) {
        pane.add(label, 0, pos)
        pane.add(this, 1, pos)
    }

    fun getValue() = textFormatterProperty().get().value!!
}


class InputDialog<R : Any>(klass: KClass<R>) : Dialog<R>() {

    private val fields = mutableListOf<InputField>()

    init {
        if (klass.isData.not()) {
            throw UnsupportedOperationException("${klass.simpleName} must be data class")
        }
        title = klass.simpleName?.capitalize()
        headerText = "Input Requested"
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialogPane.minHeight = Region.USE_PREF_SIZE
        val grid = GridPane()
        grid.hgap = 10.0
        grid.vgap = 10.0
        grid.padding = Insets(20.0, 10.0, 10.0, 10.0)

        for ((index, prop) in klass.primaryConstructor!!.parameters.withIndex()) {
            val inputField = InputField(prop)
            fields.add(inputField)
            inputField.addTo(grid, index)
            if (index == 0) {
                Platform.runLater { inputField.requestFocus() }
            }
        }
        dialogPane.content = grid

        resultConverter = Callback { buttonType ->
            when (buttonType) {
                ButtonType.OK -> {
                    val first = klass.constructors.first { kFunction -> kFunction.parameters.size == fields.size }
                    val map = mutableMapOf<KParameter, Any?>()
                    first.parameters.associateTo(map, { kParameter: KParameter ->
                        Pair(kParameter, fields.find { it.name == kParameter.name }?.getValue())
                    })
                    first.callBy(map)
                }
                else -> null
            }
        }
    }
}
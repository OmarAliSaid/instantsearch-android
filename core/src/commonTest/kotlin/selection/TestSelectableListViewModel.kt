package selection

import selection.list.SelectableListViewModel
import selection.list.SelectionMode
import shouldEqual
import kotlin.test.Test


class TestSelectableListViewModel {

    private val valueA = "valueA"
    private val valueB = "valueB"

    @Test
    fun singleChoice() {
        SelectableListViewModel<String, String>(SelectionMode.Single).apply {
            items = listOf(valueA, valueB)
            onSelectionsComputed += { selections = it }
            computeSelections(valueA)
            selections shouldEqual setOf(valueA)
            computeSelections(valueB)
            selections shouldEqual setOf(valueB)
            computeSelections(valueB)
            selections shouldEqual setOf()
        }
    }

    @Test
    fun multipleChoice() {
        SelectableListViewModel<String, String>(SelectionMode.Multiple).apply {
            items = listOf(valueA, valueB)
            onSelectionsComputed += { selections = it }
            computeSelections(valueA)
            selections shouldEqual setOf(valueA)
            computeSelections(valueB)
            selections shouldEqual setOf(valueA, valueB)
            computeSelections(valueB)
            selections shouldEqual setOf(valueA)
        }
    }
}
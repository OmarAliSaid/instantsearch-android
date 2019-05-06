package selection.list

import selection.list.SelectionMode.Multiple
import selection.list.SelectionMode.Single
import kotlin.properties.Delegates


public open class SelectableListViewModel<K, V>(
    val selectionMode: SelectionMode
) {

    public val onItemsChanged: MutableList<(List<V>) -> Unit> = mutableListOf()
    public val onSelectionsChanged: MutableList<(Set<K>) -> Unit> = mutableListOf()
    public val onSelectionsComputed: MutableList<(Set<K>) -> Unit> = mutableListOf()

    public var items: List<V> by Delegates.observable(listOf()) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            onItemsChanged.forEach { it(newValue) }
        }
    }
    public var selections by Delegates.observable(setOf<K>()) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            onSelectionsChanged.forEach { it(newValue) }
        }
    }

    public fun computeSelections(key: K) {
        val selections = when (selectionMode) {
            Single -> if (key in selections) setOf() else setOf(key)
            Multiple -> if (key in selections) selections - key else selections + key
        }

        onSelectionsComputed.forEach { it(selections) }
    }
}
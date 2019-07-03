package com.algolia.instantsearch.helper.hierarchical

import com.algolia.instantsearch.core.item.ItemViewModel
import com.algolia.search.model.Attribute
import kotlin.properties.Delegates


/**
 * @param hierarchicalAttributes attributes of the hierarchy.
 * These MUST be specified in order, e.g. `["hierarchy.lvl0", "hierarchy.lvl1", "hierarchy.lvl2"]`.
 */
public open class HierarchicalViewModel(
    val hierarchicalAttributes: List<Attribute>,
    val separator: String,
    hierarchy: HierarchicalTree = HierarchicalTree()
) : ItemViewModel<HierarchicalTree>(hierarchy) {

    init {
        if (hierarchicalAttributes.isEmpty())
            throw IllegalArgumentException("HierarchicalAttributes should not be empty")
    }

    public val onSelectionsChanged: MutableList<(List<String>) -> Unit> = mutableListOf()
    public val onSelectionsComputed: MutableList<(HierarchicalPath) -> Unit> = mutableListOf()

    public var selections by Delegates.observable(listOf<String>()) { _, _, newValue ->
        onSelectionsChanged.forEach { it(newValue) }
    }

    /**
     * Computes selected levels as a List of Pair([Attribute], value) given a hierarchical key.
     *
     * @param key a hierarchy level separated by [separator], e.g. "products > shoes > running"
     *
     */
    public fun computeSelections(key: String) {
        val selections = key.split(separator).fold(listOf<String>()) { acc, s ->
            acc + if (acc.isEmpty()) s else acc.last() + separator + s
        }
        val hierarchicalPath = hierarchicalAttributes.mapIndexed { index, item ->
            selections.getOrNull(index)?.let { item to it }
        }.filterNotNull()

        onSelectionsComputed.forEach { it(hierarchicalPath) }
    }
}
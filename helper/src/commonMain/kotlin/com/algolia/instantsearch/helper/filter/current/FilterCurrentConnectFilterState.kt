package com.algolia.instantsearch.helper.filter.current

import com.algolia.instantsearch.core.observable.ObservableKey
import com.algolia.instantsearch.helper.filter.state.FilterGroupID
import com.algolia.instantsearch.helper.filter.state.FilterState
import com.algolia.search.model.filter.Filter

internal fun Map<FilterGroupID, Set<Filter>>.toFilterAndIds(): Set<FilterAndID> {
    return flatMap { (key, value) -> value.map { key to it } }.toSet()
}

public fun FilterCurrentViewModel.connectFilterState(
    filterState: FilterState,
    groupID: FilterGroupID? = null,
    key: ObservableKey? = null
) {
    filterState.filters.subscribePast(key) { filters ->
        val groups = filters.getGroups().filter { groupID == null || it.key == groupID }
        val filterAndIDs = groups.toFilterAndIds()

        this.filters.set(filterAndIDs)
    }
    event.subscribe(key) {
        filterState.notify {
            if (groupID != null) {
                clear(groupID)
            } else {
                clear()
            }
            it.forEach { add(it.first, it.second) }
        }
    }
}
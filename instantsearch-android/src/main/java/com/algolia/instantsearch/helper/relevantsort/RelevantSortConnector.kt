package com.algolia.instantsearch.helper.relevantsort

import com.algolia.instantsearch.core.relevantsort.RelevantSortConnector
import com.algolia.instantsearch.core.relevantsort.RelevantSortViewModel
import com.algolia.instantsearch.helper.searcher.SearcherMultipleIndex
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.relevantsort.internal.RelevantSortConnectorMultipleIndex
import com.algolia.instantsearch.helper.relevantsort.internal.RelevantSortConnectorSingleIndex

/**
 * Creates a [RelevantSortConnector] instance.
 *
 * @param searcher searcher that handles your searches
 * @param viewModel component handling relevant sort logic
 */
public fun RelevantSortConnector(
    searcher: SearcherSingleIndex,
    viewModel: RelevantSortViewModel = RelevantSortViewModel(),
): RelevantSortConnector {
    return RelevantSortConnectorSingleIndex(searcher, viewModel)
}

/**
 * Creates a [RelevantSortConnector] instance.
 *
 * @param searcher searcher that handles your searches
 * @param queryIndex index of query from response of which the user data will be extracted
 * @param viewModel component handling relevant sort logic
 */
public fun RelevantSortConnector(
    searcher: SearcherMultipleIndex,
    queryIndex: Int,
    viewModel: RelevantSortViewModel = RelevantSortViewModel(),
): RelevantSortConnector {
    return RelevantSortConnectorMultipleIndex(searcher, queryIndex, viewModel)
}

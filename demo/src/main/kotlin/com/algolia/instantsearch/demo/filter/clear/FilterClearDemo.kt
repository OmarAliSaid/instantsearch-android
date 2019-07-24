package com.algolia.instantsearch.demo.filter.clear

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.algolia.instantsearch.core.connection.Connection
import com.algolia.instantsearch.core.connection.disconnect
import com.algolia.instantsearch.demo.*
import com.algolia.instantsearch.helper.android.filter.FilterClearViewImpl
import com.algolia.instantsearch.helper.filter.clear.ClearMode
import com.algolia.instantsearch.helper.filter.clear.FilterClearWidget
import com.algolia.instantsearch.helper.filter.state.FilterState
import com.algolia.instantsearch.helper.filter.state.filters
import com.algolia.instantsearch.helper.filter.state.groupOr
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.searcher.with
import com.algolia.search.model.Attribute
import kotlinx.android.synthetic.main.demo_filter_clear.*
import kotlinx.android.synthetic.main.demo_filter_toggle_default.toolbar
import kotlinx.android.synthetic.main.header_filter.*


class FilterClearDemo : AppCompatActivity() {

    private val color = Attribute("color")
    private val category = Attribute("category")
    private val groupColor = groupOr(color)
    private val groupCategory = groupOr(category)
    private val filters = filters {
        group(groupColor) {
            facet(color, "red")
            facet(color, "green")
        }
        group(groupCategory) {
            facet(category, "shoe")
        }
    }
    private val filterState = FilterState(filters)
    private val searcher = SearcherSingleIndex(stubIndex)
    private val widgetClearAll = FilterClearWidget(filterState)
    private val widgetClearSpecified = FilterClearWidget(filterState, listOf(groupColor), ClearMode.Specified)
    private val widgetClearExcept = FilterClearWidget(filterState, listOf(groupColor), ClearMode.Except)
    private val connections = mutableListOf<Connection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_filter_clear)

        connections += searcher.with(filterState)
        connections += widgetClearAll.with(FilterClearViewImpl(filtersClearAll))
        connections += widgetClearSpecified.with(FilterClearViewImpl(buttonClearSpecified))
        connections += widgetClearExcept.with(FilterClearViewImpl(buttonClearExcept))

        configureToolbar(toolbar)
        configureSearcher(searcher)
        onFilterChangedThenUpdateFiltersText(filterState, filtersTextView, color, category)
        onErrorThenUpdateFiltersText(searcher, filtersTextView)
        onResponseChangedThenUpdateNbHits(searcher, nbHits)
        onResetThenRestoreFilters(reset, filterState, filters)

        searcher.searchAsync()
    }

    override fun onDestroy() {
        super.onDestroy()
        searcher.cancel()
        connections.disconnect()
        widgetClearAll.disconnect()
        widgetClearExcept.disconnect()
        widgetClearSpecified.disconnect()
    }
}
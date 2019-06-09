package com.algolia.instantsearch.demo.filter.toggle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.algolia.instantsearch.demo.*
import com.algolia.instantsearch.helper.android.filter.FilterToggleViewCompoundButton
import com.algolia.instantsearch.helper.filter.state.FilterState
import com.algolia.instantsearch.helper.filter.toggle.FilterToggleViewModel
import com.algolia.instantsearch.helper.filter.toggle.connectFilterState
import com.algolia.instantsearch.helper.filter.toggle.connectView
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.searcher.connectFilterState
import com.algolia.search.model.Attribute
import com.algolia.search.model.filter.Filter
import kotlinx.android.synthetic.main.demo_filter_toggle_default.*
import kotlinx.android.synthetic.main.header_filter.*


class FilterToggleDefaultDemo : AppCompatActivity() {

    private val popular = Attribute("popular")
    private val filterState = FilterState()
    private val searcher = SearcherSingleIndex(stubIndex)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_filter_toggle_default)

        searcher.connectFilterState(filterState)

        val viewModelPopular = FilterToggleViewModel(Filter.Facet(popular, true))
        val viewPopular =
            FilterToggleViewCompoundButton(checkBoxPopular)

        viewModelPopular.connectFilterState(filterState, default = Filter.Facet(popular, false))
        viewModelPopular.connectView(viewPopular)

        configureToolbar(toolbar)
        configureSearcher(searcher)
        onFilterChangedThenUpdateFiltersText(filterState, filtersTextView, popular)
        onClearAllThenClearFilters(filterState, filtersClearAll)
        onErrorThenUpdateFiltersText(searcher, filtersTextView)
        onResponseChangedThenUpdateNbHits(searcher, nbHits)

        searcher.searchAsync()
    }

    override fun onDestroy() {
        super.onDestroy()
        searcher.cancel()
    }
}
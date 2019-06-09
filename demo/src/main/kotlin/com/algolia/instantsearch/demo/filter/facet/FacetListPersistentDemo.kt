package com.algolia.instantsearch.demo.filter.facet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.algolia.instantsearch.core.selectable.list.SelectionMode
import com.algolia.instantsearch.demo.*
import com.algolia.instantsearch.helper.filter.facet.FacetListViewModel
import com.algolia.instantsearch.helper.filter.facet.connectFilterState
import com.algolia.instantsearch.helper.filter.facet.connectSearcher
import com.algolia.instantsearch.helper.filter.facet.connectView
import com.algolia.instantsearch.helper.filter.state.FilterState
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.searcher.connectFilterState
import com.algolia.search.model.Attribute
import com.algolia.search.model.IndexName
import kotlinx.android.synthetic.main.demo_facet_list_persistent.*
import kotlinx.android.synthetic.main.header_filter.*
import kotlinx.android.synthetic.main.include_list.*
import kotlinx.android.synthetic.main.include_search.*


class FacetListPersistentDemo : AppCompatActivity() {

    private val color = Attribute("color")
    private val category = Attribute("category")
    private val index = client.initIndex(IndexName("stub"))
    private val filterState = FilterState()
    private val searcher = SearcherSingleIndex(index)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_facet_list_persistent)

        searcher.connectFilterState(filterState)

        val colorViewModel = FacetListViewModel(persistentSelection = true)
        val colorAdapter = FacetListAdapter()

        colorViewModel.connectFilterState(color, filterState)
        colorViewModel.connectView(colorAdapter)
        colorViewModel.connectSearcher(color, searcher)

        val categoryViewModel = FacetListViewModel(selectionMode = SelectionMode.Single, persistentSelection = true)
        val categoryAdapter = FacetListAdapter()

        categoryViewModel.connectFilterState(category, filterState)
        categoryViewModel.connectView(categoryAdapter)
        categoryViewModel.connectSearcher(category, searcher)

        configureToolbar(toolbar)
        configureSearcher(searcher)
        configureSearchBox(searchView, searcher)
        configureSearchView(searchView, getString(R.string.search_items))
        configureRecyclerView(listTopLeft, colorAdapter)
        configureRecyclerView(listTopRight, categoryAdapter)
        configureTitle(titleTopLeft, getString(R.string.multiple_choice))
        configureTitle(titleTopRight, getString(R.string.single_choice))
        onFilterChangedThenUpdateFiltersText(filterState, filtersTextView, color, category)
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
package com.algolia.instantsearch.demo.filter.toggle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.algolia.instantsearch.demo.*
import com.algolia.instantsearch.helper.android.selectable.SelectableCompoundButton
import com.algolia.instantsearch.helper.filter.toggle.FilterToggleViewModel
import com.algolia.instantsearch.helper.filter.toggle.connectFilterState
import com.algolia.instantsearch.helper.filter.toggle.connectView
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.model.Attribute
import com.algolia.search.model.IndexName
import com.algolia.search.model.filter.Filter
import com.algolia.search.model.filter.NumericOperator
import kotlinx.android.synthetic.main.demo_filter_toggle.*
import kotlinx.android.synthetic.main.header_filter.*


class FilterToggleDemo : AppCompatActivity() {

    private val promotions = Attribute("promotions")
    private val size = Attribute("size")
    private val tags = "_tags"
    private val colors
        get() = mapOf(
            promotions.raw to ContextCompat.getColor(this, android.R.color.holo_blue_dark),
            size.raw to ContextCompat.getColor(this, android.R.color.holo_green_dark),
            tags to ContextCompat.getColor(this, android.R.color.holo_purple)
        )

    private val index = client.initIndex(IndexName("stub"))
    private val searcher = SearcherSingleIndex(index)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_filter_toggle)

        searcher.index = client.initIndex(intent.indexName)

        val viewModelFreeShipping = FilterToggleViewModel(Filter.Facet(promotions, "free shipping"))
        val viewFreeShipping =
            SelectableCompoundButton(checkBoxFreeShipping)

        viewModelFreeShipping.connectFilterState(searcher.filterState)
        viewModelFreeShipping.connectView(viewFreeShipping)

        val viewModelCoupon = FilterToggleViewModel(Filter.Facet(promotions, "coupon"))
        val viewCoupon = SelectableCompoundButton(switchCoupon)

        viewModelCoupon.connectFilterState(searcher.filterState)
        viewModelCoupon.connectView(viewCoupon)

        val viewModelSize = FilterToggleViewModel(Filter.Numeric(size, NumericOperator.Greater, 40))
        val viewSize = SelectableCompoundButton(checkBoxSize)

        viewModelSize.connectFilterState(searcher.filterState)
        viewModelSize.connectView(viewSize)

        val viewModelVintage = FilterToggleViewModel(Filter.Tag("vintage"))
        val viewVintage = SelectableCompoundButton(checkBoxVintage)

        viewModelVintage.connectFilterState(searcher.filterState)
        viewModelVintage.connectView(viewVintage)

        onChangeThenUpdateFiltersText(searcher.filterState, colors, filtersTextView)
        onClearAllThenClearFilters(searcher.filterState, filtersClearAll)
        onErrorThenUpdateFiltersText(searcher, filtersTextView)
        onResponseChangedThenUpdateNbHits(searcher, nbHits)
        configureToolbar()

        searcher.search()
    }

    override fun onDestroy() {
        super.onDestroy()
        searcher.cancel()
    }
}



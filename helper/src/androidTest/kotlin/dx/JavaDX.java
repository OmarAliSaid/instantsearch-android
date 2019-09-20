package dx;

import com.algolia.instantsearch.core.connection.Connection;
import com.algolia.instantsearch.core.connection.ConnectionHandler;
import com.algolia.instantsearch.core.connection.ConnectionImpl;
import com.algolia.instantsearch.core.highlighting.HighlightTags;
import com.algolia.instantsearch.core.highlighting.HighlightToken;
import com.algolia.instantsearch.core.highlighting.HighlightTokenizer;
import com.algolia.instantsearch.core.highlighting.HighlightedString;
import com.algolia.instantsearch.core.hits.Hits;
import com.algolia.instantsearch.core.hits.HitsView;
import com.algolia.instantsearch.core.loading.LoadingViewModel;
import com.algolia.instantsearch.core.map.MapViewModel;
import com.algolia.instantsearch.core.number.NumberPresenterImpl;
import com.algolia.instantsearch.core.number.NumberViewModel;
import com.algolia.instantsearch.core.number.range.NumberRangeViewModel;
import com.algolia.instantsearch.core.number.range.Range;
import com.algolia.instantsearch.core.searchbox.SearchBoxViewModel;
import com.algolia.instantsearch.core.searcher.Debouncer;
import com.algolia.instantsearch.core.searcher.Searcher;
import com.algolia.instantsearch.core.searcher.SearcherConstants;
import com.algolia.instantsearch.core.searcher.Sequencer;
import com.algolia.instantsearch.core.selectable.SelectableItemViewModel;
import com.algolia.instantsearch.core.selectable.list.SelectableListViewModel;
import com.algolia.instantsearch.core.selectable.list.SelectionMode;
import com.algolia.instantsearch.core.selectable.map.SelectableMapViewModel;
import com.algolia.instantsearch.core.subscription.Subscription;
import com.algolia.instantsearch.core.subscription.SubscriptionValue;
import com.algolia.instantsearch.core.tree.Node;
import com.algolia.instantsearch.core.tree.Tree;
import com.algolia.instantsearch.core.tree.TreeViewModel;
import com.algolia.instantsearch.helper.attribute.AttributeMatchAndReplace;
import com.algolia.instantsearch.helper.attribute.AttributePresenterImpl;
import com.algolia.instantsearch.helper.filter.clear.ClearMode;
import com.algolia.instantsearch.helper.filter.clear.FilterClear;
import com.algolia.instantsearch.helper.filter.clear.FilterClearViewModel;
import com.algolia.instantsearch.helper.filter.current.FilterCurrent;
import com.algolia.instantsearch.helper.filter.current.FilterCurrentPresenterImpl;
import com.algolia.instantsearch.helper.filter.current.FilterCurrentViewModel;
import com.algolia.instantsearch.helper.filter.facet.FacetListPresenterImpl;
import com.algolia.instantsearch.helper.filter.facet.FacetListViewModel;
import com.algolia.instantsearch.helper.filter.list.FilterListViewModel;
import com.algolia.instantsearch.helper.filter.numeric.comparison.ComputeBounds;
import com.algolia.instantsearch.helper.filter.numeric.comparison.FilterComparison;
import com.algolia.instantsearch.helper.filter.range.FilterRange;
import com.algolia.instantsearch.helper.filter.state.FilterGroupID;
import com.algolia.instantsearch.helper.filter.state.FilterOperator;
import com.algolia.instantsearch.helper.filter.state.FilterState;
import com.algolia.instantsearch.helper.filter.state.Filters;
import com.algolia.instantsearch.helper.filter.toggle.FilterToggle;
import com.algolia.instantsearch.helper.hierarchical.Hierarchical;
import com.algolia.instantsearch.helper.hierarchical.HierarchicalFilter;
import com.algolia.instantsearch.helper.hierarchical.HierarchicalItem;
import com.algolia.instantsearch.helper.hierarchical.HierarchicalPresenterImpl;
import com.algolia.instantsearch.helper.hierarchical.HierarchicalViewModel;
import com.algolia.instantsearch.helper.highlighting.Highlightable;
import com.algolia.instantsearch.helper.loading.Loading;
import com.algolia.instantsearch.helper.searcher.SearcherForFacets;
import com.algolia.instantsearch.helper.searcher.SearcherMultipleIndex;
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex;
import com.algolia.search.client.ClientSearch;
import com.algolia.search.client.Index;
import com.algolia.search.model.Attribute;
import com.algolia.search.model.filter.Filter;
import com.algolia.search.model.filter.NumericOperator;
import com.algolia.search.model.multipleindex.IndexQuery;
import com.algolia.search.model.multipleindex.MultipleQueriesStrategy;
import com.algolia.search.model.response.ResponseSearch;
import com.algolia.search.model.response.ResponseSearchForFacets;
import com.algolia.search.model.response.ResponseSearches;
import com.algolia.search.model.search.Facet;
import com.algolia.search.model.search.FacetStats;
import com.algolia.search.model.search.Query;
import com.algolia.search.transport.RequestOptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import kotlin.Pair;
import kotlin.Unit;
import kotlin.ranges.LongRange;
import kotlinx.coroutines.Job;
import kotlinx.serialization.json.JsonObject;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//FIXME: Why is mockito not visible from IDE?


@SuppressWarnings("UnusedAssignment") // Variables are created just to assess DX
public class JavaDX {

    private static SearcherSingleIndex searcherSingleIndex;
    private static SearcherMultipleIndex searcherMultipleIndex;
    private static SearcherForFacets searcherForFacets;
    private static List<Searcher<?>> searchers;
    private static FilterState filterState;

    private static Attribute attribute = new Attribute("attribute");
    private static Query query = mock(Query.class);
    private static ClientSearch client = mock(ClientSearch.class);
    private static RequestOptions requestOptions = new RequestOptions();
    private static final List<FilterGroupID> groupIDs = Collections.singletonList(new FilterGroupID());
    private static Filter.Facet filterFacet = new Filter.Facet(attribute, "foo", 0, false);
    private static Facet facet = new Facet("red", 42, null);
    private List<Facet> facets = Collections.singletonList(facet);
    private static Map<Attribute, FacetStats> facetStats = new HashMap<>();

    //region Test Setup
    @BeforeClass
    public static void setUp() {
        filterState = new FilterState();
        facetStats.put(attribute, new FacetStats(0, 2, 1, 3));

        // region Prepare Searcher mocks
        final SubscriptionValue<Throwable> error = new SubscriptionValue<>(new Exception());
        final SubscriptionValue<ResponseSearch> responseSearch = new SubscriptionValue<>(new ResponseSearch());
        final SubscriptionValue<ResponseSearches> responseSearches = new SubscriptionValue<>(new ResponseSearches(Collections.singletonList(responseSearch.getValue())));
        final SubscriptionValue<ResponseSearchForFacets> responseSearchForFacet = new SubscriptionValue<>(new ResponseSearchForFacets(Collections.emptyList(), true, 0));

        searcherSingleIndex = mock(SearcherSingleIndex.class);
        searcherMultipleIndex = mock(SearcherMultipleIndex.class);
        searcherForFacets = mock(SearcherForFacets.class);
        searchers = Arrays.asList(searcherSingleIndex, searcherMultipleIndex, searcherForFacets);

        searchers.forEach(searcher -> {
            when(searcher.getError()).thenReturn(error);
            when(searcher.isLoading()).thenReturn(new SubscriptionValue<>(true));
            when(searcher.searchAsync()).thenReturn(mock(Job.class));
        });
        //FIXME: Why does IDE say `Cannot resolve method`?
        when(searcherSingleIndex.getResponse()).thenReturn(responseSearch);
        when(searcherMultipleIndex.getResponse()).thenReturn(responseSearches);
        when(searcherForFacets.getResponse()).thenReturn(responseSearchForFacet);
        //endregion
    }


    @AfterClass
    public static void tearDown() {
        System.out.println("Java DX Rocks! \uD83D\uDC4C");
    }
    //endregion

    //region Core
    @Test
    public final void connection() {
        ConnectionHandler handler = new ConnectionHandler();
        handler.add(new ConnectionImpl());
        handler.disconnect();
    }

    @Test
    public final void highlighting() {
        // Token
        HighlightToken token = new HighlightToken("foo", true);
        final String content = token.content;
        final boolean highlighted = token.highlighted;

        // Tokenizer
        HighlightTokenizer tokenizer = new HighlightTokenizer();
        String preTag = HighlightTags.DefaultPreTag;
        String postTag = HighlightTags.DefaultPostTag;
        tokenizer = new HighlightTokenizer(preTag);
        tokenizer = new HighlightTokenizer(preTag, postTag);
        final String preTagGot = tokenizer.preTag;
        final String postTagGot = tokenizer.postTag;

        // String
        final HighlightedString invoke = tokenizer.tokenize("foo<b>bar</b>");
        final List<HighlightToken> tokens = invoke.tokens;
        invoke.getHighlightedTokens();
        final String original = invoke.original;
    }

    @Test
    public void hits() {
        HitsView<ResponseSearch.Hit> hitsView = new HitsView<ResponseSearch.Hit>() {
            private List<? extends ResponseSearch.Hit> hits;

            @Override
            public void setHits(@NotNull List<? extends ResponseSearch.Hit> hits) {
                this.hits = hits;
            }
        };
        final Connection connection = Hits.<ResponseSearch, ResponseSearch.Hit>connectHitsView(searcherSingleIndex,
                hitsView,
                it -> it.getHits() // FIXME: Why doesn't IDE see the getHits method?
        );
        connection.connect();
        if (connection.isConnected()) connection.disconnect();
    }

    @Test
    public void loading() {
        // ViewModel
        LoadingViewModel viewModel = new LoadingViewModel();
        viewModel.eventReload.subscribe(it -> {
        });
        viewModel.isLoading.getValue();

        // TODO LoadingView - can't be done without a Java-friendly `Callback()` due to onReload
//        LoadingView view = new LoadingView() {}
//        Loading.connectView(viewModel, view)

        //FIXME: Why does IDE report "Wrong 2nd argument type"?
        Loading.connectSearcher(viewModel, searcherSingleIndex);
        Loading.connectSearcher(viewModel, searcherSingleIndex, new Debouncer(42));
    }

    @Test
    public void map() {
        // ViewModel
        final HashMap<String, String> initialMap = new HashMap<>();
        initialMap.put("id", "value");
        MapViewModel<String, String> viewModel = new MapViewModel<>(initialMap);

        viewModel.event.subscribe(viewModel.map::setValue);
        viewModel.remove("id");
        viewModel.map.getValue();
    }

    @Test
    public void number() {
        // TODO Computation - Refactor as SMI to allow use from Java
        Integer valueInt = 0;
//        Computation computation = it -> valueInt = it(valueInt);
//        computation.increment(1, 1);
//        computation.decrement(1);

        // ViewModel
        NumberViewModel<Integer> viewModel = new NumberViewModel<>();
        viewModel = new NumberViewModel<>(0);
        viewModel = new NumberViewModel<>(0, new Range<>(0, 10));
        viewModel.eventNumber.subscribe(viewModel.number::setValue);
        viewModel.coerce(-1);
        viewModel.number.getValue();
        viewModel.bounds.setValue(new Range<>(0, 10));
        ComputeBounds.setBoundsFromFacetStatsInt(viewModel, attribute, facetStats);

        // Presenter
        final NumberPresenterImpl presenter = NumberPresenterImpl.INSTANCE;
        presenter.present(10);

        // TODO NumberView - can't be done without a Java-friendly `Computation()` due to setComputation
//        NumberView<Integer> view = new NumberView<Integer>() {};
//        Number.connectView(viewModel, view);
//        Number.connectView(viewModel, view, presenter);
    }

    @Test
    public void number_range() {
        // Range
        Range<Long> bounds = new Range<>(0L, 100L);
        // FIXME: Why does this display as error but compiles fine?
        Range<Long> range = Range.Companion.invoke(new LongRange(10L, 20L));

        // ViewModel
        NumberRangeViewModel<Long> viewModel = new NumberRangeViewModel<>(range);
        viewModel = new NumberRangeViewModel<>(range, bounds);
        viewModel.eventRange.subscribe(viewModel.range::setValue);
        viewModel.coerce(bounds);
        viewModel.range.getValue();

        FilterRange.connectFilterState(viewModel, filterState, attribute);
        FilterRange.connectFilterState(viewModel, filterState, attribute, groupIDs.get(0));

        // TODO View - can't be done without a Java-friendly `Callback()` due to onRangeChanged
//        NumberRangeView view = new NumberRangeView() {}
//        NumberRange.connectView(viewModel, view);
    }

    @Test
    public void searchbox() {
        SearchBoxViewModel viewModel = new SearchBoxViewModel();
        viewModel.query.subscribe(System.out::println);
        viewModel.eventSubmit.send("foo");

        // TODO View - can't be done without a Java-friendly `Callback()` due to onQueryXX
//        SearchBoxView view = new SearchBoxView() {}
//        SearchBox.connectView(viewModel, view);
    }

    @Test
    public void searcher() {
        // Constants
        long constA = SearcherConstants.debounceLoadingInMillis;
        long constB = SearcherConstants.debounceSearchInMillis;
        long constC = SearcherConstants.debounceFilteringInMillis;

        // TODO Debouncer - currently blocked by creation of CoroutineScope, then needs a refactoring around Function1/Function2

        // Searcher
        searchers.forEach(searcher -> {
            searcher.getError().subscribe(System.out::println);
            searcher.getResponse().getValue();
            searcher.isLoading().subscribe(it -> System.out.println(it ? "Loading..." : "Done loading"));
            searcher.setQuery("foo");
            searcher.searchAsync();
            searcher.cancel();
        });

        // SearcherSingleIndex
        //FIXME: Why does IDE report `Incompatible types`?
        Index index = searcherSingleIndex.index;
        Query query = searcherSingleIndex.query;
        RequestOptions requestOptions = searcherSingleIndex.requestOptions;
        final Boolean isDisjunctiveFacetingEnabled = searcherSingleIndex.isDisjunctiveFacetingEnabled;
        final ResponseSearch responseSearch = searcherSingleIndex.getResponse().getValue();

        //FIXME: Why does IDE report `Incompatible types`?
        final List<IndexQuery> queries = searcherMultipleIndex.queries;
        final ClientSearch client = searcherMultipleIndex.client;
        requestOptions = searcherMultipleIndex.requestOptions;
        final MultipleQueriesStrategy strategy = searcherMultipleIndex.strategy;
        final ResponseSearches responseSearches = searcherMultipleIndex.getResponse().getValue();

        // SearcherForFacets
        index = searcherForFacets.index;
        Attribute attribute = searcherForFacets.attribute;
        query = searcherForFacets.query;
        final String facetQuery = searcherForFacets.facetQuery;
        requestOptions = searcherForFacets.requestOptions;
        final ResponseSearchForFacets responseSearchForFacets = searcherForFacets.getResponse().getValue();

        // Sequencer
        Sequencer sequencer = new Sequencer();
        final int maxOperations = sequencer.maxOperations;
        searchers.forEach(s -> sequencer.addOperation(s.searchAsync()));
        //FIXME: Why does IDE report no `cancel` method?
        sequencer.getCurrentOperation().cancel(new CancellationException());
        sequencer.cancelAll();
    }

    @Test
    public void selectable() {
        SelectableItemViewModel<String> viewModel = new SelectableItemViewModel<>("foo");
        viewModel.eventSelection.subscribe(it -> System.out.println(it ? "Selected" : "No more"));
        final Boolean value = viewModel.isSelected.getValue();
        viewModel.item.setValue("bar");

        //TODO View - can't be done without a Java-friendly `Callback()` due to onSelectionChanged
        //SelectableItemView view = new SelectableItemView() {}
        //SelectableItem.connectView(viewModel, view);
    }

    @Test
    public void selectable_list() {
        Pair<String, Boolean> item = new Pair<>("foo", false);
        final String first = item.getFirst();
        final Boolean second = item.getSecond();

        SelectableListViewModel<String, String> viewModel = new SelectableListViewModel<>(SelectionMode.Multiple);
        final List<String> value = viewModel.items.getValue();
        final String name = viewModel.selectionMode.name();
        viewModel.selections.subscribe(it -> System.out.println("New selections:" + it));
        viewModel.eventSelection.subscribe(it -> System.out.println("Selected " + it));

        //TODO View - can't be done without a Java-friendly `Callback()` due to onSelection
//        SelectableListView view = new SelectableListView<String> {}
//        SelectableList.connectView(viewModel, view)
    }

    @Test
    public void selectable_map() {
        SelectableMapViewModel<String, String> viewModel = new SelectableMapViewModel<>();
        viewModel.event.subscribe(System.out::println);
        viewModel.eventSelection.subscribe(it -> System.out.println("New selection: " + it));
        viewModel.selected.setValue("foo");
        final Map<String, String> value = viewModel.map.getValue();

//        SelectableMapView<String, String> view = new SelectableMapView<String, String>() {}
//        SelectableItem.connectView(viewModel, view)
    }


    @Test
    public void subscription() {
        Subscription<Boolean> subscription = new Subscription<>();
        final Subscription.Callback<Boolean> callback = it -> fail("Why not?");
        subscription.subscribe(callback);
        subscription.unsubscribe(callback);
        subscription.unsubscribeAll();
    }

    @Test
    public void tree() {
        Node<String> node = new Node<>("Foo");
        Tree<String> tree = new Tree<>(Collections.singletonList(node));
        final List<Node<String>> children = tree.children;

        TreeViewModel<String, String> viewModel = new TreeViewModel<String, String>() {
            @Override
            public void computeSelections(String key) {
            }
        };

        //TODO View - can't be done without a Java-friendly `Callback()` due to onSelection
//        TreeView view = new TreeView() {};
//        TreeUtils.connectView(viewModel, view);
    }

    @Test
    public void presenter() {
        //TODO: Assess/Improve DX for Presenter, TreePresenter
    }
    //endregion

    //region Helper.commonMain
    @Test
    public void attribute() {
        AttributeMatchAndReplace matchAndReplace = new AttributeMatchAndReplace(new Attribute("foo"), "bar");
        //FIXME: Why does IDE report `cannot be applied to`?
        final Attribute toReplace = new Attribute("the fool!");
        matchAndReplace.replace(toReplace);

        AttributePresenterImpl presenter = new AttributePresenterImpl();
        //FIXME: Why does IDE report `cannot be applied to`?
        presenter.present(toReplace);
    }

    @Test
    public void filter() {
        //FIXME: FilterPresenter cannot be used as-is from Java
    }

    @Test
    public void filter_clear() {
        FilterClearViewModel viewModel = new FilterClearViewModel();
        viewModel.eventClear.send(Unit.INSTANCE);

        FilterClear.connectFilterState(viewModel, filterState);
        FilterClear.connectFilterState(viewModel, filterState, groupIDs);
        FilterClear.connectFilterState(viewModel, filterState, groupIDs, ClearMode.Except);

        // TODO View - can't be done without a Java-friendly `Callback()` due to onClear
//        FilterClearView view = new FilterClearView();
//        FilterClear.connectView(viewModel, view);
    }

    @Test
    public void filter_current() {
        // ViewModel
        FilterCurrentViewModel viewModel = new FilterCurrentViewModel();
        FilterCurrent.connectFilterState(viewModel, filterState);
        FilterCurrent.connectFilterState(viewModel, filterState, groupIDs);

        // TODO View - can't be done without a Java-friendly `Callback()` due to onClear
//        FilterCurrentView view = new FilterCurrentViewImpl();
//        FilterCurrent.connectView(viewModel, view);

        // Presenter
        FilterCurrentPresenterImpl presenter = new FilterCurrentPresenterImpl();
        final HashMap<Pair<FilterGroupID, Filter>, Filter> filterMap = new HashMap<>();
        filterMap.put(new Pair<>(new FilterGroupID(), filterFacet), filterFacet);
        //FIXME: Why does IDE say `cannot be applied to`?
        presenter.present(filterMap);
    }

    @Test
    public void filter_facet() {
        List<Pair<Facet, Boolean>> facetPairs = Arrays.asList(
                new Pair<>(new Facet("a", 1, null), false),
                new Pair<>(new Facet("b", 2, null), false));

        // ViewModel
        FacetListViewModel viewModel = new FacetListViewModel();
        viewModel = new FacetListViewModel(facets);
        viewModel = new FacetListViewModel(facets, SelectionMode.Multiple);
        viewModel = new FacetListViewModel(facets, SelectionMode.Multiple, true);
        viewModel.facets.setValue(facetPairs);
        final boolean persistentSelection = viewModel.persistentSelection;
        final SelectionMode selectionMode = viewModel.selectionMode;

        //FIXME: Why does IDE say `wrong argument type`?
//        FacetList.connectFilterState(viewModel, filterState, attribute);
//        FacetList.connectFilterState(viewModel, filterState, attribute, groupIDs.get(0));
//
//        FacetList.connectFilterStateWithOperator(viewModel, filterState, attribute);
//        FacetList.connectFilterStateWithOperator(viewModel, filterState, attribute, FilterOperator.Or);

        // TODO View - can't be done without a Java-friendly `Callback()` due to onSelection
//        FacetListView view = new FacetListView() {};
//        FacetList.connectView(viewModel, view);

        // Presenter
        //FIXME: Why does IDE say `cannot be applied to`?
        new FacetListPresenterImpl().present(facetPairs);
    }

    @Test
    public void filter_list() {
        // ViewModel
        FilterListViewModel.Facet viewModelFacet = new FilterListViewModel.Facet();
        List<? extends Filter> filters = viewModelFacet.items.getValue();
        FilterListViewModel.All viewModelAll = new FilterListViewModel.All();
        filters = viewModelAll.items.getValue();

        // TODO View - can't be done without a Java-friendly `Callback()` due to onSelection
//        FilterListView.All view = new FilterListView.All() {}
//        SelectableList.<com.algolia.search.model.filter.Filter>connectView(viewModelAll, null);

    }


    @Test
    public void filter_map() {
        // TODO ViewModel - refactor as subclass
//        FilterMapViewModel viewModel = new FilterMapViewModel();

        // View
    }


    @Test
    public void filter_comparison() {
        // ViewModel
        NumberViewModel<Integer> viewModel = new NumberViewModel<>();
        FilterComparison.connectFilterState(viewModel, filterState, attribute, NumericOperator.Equals);
        FilterComparison.connectFilterState(viewModel, filterState, attribute, NumericOperator.Equals, groupIDs.get(0));
    }


    @Test
    public void filter_state() {
        FilterGroupID groupID = new FilterGroupID(FilterOperator.Or);
        groupID = new FilterGroupID("groupID");
        groupID = new FilterGroupID(attribute, FilterOperator.And);
        final Filters value = filterState.filters.getValue();
    }

    @Test
    public void filter_toggle() {
        // ViewModel
        SelectableItemViewModel<Filter> viewModel = new SelectableItemViewModel<>(filterFacet);
        FilterToggle.connectFilterState(viewModel, filterState);
        FilterToggle.connectFilterState(viewModel, filterState, groupIDs.get(0));
    }


    @Test
    public void hierarchical() {
        final List<Attribute> hierarchicalAttributes = Collections.singletonList(attribute);

        // Item
        HierarchicalItem item = new HierarchicalItem(facet, "item", 0);
        final String displayName = item.displayName;
        final Facet facet = item.facet;
        final int level = item.level;

        // Node
        Node<Facet> hierarchicalNode = new Node<>(facet);
        final Facet content = hierarchicalNode.content;
        final List<Node<Facet>> children = hierarchicalNode.children;

        // Filter
        HierarchicalFilter hierarchicalFilter = new HierarchicalFilter(hierarchicalAttributes, Collections.singletonList(filterFacet), filterFacet);
        final List<Attribute> attributes = (List<Attribute>) hierarchicalFilter.attributes;
        final Filter.Facet filter = (Filter.Facet) hierarchicalFilter.filter;
        final List<Filter.Facet> path = (List<Filter.Facet>) hierarchicalFilter.path;

        // Tree
                Tree<Facet> tree = new Tree<>(Collections.singletonList(new Node<>(facet)));

        // ViewModel
        HierarchicalViewModel viewModelMin = new HierarchicalViewModel(attribute,
                hierarchicalAttributes, " > ");
        final HierarchicalViewModel viewModel = new HierarchicalViewModel(attribute, hierarchicalAttributes, " > ", tree);
        final List<String> selections = viewModel.selections.getValue();
        final List<Attribute> hierarchicalAttributes1 = (List<Attribute>) viewModel.hierarchicalAttributes;
        final Attribute attribute = (Attribute) viewModel.attribute;
        viewModel.eventHierarchicalPath.subscribe(it -> {
            // Put in a never running handler to avoid calling addFacet on mocked Searcher
            Hierarchical.connectFilterState(viewModel, filterState);
            Hierarchical.connectSearcher(viewModel, searcherSingleIndex);
        });

        //TODO View - can't be done without a Java-friendly `Callback()` due to onSelection

        HierarchicalPresenterImpl presenter = new HierarchicalPresenterImpl(" > ");
        presenter.present(tree);
    }


    @Test
    public void highlightable() {
        //TODO: Use @JvmDefault, then confirm DX from Java
    }


    //endregion

    //region Helper.androidMain
    //endregion

    @Test
    public void x() {
        // ViewModel

        // View

        // Presenter
    }

}
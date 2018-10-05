package com.example.ajiekc.tochka.ui.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import com.example.ajiekc.tochka.Injection
import com.example.ajiekc.tochka.LceState
import com.example.ajiekc.tochka.R
import com.example.ajiekc.tochka.db.User
import com.example.ajiekc.tochka.extensions.toast
import com.example.ajiekc.tochka.widget.LceRecyclerView
import com.example.ajiekc.tochka.widget.RxSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SearchUsersFragment : Fragment(), UsersAdapter.RepeatButtonClickListener {
    
    companion object {
        const val SEARCH_VIEW_TEXT_PREFS = "search_view_text_prefs"
        val TAG = SearchUsersFragment::class.java.simpleName
    }

    private var mNextPageLoading = false
    private lateinit var mAdapter: UsersAdapter
    private lateinit var mRecyclerView: LceRecyclerView
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mViewModel: SearchViewModel
    private var mSearchViewText: String = ""

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main, menu)
        val searchMenuItem = menu?.findItem(R.id.action_search)
        if (searchMenuItem != null) {
            setupSearchView(searchMenuItem)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupSearchView(menuItem: MenuItem) {
        Log.i(TAG,"setupSearchView")
        val searchView = menuItem.actionView as SearchView
        if (mSearchViewText.isNotEmpty()) {
            searchView.setQuery(mSearchViewText, false)
        }
        searchView.queryHint = getString(R.string.search_view_hint)
        RxSearchView.queryTextChanged(searchView)
                .debounce(600, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {filter ->
                    Log.d(TAG,"onNext: $filter")
                    mSearchViewText = filter
                    mViewModel.getDataWithFilter(filter)
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_serach_users, container, false)
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        setUpRefreshLayout()
        val linearLayoutManager = LinearLayoutManager(context)
        mRecyclerView = view.findViewById<LceRecyclerView>(R.id.users_recycler_view).apply {
            layoutManager = linearLayoutManager
            emptyView = view.findViewById(R.id.empty_text_view)
            progressView = view.findViewById(R.id.progress_view)
        }
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = linearLayoutManager.childCount
                val totalItemCount = linearLayoutManager.itemCount
                val pastVisibleItems =
                        linearLayoutManager.findFirstVisibleItemPosition()
                if (visibleItemCount + pastVisibleItems >= totalItemCount && isRecyclerScrollable(recyclerView) && !mNextPageLoading) {
                    val userId = mAdapter.getLastItem()?.id
                    if (userId != null && userId > 0) {
                        mViewModel.loadData(since = userId)
                    }
                }
            }
        })
        val viewModelFactory = Injection.provideSearchViewModelFactory(context!!)
        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)
        mAdapter = UsersAdapter(mViewModel.dataSet)
        mAdapter.attachToRecyclerView(mRecyclerView)
        mAdapter.setOnRepeatButtonClickListener(this)
        mViewModel.viewState().observe(this, Observer {
            renderViewState(it)
        })
        mViewModel.loadData()

        return view
    }

    fun isRecyclerScrollable(recyclerView: RecyclerView): Boolean {
        return recyclerView.computeHorizontalScrollRange() > recyclerView.width
                || recyclerView.computeVerticalScrollRange() > recyclerView.height
    }

    private fun setUpRefreshLayout() {
        val color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
        mSwipeRefreshLayout.setColorSchemeColors(color)
        mSwipeRefreshLayout.setOnRefreshListener {
            mViewModel.loadData(true)
        }
    }

    private fun renderViewState(searchState: SearchViewState?) {
        when (searchState?.state) {
            LceSearchState.INITIAL_LOADING -> {
                mSwipeRefreshLayout.isEnabled = false
                mRecyclerView.showProgress()
                Log.d(TAG, "INITIAL_LOADING")
            }
            LceSearchState.LOADING_NEXT_PAGE -> {
                mSwipeRefreshLayout.isEnabled = true
                mNextPageLoading = true
                if (mAdapter.isLastItemUser()) {
                    mAdapter.add(User(type = LceState.LOADING.name))
                } else {
                    mAdapter.replaceLastItem(User(type = LceState.LOADING.name))
                }
                Log.d(TAG, "LOADING_NEXT_PAGE")
            }
            LceSearchState.NEXT_PAGE_LOADED -> {
                mSwipeRefreshLayout.isEnabled = true
                mNextPageLoading = false
                mAdapter.removeLastItem()
                Log.d(TAG, "NEXT_PAGE_LOADED")
            }
            LceSearchState.LOADING -> {
                mSwipeRefreshLayout.isEnabled = true
                mSwipeRefreshLayout.isRefreshing = true
                Log.d(TAG, "LOADING")
            }
            LceSearchState.HIDE_LOADING -> {
                mSwipeRefreshLayout.isEnabled = true
                mSwipeRefreshLayout.isRefreshing = false
                mRecyclerView.hideProgress()
                Log.d(TAG, "HIDE_LOADING")
            }
            LceSearchState.CONTENT -> {
                mSwipeRefreshLayout.isEnabled = true
                mSwipeRefreshLayout.isRefreshing = false
                mRecyclerView.hideProgress()
                if(searchState.reload) {
                    mNextPageLoading = false
                }
                onDataReceive(searchState.data)
                Log.d(TAG, "CONTENT")
            }
            LceSearchState.ERROR_NEXT_PAGE_LOADING -> {
                mSwipeRefreshLayout.isEnabled = true
                mAdapter.replaceLastItem(User(type = LceState.ERROR.name))
                Log.d(TAG, "ERROR_NEXT_PAGE_LOADING")
            }
            LceSearchState.ERROR -> {
                mSwipeRefreshLayout.isEnabled = true
                mRecyclerView.updateView()
                context?.toast(getString(R.string.loading_failed))
                Log.d(TAG, "ERROR")
            }
        }
    }

    private fun onDataReceive(data: List<User>?) {
        if (data == null) {
            return
        }
        mAdapter.changeDataSet(data)
    }

    override fun onRepeatButtonClick() {
        val count = mAdapter.itemCount
        if (count > 1) {
            val userId = mAdapter.getItem(count - 2).id
            if (userId != null && userId > 0) {
                mViewModel.loadData(since = userId)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_VIEW_TEXT_PREFS, mSearchViewText)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        mSearchViewText = savedInstanceState?.getString(SEARCH_VIEW_TEXT_PREFS, "") ?: ""
    }
}
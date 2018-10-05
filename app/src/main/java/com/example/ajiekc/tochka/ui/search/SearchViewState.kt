package com.example.ajiekc.tochka.ui.search

import com.example.ajiekc.tochka.db.User

data class SearchViewState constructor(val state: LceSearchState,
                                          val data: List<User>? = null,
                                          val error: Throwable? = null,
                                          val reload: Boolean = false) {
    companion object {
        fun initialLoading() = SearchViewState(LceSearchState.INITIAL_LOADING)
        fun loadingNextPage() = SearchViewState(LceSearchState.LOADING_NEXT_PAGE)
        fun loading() = SearchViewState(LceSearchState.LOADING)
        fun nextPageLoaded() = SearchViewState(LceSearchState.NEXT_PAGE_LOADED)
        fun hideLoading() = SearchViewState(LceSearchState.HIDE_LOADING)
        fun content(data: List<User>?, reload: Boolean = false) = SearchViewState(LceSearchState.CONTENT, data, reload = reload)
        fun error(error: Throwable?) = SearchViewState(LceSearchState.ERROR, error = error)
        fun errorNextPageLoading() = SearchViewState(LceSearchState.ERROR_NEXT_PAGE_LOADING)
    }
}
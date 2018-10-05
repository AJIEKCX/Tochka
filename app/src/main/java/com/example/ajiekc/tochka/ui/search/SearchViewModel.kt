package com.example.ajiekc.tochka.ui.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.example.ajiekc.tochka.db.User
import com.example.ajiekc.tochka.repository.IGithubRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class SearchViewModel(private val repository: IGithubRepository) : ViewModel() {

    private val disposables: CompositeDisposable = CompositeDisposable()
    private val viewState: MutableLiveData<SearchViewState> = MutableLiveData()
    var dataSet = mutableListOf<User>()

    fun viewState(): MutableLiveData<SearchViewState> {
        return viewState
    }

    fun loadData(reload: Boolean = false, since: Int = 0) {
        if (!reload && since == 0 && !dataSet.isEmpty()) {
            return
        }
        load(reload, since)
    }

    fun getDataWithFilter(filter: String?) {
        val disposable = repository.getUsersWithFilter(filter)
                .doOnSubscribe { viewState.postValue(SearchViewState.loading()) }
                .doOnSuccess { viewState.postValue(SearchViewState.hideLoading()) }
                .doOnError { viewState.postValue(SearchViewState.hideLoading()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { list ->
                        dataSet.clear()
                        dataSet.addAll(list)
                        viewState.value = SearchViewState.content(dataSet)
                    },
                    onError = { t: Throwable? ->
                        viewState.value = SearchViewState.error(t)
                        Log.e("SearchViewModel", "ERROR: ${t?.message}")
                    })
        disposables.add(disposable)
    }

    private fun load(reload: Boolean = false, since: Int = 0) {
        disposables.add(repository.getUsers(since)
                .doOnSubscribe {
                    viewState.postValue(when {
                        since != 0 -> SearchViewState.loadingNextPage()
                        !reload -> SearchViewState.initialLoading()
                        else -> SearchViewState.loading()
                    })
                }
                .doOnSuccess {
                    if (since != 0) {
                        viewState.postValue(SearchViewState.nextPageLoaded())
                    } else {
                        viewState.postValue(SearchViewState.hideLoading())
                    }
                }
                .doOnError { viewState.postValue(SearchViewState.hideLoading()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { list ->
                        if (reload) {
                            dataSet.clear()
                        }
                        dataSet.addAll(list)
                        viewState.value = SearchViewState.content(dataSet, reload)
                    },
                    onError = { t: Throwable? ->
                        if (since != 0) {
                            viewState.postValue(SearchViewState.errorNextPageLoading())
                        } else {
                            viewState.value = SearchViewState.error(t)
                        }
                        Log.e("SearchViewModel", "ERROR: ${t?.message}")
                    }))
    }

    override fun onCleared() {
        disposables.clear()
    }
}
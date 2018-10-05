package com.example.ajiekc.tochka

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.ajiekc.tochka.repository.IGithubRepository
import com.example.ajiekc.tochka.ui.search.SearchViewModel
import com.example.ajiekc.tochka.ui.search.SearchViewState
import com.google.common.truth.Truth
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.reset
import org.mockito.junit.MockitoJUnit

class MainViewModelTest {

    @get:Rule
    val mockitoRule = MockitoJUnit.rule()

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rxSchedulerRule = RxSchedulerRule()

    private lateinit var viewModel: SearchViewModel
    private var repository = mock<IGithubRepository>()

    @Before
    fun setUp() {
        reset(repository)
        viewModel = SearchViewModel(repository)
    }

    @Test
    fun `init test`() {
        val viewState = viewModel.viewState().testObserver()
        Truth.assert_()
                .that(viewState.observedValues)
                .isEmpty()
    }

    @Test
    fun `initial load data`() {
        val data = singleUserList()
        whenever(repository.getUsers()).thenReturn(Single.just(data))

        val viewState = viewModel.viewState().testObserver()
        viewModel.loadData()

        val load = SearchViewState.initialLoading()
        val hide = SearchViewState.hideLoading()
        val content = SearchViewState.content(data, false)
        Truth.assert_()
                .that(viewState.observedValues)
                .isEqualTo(listOf(load, hide, content))
    }

    @Test
    fun `initial loading when data is empty`() {
        val data = emptyUserList()
        whenever(repository.getUsers()).thenReturn(Single.just(data))

        val viewState = viewModel.viewState().testObserver()
        viewModel.loadData()

        val load = SearchViewState.initialLoading()
        val hide = SearchViewState.hideLoading()
        val content = SearchViewState.content(data, false)
        Truth.assert_()
                .that(viewState.observedValues)
                .isEqualTo(listOf(load, hide, content))
    }

    @Test
    fun `reload data`() {
        val data = singleUserList()
        whenever(repository.getUsers()).thenReturn(Single.just(data))

        val viewState = viewModel.viewState().testObserver()
        viewModel.loadData(reload = true)

        val load = SearchViewState.loading()
        val hide = SearchViewState.hideLoading()
        val content = SearchViewState.content(data, true)
        Truth.assert_()
                .that(viewState.observedValues)
                .isEqualTo(listOf(load, hide, content))
    }

    @Test
    fun `next page loading`() {
        val data = singleUserList()
        whenever(repository.getUsers(since = 30)).thenReturn(Single.just(data))

        val viewState = viewModel.viewState().testObserver()
        viewModel.loadData(since = 30)

        val load = SearchViewState.loadingNextPage()
        val hide = SearchViewState.nextPageLoaded()
        val content = SearchViewState.content(data, false)
        Truth.assert_()
                .that(viewState.observedValues)
                .isEqualTo(listOf(load, hide, content))
    }

    @Test
    fun `get data with filter`() {
        val data = singleUserList()
        whenever(repository.getUsersWithFilter(ArgumentMatchers.anyString())).thenReturn(Single.just(data))

        val viewState = viewModel.viewState().testObserver()
        viewModel.getDataWithFilter("Alex")

        val load = SearchViewState.loading()
        val hide = SearchViewState.hideLoading()
        val content = SearchViewState.content(data, false)
        Truth.assert_()
                .that(viewState.observedValues)
                .isEqualTo(listOf(load, hide, content))
    }

    @Test
    fun `next page loading error`() {
        whenever(repository.getUsers(since = 30)).thenReturn(Single.error(Exception()))

        val viewState = viewModel.viewState().testObserver()
        viewModel.loadData(since = 30)

        val load = SearchViewState.loadingNextPage()
        val hide = SearchViewState.hideLoading()
        val error = SearchViewState.errorNextPageLoading()
        Truth.assert_()
                .that(viewState.observedValues.map { it?.state })
                .isEqualTo(listOf(load.state, hide.state, error.state))
    }

    @Test
    fun `initial loading data error`() {
        whenever(repository.getUsers()).thenReturn(Single.error(Exception()))
        val viewState = viewModel.viewState().testObserver()
        viewModel.loadData()

        val load = SearchViewState.initialLoading()
        val hide = SearchViewState.hideLoading()
        val error = SearchViewState.error(Exception())
        Truth.assert_()
                .that(viewState.observedValues.map { it?.state })
                .isEqualTo(listOf(load.state, hide.state, error.state))
    }

    @Test
    fun `reloading data error`() {
        whenever(repository.getUsersWithFilter(ArgumentMatchers.anyString())).thenReturn(Single.error(Exception()))
        val viewState = viewModel.viewState().testObserver()
        viewModel.getDataWithFilter("Alex")

        val load = SearchViewState.loading()
        val hide = SearchViewState.hideLoading()
        val error = SearchViewState.error(Exception())
        Truth.assert_()
                .that(viewState.observedValues.map { it?.state })
                .isEqualTo(listOf(load.state, hide.state, error.state))
    }

    @Test
    fun `get data with filter error`() {
        whenever(repository.getUsers()).thenReturn(Single.error(Exception()))
        val viewState = viewModel.viewState().testObserver()
        viewModel.loadData(reload = true)

        val load = SearchViewState.loading()
        val hide = SearchViewState.hideLoading()
        val error = SearchViewState.error(Exception())
        Truth.assert_()
                .that(viewState.observedValues.map { it?.state })
                .isEqualTo(listOf(load.state, hide.state, error.state))
    }

}

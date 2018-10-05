package com.example.ajiekc.tochka.ui.auth

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.example.ajiekc.tochka.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val disposables: CompositeDisposable = CompositeDisposable()

    val viewState: MutableLiveData<AuthViewState> = MutableLiveData()

    fun getUserData(authType: AuthType,
                    accessToken: String? = null,
                    userId: String? = null,
                    account: GoogleSignInAccount? = null) {
        disposables.add(repository.getUserData(authType, accessToken, userId, account)
                .doOnSubscribe { viewState.postValue(AuthViewState.loading()) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { data ->
                        viewState.value = AuthViewState.content(data)
                        Log.i(AuthActivity::class.java.simpleName, "Success: ${data.firstName}")
                    },
                    onError = { error ->
                        viewState.value = AuthViewState.error(error)
                        Log.e(AuthActivity::class.java.simpleName, "Error: ${error.message}")
                    }))
    }

    override fun onCleared() {
        disposables.clear()
    }

}
package com.example.ajiekc.tochka.widget

import android.support.v7.widget.SearchView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class RxSearchView {
    companion object {
        fun queryTextChanged(view: SearchView): Flowable<String> {
            val emitter: PublishSubject<String> = PublishSubject.create()

            view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(text: String?): Boolean {
                    emitter.onComplete()
                    return true
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    if (text != null) {
                        emitter.onNext(text.trim())
                    }
                    return true
                }

            })

            return emitter.toFlowable(BackpressureStrategy.LATEST)
        }
    }
}
package com.telefonica.androidlogger.ui.livedata

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map

internal class FilterableLiveData<T>(
    source: LiveData<T>,
    filterTrigger: LiveData<Unit>,
    private val filterFunc: (T) -> T
) : MediatorLiveData<T>() {

    private var unfilteredData: T? = null

    val filter = {
        unfilteredData?.let {
            value = filterFunc.invoke(it)
        }
    }

    init {
        addSource(source) {
            unfilteredData = source.value
            filter()
        }
        addSource(filterTrigger) {
            filter()
        }
    }
}

internal fun <T> LiveData<T>.filter(
    filterTrigger: LiveData<Unit>,
    filterFunc: (T) -> T
): LiveData<T> =
    FilterableLiveData(this, filterTrigger, filterFunc)

internal fun <T> LiveData<T>.throttle(duration: Long = 1000L): LiveData<T> = MediatorLiveData<T>().also { mediator ->

    var callbackSet = false
    var lastEmitTime = 0L

    val handler = Handler(Looper.getMainLooper())

    val emit = Runnable {
        mediator.value = this.value
        callbackSet = false
        lastEmitTime = System.currentTimeMillis()
    }

    mediator.addSource(this) {
        if (!callbackSet) {
            if (lastEmitTime + duration < System.currentTimeMillis()) {
                emit.run()
            } else {
                callbackSet = true
                handler.postDelayed(emit, duration)
            }
        }
    }
}

internal fun <From, To> LiveData<From>.map(mapFunc: (From) -> To): LiveData<To> = map(mapFunc)
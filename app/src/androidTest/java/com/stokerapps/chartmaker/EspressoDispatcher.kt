package com.stokerapps.chartmaker

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * Wraps a [CoroutineDispatcher], so Espresso knows when the app is idle or not.
 */
class EspressoDispatcher(
    private val wrappedCoroutineDispatcher: CoroutineDispatcher
) : CoroutineDispatcher() {

    private val counter: CountingIdlingResource =
        CountingIdlingResource("EspressoTrackedDispatcher for $wrappedCoroutineDispatcher")

    init {
        IdlingRegistry.getInstance().register(counter)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        counter.increment()
        val blockWithDecrement = Runnable {
            try {
                block.run()
            } finally {
                counter.decrement()
            }
        }
        wrappedCoroutineDispatcher.dispatch(context, blockWithDecrement)
    }

    fun cleanUp() {
        IdlingRegistry.getInstance().unregister(counter)
    }
}
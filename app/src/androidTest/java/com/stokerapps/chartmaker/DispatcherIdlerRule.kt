package com.stokerapps.chartmaker

import com.stokerapps.chartmaker.common.AppDispatchers
import kotlinx.coroutines.Dispatchers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Wraps [AppDispatchers] with [EspressoDispatcher], so Espresso knows when the app is idle.
 */
class DispatcherIdlerRule : TestRule {

    override fun apply(base: Statement?, description: Description?): Statement =
        object : Statement() {
            override fun evaluate() {

                val espressoDispatcherIO = EspressoDispatcher(Dispatchers.IO)
                val espressoDispatcherDefault = EspressoDispatcher(Dispatchers.Default)

                AppDispatchers.IO = espressoDispatcherIO
                AppDispatchers.Default = espressoDispatcherDefault

                try {
                    base?.evaluate()

                } finally {
                    espressoDispatcherIO.cleanUp()
                    espressoDispatcherDefault.cleanUp()
                    AppDispatchers.resetAll()
                }
            }
        }
}
package com.stokerapps.chartmaker

import androidx.paging.PagedList
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

@Suppress("unchecked_cast", "unused")
fun <T> mockPagedList(list: List<T> = emptyList()): PagedList<T> {
    val pagedList = Mockito.mock(PagedList::class.java)
    Mockito.`when`(pagedList[ArgumentMatchers.anyInt()]).then { invocation ->
        val index = invocation.arguments.first() as Int
        list[index]
    }
    Mockito.`when`(pagedList.size).thenReturn(list.size)
    return pagedList as PagedList<T>
}

fun waitUntil(
    intervalMillis: Long = 100,
    timeoutMillis: Long = 3000,
    condition: () -> Boolean
) {
    runBlocking {
        withTimeout(timeoutMillis) {
            while (condition().not()) {
                delay(intervalMillis)
            }
        }
    }
}
/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.event_handlers

interface EventHandler<Event> {
    fun handle(event: Event)
}
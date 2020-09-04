/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.domain

enum class Delimiter(val symbol: Char) {
    COMMA(','),
    SEMICOLON(';'),
    COLON(':'),
    SPACE(' '),
    TAB('\t')
}
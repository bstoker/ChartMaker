/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.data.files

import androidx.annotation.VisibleForTesting
import java.io.BufferedReader
import java.io.InputStream
import java.nio.charset.Charset

class CsvReader {

    fun readAll(
        inputStream: InputStream,
        mimeType: String? = null,
        charset: Charset = Charsets.UTF_8
    ): List<Any> {

        val input: BufferedReader = when (inputStream) {
            is BufferedReader -> inputStream
            else -> inputStream.bufferedReader(charset)
        }

        val delimiters = when(mimeType) {
            "text/csv", "csv" -> ","
            "text/tsv", "tsv" -> "\t"
            else -> Parser.DELIMITERS
        }

        val parser = Parser(delimiters)
        var line = input.readLine()
        while (line != null && !parser.isFinished) {

            line.toCharArray().forEach { c ->
                parser.handle(c)
            }

            line = input.readLine()
            if (line != null) {
                parser.handle('\n')
            } else {
                parser.handleEndOfFile()
            }
        }
        return parser.getData()
    }

    @VisibleForTesting
    class Parser(private val delimiters: String = DELIMITERS) {

        companion object {
            const val DELIMITERS = ",;: \t"
        }

        private val character = Character()
        private val delimiter = Delimiter()
        private val end = End()
        private val escapedCharacter = EscapedCharacter()
        private val escapedQuotedCharacter = EscapedQuotedCharacter()
        private val linefeed = LineFeed()
        private val quotedCharacter = QuotedCharacter()
        private val start = Start()
        private val term = Term()

        private var state: State = start
        private var buffer = mutableListOf<Char>()
        private var data = mutableListOf<String>()
        private val dataList = mutableListOf<List<String>>()
        val isFinished: Boolean get() = state is End

        fun getData(): List<Any> {
            return if (dataList.isEmpty()) data else dataList
        }

        fun handle(c: Char?) {
            state = when (c) {
                null -> end
                else -> state.handle(c)
            }
        }

        fun handleEndOfFile() {
            term.handle('\n')
            if (dataList.isNotEmpty()) {
                dataList.add(data)
            }
            state = end
        }

        private interface State {
            fun handle(c: Char): State
        }

        private inner class Start : State {
            override fun handle(c: Char) = character.handle(c)
        }

        private inner class End : State {
            override fun handle(c: Char): State = this
        }

        private inner class Character : State {
            override fun handle(c: Char): State {
                return when (c) {
                    '\"' -> quotedCharacter
                    '\\' -> escapedCharacter
                    '\n' -> linefeed.handle(c)
                    in delimiters -> delimiter.handle(c)
                    else -> {
                        buffer.add(c)
                        return this
                    }
                }
            }
        }

        private inner class EscapedCharacter : State {
            override fun handle(c: Char): State {
                buffer.add(c)
                return character
            }
        }

        private inner class QuotedCharacter : State {
            override fun handle(c: Char): State {
                return when (c) {
                    '\\' -> escapedQuotedCharacter
                    '\"' -> character
                    else -> {
                        buffer.add(c)
                        return this
                    }
                }
            }
        }

        private inner class EscapedQuotedCharacter : State {
            override fun handle(c: Char): State {
                buffer.add(c)
                return quotedCharacter
            }
        }

        private inner class Delimiter : State {
            override fun handle(c: Char) = term.handle(c)
        }

        private inner class Term : State {
            override fun handle(c: Char): State {
                data.add(buffer.joinToString(""))
                buffer.clear()
                return character
            }
        }

        private inner class LineFeed : State {
            override fun handle(c: Char): State {
                term.handle(c)
                dataList.add(data)
                data = mutableListOf()
                return character
            }
        }
    }
}
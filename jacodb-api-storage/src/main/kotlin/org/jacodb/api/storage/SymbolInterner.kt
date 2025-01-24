/*
 *  Copyright 2022 UnitTestBot contributors (utbot.org)
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jacodb.api.storage

import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicLong

interface SymbolInterner : Closeable {
    fun findOrNew(symbol: String): Long
    fun findSymbolName(symbolId: Long): String?
    fun flush(context: StorageContext, force: Boolean = false)
}

abstract class ConcurrentSymbolInterner : SymbolInterner {

    protected val symbolsIdGen = AtomicLong()
    protected val symbolsCache = ConcurrentHashMap<String, Long>()
    protected val idCache = ConcurrentHashMap<Long, String>()
    protected val newElements = ConcurrentSkipListMap<String, Long>()

    override fun findOrNew(symbol: String): Long {
        return symbolsCache.computeIfAbsent(symbol) {
            symbolsIdGen.incrementAndGet().also {
                newElements[symbol] = it
                idCache[it] = symbol
            }
        }
    }

    override fun findSymbolName(symbolId: Long): String? = idCache[symbolId]

    override fun close() {
        symbolsCache.clear()
        newElements.clear()
    }
}

fun String.asSymbolId(symbolInterner: SymbolInterner): Long {
    return symbolInterner.findOrNew(this)
}
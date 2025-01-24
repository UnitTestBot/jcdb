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

package org.jacodb.impl.storage

import org.jacodb.api.jvm.JcDatabasePersistence
import org.jacodb.api.storage.ConcurrentSymbolInterner
import org.jacodb.api.storage.StorageContext
import org.jacodb.impl.storage.jooq.tables.references.SYMBOLS
import org.jooq.DSLContext

class SqlSymbolInterner : ConcurrentSymbolInterner() {

    fun setup(persistence: JcDatabasePersistence) = persistence.read { context ->
        context.execute { jooq ->
            jooq.selectFrom(SYMBOLS).fetch().forEach {
                val (id, name) = it
                if (name != null && id != null) {
                    symbolsCache[name] = id
                    idCache[id] = name
                }
            }
            symbolsIdGen.set(SYMBOLS.ID.maxId(jooq) ?: 0L)
        }
    }

    override fun flush(context: StorageContext, force: Boolean) {
        val entries = newElements.entries.toList()
        if (entries.isNotEmpty()) {
            context.execute {
                context.connection.insertElements(
                    SYMBOLS,
                    entries,
                    onConflict = "ON CONFLICT(id) DO NOTHING"
                ) { (value, id) ->
                    setLong(1, id)
                    setString(2, value)
                }
            }
            entries.forEach {
                newElements.remove(it.key)
            }
        }
    }

    private fun StorageContext.execute(action: (DSLContext) -> Unit) {
        execute(sqlAction = action, noSqlAction = { error("Can't execute NoSql action in SqlSymbolInterner") })
    }
}
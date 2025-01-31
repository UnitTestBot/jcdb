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

import org.jacodb.api.storage.ContextProperty
import org.jacodb.api.storage.StorageContext
import org.jacodb.api.storage.invoke
import org.jooq.DSLContext
import java.sql.Connection

private object DSLContextProperty : ContextProperty<DSLContext> {
    override fun toString() = "dslContext"
}

private object ConnectionProperty : ContextProperty<Connection> {
    override fun toString() = "connection"
}

fun toStorageContext(dslContext: DSLContext, connection: Connection): StorageContext =
    toStorageContext(dslContext)(ConnectionProperty, connection)

fun toStorageContext(dslContext: DSLContext): StorageContext = StorageContext.of(DSLContextProperty, dslContext)

val StorageContext.dslContext: DSLContext get() = getContextObject(DSLContextProperty)

val StorageContext.connection: Connection get() = getContextObject(ConnectionProperty)

val StorageContext.isSqlContext: Boolean get() = hasContextObject(DSLContextProperty)

fun <T> StorageContext.execute(sqlAction: () -> T, noSqlAction: () -> T): T {
    return if (isErsContext) {
        noSqlAction()
    } else if (isSqlContext) {
        sqlAction()
    } else {
        throw IllegalArgumentException("StorageContext should support SQL or NoSQL persistence")
    }
}
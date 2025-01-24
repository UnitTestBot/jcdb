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

import org.jacodb.api.storage.ers.EntityRelationshipStorage
import org.jacodb.api.storage.ers.Transaction
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

/**
 * Abstract database access context contains several named context objects.
 * Normally, there should be implemented specific context classes for each persistence
 * implementation with its specific context objects.
 *
 * For SQLite persistence, StorageContext should contain [DSLContext] object and may contain [Connection] object.
 *
 * For [EntityRelationshipStorage] persistence, StorageContext should contain [Transaction] object.
 */
@Suppress("UNCHECKED_CAST")
class StorageContext private constructor() {

    private val contextObjects = ConcurrentHashMap<ContextProperty<*>, Any>()

    fun <T : Any> setContextObject(contextKey: ContextProperty<T>, contextObject: T) = apply {
        contextObjects[contextKey] = contextObject
    }

    fun <T : Any> getContextObject(property: ContextProperty<T>): T =
        contextObjects[property] as? T?
            ?: throw NullPointerException("StorageContext doesn't contain context object $property")

    fun <T : Any> hasContextObject(property: ContextProperty<T>): Boolean = contextObjects.containsKey(property)

    companion object {
        fun <T : Any> of(contextKey: ContextProperty<T>, contextObject: T): StorageContext {
            return StorageContext().apply { this(contextKey, contextObject) }
        }

        fun empty() = StorageContext()
    }
}

interface ContextProperty<T : Any>

operator fun <T : Any> StorageContext.invoke(contextKey: ContextProperty<T>, contextObject: T) =
    setContextObject(contextKey, contextObject)

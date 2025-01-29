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

package org.jacodb.impl.storage.ers

import org.jacodb.api.jvm.ClassSource
import org.jacodb.api.jvm.JcDatabase
import org.jacodb.api.jvm.JcDatabasePersistence
import org.jacodb.api.storage.ers.Entity
import org.jacodb.api.storage.ers.compressed
import org.jacodb.api.storage.ers.getEntityOrNull
import org.jacodb.impl.storage.txn

fun Entity.toClassSource(
    persistence: JcDatabasePersistence,
    className: String,
    nameId: Long,
    cachedByteCode: ByteArray? = null
): ClassSource =
    ErsClassSource(
        persistence = persistence as ErsPersistenceImpl,
        className = className,
        nameId = nameId,
        classId = id.instanceId,
        locationId = requireNotNull(getCompressed<Long>("locationId")) { "locationId property isn't set" },
        cachedByteCode = cachedByteCode
    )

fun Sequence<Entity>.toClassSourceSequence(db: JcDatabase): Sequence<ClassSource> {
    val persistence = db.persistence
    return map { clazz ->
        val nameId = requireNotNull(clazz.getCompressed<Long>("nameId")) { "nameId property isn't set" }
        clazz.toClassSource(persistence, persistence.findSymbolName(nameId), nameId)
    }
}

fun Sequence<Entity>.filterDeleted(): Sequence<Entity> = filter { it.get<Boolean>("isDeleted") != true }

fun Sequence<Entity>.filterLocations(locationIds: Set<Long>): Sequence<Entity> = filter {
    it.getCompressed<Long>("locationId") in locationIds
}

fun Sequence<Entity>.filterLocations(locationId: Long): Sequence<Entity> = filter {
    it.getCompressed<Long>("locationId") == locationId
}

fun <T> Sequence<T>.exactSingleOrNull(): T? {
    val it = iterator()
    if (!it.hasNext()) return null
    return it.next().apply {
        check(!it.hasNext()) {
            "Sequence should have exactly one element or no elements"
        }
    }
}

private class ErsClassSource(
    private val persistence: ErsPersistenceImpl,
    override val className: String,
    private val nameId: Long,
    private var classId: Long,
    private val locationId: Long,
    private var cachedByteCode: ByteArray?
) : ClassSource {

    override val byteCode: ByteArray
        get() {
            val prevClassId = classId
            val checkedClassId = checkClassId()
            return if (prevClassId == checkedClassId && cachedByteCode != null) {
                cachedByteCode!!
            } else {
                persistence.findBytecode(checkedClassId).also {
                    cachedByteCode?.let {
                        cachedByteCode = it
                    }
                }
            }
        }

    override val location = persistence.findLocation(locationId)

    private fun checkClassId(): Long = persistence.read { context ->
        val txn = context.txn
        var result = txn.getEntityOrNull("Class", classId)
        // Since location is mutable, class entity can become expired (deleted)
        // In that case, we have to re-evaluate it
        if (result == null || result.get<Boolean>("isDeleted") == true) {
            result = txn.find("Class", "nameId", nameId.compressed)
                .filterDeleted()
                .filterLocations(locationId)
                .single()
            classId = result.id.instanceId
        }
        classId
    }
}
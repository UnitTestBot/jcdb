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

import org.jacodb.api.caches.PluggableCache
import org.jacodb.api.caches.PluggableCacheProvider
import org.jacodb.api.jvm.JcByteCodeLocation
import org.jacodb.api.jvm.JcDatabasePersistence
import org.jacodb.api.jvm.RegisteredLocation
import org.jacodb.api.storage.ers.getEntityOrNull
import org.jacodb.impl.caches.xodus.XODUS_CACHE_PROVIDER_ID
import org.jacodb.impl.fs.JavaRuntime
import org.jacodb.impl.fs.asByteCodeLocation
import org.jacodb.impl.storage.ers.bytecode
import org.jacodb.impl.storage.jooq.tables.references.BYTECODELOCATIONS
import org.jacodb.impl.storage.jooq.tables.references.CLASSES
import java.io.File
import java.time.Duration

abstract class AbstractJcDbPersistence(
    private val javaRuntime: JavaRuntime,
) : JcDatabasePersistence {

    companion object {
        private const val CACHE_PREFIX = "org.jacodb.persistence.caches"
        private val locationsCacheSize = Integer.getInteger("$CACHE_PREFIX.locations", 1_000)
        private val byteCodeCacheSize = Integer.getInteger("$CACHE_PREFIX.bytecode", 10_000)
        private val cacheProvider = PluggableCacheProvider.getProvider(
            System.getProperty("$CACHE_PREFIX.cacheProviderId", XODUS_CACHE_PROVIDER_ID)
        )

        fun <KEY : Any, VALUE : Any> cacheOf(size: Int): PluggableCache<KEY, VALUE> {
            return cacheProvider.newCache {
                maximumSize = size
                expirationDuration = Duration.ofSeconds(
                    Integer.getInteger("$CACHE_PREFIX.expirationDurationSec", 10).toLong()
                )
            }
        }
    }

    private val locationsCache = cacheOf<Long, RegisteredLocation>(locationsCacheSize)
    private val byteCodeCache = cacheOf<Long, ByteArray>(byteCodeCacheSize)

    override val locations: List<JcByteCodeLocation>
        get() {
            return read { context ->
                context.execute(
                    sqlAction = {
                        context.dslContext.selectFrom(BYTECODELOCATIONS).fetch().map {
                            PersistentByteCodeLocationData.fromSqlRecord(it)
                        }
                    },
                    noSqlAction = {
                        context.txn.all(BytecodeLocationEntity.BYTECODE_LOCATION_ENTITY_TYPE).map {
                            PersistentByteCodeLocationData.fromErsEntity(it)
                        }.toList()
                    }
                ).mapNotNull {
                    try {
                        File(it.path).asByteCodeLocation(javaRuntime.version, isRuntime = it.runtime)
                    } catch (_: Exception) {
                        null
                    }
                }.flatten().distinct()
            }
        }

    override fun findBytecode(classId: Long): ByteArray {
        return byteCodeCache.get(classId) {
            read { context ->
                context.execute(
                    sqlAction = {
                        context.dslContext.select(CLASSES.BYTECODE).from(CLASSES).where(CLASSES.ID.eq(classId))
                            .fetchAny()?.value1()
                    },
                    noSqlAction = {
                        context.txn.getEntityOrNull("Class", classId).bytecode()
                    }
                )
            } ?: throw IllegalArgumentException("Can't find bytecode for $classId")
        }
    }

    override fun findSymbolId(symbol: String): Long {
        return symbol.asSymbolId()
    }

    override fun findSymbolName(symbolId: Long): String {
        return symbolInterner.findSymbolName(symbolId)!!
    }

    override fun findLocation(locationId: Long): RegisteredLocation {
        return locationsCache.get(locationId) {
            val locationData = read { context ->
                context.execute(
                    sqlAction = {
                        context.dslContext.fetchOne(BYTECODELOCATIONS, BYTECODELOCATIONS.ID.eq(locationId))
                            ?.let { PersistentByteCodeLocationData.fromSqlRecord(it) }
                    },
                    noSqlAction = {
                        context.txn.getEntityOrNull(BytecodeLocationEntity.BYTECODE_LOCATION_ENTITY_TYPE, locationId)
                            ?.let { PersistentByteCodeLocationData.fromErsEntity(it) }
                    }
                ) ?: throw IllegalArgumentException("location not found by id $locationId")
            }
            PersistentByteCodeLocation(
                persistence = this,
                runtimeVersion = javaRuntime.version,
                id = locationId,
                cachedData = locationData,
                cachedLocation = null
            )
        }
    }

    override fun close() {
        try {
            symbolInterner.close()
        } catch (_: Exception) {
            // ignore
        }
    }

    protected val runtimeProcessed: Boolean
        get() {
            return read { context ->
                context.execute(
                    sqlAction = {
                        val jooq = context.dslContext
                        val hasBytecodeLocations =
                            jooq.meta().tables.any { it.name.equals(BYTECODELOCATIONS.name, true) }
                        if (!hasBytecodeLocations) {
                            return@execute false
                        }

                        val count = jooq.fetchCount(
                            BYTECODELOCATIONS,
                            BYTECODELOCATIONS.STATE.notEqual(LocationState.PROCESSED.ordinal)
                                .and(BYTECODELOCATIONS.RUNTIME.isTrue)
                        )
                        count == 0
                    },
                    noSqlAction = {
                        context.txn.all(BytecodeLocationEntity.BYTECODE_LOCATION_ENTITY_TYPE).none {
                            it.get<Boolean>(BytecodeLocationEntity.IS_RUNTIME) == true &&
                                    it.get<Int>(BytecodeLocationEntity.STATE) != LocationState.PROCESSED.ordinal
                        }
                    }
                )
            }
        }
}

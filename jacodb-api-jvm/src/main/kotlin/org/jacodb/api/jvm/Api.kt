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

package org.jacodb.api.jvm

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.jacodb.api.storage.StorageContext
import org.jacodb.api.storage.SymbolInterner
import org.jacodb.api.storage.asSymbolId
import org.jacodb.api.storage.ers.EntityRelationshipStorage
import java.io.Closeable
import java.io.File

enum class LocationType {
    RUNTIME,
    APP
}

interface ClassSource {
    val className: String
    val byteCode: ByteArray
    val location: RegisteredLocation
}

/**
 * field usage mode
 */
enum class FieldUsageMode {

    /** search for reads */
    READ,

    /** search for writes */
    WRITE
}

interface JavaVersion {
    val majorVersion: Int
}


/**
 * Compilation database
 *
 * `close` method should be called when database is not needed anymore
 */
@JvmDefaultWithoutCompatibility
interface JcDatabase : Closeable {

    /**
     * Unique id of the database. Databases built against same locations have the same id.
     * Id changes if locations change. Databases with the same id can be considered as equal.
     */
    val id: String
    val locations: List<RegisteredLocation>
    val persistence: JcDatabasePersistence

    val runtimeVersion: JavaVersion

    /**
     * create classpath instance
     *
     * @param dirOrJars list of byte-code resources to be processed and included in classpath
     * @return new classpath instance associated with specified byte-code locations
     */
    suspend fun classpath(dirOrJars: List<File>, features: List<JcClasspathFeature>?): JcClasspath
    suspend fun classpath(dirOrJars: List<File>): JcClasspath = classpath(dirOrJars, null)
    fun asyncClasspath(dirOrJars: List<File>) = GlobalScope.future { classpath(dirOrJars) }
    fun asyncClasspath(dirOrJars: List<File>, features: List<JcClasspathFeature>?) =
        GlobalScope.future { classpath(dirOrJars, features) }

    /**
     * process and index single byte-code resource
     * @param dirOrJar build folder or jar file
     * @return current database instance
     */
    suspend fun load(dirOrJar: File): JcDatabase
    fun asyncLoad(dirOrJar: File) = GlobalScope.future { load(dirOrJar) }

    /**
     * process and index byte-code resources
     * @param dirOrJars list of build folder or jar file
     * @return current database instance
     */
    suspend fun load(dirOrJars: List<File>): JcDatabase
    fun asyncLoad(dirOrJars: List<File>) = GlobalScope.future { load(dirOrJars) }

    /**
     * load locations
     * @param locations locations to load
     * @return current database instance
     */
    suspend fun loadLocations(locations: List<JcByteCodeLocation>): JcDatabase
    fun asyncLocations(locations: List<JcByteCodeLocation>) = GlobalScope.future { loadLocations(locations) }

    /**
     * explicitly refreshes the state of resources from file-system.
     * That means that any new classpath created after refresh is done will
     * reference fresh byte-code from file-system. While any classpath created
     * before `refresh` will still reference byte-code which is outdated
     * according to file-system
     */
    suspend fun refresh()
    fun asyncRefresh() = GlobalScope.future { refresh() }

    /**
     * rebuilds features data (indexes)
     */
    suspend fun rebuildFeatures()
    fun asyncRebuildFeatures() = GlobalScope.future { rebuildFeatures() }

    /**
     * watch file system for changes and refreshes the state of database in case loaded resources and resources from
     * file systems are different.
     *
     * @return current database instance
     */
    fun watchFileSystemChanges(): JcDatabase

    /**
     * await background jobs
     */
    suspend fun awaitBackgroundJobs()
    suspend fun cancelBackgroundJobs()
    fun asyncAwaitBackgroundJobs() = GlobalScope.future { awaitBackgroundJobs() }

    /**
     * Sets this database's internal state to immutable if corresponding backend supports this operation.
     * If it does, any write operation is no longer possible.
     * The method can be used in order to "fix" current snapshot of the model.
     * Generally, there is no way to switch the database back to mutable.
     */
    suspend fun setImmutable()

    fun isInstalled(feature: JcFeature<*, *>): Boolean = features.contains(feature)

    val features: List<JcFeature<*, *>>
}


interface JcDatabasePersistence : Closeable {

    val locations: List<JcByteCodeLocation>

    val ers: EntityRelationshipStorage

    fun setup()

    /**
     * Try to load code model by database id
     */
    fun tryLoad(databaseId: String): Boolean = false

    fun <T> write(action: (StorageContext) -> T): T
    fun <T> read(action: (StorageContext) -> T): T

    fun persist(location: RegisteredLocation, classes: List<ClassSource>)
    fun findSymbolId(symbol: String): Long
    fun findSymbolName(symbolId: Long): String
    fun findLocation(locationId: Long): RegisteredLocation

    val symbolInterner: SymbolInterner
    fun findBytecode(classId: Long): ByteArray

    fun findClassSourceByName(cp: JcClasspath, fullName: String): ClassSource?
    fun findClassSources(db: JcDatabase, location: RegisteredLocation): List<ClassSource>
    fun findClassSources(cp: JcClasspath, fullName: String): List<ClassSource>

    fun createIndexes() {}

    /**
     * Sets this persistence's internal state to immutable if corresponding backend supports this operation.
     * If it does, any write operation is no longer possible.
     * The method can be used in order to "fix" current snapshot of the model.
     * Generally, there is no way to switch the persistence back to mutable.
     */
    fun setImmutable(databaseId: String) {}

    fun String.asSymbolId(): Long {
        return asSymbolId(symbolInterner)
    }
}

interface RegisteredLocation {
    val jcLocation: JcByteCodeLocation?
    val id: Long
    val path: String
    val isRuntime: Boolean
}
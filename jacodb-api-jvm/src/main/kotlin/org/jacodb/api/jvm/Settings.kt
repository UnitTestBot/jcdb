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

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import java.io.File
import java.time.Duration

/**
 * Settings for database
 */
class JcSettings {

    /** watch file system changes delay */
    var watchFileSystemDelay: Int? = null
        private set

    /** persisted  */
    val persistenceId: String?
        get() = persistenceSettings.persistenceId

    val persistenceLocation: String?
        get() = persistenceSettings.persistenceLocation

    val persistenceClearOnStart: Boolean?
        get() = persistenceSettings.persistenceClearOnStart

    var persistenceSettings: JcPersistenceSettings = JcPersistenceSettings()

    var keepLocalVariableNames: Boolean = false
        private set

    var buildModelForJRE: Boolean = true
        private set

    /** jar files which should be loaded right after database is created */
    var predefinedDirOrJars: List<File> = persistentListOf()
        private set

    var cacheSettings: JcCacheSettings = JcCacheSettings()
        private set

    var byteCodeSettings: JcByteCodeCache = JcByteCodeCache()
        private set

    var hooks: MutableList<(JcDatabase) -> Hook> = arrayListOf()
        private set

    /** mandatory setting for java runtime location */
    lateinit var jre: File

    /** features to add */
    var features: List<JcFeature<*, *>> = emptyList()
        private set

    init {
        useProcessJavaRuntime()
    }

    /**
     * builder for persistent settings
     * @param location - file for db location
     * @param clearOnStart -if true old data from this folder will be dropped
     */
    @JvmOverloads
    fun persistent(
        location: String,
        clearOnStart: Boolean = false,
        implSettings: JcPersistenceImplSettings? = null
    ) = apply {
        persistenceSettings.persistenceLocation = location
        persistenceSettings.persistenceClearOnStart = clearOnStart
        persistenceSettings.implSettings = implSettings
    }

    fun persistenceImpl(persistenceImplSettings: JcPersistenceImplSettings) = apply {
        persistenceSettings.implSettings = persistenceImplSettings
    }

    fun caching(settings: JcCacheSettings.() -> Unit) = apply {
        cacheSettings = JcCacheSettings().also { it.settings() }
    }

    fun caching(settings: JcCacheSettings) = apply {
        cacheSettings = settings
    }

    fun bytecodeCaching(byteCodeCache: JcByteCodeCache) = apply {
        this.byteCodeSettings = byteCodeCache
    }

    fun loadByteCode(files: List<File>) = apply {
        predefinedDirOrJars = (predefinedDirOrJars + files).toPersistentList()
    }

    fun keepLocalVariableNames() = apply {
        keepLocalVariableNames = true
    }

    fun buildModelForJRE(build: Boolean) = apply {
        buildModelForJRE = build
    }

    /**
     * builder for watching file system changes
     * @param delay - delay between syncs
     */
    @JvmOverloads
    fun watchFileSystem(delay: Int = 10_000) = apply {
        watchFileSystemDelay = delay
    }

    /** builder for hooks */
    fun withHook(hook: (JcDatabase) -> Hook) = apply {
        hooks += hook
    }

    /**
     * use java from JAVA_HOME env variable
     */
    fun useJavaHomeRuntime() = apply {
        val javaHome = System.getenv("JAVA_HOME") ?: throw IllegalArgumentException("JAVA_HOME is not set")
        jre = javaHome.asValidJRE()
    }

    /**
     * use java from current system process
     */
    fun useProcessJavaRuntime() = apply {
        val javaHome = System.getProperty("java.home") ?: throw IllegalArgumentException("java.home is not set")
        jre = javaHome.asValidJRE()
    }

    /**
     * use java from current system process
     */
    fun useJavaRuntime(runtime: File) = apply {
        jre = runtime.absolutePath.asValidJRE()
    }

    /**
     * install additional indexes
     */
    fun installFeatures(vararg feature: JcFeature<*, *>) = apply {
        features = features + feature.toList()
    }

    private fun String.asValidJRE(): File {
        val file = File(this)
        if (!file.exists()) {
            throw IllegalArgumentException("$this points to folder that do not exists")
        }
        return file
    }
}

class JcPersistenceSettings {
    val persistenceId: String? get() = implSettings?.persistenceId
    var persistenceLocation: String? = null
    var persistenceClearOnStart: Boolean? = null
    var implSettings: JcPersistenceImplSettings? = null
}

interface JcPersistenceImplSettings {
    val persistenceId: String
}

data class JcCacheSegmentSettings(
    val valueStoreType: ValueStoreType = ValueStoreType.STRONG,
    val maxSize: Long = 10_000,
    val expiration: Duration = Duration.ofMinutes(1)
)

enum class ValueStoreType { WEAK, SOFT, STRONG }

class JcCacheSettings {
    var cacheSpiId: String? = null
    var classes: JcCacheSegmentSettings = JcCacheSegmentSettings()
    var types: JcCacheSegmentSettings = JcCacheSegmentSettings()
    var rawInstLists: JcCacheSegmentSettings = JcCacheSegmentSettings()
    var instLists: JcCacheSegmentSettings = JcCacheSegmentSettings()
    var flowGraphs: JcCacheSegmentSettings = JcCacheSegmentSettings()

    @JvmOverloads
    fun classes(maxSize: Long, expiration: Duration, valueStoreType: ValueStoreType = ValueStoreType.STRONG) = apply {
        classes = JcCacheSegmentSettings(maxSize = maxSize, expiration = expiration, valueStoreType = valueStoreType)
    }

    @JvmOverloads
    fun types(maxSize: Long, expiration: Duration, valueStoreType: ValueStoreType = ValueStoreType.STRONG) = apply {
        types = JcCacheSegmentSettings(maxSize = maxSize, expiration = expiration, valueStoreType = valueStoreType)
    }

    @JvmOverloads
    fun rawInstLists(maxSize: Long, expiration: Duration, valueStoreType: ValueStoreType = ValueStoreType.STRONG) =
        apply {
            rawInstLists =
                JcCacheSegmentSettings(maxSize = maxSize, expiration = expiration, valueStoreType = valueStoreType)
        }

    @JvmOverloads
    fun instLists(maxSize: Long, expiration: Duration, valueStoreType: ValueStoreType = ValueStoreType.STRONG) = apply {
        instLists = JcCacheSegmentSettings(maxSize = maxSize, expiration = expiration, valueStoreType = valueStoreType)
    }

    @JvmOverloads
    fun flowGraphs(maxSize: Long, expiration: Duration, valueStoreType: ValueStoreType = ValueStoreType.STRONG) =
        apply {
            flowGraphs =
                JcCacheSegmentSettings(maxSize = maxSize, expiration = expiration, valueStoreType = valueStoreType)
        }
}

class JcByteCodeCache(val prefixes: List<String> = persistentListOf("java.", "javax.", "kotlinx.", "kotlin."))
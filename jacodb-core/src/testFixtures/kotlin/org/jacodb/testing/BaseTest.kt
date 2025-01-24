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

package org.jacodb.testing

import kotlinx.coroutines.runBlocking
import org.jacodb.api.jvm.JcClasspath
import org.jacodb.api.jvm.JcClasspathFeature
import org.jacodb.api.jvm.JcDatabase
import org.jacodb.api.jvm.JcFeature
import org.jacodb.api.jvm.JcPersistenceImplSettings
import org.jacodb.impl.JcErsSettings
import org.jacodb.impl.JcRamErsSettings
import org.jacodb.impl.JcSQLitePersistenceSettings
import org.jacodb.impl.RamErsSettings
import org.jacodb.impl.features.Builders
import org.jacodb.impl.features.InMemoryHierarchy
import org.jacodb.impl.features.Usages
import org.jacodb.impl.features.classpaths.UnknownClassMethodsAndFields
import org.jacodb.impl.features.classpaths.UnknownClasses
import org.jacodb.impl.jacodb
import org.jacodb.impl.storage.ers.ram.RAM_ERS_SPI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.reflect.full.companionObjectInstance

@Tag("lifecycle")
annotation class LifecycleTest


abstract class BaseTest(getAdditionalFeatures: (() -> List<JcClasspathFeature>)? = null) {

    protected open val cp: JcClasspath by lazy {
        runBlocking {
            val withDb = this@BaseTest.javaClass.withDb
            val additionalFeatures = getAdditionalFeatures?.invoke().orEmpty()
            withDb.db.classpath(allClasspath, additionalFeatures + withDb.classpathFeatures.toList())
        }
    }

    @AfterEach
    open fun close() {
        cp.close()
    }
}

val Class<*>.withDb: JcDatabaseHolder
    get() {
        val comp = kotlin.companionObjectInstance
        if (comp is JcDatabaseHolder) {
            return comp
        }
        val s = superclass
            ?: throw IllegalStateException("can't find WithDb companion object. Please check that test class has it.")
        return s.withDb
    }


interface JcDatabaseHolder {

    val classpathFeatures: List<JcClasspathFeature>
    val db: JcDatabase
    fun cleanup()
}

open class WithDb(vararg features: Any) : JcDatabaseHolder {

    protected var allFeatures = features.toList().toTypedArray()

    init {
        System.setProperty("org.jacodb.impl.storage.defaultBatchSize", "500")
    }

    val dbFeatures = allFeatures.mapNotNull { it as? JcFeature<*, *> }
    override val classpathFeatures = allFeatures.mapNotNull { it as? JcClasspathFeature }

    override var db = runBlocking {
        jacodb {
            // persistent("D:\\work\\jacodb\\jcdb-index.db")
            persistenceImpl(persistenceImpl())
            loadByteCode(allClasspath)
            useProcessJavaRuntime()
            keepLocalVariableNames()
            installFeatures(*dbFeatures.toTypedArray())
        }.also {
            it.awaitBackgroundJobs()
        }
    }

    override fun cleanup() {
        db.close()
    }

    internal open fun persistenceImpl(): JcPersistenceImplSettings = JcRamErsSettings
}

open class WithDbImmutable(vararg features: Any) : JcDatabaseHolder {

    protected var allFeatures = features.toList().toTypedArray()

    val dbFeatures = allFeatures.mapNotNull { it as? JcFeature<*, *> }
    override val classpathFeatures = allFeatures.mapNotNull { it as? JcClasspathFeature }

    override var db = runBlocking {
        jacodb {
            persistenceImpl(JcErsSettings(RAM_ERS_SPI, RamErsSettings(immutableDumpsPath = tempDir)))
            loadByteCode(allClasspath)
            useProcessJavaRuntime()
            keepLocalVariableNames()
            installFeatures(*dbFeatures.toTypedArray())
        }.also {
            it.setImmutable()
        }
    }

    override fun cleanup() {
        db.close()
    }

    companion object {
        private val tempDir by lazy {
            Paths.get(System.getProperty("java.io.tmpdir"), "jcdb-ers-immutable")
                .also {
                    if (!Files.exists(it)) {
                        Files.createDirectories(it)
                    }
                }
                .absolutePathString()
        }
    }
}

open class WithSQLiteDb(vararg features: Any) : WithDb(*features) {

    override fun persistenceImpl() = JcSQLitePersistenceSettings
}

val globalDb by lazy {
    WithDb(Usages, Builders, InMemoryHierarchy).db
}

val globalDbImmutable by lazy {
    WithDbImmutable(Usages, Builders, InMemoryHierarchy).db
}

val globalSQLiteDb by lazy {
    WithSQLiteDb(Usages, Builders, InMemoryHierarchy).db
}

open class WithGlobalDb(vararg _classpathFeatures: JcClasspathFeature) : JcDatabaseHolder {

    init {
        System.setProperty("org.jacodb.impl.storage.defaultBatchSize", "500")
    }

    override val classpathFeatures: List<JcClasspathFeature> = _classpathFeatures.toList()

    override val db: JcDatabase get() = globalDb

    override fun cleanup() {
    }
}

open class WithGlobalDbImmutable(vararg _classpathFeatures: JcClasspathFeature) : JcDatabaseHolder {

    init {
        System.setProperty("org.jacodb.impl.storage.defaultBatchSize", "500")
    }

    override val classpathFeatures: List<JcClasspathFeature> = _classpathFeatures.toList()

    override val db: JcDatabase get() = globalDbImmutable

    override fun cleanup() {
    }
}

open class WithGlobalSQLiteDb(vararg _classpathFeatures: JcClasspathFeature) : JcDatabaseHolder {

    init {
        System.setProperty("org.jacodb.impl.storage.defaultBatchSize", "500")
    }

    override val classpathFeatures: List<JcClasspathFeature> = _classpathFeatures.toList()

    override val db: JcDatabase get() = globalSQLiteDb

    override fun cleanup() {
    }
}

open class WithGlobalDbWithoutJRE(vararg _classpathFeatures: JcClasspathFeature) :
    WithGlobalDb(*_classpathFeatures) {

    override val classpathFeatures: List<JcClasspathFeature> =
        super.classpathFeatures + UnknownClasses + UnknownClassMethodsAndFields

    override val db: JcDatabase = runBlocking {
        jacodb {
            persistenceImpl(JcRamErsSettings)
            loadByteCode(allClasspath)
            keepLocalVariableNames()
            buildModelForJRE(build = false)
            installFeatures(Usages, Builders, InMemoryHierarchy)
        }.also {
            it.awaitBackgroundJobs()
        }
    }
}

open class WithRestoredDb(vararg features: JcFeature<*, *>) : WithDb(*features) {

    private val location by lazy {
        if (implSettings is JcSQLitePersistenceSettings) {
            Files.createTempFile("jcdb-", null).toFile().absolutePath
        } else {
            Files.createTempDirectory("jcdb-").toFile().absolutePath
        }
    }

    var tempDb: JcDatabase? = newDb()

    override var db: JcDatabase = newDb {
        tempDb?.close()
        tempDb = null
    }

    open val implSettings: JcPersistenceImplSettings get() = JcSQLitePersistenceSettings

    private fun newDb(before: () -> Unit = {}): JcDatabase {
        before()
        return runBlocking {
            jacodb {
                require(implSettings !is JcRamErsSettings) { "cannot restore in-RAM database" }
                persistent(
                    location = location,
                    implSettings = implSettings
                )
                loadByteCode(allClasspath)
                useProcessJavaRuntime()
                keepLocalVariableNames()
                installFeatures(*dbFeatures.toTypedArray())
            }.also {
                it.awaitBackgroundJobs()
            }
        }
    }
}

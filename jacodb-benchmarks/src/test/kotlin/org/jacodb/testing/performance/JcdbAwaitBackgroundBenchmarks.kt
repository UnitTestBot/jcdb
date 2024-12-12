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

package org.jacodb.testing.performance

import kotlinx.coroutines.runBlocking
import org.jacodb.api.jvm.JcDatabase
import org.jacodb.impl.JcErsSettings
import org.jacodb.impl.JcRamErsSettings
import org.jacodb.impl.JcSettings
import org.jacodb.impl.RamErsSettings
import org.jacodb.impl.features.InMemoryHierarchy
import org.jacodb.impl.features.Usages
import org.jacodb.impl.jacodb
import org.jacodb.impl.storage.ers.ram.RAM_ERS_SPI
import org.jacodb.testing.allClasspath
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

abstract class JcdbAbstractAwaitBackgroundBenchmarks {

    companion object {
        val tempDir by lazy {
            Files.createTempDirectory("ersImmutable").absolutePathString()
        }
    }

    private var db: JcDatabase? = null

    abstract fun JcSettings.configure()

    open val isImmutable = false

    @Setup(Level.Iteration)
    fun setup() {
        db = runBlocking {
            jacodb {
                useProcessJavaRuntime()
                configure()
            }
        }
    }

    @Benchmark
    fun awaitBackground() {
        runBlocking {
            if (isImmutable) {
                db?.setImmutable()
            } else {
                db?.awaitBackgroundJobs()
            }
        }
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        db?.let {
            db = null
            it.close()
        }
    }
}


@State(Scope.Benchmark)
@Fork(1, jvmArgs = ["-Xmx12g", "-Xms12g"])
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
class JcdbJvmBackgroundBenchmarks : JcdbAbstractAwaitBackgroundBenchmarks() {

    override fun JcSettings.configure() {
        persistent(File.createTempFile("jcdb-", "-db").absolutePath)
    }
}

@State(Scope.Benchmark)
@Fork(1, jvmArgs = ["-Xmx12g", "-Xms12g", "-XX:+HeapDumpOnOutOfMemoryError"])
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
class JcdbRAMJvmBackgroundBenchmarks : JcdbAbstractAwaitBackgroundBenchmarks() {

    override fun JcSettings.configure() {
        persistenceImpl(JcRamErsSettings)
    }
}

@State(Scope.Benchmark)
@Fork(1, jvmArgs = ["-Xmx12g", "-Xms12g"])
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
class JcdbAllClasspathBackgroundBenchmarks : JcdbAbstractAwaitBackgroundBenchmarks() {

    override fun JcSettings.configure() {
        loadByteCode(allClasspath)
        persistent(File.createTempFile("jcdb-", "-db").absolutePath)
    }
}

@State(Scope.Benchmark)
@Threads(1)
@Fork(1, jvmArgs = ["-Xmx12g", "-Xms12g", "-XX:+HeapDumpOnOutOfMemoryError"])
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
class JcdbRAMAllClasspathBackgroundBenchmarks : JcdbAbstractAwaitBackgroundBenchmarks() {

    override fun JcSettings.configure() {
        persistenceImpl(JcRamErsSettings)
        loadByteCode(allClasspath)
    }
}

@State(Scope.Benchmark)
@Fork(1, jvmArgs = ["-Xmx12g", "-Xms12g"])
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
class JcdbIdeaBackgroundBenchmarks : JcdbAbstractAwaitBackgroundBenchmarks() {

    override fun JcSettings.configure() {
        loadByteCode(allIdeaJars)
        installFeatures(Usages, InMemoryHierarchy)
        persistent(File.createTempFile("jcdb-", "-db").absolutePath)
    }
}

@State(Scope.Benchmark)
@Fork(1, jvmArgs = ["-Xmx12g", "-Xms12g", "-XX:+HeapDumpOnOutOfMemoryError"])
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
class JcdbRAMIdeaBackgroundBenchmarks : JcdbAbstractAwaitBackgroundBenchmarks() {

    override fun JcSettings.configure() {
        persistenceImpl(JcRamErsSettings)
        loadByteCode(allIdeaJars)
        installFeatures(Usages, InMemoryHierarchy)
    }
}

@State(Scope.Benchmark)
@Fork(1, jvmArgs = ["-Xmx20g", "-Xms12g", "-XX:+HeapDumpOnOutOfMemoryError"])
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
class JcdbRAMImmutableIdeaBackgroundBenchmarks : JcdbAbstractAwaitBackgroundBenchmarks() {

    override val isImmutable = true

    override fun JcSettings.configure() {
        persistenceImpl(JcErsSettings(RAM_ERS_SPI, RamErsSettings(immutableDumpsPath = tempDir)))
        loadByteCode(allIdeaJars)
        installFeatures(Usages, InMemoryHierarchy)
    }
}
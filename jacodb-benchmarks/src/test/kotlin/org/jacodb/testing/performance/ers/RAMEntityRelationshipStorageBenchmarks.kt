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

package org.jacodb.testing.performance.ers

import org.jacodb.api.storage.ers.EmptyErsSettings
import org.jacodb.api.storage.ers.Entity
import org.jacodb.api.storage.ers.EntityId
import org.jacodb.api.storage.ers.EntityRelationshipStorage
import org.jacodb.api.storage.ers.EntityRelationshipStorageSPI
import org.jacodb.api.storage.ers.Transaction
import org.jacodb.impl.storage.ers.ram.RAM_ERS_SPI
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
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

abstract class RAMEntityRelationshipStorageBenchmarks {

    private val ersSpi by lazy(LazyThreadSafetyMode.NONE) {
        EntityRelationshipStorageSPI.getProvider(RAM_ERS_SPI)
    }
    protected lateinit var storage: EntityRelationshipStorage
    private lateinit var txn: Transaction
    private lateinit var entity: Entity
    private lateinit var loginSearchValue: String
    private lateinit var passwordSearchValue: String

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun getLogin(hole: Blackhole) {
        hole.consume(entity.getRawProperty("login"))
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun getPassword(hole: Blackhole) {
        hole.consume(entity.getRawProperty("password"))
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun findByLogin(hole: Blackhole) {
        hole.consume(txn.find("User", "login", loginSearchValue).first())
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun findByPassword(hole: Blackhole) {
        hole.consume(txn.find("User", "password", passwordSearchValue).first())
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun findLt(hole: Blackhole) {
        hole.consume(txn.findLt("User", "age", 50).first())
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun findEqOrLt(hole: Blackhole) {
        hole.consume(txn.findEqOrLt("User", "age", 50).first())
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun findGt(hole: Blackhole) {
        hole.consume(txn.findGt("User", "age", 50).first())
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun findEqOrGt(hole: Blackhole) {
        hole.consume(txn.findEqOrGt("User", "age", 50).first())
    }

    @Setup(Level.Iteration)
    fun setupIteration() {
        createMutable()
        populate()
        setImmutable()
        txn = storage.beginTransaction(readonly = true)
        entity = txn.getEntityUnsafe(EntityId(0, (Math.random() * 1_000_000).toLong()))
        loginSearchValue = "login${entity.id.instanceId}"
        passwordSearchValue = "a very secure password ${entity.id.instanceId}"
    }

    @TearDown(Level.Iteration)
    fun tearDownIteration() {
        txn.abort()
        storage.close()
    }

    @Setup(Level.Invocation)
    fun setupInvocation() {
        loginSearchValue = "login${entity.id.instanceId}"
        passwordSearchValue = "a very secure password ${entity.id.instanceId}"
    }

    private fun createMutable() {
        storage = ersSpi.newStorage(null, EmptyErsSettings)
    }

    private fun populate() {
        storage.transactional { txn ->
            repeat(1_000_000) { i ->
                val user = txn.newEntity("User")
                user["login"] = "login$i"
                user["password"] = "a very secure password $i"
                user["age"] = 20 + i % 80
            }
        }
    }

    abstract fun setImmutable()
}

@State(Scope.Benchmark)
@Fork(1, jvmArgs = ["-Xmx8g", "-Xms8g"])
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
class RAMEntityRelationshipStorageMutableBenchmarks : RAMEntityRelationshipStorageBenchmarks() {

    override fun setImmutable() {
        // do nothing im mutable benchmark
    }
}

@State(Scope.Benchmark)
@Fork(1, jvmArgs = ["-Xmx8g", "-Xms8g"])
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
class RAMEntityRelationshipStorageImmutableBenchmarks : RAMEntityRelationshipStorageBenchmarks() {

    override fun setImmutable() {
        storage = storage.asImmutable("no id")
    }
}
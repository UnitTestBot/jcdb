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

@file:Suppress("UnstableApiUsage", "DEPRECATION")

package org.jacodb.testing.performance.hash

import com.google.common.hash.Hasher
import com.google.common.hash.Hashing
import kotlinx.benchmark.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 5, time = 1)
class HashBenchmarks {

    companion object {
        val array = ByteArray(1_000_000).also { SecureRandom().nextBytes(it) }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun createSip24Hasher(): Hasher {
        return Hashing.sipHash24().newHasher()
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun createMurMur3Hasher(): Hasher {
        return Hashing.murmur3_128().newHasher()
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun createMd5Hasher(): Hasher {
        return Hashing.md5().newHasher()
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    fun createSha256Hasher(): Hasher {
        return Hashing.sha256().newHasher()
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun sip24Hash(): Long {
        return Hashing.sipHash24().newHasher().putBytes(array).hash().asLong()
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun murMur3Hash(): Long {
        return Hashing.murmur3_128().newHasher().putBytes(array).hash().asLong()
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun sha256Hash(): Long {
        return Hashing.sha256().newHasher().putBytes(array).hash().asLong()
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    fun md5Hash(): Long {
        return Hashing.md5().newHasher().putBytes(array).hash().asLong()
    }
}
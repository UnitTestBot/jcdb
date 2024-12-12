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

package org.jacodb.util

import java.nio.ByteBuffer
import kotlin.math.max

private const val maxCapacity = Int.MAX_VALUE - 8 // same as ArraysSupport.SOFT_MAX_ARRAY_LENGTH

class ByteArrayBuilder(initialCapacity: Int = 1024) {

    private var buffer = ByteArray(initialCapacity)
    private var count = 0

    fun reset() {
        count = 0
    }

    fun append(data: ByteArray): ByteArrayBuilder {
        val len = data.size
        ensureCapacity(count + len)
        System.arraycopy(data, 0, buffer, count, len)
        count += len
        return this
    }

    fun append(b: Byte): ByteArrayBuilder {
        ensureCapacity(count + 1)
        buffer[count++] = b
        return this
    }

    fun toByteArray(): ByteArray {
        return if (buffer.size == count) buffer else buffer.copyOf(count)
    }

    fun toByteBuffer(): ByteBuffer {
        return ByteBuffer.wrap(toByteArray())
    }

    private fun ensureCapacity(minCapacity: Int) {
        val capacity = buffer.size
        if (capacity < minCapacity) {
            var advancedCapacity = if (capacity < 0x100000) capacity * 2 else capacity / 34 * 55 /* phi */
            if (advancedCapacity < 0 || advancedCapacity > maxCapacity) {
                advancedCapacity = maxCapacity
            }
            buffer = buffer.copyOf(max(minCapacity, advancedCapacity))
        }
    }
}

inline fun ByteArrayBuilder.build(builderAction: ByteArrayBuilder.() -> Unit): ByteArray {
    reset()
    builderAction()
    return toByteArray()
}
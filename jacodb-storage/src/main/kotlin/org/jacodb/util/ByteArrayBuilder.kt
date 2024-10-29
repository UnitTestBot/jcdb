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

import kotlin.math.max

class ByteArrayBuilder(initialCapacity: Int = 1024) {

    private var buffer = ByteArray(initialCapacity)
    private var count = 0

    fun append(data: ByteArray): ByteArrayBuilder {
        val len = data.size
        ensureCapacity(count + len)
        System.arraycopy(data, 0, buffer, count, len)
        count += len
        return this
    }

    fun toByteArray(): ByteArray {
        return if (buffer.size == count) buffer else buffer.copyOf(count)
    }

    private fun ensureCapacity(minCapacity: Int) {
        val capacity = buffer.size
        if (capacity < minCapacity) {
            buffer = buffer.copyOf(max(minCapacity, capacity * 2))
        }
    }
}
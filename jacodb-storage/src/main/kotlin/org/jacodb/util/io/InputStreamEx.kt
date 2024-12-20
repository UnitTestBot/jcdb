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

package org.jacodb.util.io

import java.io.EOFException
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets

/**
 * Reads [VLQ](https://en.wikipedia.org/wiki/Variable-length_quantity) as long value.
 *
 * @return variable-length quantity as long value or `-1L` is EOF is reached
 */
fun InputStream.readVlqUnsigned(): Long {
    var result = 0L
    var shift = 0
    while (true) {
        val c = read()
        if (c == -1) return -1L // EOF
        result += (((c and 0x7f).toLong()) shl shift)
        if ((c and 0x80) == 0) break
        shift += 7
    }
    return result
}

/**
 * @return UTF-8 nullable string
 */
fun InputStream.readString(): String? {
    return when (val length = readVlqUnsigned().toInt()) {
        0 -> null
        1 -> ""
        else -> {
            val bytes = ByteArray(length - 1)
            if (readFully(bytes) != bytes.size) {
                throw EOFException("Failed to read all bytes")
            }
            String(bytes, StandardCharsets.UTF_8)
        }
    }
}

fun InputStream.readFully(bytes: ByteArray): Int {
    val len = bytes.size
    var off = 0
    while (off < len) {
        val read = read(bytes, off, len - off)
        if (read < 0) {
            break
        }
        off += read
    }
    return off
}

/**
 * Complementary to [OutputStream.writeUnsignedOrderedLongs]() function
 */
fun InputStream.readUnsignedOrderedLongs(): Iterable<Long> = Iterable {
    object : LongIterator() {

        var prev = 0L
        var current = Long.MIN_VALUE

        override fun hasNext(): Boolean {
            if (current == Long.MIN_VALUE) {
                val diff = readVlqUnsigned()
                // if diff = 0 then current < prev, that means iterable is over
                current = prev + diff - 1L
            }
            return current >= prev
        }

        override fun nextLong(): Long {
            if (!hasNext()) throw NoSuchElementException()
            return current.also {
                prev = it
                current = Long.MIN_VALUE
            }
        }
    }
}

fun InputStream.readByteBuffer(direct: Boolean = true): ByteBuffer {
    val size = readVlqUnsigned().toInt()
    if (this is FileInputStream) {
        return if (direct) {
            val position = channel.position()
            channel.map(FileChannel.MapMode.READ_ONLY, position, size.toLong()).also {
                channel.position(position + size)
            }
        } else {
            ByteBuffer.allocate(size).also { buffer ->
                check(channel.read(buffer) == size) {
                    "Channel wasn't read fully"
                }
            }
        }
    }
    val result = if (direct) ByteBuffer.allocateDirect(size) else ByteBuffer.allocate(size)
    check(Channels.newChannel(this).read(result) == size) {
        "Channel wasn't read fully"
    }
    return result.flip() as ByteBuffer
}
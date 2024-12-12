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

package org.jacodb.impl.storage.ers.ram

import org.jacodb.util.ByteArrayBuilder
import org.jacodb.util.build
import org.jacodb.util.io.readVlqUnsigned
import java.io.InputStream

internal fun LinksMutable.toImmutable(builder: ByteArrayBuilder): LinksImmutable {
    val linksSnapshot = links.beginRead()
    val linkList = ArrayList<Pair<Long, ByteArray>>(linksSnapshot.size())
    linksSnapshot.forEach { link ->
        val instanceId = link.key
        val linkSet = link.value
        val valueArray = builder.build {
            linkSet.forEach { targetId ->
                writeCompressedUnsignedLong(builder, targetId)
            }
        }
        linkList += instanceId to valueArray
    }
    return LinksImmutable(targetTypeId, linkList.toAttributesImmutable(builder))
}

internal fun InputStream.readLinksImmutable(): LinksImmutable = LinksImmutable(
    targetTypeId = readVlqUnsigned().toInt(),
    attributes = readAttributesImmutable()
)

/**
 * Returns read unsigned long value and the length of the byte array used for the value.
 */
internal fun readCompressedUnsignedLong(bytes: ByteArray, offset: Int): Pair<Long, Int> {
    var len = 0
    var result = 0L
    while (true) {
        val b = bytes[offset + len].toInt()
        result += (b and 0x7F) shl (len * 7)
        len++
        if ((b and 0x80) != 0) break
    }
    return result to len
}

/**
 * Writes compressed unsigned long value to a new byte array.
 */
internal fun writeCompressedUnsignedLong(builder: ByteArrayBuilder, l: Long) {
    check(l >= 0)
    var t = l
    while (true) {
        if (t > 127) {
            builder.append((t and 0x7F).toByte())
        } else {
            builder.append((t or 0x80).toByte())
            break
        }
        t = t shr 7
    }
}
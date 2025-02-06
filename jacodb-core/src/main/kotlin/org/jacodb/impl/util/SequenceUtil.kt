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

package org.jacodb.impl.util

import java.util.*

inline fun <T> Sequence(crossinline it: () -> Iterable<T>): Sequence<T> = object : Sequence<T> {
    override fun iterator(): Iterator<T> = it().iterator()
}

fun <T> Enumeration<T>?.asSequence(): Sequence<T> {
    if (this == null) return emptySequence()
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            override fun hasNext() = this@asSequence.hasMoreElements()
            override fun next(): T = this@asSequence.nextElement()
        }
    }
}
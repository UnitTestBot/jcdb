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

package org.jacodb.api.jvm.spi

import java.lang.ref.SoftReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class SPILoader {

    val spiCache = ConcurrentHashMap<String, SoftReference<*>>()

    inline fun <reified T : CommonSPI> loadSPI(id: String): T? {
        return spiCache[id]?.get() as? T ?: run {
            val clazz = T::class.java
            var serviceLoader = ServiceLoader.load(clazz)
            if (!serviceLoader.iterator().hasNext()) {
                serviceLoader = ServiceLoader.load(clazz, clazz.getClassLoader())
            }
            serviceLoader.find { it.id == id }?.also {
                spiCache.putIfAbsent(id, SoftReference(it))
            }
        }
    }
}
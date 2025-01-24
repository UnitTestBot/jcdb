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

package org.jacodb.impl.fs

import com.google.common.hash.Hashing
import mu.KLogging
import org.jacodb.api.jvm.JavaVersion
import org.jacodb.api.jvm.LocationType
import org.jacodb.impl.softLazy
import java.io.File
import java.math.BigInteger
import java.util.jar.JarFile
import kotlin.text.Charsets.UTF_8

open class JarLocation(
    file: File,
    private val isRuntime: Boolean,
    private val runtimeVersion: JavaVersion
) : AbstractByteCodeLocation(file) {

    companion object : KLogging()

    @Suppress("UnstableApiUsage")
    override val currentHash: BigInteger
        get() {
            val jarFile = jarFile() ?: return BigInteger.ZERO
            return Hashing.sha256().newHasher().let { h ->
                jarFile.entries().asSequence().filter { !it.isDirectory }.sortedBy { it.name }.forEach { entry ->
                    h.putString(entry.name, UTF_8)
                    h.putLong(entry.crc)
                    h.putLong(entry.size)
                    h.putLong(entry.compressedSize)
                }
                BigInteger(h.hash().asBytes())
            }
        }

    override val type: LocationType
        get() = when {
            isRuntime -> LocationType.RUNTIME
            else -> LocationType.APP
        }

    override fun createRefreshed() = JarLocation(jarOrFolder, isRuntime, runtimeVersion)

    override val classes: Map<String, ByteArray> by softLazy {
        try {
            jarFacade.bytecode
        } catch (e: Exception) {
            logger.warn(e) { "error loading classes from jar: ${jarOrFolder.absolutePath}. returning empty loader" }
            emptyMap()
        }
    }

    override val classNames: Set<String>?
        get() = jarFacade.classes.keys

    override fun resolve(classFullName: String): ByteArray? {
        return jarFacade.inputStreamOf(classFullName)
    }

    protected open val jarFacade: JarFacade by lazy {
        JarFacade(runtimeVersion.majorVersion) { jarFile() }
    }

    private fun jarFile(): JarFile? {
        return if (!jarOrFolder.exists() || !jarOrFolder.isFile) {
            null
        } else {
            try {
                JarFile(jarOrFolder)
            } catch (e: Exception) {
                logger.warn(e) { "error processing jar ${jarOrFolder.absolutePath}" }
                null
            }
        }
    }

    override fun toString(): String = jarOrFolder.absolutePath

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is JarLocation) {
            return false
        }
        return other.jarOrFolder == jarOrFolder
    }

    override fun hashCode(): Int {
        return jarOrFolder.hashCode()
    }
}

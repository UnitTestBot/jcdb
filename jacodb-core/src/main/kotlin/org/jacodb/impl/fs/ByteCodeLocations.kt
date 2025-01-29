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

import mu.KLogging
import org.jacodb.api.jvm.JavaVersion
import org.jacodb.api.jvm.JcByteCodeLocation
import java.io.File
import java.nio.file.Paths
import java.util.jar.JarFile

val logger = object : KLogging() {}.logger

/**
 * Returns collection of `JcByteCodeLocation` of a file or directory.
 * Any jar file can have its own classpath defined in the manifest, that's why the method returns collection.
 * The method called of different files can have same locations in the result, so use `distinct()` to
 * filter duplicates out.
 */
fun File.asByteCodeLocation(runtimeVersion: JavaVersion, isRuntime: Boolean = false): Collection<JcByteCodeLocation> {
    if (!exists()) {
        throw IllegalArgumentException("file $absolutePath doesn't exist")
    }
    return if (isJar()) {
        mutableSetOf<File>().also { classPath(it) }.map { JarLocation(it, isRuntime, runtimeVersion) }
    } else if (isDirectory) {
        listOf(BuildFolderLocation(this))
    } else {
        error("$absolutePath is nether a jar file nor a build directory")
    }
}

fun Collection<File>.filterExisting(): List<File> = filter { file ->
    file.exists().also {
        if (!it) {
            logger.warn("${file.absolutePath} doesn't exists. make sure there is no mistake")
        }
    }
}

private fun File.classPath(classpath: MutableCollection<File>) {
    if (exists() && classpath.add(this) && isJar()) {
        JarFile(this).use { jarFile ->
            jarFile.manifest?.mainAttributes?.getValue("Class-Path")?.classpathFiles()?.forEach { ref ->
                Paths.get(ref).toFile().classPath(classpath)
            }
        }
    }
}

fun File.isJar() = isFile && name.endsWith(".jar") || name.endsWith(".jmod")

private const val file = "file:"

private fun String.classpathFiles(): List<String> {
    val fileOffsets = mutableListOf<Int>()
    var prevOffset = -1
    while (true) {
        val offset = indexOf(file, prevOffset + 1)
        if (offset == -1) break
        fileOffsets.add(offset)
        prevOffset = offset
    }
    if (fileOffsets.isEmpty()) return emptyList()
    val result = mutableListOf<String>()
    for (i in 1 until fileOffsets.size) {
        result += substring(fileOffsets[i - 1] + file.length, fileOffsets[i]).remove0d0aFix20()
    }
    return result.also {
        it += substring(fileOffsets.last() + file.length).remove0d0aFix20()
    }
}

private fun String.remove0d0aFix20() = replace("\r", "")
    .replace("\n", "")
    .replace(" ", "")
    .replace("%20", " ")
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

package org.jacodb.testing

import java.net.URL
import java.nio.file.Files
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.io.path.createTempDirectory

fun cookJar(link: String): Path {
    val url = URL(link)
    val result = createTempJar(url.file)
    Files.copy(url.openStream(), result, StandardCopyOption.REPLACE_EXISTING)
    return result
}

fun createTempJar(name: String) =
    Path(createTempDirectory("jcdb-temp-jar").toString(), name).also {
        createDirectories(it.parent)
    }
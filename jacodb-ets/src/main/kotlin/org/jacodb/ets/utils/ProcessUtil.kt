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

package org.jacodb.ets.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Reader

object ProcessUtil {
    fun run(command: List<String>, input: String? = null): String {
        val reader = input?.reader() ?: "".reader()
        return run(command, reader)
    }

    fun run(command: List<String>, input: Reader): String {
        val process = ProcessBuilder(command).start()
        return communicate(process, input)
    }

    private fun communicate(process: Process, input: Reader): String {
        val stdout = StringBuilder()
        val stderr = StringBuilder()

        val scope = CoroutineScope(Dispatchers.IO)

        // Handle process input
        val stdinJob = scope.launch {
            process.outputWriter().use { writer ->
                input.copyTo(writer)
            }
        }

        // Launch output capture coroutines
        val stdoutJob = scope.launch {
            process.inputReader().useLines { lines ->
                lines.forEach { stdout.appendLine(it) }
            }
        }
        val stderrJob = scope.launch {
            process.errorReader().useLines { lines ->
                lines.forEach { stderr.appendLine(it) }
            }
        }

        // Wait for completion
        val exitCode = process.waitFor()
        runBlocking {
            stdinJob.join()
            stdoutJob.join()
            stderrJob.join()
        }

        if (exitCode != 0) {
            throw ProcessException(
                "Process failed with exit code $exitCode",
                exitCode,
                stderr.toString()
            )
        }

        return stdout.toString()
    }

    class ProcessException(
        message: String,
        val exitCode: Int,
        val errorOutput: String,
    ) : RuntimeException(message)
}

fun main() {
    // Note: `ls -l /bin/` has big enough output to demonstrate the necessity
    //   of separate output capture threads/coroutines.
    val output = ProcessUtil.run(listOf("ls", "-l", "/bin/"))
    println(output)
}

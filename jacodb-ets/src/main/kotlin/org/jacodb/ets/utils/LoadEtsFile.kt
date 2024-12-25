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

import org.jacodb.ets.dto.EtsFileDto
import org.jacodb.ets.dto.convertToEtsFile
import org.jacodb.ets.model.EtsFile
import org.jacodb.ets.model.EtsScene
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.io.path.walk
import kotlin.time.Duration.Companion.seconds

private const val ENV_VAR_ARK_ANALYZER_DIR = "ARKANALYZER_DIR"
private const val DEFAULT_ARK_ANALYZER_DIR = "arkanalyzer"

private const val ENV_VAR_SERIALIZE_SCRIPT_PATH = "SERIALIZE_SCRIPT_PATH"
private const val DEFAULT_SERIALIZE_SCRIPT_PATH = "out/src/save/serializeArkIR.js"

private const val ENV_VAR_NODE_EXECUTABLE = "NODE_EXECUTABLE"
private const val DEFAULT_NODE_EXECUTABLE = "node"

fun generateEtsIR(
    projectPath: Path,
    isProject: Boolean = false,
    loadEntrypoints: Boolean = true,
    useArkAnalyzerTypeInference: Int? = null,
): Path {
    val arkAnalyzerDir = Path(System.getenv(ENV_VAR_ARK_ANALYZER_DIR) ?: DEFAULT_ARK_ANALYZER_DIR)
    if (!arkAnalyzerDir.exists()) {
        throw FileNotFoundException(
            "ArkAnalyzer directory does not exist: '${arkAnalyzerDir.absolute()}'. " +
                "Did you forget to set the '$ENV_VAR_ARK_ANALYZER_DIR' environment variable? " +
                "Current value is '${System.getenv(ENV_VAR_ARK_ANALYZER_DIR)}', " +
                "current dir is '${Path("").toAbsolutePath()}'."
        )
    }

    val scriptPath = System.getenv(ENV_VAR_SERIALIZE_SCRIPT_PATH) ?: DEFAULT_SERIALIZE_SCRIPT_PATH
    val script = arkAnalyzerDir.resolve(scriptPath)
    if (!script.exists()) {
        throw FileNotFoundException(
            "Script file not found: '$script'. " +
                "Did you forget to execute 'npm run build' in the arkanalyzer project?"
        )
    }

    val node = System.getenv(ENV_VAR_NODE_EXECUTABLE) ?: DEFAULT_NODE_EXECUTABLE
    val output = if (isProject) {
        createTempDirectory(prefix = projectPath.nameWithoutExtension)
    } else {
        kotlin.io.path.createTempFile(prefix = projectPath.nameWithoutExtension, suffix = ".json")
    }

    val cmd = listOfNotNull(
        node,
        script.pathString,
        if (isProject) "-p" else null,
        if (loadEntrypoints) "-e" else null,
        useArkAnalyzerTypeInference?.let { "-t $it" },
        projectPath.pathString,
        output.pathString,
    )
    runProcess(cmd, 10.seconds)
    return output
}

fun generateSdkIR(sdkPath: Path): Path = generateEtsIR(
    sdkPath,
    isProject = true,
    loadEntrypoints = false,
    useArkAnalyzerTypeInference = 0,
)

fun loadEtsFileAutoConvert(projectPath: Path): EtsFile {
    val irFilePath = generateEtsIR(
        projectPath,
        isProject = false,
        useArkAnalyzerTypeInference = 1,
    )
    irFilePath.inputStream().use { stream ->
        val etsFileDto = EtsFileDto.loadFromJson(stream)
        return convertToEtsFile(etsFileDto)
    }
}

fun loadEtsProjectAutoConvert(
    projectPath: Path,
    sdkIRPath: Path? = null,
    loadEntrypoints: Boolean = false,
    useArkAnalyzerTypeInference: Int? = 1,
): EtsScene {
    val irFolderPath = generateEtsIR(
        projectPath,
        isProject = true,
        loadEntrypoints = loadEntrypoints,
        useArkAnalyzerTypeInference = useArkAnalyzerTypeInference,
    )

    return loadEtsProjectFromIR(irFolderPath, sdkIRPath)
}

fun loadEtsProjectFromIR(
    projectFilesPath: Path,
    sdkFilesPath: Path?,
): EtsScene {
    val walker = { irFolder: Path ->
        irFolder.walk()
            .filter { it.extension == "json" }
            .map {
                it.inputStream().use { stream ->
                    val etsFileDto = EtsFileDto.loadFromJson(stream)
                    convertToEtsFile(etsFileDto)
                }
            }
            .toList()
    }

    val projectFiles = walker(projectFilesPath)
    val sdkFiles = sdkFilesPath?.let { walker(it) }.orEmpty()

    return EtsScene(projectFiles, sdkFiles)
}

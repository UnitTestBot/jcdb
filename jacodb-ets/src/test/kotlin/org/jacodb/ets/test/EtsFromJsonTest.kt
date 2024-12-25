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

package org.jacodb.ets.test

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.jacodb.ets.base.DEFAULT_ARK_CLASS_NAME
import org.jacodb.ets.base.DEFAULT_ARK_METHOD_NAME
import org.jacodb.ets.base.EtsAnyType
import org.jacodb.ets.base.EtsInstLocation
import org.jacodb.ets.base.EtsLocal
import org.jacodb.ets.base.EtsReturnStmt
import org.jacodb.ets.base.EtsUnknownType
import org.jacodb.ets.dto.AnyTypeDto
import org.jacodb.ets.dto.ClassSignatureDto
import org.jacodb.ets.dto.DecoratorDto
import org.jacodb.ets.dto.EtsMethodBuilder
import org.jacodb.ets.dto.FieldDto
import org.jacodb.ets.dto.FieldSignatureDto
import org.jacodb.ets.dto.FileSignatureDto
import org.jacodb.ets.dto.LiteralTypeDto
import org.jacodb.ets.dto.LocalDto
import org.jacodb.ets.dto.MethodDto
import org.jacodb.ets.dto.NumberTypeDto
import org.jacodb.ets.dto.PrimitiveLiteralDto
import org.jacodb.ets.dto.ReturnVoidStmtDto
import org.jacodb.ets.dto.StmtDto
import org.jacodb.ets.dto.TypeDto
import org.jacodb.ets.dto.convertToEtsFile
import org.jacodb.ets.dto.convertToEtsMethod
import org.jacodb.ets.model.EtsClassSignature
import org.jacodb.ets.model.EtsFileSignature
import org.jacodb.ets.model.EtsMethodSignature
import org.jacodb.ets.test.utils.getResourcePath
import org.jacodb.ets.test.utils.getResourcePathOrNull
import org.jacodb.ets.test.utils.loadEtsFileDtoFromResource
import org.jacodb.ets.test.utils.loadEtsProjectFromResources
import org.jacodb.ets.test.utils.testFactory
import org.jacodb.ets.utils.loadEtsFileAutoConvert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.condition.EnabledIf
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.toPath
import kotlin.test.assertEquals
import kotlin.test.assertIs

private val logger = KotlinLogging.logger {}

class EtsFromJsonTest {

    companion object {
        private val json = Json {
            // classDiscriminator = "_"
            prettyPrint = true
        }

        private val defaultSignature = EtsMethodSignature(
            enclosingClass = EtsClassSignature(
                name = DEFAULT_ARK_CLASS_NAME,
                file = EtsFileSignature.DEFAULT,
            ),
            name = DEFAULT_ARK_METHOD_NAME,
            parameters = emptyList(),
            returnType = EtsAnyType,
        )

        private const val PROJECT_PATH = "/projects/ArkTSDistributedCalc"
    }

    @Test
    fun testLoadEtsFileFromJson() {
        val path = "/samples/etsir/ast/save/basic.ts.json"
        val etsDto = loadEtsFileDtoFromResource(path)
        println("etsDto = $etsDto")
        val ets = convertToEtsFile(etsDto)
        println("ets = $ets")

        println("Classes: ${ets.classes.size}")
        for (cls in ets.classes) {
            println("= $cls with ${cls.methods.size} methods:")
            for (method in cls.methods) {
                println("  - $method")
            }
        }
    }

    @Test
    fun testLoadEtsFileAutoConvert() {
        val path = "/samples/source/example.ts"
        val res = getResourcePath(path)
        val etsFile = loadEtsFileAutoConvert(res)
        println("etsFile = $etsFile")
    }

    private fun projectAvailable(): Boolean {
        val path = this::class.java.getResource(PROJECT_PATH)?.toURI()?.toPath()
        return path != null && path.exists()
    }

    @EnabledIf("projectAvailable")
    @Test
    fun testLoadEtsProject() {
        val modules = listOf(
            "entry",
        )
        val prefix = "$PROJECT_PATH/etsir"
        val project = loadEtsProjectFromResources(modules, prefix)
        println("Classes: ${project.projectClasses.size}")
        for (cls in project.projectClasses) {
            println("= ${cls.signature} with ${cls.methods.size} methods:")
            for (method in cls.methods) {
                println("  - ${method.signature}")
            }
        }
    }

    @TestFactory
    fun testLoadAllAvailableEtsProjects() = testFactory {
        val p = getResourcePathOrNull("/projects") ?: run {
            logger.warn { "No projects directory found in resources" }
            return@testFactory
        }
        val availableProjectNames = p.toFile().listFiles { f -> f.isDirectory }!!.map { it.name }
        logger.info {
            buildString {
                appendLine("Found projects: ${availableProjectNames.size}")
                for (name in availableProjectNames) {
                    appendLine("  - $name")
                }
            }
        }
        if (availableProjectNames.isEmpty()) {
            logger.warn { "No projects found" }
            return@testFactory
        }
        container("load ${availableProjectNames.size} projects") {
            for (projectName in availableProjectNames.sorted()) {
                test("load $projectName") {
                    dynamicLoadEtsProject(projectName)
                }
            }
        }
    }

    private fun dynamicLoadEtsProject(projectName: String) {
        logger.info { "Loading project: $projectName" }
        val projectPath = getResourcePath("/projects/$projectName")
        val etsirPath = projectPath / "etsir"
        if (!etsirPath.exists()) {
            logger.warn { "No etsir directory found for project $projectName" }
            return
        }
        val modules = etsirPath.toFile().listFiles { f -> f.isDirectory }!!.map { it.name }
        logger.info { "Found ${modules.size} modules: $modules" }
        if (modules.isEmpty()) {
            logger.warn { "No modules found for project $projectName" }
            return
        }
        val project = loadEtsProjectFromResources(modules, "/projects/$projectName/etsir")
        logger.info {
            buildString {
                appendLine("Loaded project with ${project.projectClasses.size} classes and ${project.projectClasses.sumOf { it.methods.size }} methods")
                for (cls in project.projectClasses) {
                    appendLine("= ${cls.signature} with ${cls.methods.size} methods:")
                    for (method in cls.methods) {
                        appendLine("  - ${method.signature}")
                    }
                }
            }
        }
    }

    @Test
    fun testLoadValueFromJson() {
        val jsonString = """
            {
              "name": "x",
              "type": {
                "_": "AnyType"
              }
            }
        """.trimIndent()
        val valueDto = Json.decodeFromString<LocalDto>(jsonString)
        println("valueDto = $valueDto")
        Assertions.assertEquals(LocalDto("x", AnyTypeDto), valueDto)
        val ctx = EtsMethodBuilder(defaultSignature)
        val value = ctx.convertToEtsEntity(valueDto)
        println("value = $value")
        Assertions.assertEquals(EtsLocal("x", EtsAnyType), value)
    }

    @Test
    fun testLoadFieldFromJson() {
        val field = FieldDto(
            signature = FieldSignatureDto(
                declaringClass = ClassSignatureDto(
                    name = "TestClass",
                    declaringFile = FileSignatureDto(
                        projectName = "TestProject",
                        fileName = "test.ts",
                    )
                ),
                name = "x",
                type = NumberTypeDto,
            ),
            modifiers = 0,
            decorators = emptyList(),
            isOptional = true,
            isDefinitelyAssigned = false,
        )
        println("field = $field")

        val jsonString = json.encodeToString(field)
        println("json: $jsonString")

        val fieldDto = json.decodeFromString<FieldDto>(jsonString)
        println("fieldDto = $fieldDto")
        Assertions.assertEquals(field, fieldDto)
    }

    @Test
    fun testLoadReturnVoidStmtFromJson() {
        val jsonString = """
            {
              "_": "ReturnVoidStmt"
            }
        """.trimIndent()
        val stmtDto = Json.decodeFromString<StmtDto>(jsonString)
        println("stmtDto = $stmtDto")
        Assertions.assertEquals(ReturnVoidStmtDto, stmtDto)
    }

    @Test
    fun testLoadMethodFromJson() {
        val jsonString = """
             {
               "signature": {
                 "declaringClass": {
                   "name": "$DEFAULT_ARK_CLASS_NAME",
                   "declaringFile": {
                     "projectName": "TestProject",
                     "fileName": "test.ts"
                   }
                 },
                 "name": "$DEFAULT_ARK_METHOD_NAME",
                 "parameters": [],
                 "returnType": {
                    "_": "UnknownType"
                  }
               },
               "modifiers": 0,
               "decorators": [],
               "typeParameters": [],
               "body": {
                 "locals": [],
                 "cfg": {
                   "blocks": [
                     {
                       "id": 0,
                       "successors": [],
                       "predecessors": [],
                       "stmts": [
                         {
                           "_": "ReturnVoidStmt"
                         }
                       ]
                     }
                   ]
                 }
               }
             }
        """.trimIndent()
        val methodDto = Json.decodeFromString<MethodDto>(jsonString)
        println("methodDto = $methodDto")
        val method = convertToEtsMethod(methodDto)
        println("method = $method")
        Assertions.assertEquals(
            EtsMethodSignature(
                enclosingClass = EtsClassSignature(
                    name = DEFAULT_ARK_CLASS_NAME,
                    file = EtsFileSignature(
                        projectName = "TestProject",
                        fileName = "test.ts",
                    ),
                ),
                name = DEFAULT_ARK_METHOD_NAME,
                parameters = emptyList(),
                returnType = EtsUnknownType,
            ),
            method.signature
        )
        Assertions.assertEquals(0, method.locals.size)
        Assertions.assertEquals(1, method.cfg.stmts.size)
        Assertions.assertEquals(
            listOf(
                EtsReturnStmt(EtsInstLocation(method, 0), null),
            ),
            method.cfg.stmts
        )
    }

    @Test
    fun testLoadDecoratorFromJson() {
        val jsonString = """
            {
              "kind": "cat"
            }
        """.trimIndent()
        val decoratorDto = Json.decodeFromString<DecoratorDto>(jsonString)
        println("decoratorDto = $decoratorDto")
        Assertions.assertEquals(DecoratorDto("cat"), decoratorDto)
        val jsonString2 = json.encodeToString(decoratorDto)
        println("json: $jsonString2")
    }

    @Test
    fun testLoadLiteralTypeFromJson() {
        // TS: `let x: "hello" = "hello";`
        val jsonString = """
            {
              "_": "LiteralType",
              "literal": "hello"
            }
        """.trimIndent()
        val typeDto = Json.decodeFromString<TypeDto>(jsonString)
        println("typeDto = $typeDto")
        assertIs<LiteralTypeDto>(typeDto)
        assertEquals(PrimitiveLiteralDto.StringLiteral("hello"), typeDto.literal)
    }
}

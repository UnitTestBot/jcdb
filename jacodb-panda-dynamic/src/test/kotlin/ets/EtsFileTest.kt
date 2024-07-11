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

package ets

import ets.utils.loadIr
import org.jacodb.panda.dynamic.ets.base.EtsAssignStmt
import org.jacodb.panda.dynamic.ets.base.EtsInstanceFieldRef
import org.jacodb.panda.dynamic.ets.base.EtsReturnStmt
import org.jacodb.panda.dynamic.ets.base.EtsThis
import org.jacodb.panda.dynamic.ets.model.EtsFile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

private val logger = mu.KotlinLogging.logger {}

class EtsFileTest {

    companion object {
        private const val SAMPLE_NAME = "classes/SimpleClass"

        private fun load(name: String): EtsFile {
            return loadIr(
                filePath = "/etsir/$name.json",
            )
        }
    }

    @Test
    fun getEtsMethods() {
        val etsFile = load(SAMPLE_NAME)
        etsFile.classes.forEach { cls ->
            cls.methods.forEach { etsMethod ->
                assertNotNull(etsMethod.name)
                assertNotNull(etsMethod.cfg.instructions)
                logger.info { "Ets method '$etsMethod', instructions: ${etsMethod.cfg.instructions}" }

            }
        }
    }

    @Test
    fun printEtsInstructions() {
        val etsFile = load(SAMPLE_NAME)
        etsFile.classes.forEach { cls ->
            cls.methods.forEach { etsMethod ->
                assertNotNull(etsMethod.name)
                assertNotNull(etsMethod.cfg.instructions)
                logger.info { "Ets method '$etsMethod', localsCount = ${etsMethod.localsCount}" }
                etsMethod.cfg.instructions.forEach { inst ->
                    logger.info { "${inst.location.index}. $inst" }
                }
                logger.info { "-------------------------------------" }
            }
        }
    }

    @Test
    fun `test etsFile on TypeMismatch`() {
        val etsFile = load("TypeMismatch")
        etsFile.classes.forEach { cls ->
            cls.methods.forEach { etsMethod ->
                assertNotNull(etsMethod.name)
                assertNotNull(etsMethod.cfg.instructions)
                when (etsMethod.name) {
                    "add" -> {
                        assertEquals(9, etsMethod.cfg.instructions.size)
                    }

                    "main" -> {
                        assertEquals(4, etsMethod.cfg.instructions.size)
                    }
                }
            }
        }
    }

    @Test
    fun `test initializers prepended to class constructor`() {
        val etsFile = load("PrependInitializer")
        val cls = etsFile.classes.single { it.name == "Foo" }
        val ctorBegin = cls.ctor.cfg.instructions.first() as EtsAssignStmt
        val fieldRef = ctorBegin.lhv as EtsInstanceFieldRef
        assert(fieldRef.instance is EtsThis)
        assert(fieldRef.field.name == "x")
    }

    @Test
    fun `test static field should not be initialized in constructor`() {
        val etsFile = load("StaticField")
        val cls = etsFile.classes.single { it.name == "Foo" }
        assertFalse(cls.ctor.cfg.stmts.any {
            it is EtsAssignStmt && it.lhv is EtsInstanceFieldRef
        })
    }

    @Test
    fun `test default constructor should be synthesized`() {
        val etsFile = load("DefaultConstructor")
        val cls = etsFile.classes.single { it.name == "Foo" }
        val fieldInit = cls.ctor.cfg.instructions.first() as EtsAssignStmt
        val fieldRef = fieldInit.lhv as EtsInstanceFieldRef
        assert(fieldRef.instance is EtsThis)
        assert(fieldRef.field.name == "x")
    }
}

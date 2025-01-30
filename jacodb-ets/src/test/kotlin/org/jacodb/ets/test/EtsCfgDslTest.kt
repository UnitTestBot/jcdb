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

import org.jacodb.ets.base.EtsAssignStmt
import org.jacodb.ets.base.EtsLocal
import org.jacodb.ets.base.EtsNumberType
import org.jacodb.ets.base.EtsUnknownType
import org.jacodb.ets.graph.EtsBlockCfgBuilder
import org.jacodb.ets.dsl.add
import org.jacodb.ets.dsl.const
import org.jacodb.ets.dsl.local
import org.jacodb.ets.dsl.lt
import org.jacodb.ets.dsl.param
import org.jacodb.ets.dsl.program
import org.jacodb.ets.dsl.toBlockCfg
import org.jacodb.ets.dsl.toDot
import org.jacodb.ets.graph.linearize
import org.jacodb.ets.graph.toDot
import org.jacodb.ets.model.EtsClassSignature
import org.jacodb.ets.model.EtsFileSignature
import org.jacodb.ets.model.EtsMethodImpl
import org.jacodb.ets.model.EtsMethodParameter
import org.jacodb.ets.model.EtsMethodSignature
import org.jacodb.ets.utils.toDot
import org.junit.jupiter.api.Test

class EtsCfgDslTest {
    @Test
    fun `test simple program`() {
        val prog = program {
            val i = local("i")

            // i := arg(0)
            assign(i, param(0))

            // if (i < 10) i += 50
            ifStmt(lt(i, const(10.0))) {
                assign(i, add(i, const(50.0)))
            }

            // return i
            ret(i)
        }
        println("program:\n${prog.toText()}")
        val blockCfg = prog.toBlockCfg()
        println("blockCfg:\n${blockCfg.toDot()}")

        val locals = mutableListOf<EtsLocal>()
        val method = EtsMethodImpl(
            signature = EtsMethodSignature(
                enclosingClass = EtsClassSignature(
                    name = "Test",
                    file = EtsFileSignature(
                        projectName = "TestProject",
                        fileName = "Test.ts",
                    ),
                ),
                name = "testMethod",
                parameters = listOf(
                    EtsMethodParameter(0, "a", EtsUnknownType),
                ),
                returnType = EtsNumberType,
            ),
            locals = locals,
        )

        val etsBlockCfg = EtsBlockCfgBuilder(method).build(blockCfg)
        println("etsBlockCfg:\n${etsBlockCfg.toDot()}")
        val etsCfg = etsBlockCfg.linearize()
        println("etsCfg:\n${etsCfg.toDot()}")

        method._cfg = etsCfg
        locals += etsCfg.stmts
            .filterIsInstance<EtsAssignStmt>()
            .mapNotNull {
                val left = it.lhv
                if (left is EtsLocal) {
                    left
                } else {
                    null
                }
            }
    }
}

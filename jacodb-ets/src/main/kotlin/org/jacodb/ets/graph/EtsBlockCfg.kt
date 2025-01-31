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

package org.jacodb.ets.graph

import org.jacodb.ets.base.EtsAddExpr
import org.jacodb.ets.base.EtsAndExpr
import org.jacodb.ets.base.EtsAssignStmt
import org.jacodb.ets.base.EtsClassType
import org.jacodb.ets.base.EtsDivExpr
import org.jacodb.ets.base.EtsEntity
import org.jacodb.ets.base.EtsEqExpr
import org.jacodb.ets.base.EtsGtEqExpr
import org.jacodb.ets.base.EtsGtExpr
import org.jacodb.ets.base.EtsIfStmt
import org.jacodb.ets.base.EtsInstLocation
import org.jacodb.ets.base.EtsLocal
import org.jacodb.ets.base.EtsLtEqExpr
import org.jacodb.ets.base.EtsLtExpr
import org.jacodb.ets.base.EtsMulExpr
import org.jacodb.ets.base.EtsNegExpr
import org.jacodb.ets.base.EtsNopStmt
import org.jacodb.ets.base.EtsNotEqExpr
import org.jacodb.ets.base.EtsNotExpr
import org.jacodb.ets.base.EtsNumberConstant
import org.jacodb.ets.base.EtsOrExpr
import org.jacodb.ets.base.EtsParameterRef
import org.jacodb.ets.base.EtsReturnStmt
import org.jacodb.ets.base.EtsStmt
import org.jacodb.ets.base.EtsSubExpr
import org.jacodb.ets.base.EtsThis
import org.jacodb.ets.base.EtsType
import org.jacodb.ets.base.EtsUnknownType
import org.jacodb.ets.base.EtsValue
import org.jacodb.ets.dsl.BinaryExpr
import org.jacodb.ets.dsl.BinaryOperator
import org.jacodb.ets.dsl.Block
import org.jacodb.ets.dsl.BlockAssign
import org.jacodb.ets.dsl.BlockCfg
import org.jacodb.ets.dsl.BlockIf
import org.jacodb.ets.dsl.BlockNop
import org.jacodb.ets.dsl.BlockReturn
import org.jacodb.ets.dsl.Constant
import org.jacodb.ets.dsl.Expr
import org.jacodb.ets.dsl.Local
import org.jacodb.ets.dsl.Parameter
import org.jacodb.ets.dsl.ThisRef
import org.jacodb.ets.dsl.UnaryExpr
import org.jacodb.ets.dsl.UnaryOperator
import org.jacodb.ets.dsl.add
import org.jacodb.ets.dsl.and
import org.jacodb.ets.dsl.const
import org.jacodb.ets.dsl.local
import org.jacodb.ets.dsl.param
import org.jacodb.ets.dsl.program
import org.jacodb.ets.dsl.toBlockCfg
import org.jacodb.ets.model.EtsClassSignature
import org.jacodb.ets.model.EtsMethod
import org.jacodb.ets.model.EtsMethodImpl
import org.jacodb.ets.model.EtsMethodParameter
import org.jacodb.ets.model.EtsMethodSignature
import org.jacodb.ets.utils.toDot
import org.jacodb.ets.utils.view
import java.util.IdentityHashMap

data class EtsBasicBlock(
    val id: Int,
    val statements: List<EtsStmt>,
)

class EtsBlockCfg(
    val blocks: List<EtsBasicBlock>,
    val successors: Map<Int, List<Int>>, // for 'if-stmt' block, successors are (true, false) branches
)

fun BlockCfg.toEtsBlockCfg(method: EtsMethod): EtsBlockCfg {
    return EtsBlockCfgBuilder(method).build(this)
}

class EtsBlockCfgBuilder(
    val method: EtsMethod,
) {
    fun build(blockCfg: BlockCfg): EtsBlockCfg {
        return EtsBlockCfg(
            blocks = blockCfg.blocks.map { it.toEtsBasicBlock() },
            successors = blockCfg.successors,
        )
    }

    private var freeTempLocal: Int = 0
    private fun newTempLocal(type: EtsType): EtsLocal {
        return EtsLocal(
            name = "_tmp${freeTempLocal++}",
            type = type,
        )
    }

    private fun Block.toEtsBasicBlock(): EtsBasicBlock {
        val etsStatements: MutableList<EtsStmt> = mutableListOf()

        fun ensureSingleAddress(entity: EtsEntity): EtsValue {
            if (entity is EtsValue) {
                return entity
            }
            val newLocal = newTempLocal(entity.type)
            etsStatements += EtsAssignStmt(
                location = stub,
                lhv = newLocal,
                rhv = entity,
            )
            return newLocal
        }

        for (stmt in statements) {
            when (stmt) {
                BlockNop -> {
                    etsStatements += EtsNopStmt(location = stub)
                }

                is BlockAssign -> {
                    val lhv = stmt.target.toEtsEntity() as EtsLocal // safe cast
                    val rhv = stmt.expr.toEtsEntity()
                    etsStatements += EtsAssignStmt(
                        location = stub,
                        lhv = lhv,
                        rhv = rhv,
                    )
                }

                is BlockReturn -> {
                    val returnValue = ensureSingleAddress(stmt.expr.toEtsEntity())
                    etsStatements += EtsReturnStmt(
                        location = stub,
                        returnValue = returnValue,
                    )
                }

                is BlockIf -> {
                    val condition = stmt.condition.toEtsEntity()
                    etsStatements += EtsIfStmt(
                        location = stub,
                        condition = condition,
                    )
                }
            }
        }

        if (etsStatements.isEmpty()) {
            etsStatements += EtsNopStmt(location = stub)
        }

        return EtsBasicBlock(
            id = id,
            statements = etsStatements,
        )
    }

    private val stub = EtsInstLocation(method, -1)

    private fun Expr.toEtsEntity(): EtsEntity = when (this) {
        is Local -> EtsLocal(
            name = name,
            type = EtsUnknownType,
        )

        is Parameter -> EtsParameterRef(
            index = index,
            type = method.parameters[index].type,
        )

        ThisRef -> EtsThis(type = EtsClassType(EtsClassSignature.DEFAULT))

        is Constant -> EtsNumberConstant(value = value)

        is UnaryExpr -> when (operator) {
            UnaryOperator.NOT -> {
                EtsNotExpr(arg = expr.toEtsEntity())
            }

            UnaryOperator.NEG -> {
                EtsNegExpr(arg = expr.toEtsEntity(), type = EtsUnknownType)
            }
        }

        is BinaryExpr -> when (operator) {
            BinaryOperator.AND -> EtsAndExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
                type = EtsUnknownType,
            )

            BinaryOperator.OR -> EtsOrExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
                type = EtsUnknownType,
            )

            BinaryOperator.EQ -> EtsEqExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
            )

            BinaryOperator.NEQ -> EtsNotEqExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
            )

            BinaryOperator.LT -> EtsLtExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
            )

            BinaryOperator.LTE -> EtsLtEqExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
            )

            BinaryOperator.GT -> EtsGtExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
            )

            BinaryOperator.GTE -> EtsGtEqExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
            )

            BinaryOperator.ADD -> EtsAddExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
                type = EtsUnknownType,
            )

            BinaryOperator.SUB -> EtsSubExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
                type = EtsUnknownType,
            )

            BinaryOperator.MUL -> EtsMulExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
                type = EtsUnknownType,
            )

            BinaryOperator.DIV -> EtsDivExpr(
                left = left.toEtsEntity(),
                right = right.toEtsEntity(),
                type = EtsUnknownType,
            )
        }
    }
}

fun EtsBlockCfg.linearize(): EtsCfg {
    val linearized: MutableList<EtsStmt> = mutableListOf()
    val successorMap: MutableMap<EtsStmt, List<EtsStmt>> = hashMapOf()
    val stmtMap: MutableMap<EtsStmt, EtsStmt> = IdentityHashMap() // original -> linearized (with location)

    val queue = ArrayDeque<EtsBasicBlock>()
    val visited: MutableSet<EtsBasicBlock> = hashSetOf()

    if (blocks.isNotEmpty()) {
        queue.add(blocks.first())
    }

    while (queue.isNotEmpty()) {
        val block = queue.removeFirst()
        if (!visited.add(block)) continue

        for (stmt in block.statements) {
            val newStmt = when (stmt) {
                is EtsNopStmt -> stmt.copy(location = stmt.location.copy(index = linearized.size))
                is EtsAssignStmt -> stmt.copy(location = stmt.location.copy(index = linearized.size))
                is EtsReturnStmt -> stmt.copy(location = stmt.location.copy(index = linearized.size))
                is EtsIfStmt -> stmt.copy(location = stmt.location.copy(index = linearized.size))
                else -> error("Unsupported statement type: $stmt")
            }
            stmtMap[stmt] = newStmt
            linearized += newStmt
        }

        val successors = successors[block.id] ?: error("No successors for block ${block.id}")
        for (succId in successors.asReversed()) {
            val succ = blocks[succId]
            queue.addFirst(succ)
        }
    }

    for (block in blocks) {
        check(block.statements.isNotEmpty()) {
            "Block ${block.id} is empty"
        }

        for ((stmt, next) in block.statements.zipWithNext()) {
            successorMap[stmtMap.getValue(stmt)] = listOf(stmtMap.getValue(next))
        }

        val successors = successors[block.id] ?: error("No successors for block ${block.id}")
        val last = stmtMap.getValue(block.statements.last())
        // Note: reverse the order of successors, because in all CFGs, except EtsCfg,
        //       the successors for the if-stmt are (true, false) branches,
        //       and only the EtsCfg (which we are building here) has the (false, true) order.
        successorMap[last] = successors.asReversed().map {
            stmtMap.getValue(blocks[it].statements.first())
        }
    }

    return EtsCfg(linearized, successorMap)
}

private fun EtsStmt.toDotLabel(): String = when (this) {
    is EtsNopStmt -> "nop"
    is EtsAssignStmt -> "$lhv := $rhv"
    is EtsReturnStmt -> "return $returnValue"
    is EtsIfStmt -> "if ($condition)"
    else -> this.toString()
}

private fun String.htmlEncode(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

fun EtsBlockCfg.toDot(useHtml: Boolean = true): String {
    val lines = mutableListOf<String>()
    lines += "digraph cfg {"
    lines += "  node [shape=${if (useHtml) "none" else "rect"} fontname=\"monospace\"]"

    // Nodes
    blocks.forEach { block ->
        if (useHtml) {
            val s = block.statements.joinToString("") {
                it.toDotLabel().htmlEncode() + "<br/>"
            }
            val h = "<table border=\"0\" cellborder=\"1\" cellspacing=\"0\">" +
                "<tr><td>" + "<b>Block #${block.id}</b>" + "</td></tr>" +
                "<tr><td balign=\"left\">" + s + "</td></tr>" +
                "</table>"
            lines += "  ${block.id} [label=<${h}>]"
        } else {
            val s = block.statements.joinToString("") { it.toDotLabel() + "\\l" }
            lines += "  ${block.id} [label=\"Block #${block.id}\\n$s\"]"
        }
    }

    // Edges
    blocks.forEach { block ->
        val succs = successors[block.id]
        if (succs != null) {
            if (succs.isEmpty()) return@forEach
            if (succs.size == 1) {
                lines += "  ${block.id} -> ${succs.single()}"
            } else {
                check(succs.size == 2)
                val (trueBranch, falseBranch) = succs
                lines += "  ${block.id} -> $trueBranch [label=\"true\"]"
                lines += "  ${block.id} -> $falseBranch [label=\"false\"]"
            }
        }
    }

    lines += "}"
    return lines.joinToString("\n")
}

private fun main() {
    val method = EtsMethodImpl(
        EtsMethodSignature(
            enclosingClass = EtsClassSignature.DEFAULT,
            name = "foo",
            parameters = listOf(
                EtsMethodParameter(
                    index = 0,
                    name = "x",
                    type = EtsUnknownType,
                ),
                EtsMethodParameter(
                    index = 1,
                    name = "y",
                    type = EtsUnknownType,
                ),
            ),
            returnType = EtsUnknownType,
        )
    )
    val p = program {
        assign(local("x"), param(0))
        assign(local("y"), param(1))
        ifStmt(and(local("x"), local("y"))) {
            ret(add(local("x"), local("y")))
        }
        ret(const(0.0))
    }
    val blockCfg = p.toBlockCfg()
    val etsBlockCfg = blockCfg.toEtsBlockCfg(method)
    println(etsBlockCfg.toDot())
    view(etsBlockCfg.toDot(), name = "etsBlockCfg")
    val etsCfg = etsBlockCfg.linearize()
    println(etsCfg.toDot())
    view(etsCfg.toDot(), name = "etsCfg")
}

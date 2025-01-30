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

@file:Suppress("UnusedReceiverParameter")

package org.jacodb.ets.graph

import org.jacodb.ets.utils.view
import java.util.IdentityHashMap

sealed interface Expr

data class Local(val name: String) : Expr {
    override fun toString() = name
}

data class Parameter(val index: Int) : Expr {
    override fun toString() = "param($index)"
}

object ThisRef : Expr {
    override fun toString() = "this"
}

data class Constant(val value: Double) : Expr {
    override fun toString() = "const($value)"
}

enum class BinaryOperator {
    AND, OR,
    EQ, NEQ, LT, LTE, GT, GTE,
    ADD, SUB, MUL, DIV
}

data class BinaryExpr(
    val operator: BinaryOperator,
    val left: Expr,
    val right: Expr,
) : Expr {
    override fun toString() = "${operator.name.lowercase()}($left, $right)"
}

enum class UnaryOperator { NOT, NEG }

data class UnaryExpr(
    val operator: UnaryOperator,
    val expr: Expr,
) : Expr {
    override fun toString() = "${operator.name.lowercase()}($expr)"
}

sealed interface ProgramNode

data object ProgramNop : ProgramNode

data class ProgramAssign(
    val target: Local,
    val expr: Expr,
) : ProgramNode

data class ProgramReturn(
    val expr: Expr,
) : ProgramNode

data class ProgramIf(
    val condition: Expr,
    val thenBranch: List<ProgramNode>,
    val elseBranch: List<ProgramNode>,
) : ProgramNode

data class ProgramLabel(
    val name: String,
) : ProgramNode

data class ProgramGoto(
    val targetLabel: String,
) : ProgramNode

data class Program(
    val nodes: List<ProgramNode>,
) {
    fun toText(indent: Int = 2): String {
        val lines = mutableListOf<String>()

        fun process(nodes: List<ProgramNode>, currentIndent: Int = 0) {
            fun line(line: String) {
                lines += " ".repeat(currentIndent) + line
            }

            for (node in nodes) {
                when (node) {
                    is ProgramNop -> line("nop")
                    is ProgramAssign -> line("${node.target} := ${node.expr}")
                    is ProgramReturn -> line("return ${node.expr}")
                    is ProgramIf -> {
                        line("if (${node.condition}) {")
                        process(node.thenBranch, currentIndent + indent)
                        if (node.elseBranch.isNotEmpty()) {
                            line("} else {")
                            process(node.elseBranch, currentIndent + indent)
                        }
                        line("}")
                    }

                    is ProgramLabel -> line("label ${node.name}")
                    is ProgramGoto -> line("goto ${node.targetLabel}")
                }
            }
        }

        process(nodes)

        return lines.joinToString("\n")
    }
}

fun program(block: ProgramBuilder.() -> Unit): Program {
    val builder = ProgramBuilderImpl()
    builder.block()
    return builder.build()
}

interface ProgramBuilder {
    fun assign(target: Local, expr: Expr)
    fun ret(expr: Expr)
    fun ifStmt(condition: Expr, block: IfBuilder.() -> Unit)
    fun nop()
    fun label(name: String)
    fun goto(label: String)
}

fun ProgramBuilder.local(name: String): Local = Local(name)
fun ProgramBuilder.param(index: Int): Parameter = Parameter(index)
fun ProgramBuilder.thisRef(): Expr = ThisRef
fun ProgramBuilder.const(value: Double): Constant = Constant(value)

fun ProgramBuilder.and(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.AND, left, right)
fun ProgramBuilder.or(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.OR, left, right)
fun ProgramBuilder.eq(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.EQ, left, right)
fun ProgramBuilder.neq(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.NEQ, left, right)
fun ProgramBuilder.lt(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.LT, left, right)
fun ProgramBuilder.leq(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.LTE, left, right)
fun ProgramBuilder.gt(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.GT, left, right)
fun ProgramBuilder.geq(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.GTE, left, right)
fun ProgramBuilder.add(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.ADD, left, right)
fun ProgramBuilder.sub(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.SUB, left, right)
fun ProgramBuilder.mul(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.MUL, left, right)
fun ProgramBuilder.div(left: Expr, right: Expr): Expr = BinaryExpr(BinaryOperator.DIV, left, right)

fun ProgramBuilder.not(expr: Expr): Expr = UnaryExpr(UnaryOperator.NOT, expr)
fun ProgramBuilder.neg(expr: Expr): Expr = UnaryExpr(UnaryOperator.NEG, expr)

class ProgramBuilderImpl : ProgramBuilder {
    private val _nodes: MutableList<ProgramNode> = mutableListOf()
    val nodes: List<ProgramNode> get() = _nodes

    fun build(): Program {
        return Program(nodes)
    }

    override fun nop() {
        _nodes += ProgramNop
    }

    override fun label(name: String) {
        _nodes += ProgramLabel(name)
    }

    override fun goto(label: String) {
        _nodes += ProgramGoto(label)
    }

    override fun ret(expr: Expr) {
        _nodes += ProgramReturn(expr)
    }

    override fun assign(target: Local, expr: Expr) {
        _nodes += ProgramAssign(target, expr)
    }

    override fun ifStmt(condition: Expr, block: IfBuilder.() -> Unit) {
        val builder = IfBuilder().apply(block)
        _nodes += ProgramIf(condition, builder.thenNodes, builder.elseNodes)
    }
}

class IfBuilder : ProgramBuilder {
    private val thenBuilder = ProgramBuilderImpl()
    private val elseBuilder = ProgramBuilderImpl()
    private var elseEntered = false

    val thenNodes: List<ProgramNode> get() = thenBuilder.nodes
    val elseNodes: List<ProgramNode> get() = elseBuilder.nodes

    fun `else`(block: ProgramBuilder.() -> Unit) {
        check(!elseEntered) { "Multiple else branches" }
        elseEntered = true
        elseBuilder.apply(block)
    }

    override fun assign(target: Local, expr: Expr) = thenBuilder.assign(target, expr)
    override fun ifStmt(condition: Expr, block: IfBuilder.() -> Unit) = thenBuilder.ifStmt(condition, block)
    override fun ret(expr: Expr) = thenBuilder.ret(expr)
    override fun nop() = thenBuilder.nop()
    override fun goto(label: String) = thenBuilder.goto(label)
    override fun label(name: String) = thenBuilder.label(name)
}

private fun ProgramNode.toDotLabel() = when (this) {
    is ProgramAssign -> "$target := $expr"
    is ProgramReturn -> "return $expr"
    is ProgramIf -> "if ($condition)"
    is ProgramNop -> "nop"
    is ProgramLabel -> "label $name"
    is ProgramGoto -> "goto $targetLabel"
}

fun Program.toDot(): String {
    val lines = mutableListOf<String>()
    lines += "digraph cfg {"
    lines += "  node [shape=rect fontname=\"monospace\"]"

    val labelMap: MutableMap<String, ProgramLabel> = hashMapOf()
    val nodeToId: MutableMap<ProgramNode, Int> = IdentityHashMap()
    var freeId = 0

    fun processForNodes(nodes: List<ProgramNode>) {
        for (node in nodes) {
            val id = nodeToId.computeIfAbsent(node) { freeId++ }
            lines += "  $id [label=\"${node.toDotLabel()}\"]"
            if (node is ProgramIf) {
                processForNodes(node.thenBranch)
                processForNodes(node.elseBranch)
            }
            if (node is ProgramLabel) {
                check(node.name !in labelMap) { "Duplicate label: ${node.name}" }
                labelMap[node.name] = node
            }
        }
    }

    processForNodes(nodes)

    fun processForEdges(nodes: List<ProgramNode>) {
        for (node in nodes) {
            val id = nodeToId[node] ?: error("No ID for $node")
            when (node) {
                is ProgramIf -> {
                    if (node.thenBranch.isNotEmpty()) {
                        val thenNode = node.thenBranch.first()
                        val thenId = nodeToId[thenNode] ?: error("No ID for $thenNode")
                        lines += "  $id -> $thenId [label=\"true\"]"
                        processForEdges(node.thenBranch)
                    }
                    if (node.elseBranch.isNotEmpty()) {
                        val elseNode = node.elseBranch.first()
                        val elseId = nodeToId[elseNode] ?: error("No ID for $elseNode")
                        lines += "  $id -> $elseId [label=\"false\"]"
                        processForEdges(node.elseBranch)
                    }
                }

                is ProgramGoto -> {
                    val labelNode = labelMap[node.targetLabel] ?: error("Unknown label: ${node.targetLabel}")
                    val labelId = nodeToId[labelNode] ?: error("No ID for $labelNode")
                    lines += "  $id -> $labelId"
                }

                else -> {
                    // See below.
                }
            }
        }

        for ((cur, next) in nodes.zipWithNext()) {
            val curId = nodeToId[cur] ?: error("No ID for $cur")
            val nextId = nodeToId[next] ?: error("No ID for $next")
            lines += "  $curId -> $nextId"
        }
    }
    processForEdges(nodes)

    lines += "}"
    return lines.joinToString("\n")
}

//----- CFG -----

sealed interface BlockStmt
// TODO: merge BlockStmt and Stmt (add location to BlockStmt, initialize with -1)

data object BlockNop : BlockStmt

data class BlockAssign(
    val target: Local,
    val expr: Expr,
) : BlockStmt

data class BlockReturn(
    val expr: Expr,
) : BlockStmt

data class BlockIf(
    val condition: Expr,
) : BlockStmt

data class Block(
    val id: Int,
    val statements: List<BlockStmt>,
)

data class BlockCfg(
    val blocks: List<Block>,
    val successors: Map<Int, List<Int>>,
)

fun Program.toBlockCfg(): BlockCfg {
    val labelToNode: MutableMap<String, ProgramNode> = hashMapOf()
    val targets: MutableSet<ProgramNode> = hashSetOf()

    fun findLabels(nodes: List<ProgramNode>) {
        if (nodes.lastOrNull() is ProgramLabel) {
            error("Label at the end of the block: $nodes")
        }
        for ((stmt, next) in nodes.zipWithNext()) {
            if (stmt is ProgramLabel) {
                check(next !is ProgramLabel) { "Two labels in a row: $stmt, $next" }
                check(next !is ProgramGoto) { "Label followed by goto: $stmt, $next" }
                check(stmt.name !in labelToNode) { "Duplicate label: ${stmt.name}" }
                labelToNode[stmt.name] = next
            }
        }
        for (node in nodes) {
            if (node is ProgramIf) {
                findLabels(node.thenBranch)
                findLabels(node.elseBranch)
            }
            if (node is ProgramGoto) {
                targets += labelToNode[node.targetLabel] ?: error("Unknown label: ${node.targetLabel}")
            }
        }
    }

    findLabels(nodes)

    val blocks: MutableList<Block> = mutableListOf()
    val successors: MutableMap<Int, List<Int>> = hashMapOf()
    val stmtToBlock: MutableMap<BlockStmt, Int> = IdentityHashMap()
    val nodeToStmt: MutableMap<ProgramNode, BlockStmt> = IdentityHashMap()

    fun buildBlocks(nodes: List<ProgramNode>): Pair<Int, Int>? {
        if (nodes.isEmpty()) return null

        lateinit var currentBlock: MutableList<BlockStmt>

        fun newBlock(): Block {
            currentBlock = mutableListOf()
            val block = Block(blocks.size, currentBlock)
            blocks += block
            return block
        }

        var block = newBlock()
        val firstBlockId = block.id

        for (node in nodes) {
            if (node is ProgramLabel) continue

            if (node in targets && currentBlock.isNotEmpty()) {
                block.statements.forEach { stmtToBlock[it] = block.id }
                val prevBlock = block
                block = newBlock()
                successors[prevBlock.id] = listOf(block.id)
            }

            if (node !is ProgramGoto) {
                val stmt = when (node) {
                    ProgramNop -> BlockNop
                    is ProgramAssign -> BlockAssign(node.target, node.expr)
                    is ProgramReturn -> BlockReturn(node.expr)
                    is ProgramIf -> BlockIf(node.condition)
                    else -> error("Unexpected node: $node")
                }
                nodeToStmt[node] = stmt
                currentBlock += stmt
            }

            if (node is ProgramIf) {
                block.statements.forEach { stmtToBlock[it] = block.id }
                val ifBlock = block
                block = newBlock()

                val thenBlocks = buildBlocks(node.thenBranch)
                val elseBlocks = buildBlocks(node.elseBranch)

                when {
                    thenBlocks != null && elseBlocks != null -> {
                        val (thenStart, thenEnd) = thenBlocks
                        val (elseStart, elseEnd) = elseBlocks
                        successors[ifBlock.id] = listOf(thenStart, elseStart) // (true, false) branches
                        when (blocks[thenEnd].statements.lastOrNull()) {
                            is BlockReturn -> {}
                            is BlockIf -> error("Unexpected if statement at the end of the block")
                            else -> successors[thenEnd] = listOf(block.id)
                        }
                        when (blocks[elseEnd].statements.lastOrNull()) {
                            is BlockReturn -> {}
                            is BlockIf -> error("Unexpected if statement at the end of the block")
                            else -> successors[elseEnd] = listOf(block.id)
                        }
                    }

                    thenBlocks != null -> {
                        val (thenStart, thenEnd) = thenBlocks
                        successors[ifBlock.id] = listOf(thenStart, block.id) // (true, false) branches
                        when (blocks[thenEnd].statements.lastOrNull()) {
                            is BlockReturn -> {}
                            is BlockIf -> error("Unexpected if statement at the end of the block")
                            else -> successors[thenEnd] = listOf(block.id)
                        }
                    }

                    elseBlocks != null -> {
                        val (elseStart, elseEnd) = elseBlocks
                        successors[ifBlock.id] = listOf(block.id, elseStart) // (true, false) branches
                        when (blocks[elseEnd].statements.lastOrNull()) {
                            is BlockReturn -> {}
                            is BlockIf -> error("Unexpected if statement at the end of the block")
                            else -> successors[elseEnd] = listOf(block.id)
                        }
                    }

                    else -> {
                        successors[ifBlock.id] = listOf(block.id)
                    }
                }
            } else if (node is ProgramGoto) {
                val targetNode = labelToNode[node.targetLabel] ?: error("Unknown label: ${node.targetLabel}")
                val target = nodeToStmt[targetNode] ?: error("No statement for $targetNode")
                val targetBlockId = stmtToBlock[target] ?: error("No block for $target")
                successors[block.id] = listOf(targetBlockId)
                block.statements.forEach { stmtToBlock[it] = block.id }
                block = newBlock()
            } else if (node is ProgramReturn) {
                successors[block.id] = emptyList()
                break
            }
        }

        block.statements.forEach { stmtToBlock[it] = block.id }
        val lastBlockId = block.id

        return Pair(firstBlockId, lastBlockId)
    }

    buildBlocks(nodes)

    return BlockCfg(blocks, successors)
}

private fun BlockStmt.toDotLabel() = when (this) {
    is BlockAssign -> "$target := $expr"
    is BlockReturn -> "return $expr"
    is BlockIf -> "if ($condition)"
    is BlockNop -> "nop"
}

fun BlockCfg.toDot(): String {
    val lines = mutableListOf<String>()
    lines += "digraph cfg {"
    lines += "  node [shape=rect fontname=\"monospace\"]"

    // Nodes
    for (block in blocks) {
        val s = block.statements.joinToString("") { it.toDotLabel() + "\\l" }
        lines += "  ${block.id} [label=\"Block #\\N\\n${s}\"]"
    }

    // Edges
    for (block in blocks) {
        val succs = successors[block.id] ?: error("No successors for block ${block.id}")
        if (succs.isEmpty()) continue
        if (succs.size == 1) {
            lines += "  ${block.id} -> ${succs.single()}"
        } else {
            check(succs.size == 2)
            val (trueBranch, falseBranch) = succs // Note the order of successors: (true, false) branches
            lines += "  ${block.id} -> $trueBranch [label=\"true\"]"
            lines += "  ${block.id} -> $falseBranch [label=\"false\"]"
        }
    }

    lines += "}"
    return lines.joinToString("\n")
}

typealias StmtLocation = Int

sealed interface Stmt {
    val location: StmtLocation
}

data class NopStmt(
    override val location: StmtLocation,
) : Stmt

data class AssignStmt(
    override val location: StmtLocation,
    val target: Local,
    val expr: Expr,
) : Stmt

data class ReturnStmt(
    override val location: StmtLocation,
    val expr: Expr,
) : Stmt

data class IfStmt(
    override val location: StmtLocation,
    val condition: Expr,
) : Stmt

data class LinearizedCfg(
    val statements: List<Stmt>,
    val successors: Map<StmtLocation, List<StmtLocation>>,
)

fun BlockCfg.linearize(): LinearizedCfg {
    val linearized: MutableList<Stmt> = mutableListOf()
    val successors: MutableMap<StmtLocation, List<StmtLocation>> = hashMapOf()

    fun process(statements: List<BlockStmt>): List<Stmt> {
        val processed: MutableList<Stmt> = mutableListOf()
        var loc = linearized.size
        for (stmt in statements) {
            when (stmt) {
                is BlockNop -> {
                    // Note: ignore NOP statements
                    // processed += NopStmt(loc++)
                }

                is BlockAssign -> {
                    // TODO: three-address code
                    processed += AssignStmt(loc++, stmt.target, stmt.expr)
                }

                is BlockReturn -> {
                    // TODO: three-address code
                    processed += ReturnStmt(loc++, stmt.expr)
                }

                is BlockIf -> {
                    // TODO: three-address code
                    processed += IfStmt(loc++, stmt.condition)
                }
            }
        }
        if (processed.isEmpty()) {
            processed += NopStmt(loc++)
        }
        linearized += processed
        check(linearized.size == loc)
        return processed
    }

    val linearizedBlocks = blocks.associate { it.id to process(it.statements) }

    for ((id, statements) in linearizedBlocks) {
        for ((stmt, next) in statements.zipWithNext()) {
            check(next !is ReturnStmt) { "Return statement in the middle of the block: $next" }
            check(stmt !is IfStmt) { "If statement in the middle of the block: $stmt" }
            successors[stmt.location] = listOf(next.location)
        }
        if (statements.isNotEmpty()) {
            val last = statements.last()
            val nextBlocks = this@linearize.successors[id] ?: error("No successors for block $id")
            // TODO: handle empty blocks (next)
            successors[last.location] = nextBlocks.map { linearizedBlocks.getValue(it).first().location }
        }
    }

    return LinearizedCfg(linearized, successors)
}

private fun Stmt.toDotLabel() = when (this) {
    is NopStmt -> "nop"
    is AssignStmt -> "$target := $expr"
    is ReturnStmt -> "return $expr"
    is IfStmt -> "if ($condition)"
}

fun LinearizedCfg.toDot(): String {
    val lines = mutableListOf<String>()
    lines += "digraph cfg {"
    lines += "  node [shape=rect fontname=\"monospace\"]"

    // Nodes
    for (stmt in statements) {
        lines += "  ${stmt.location} [label=\"\\N: ${stmt.toDotLabel()}\"]"
    }

    // Edges
    for (stmt in statements) {
        when (stmt) {
            is IfStmt -> {
                val succs = successors[stmt.location] ?: error("No successors for $stmt")
                check(succs.size == 2) {
                    "Expected two successors for $stmt, but it has ${succs.size}: $succs"
                }
                val (thenBranch, elseBranch) = succs
                lines += "  ${stmt.location} -> $thenBranch [label=\"then\"]"
                lines += "  ${stmt.location} -> $elseBranch [label=\"else\"]"
            }

            else -> {
                val succs = successors[stmt.location] ?: error("No successors for $stmt")
                if (succs.isNotEmpty()) {
                    check(succs.size == 1) {
                        "Expected one successor for $stmt, but it has ${succs.size}: $succs"
                    }
                    val target = succs.single()
                    lines += "  ${stmt.location} -> $target"
                }
            }
        }
    }

    lines += "}"
    return lines.joinToString("\n")
}

private fun main() {
    val prog = program {
        assign(local("i"), param(0))

        ifStmt(gt(local("i"), const(10.0))) {
            ifStmt(eq(local("i"), const(42.0))) {
                ret(local("i"))
                `else` {
                    assign(local("i"), const(10.0))
                }
            }
            nop()
        }

        label("loop")
        ifStmt(gt(local("i"), const(0.0))) {
            assign(local("i"), sub(local("i"), const(1.0)))
            goto("loop")
            `else` {
                ret(local("i"))
            }
        }

        ret(const(42.0)) // unreachable
    }

    val doView = false

    println("PROGRAM:")
    println("-----")
    println(prog.toText())
    println("-----")

    println("=== PROGRAM:")
    println(prog.toDot())
    if (doView) view(prog.toDot(), name = "program")

    val blockCfg = prog.toBlockCfg()
    println("=== BLOCK CFG:")
    println(blockCfg.toDot())
    if (doView) view(blockCfg.toDot(), name = "block")

    val linearCfg = blockCfg.linearize()
    println("=== LINEARIZED CFG:")
    println(linearCfg.toDot())
    if (doView) view(linearCfg.toDot(), name = "linear")
}

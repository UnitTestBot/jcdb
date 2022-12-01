package org.utbot.jcdb.api.cfg.ext

import info.leadinglight.jdot.Edge
import info.leadinglight.jdot.Graph
import info.leadinglight.jdot.Node
import info.leadinglight.jdot.enums.Color
import info.leadinglight.jdot.enums.Shape
import info.leadinglight.jdot.impl.Util
import org.utbot.jcdb.api.cfg.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

fun JcGraph.view(dotCmd: String, viewerCmd: String, viewCatchConnections: Boolean = false) {
    Util.sh(arrayOf(viewerCmd, "file://${toFile(dotCmd, viewCatchConnections)}"))
}

fun JcGraph.toFile(dotCmd: String, viewCatchConnections: Boolean = false): Path {
    Graph.setDefaultCmd(dotCmd)

    val graph = Graph("jcGraph")

    val nodes = mutableMapOf<JcInst, Node>()
    for ((index, inst) in instructions.withIndex()) {
        val node = Node("$index")
            .setShape(Shape.box)
            .setLabel(inst.toString().replace("\"", "\\\""))
            .setFontSize(12.0)
        nodes[inst] = node
        graph.addNode(node)
    }

    graph.setBgColor(Color.X11.transparent)
    graph.setFontSize(12.0)
    graph.setFontName("Fira Mono")

    for ((inst, node) in nodes) {
        when (inst) {
            is JcGotoInst -> for (successor in successors(inst)) {
                graph.addEdge(Edge(node.name, nodes[successor]!!.name))
            }

            is JcIfInst -> {
                graph.addEdge(
                    Edge(node.name, nodes[inst(inst.trueBranch)]!!.name)
                        .also {
                            it.setLabel("true")
                        }
                )
                graph.addEdge(
                    Edge(node.name, nodes[inst(inst.falseBranch)]!!.name)
                        .also {
                            it.setLabel("false")
                        }
                )
            }

            is JcSwitchInst -> {
                for ((key, branch) in inst.branches) {
                    graph.addEdge(
                        Edge(node.name, nodes[inst(branch)]!!.name)
                            .also {
                                it.setLabel("$key")
                            }
                    )
                }
                graph.addEdge(
                    Edge(node.name, nodes[inst(inst.default)]!!.name)
                        .also {
                            it.setLabel("else")
                        }
                )
            }

            else -> for (successor in successors(inst)) {
                graph.addEdge(Edge(node.name, nodes[successor]!!.name))
            }
        }
        if (viewCatchConnections) {
            for (catcher in catchers(inst)) {
                graph.addEdge(Edge(node.name, nodes[catcher]!!.name).also {
                    it.setLabel("catch ${catcher.throwable.type}")
                })
            }
        }
    }

    val file = graph.dot2file("svg")
    val newFile = "${file.removeSuffix("out")}svg"
    val resultingFile = File(newFile).toPath()
    Files.move(File(file).toPath(), resultingFile)
    return resultingFile
}


fun JcBlockGraph.view(dotCmd: String, viewerCmd: String) {
    Util.sh(arrayOf(viewerCmd, "file://${toFile(dotCmd)}"))
}

fun JcBlockGraph.toFile(dotCmd: String): Path {
    Graph.setDefaultCmd(dotCmd)

    val graph = Graph("jcGraph")

    val nodes = mutableMapOf<JcBasicBlock, Node>()
    for ((index, block) in basicBlocks.withIndex()) {
        val node = Node("$index")
            .setShape(Shape.box)
            .setLabel(instructions(block).joinToString("") { "$it\\l" }.replace("\"", "\\\"").replace("\n", "\\n"))
            .setFontSize(12.0)
        nodes[block] = node
        graph.addNode(node)
    }

    graph.setBgColor(Color.X11.transparent)
    graph.setFontSize(12.0)
    graph.setFontName("Fira Mono")

    for ((block, node) in nodes) {
        val terminatingInst = instructions(block).last()
        val successors = successors(block)
        when (terminatingInst) {
            is JcGotoInst -> for (successor in successors) {
                graph.addEdge(Edge(node.name, nodes[successor]!!.name))
            }

            is JcIfInst -> {
                graph.addEdge(
                    Edge(node.name, nodes[successors.first { it.start == terminatingInst.trueBranch }]!!.name)
                        .also {
                            it.setLabel("true")
                        }
                )
                graph.addEdge(
                    Edge(node.name, nodes[successors.first { it.start == terminatingInst.falseBranch }]!!.name)
                        .also {
                            it.setLabel("false")
                        }
                )
            }

            is JcSwitchInst -> {
                for ((key, branch) in terminatingInst.branches) {
                    graph.addEdge(
                        Edge(node.name, nodes[successors.first { it.start == branch }]!!.name)
                            .also {
                                it.setLabel("$key")
                            }
                    )
                }
                graph.addEdge(
                    Edge(node.name, nodes[successors.first { it.start == terminatingInst.default }]!!.name)
                        .also {
                            it.setLabel("else")
                        }
                )
            }

            else -> for (successor in successors(block)) {
                graph.addEdge(Edge(node.name, nodes[successor]!!.name))
            }
        }
    }

    val file = graph.dot2file("svg")
    val newFile = "${file.removeSuffix("out")}svg"
    val resultingFile = File(newFile).toPath()
    Files.move(File(file).toPath(), resultingFile)
    return resultingFile
}

fun JcGraph.filter(visitor: JcInstVisitor<Boolean>) =
    JcGraph(classpath, instructions.filter { it.accept(visitor) })

fun JcGraph.filterNot(visitor: JcInstVisitor<Boolean>) =
    JcGraph(classpath, instructions.filterNot { it.accept(visitor) })

fun JcGraph.map(visitor: JcInstVisitor<JcInst>) =
    JcGraph(classpath, instructions.map { it.accept(visitor) })

fun JcGraph.mapNotNull(visitor: JcInstVisitor<JcInst?>) =
    JcGraph(classpath, instructions.mapNotNull { it.accept(visitor) })

fun JcGraph.flatMap(visitor: JcInstVisitor<Collection<JcInst>>) =
    JcGraph(classpath, instructions.flatMap { it.accept(visitor) })

fun JcGraph.apply(visitor: JcInstVisitor<Unit>): JcGraph {
    instructions.forEach { it.accept(visitor) }
    return this
}

fun <R, E, T : JcInstVisitor<E>> JcGraph.applyAndGet(visitor: T, getter: (T) -> R): R {
    instructions.forEach { it.accept(visitor) }
    return getter(visitor)
}

fun <T> JcGraph.collect(visitor: JcInstVisitor<T>): Collection<T> {
    return instructions.map { it.accept(visitor) }
}


fun <R, E, T : JcInstVisitor<E>> JcInst.applyAndGet(visitor: T, getter: (T) -> R): R {
    this.accept(visitor)
    return getter(visitor)
}

fun <R, E, T : JcExprVisitor<E>> JcExpr.applyAndGet(visitor: T, getter: (T) -> R): R {
    this.accept(visitor)
    return getter(visitor)
}

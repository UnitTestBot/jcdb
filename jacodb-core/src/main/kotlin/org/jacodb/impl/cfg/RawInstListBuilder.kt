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

package org.jacodb.impl.cfg

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jacodb.api.jvm.JcMethod
import org.jacodb.api.jvm.JcParameter
import org.jacodb.api.jvm.PredefinedPrimitives
import org.jacodb.api.jvm.TypeName
import org.jacodb.api.jvm.cfg.BsmArg
import org.jacodb.api.jvm.cfg.BsmDoubleArg
import org.jacodb.api.jvm.cfg.BsmFloatArg
import org.jacodb.api.jvm.cfg.BsmHandle
import org.jacodb.api.jvm.cfg.BsmHandleTag
import org.jacodb.api.jvm.cfg.BsmIntArg
import org.jacodb.api.jvm.cfg.BsmLongArg
import org.jacodb.api.jvm.cfg.BsmMethodTypeArg
import org.jacodb.api.jvm.cfg.BsmStringArg
import org.jacodb.api.jvm.cfg.BsmTypeArg
import org.jacodb.api.jvm.cfg.JcInstList
import org.jacodb.api.jvm.cfg.JcRawAddExpr
import org.jacodb.api.jvm.cfg.JcRawAndExpr
import org.jacodb.api.jvm.cfg.JcRawArgument
import org.jacodb.api.jvm.cfg.JcRawArrayAccess
import org.jacodb.api.jvm.cfg.JcRawAssignInst
import org.jacodb.api.jvm.cfg.JcRawBranchingInst
import org.jacodb.api.jvm.cfg.JcRawCallExpr
import org.jacodb.api.jvm.cfg.JcRawCallInst
import org.jacodb.api.jvm.cfg.JcRawCastExpr
import org.jacodb.api.jvm.cfg.JcRawCatchEntry
import org.jacodb.api.jvm.cfg.JcRawCatchInst
import org.jacodb.api.jvm.cfg.JcRawClassConstant
import org.jacodb.api.jvm.cfg.JcRawCmpExpr
import org.jacodb.api.jvm.cfg.JcRawCmpgExpr
import org.jacodb.api.jvm.cfg.JcRawCmplExpr
import org.jacodb.api.jvm.cfg.JcRawConditionExpr
import org.jacodb.api.jvm.cfg.JcRawDivExpr
import org.jacodb.api.jvm.cfg.JcRawDynamicCallExpr
import org.jacodb.api.jvm.cfg.JcRawEnterMonitorInst
import org.jacodb.api.jvm.cfg.JcRawEqExpr
import org.jacodb.api.jvm.cfg.JcRawExitMonitorInst
import org.jacodb.api.jvm.cfg.JcRawExpr
import org.jacodb.api.jvm.cfg.JcRawFieldRef
import org.jacodb.api.jvm.cfg.JcRawGeExpr
import org.jacodb.api.jvm.cfg.JcRawGotoInst
import org.jacodb.api.jvm.cfg.JcRawGtExpr
import org.jacodb.api.jvm.cfg.JcRawIfInst
import org.jacodb.api.jvm.cfg.JcRawInst
import org.jacodb.api.jvm.cfg.JcRawInstanceOfExpr
import org.jacodb.api.jvm.cfg.JcRawInterfaceCallExpr
import org.jacodb.api.jvm.cfg.JcRawLabelInst
import org.jacodb.api.jvm.cfg.JcRawLabelRef
import org.jacodb.api.jvm.cfg.JcRawLeExpr
import org.jacodb.api.jvm.cfg.JcRawLengthExpr
import org.jacodb.api.jvm.cfg.JcRawLineNumberInst
import org.jacodb.api.jvm.cfg.JcRawLocalVar
import org.jacodb.api.jvm.cfg.JcRawLtExpr
import org.jacodb.api.jvm.cfg.JcRawMethodConstant
import org.jacodb.api.jvm.cfg.JcRawMethodType
import org.jacodb.api.jvm.cfg.JcRawMulExpr
import org.jacodb.api.jvm.cfg.JcRawNegExpr
import org.jacodb.api.jvm.cfg.JcRawNeqExpr
import org.jacodb.api.jvm.cfg.JcRawNewArrayExpr
import org.jacodb.api.jvm.cfg.JcRawNewExpr
import org.jacodb.api.jvm.cfg.JcRawNullConstant
import org.jacodb.api.jvm.cfg.JcRawOrExpr
import org.jacodb.api.jvm.cfg.JcRawRemExpr
import org.jacodb.api.jvm.cfg.JcRawReturnInst
import org.jacodb.api.jvm.cfg.JcRawShlExpr
import org.jacodb.api.jvm.cfg.JcRawShrExpr
import org.jacodb.api.jvm.cfg.JcRawSimpleValue
import org.jacodb.api.jvm.cfg.JcRawSpecialCallExpr
import org.jacodb.api.jvm.cfg.JcRawStaticCallExpr
import org.jacodb.api.jvm.cfg.JcRawStringConstant
import org.jacodb.api.jvm.cfg.JcRawSubExpr
import org.jacodb.api.jvm.cfg.JcRawSwitchInst
import org.jacodb.api.jvm.cfg.JcRawThis
import org.jacodb.api.jvm.cfg.JcRawThrowInst
import org.jacodb.api.jvm.cfg.JcRawUshrExpr
import org.jacodb.api.jvm.cfg.JcRawValue
import org.jacodb.api.jvm.cfg.JcRawVirtualCallExpr
import org.jacodb.api.jvm.cfg.JcRawXorExpr
import org.jacodb.impl.cfg.util.CLASS_CLASS
import org.jacodb.impl.cfg.util.ExprMapper
import org.jacodb.impl.cfg.util.METHOD_HANDLES_CLASS
import org.jacodb.impl.cfg.util.METHOD_HANDLES_LOOKUP_CLASS
import org.jacodb.impl.cfg.util.METHOD_HANDLE_CLASS
import org.jacodb.impl.cfg.util.METHOD_TYPE_CLASS
import org.jacodb.impl.cfg.util.NULL
import org.jacodb.impl.cfg.util.OBJECT_CLASS
import org.jacodb.impl.cfg.util.STRING_CLASS
import org.jacodb.impl.cfg.util.THROWABLE_CLASS
import org.jacodb.impl.cfg.util.TOP
import org.jacodb.impl.cfg.util.UNINIT_THIS
import org.jacodb.impl.cfg.util.asArray
import org.jacodb.impl.cfg.util.elementType
import org.jacodb.impl.cfg.util.isArray
import org.jacodb.impl.cfg.util.isDWord
import org.jacodb.impl.cfg.util.isPrimitive
import org.jacodb.impl.cfg.util.typeName
import org.jacodb.impl.types.TypeNameImpl
import org.objectweb.asm.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import org.objectweb.asm.tree.TryCatchBlockNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode
import java.util.*

const val LOCAL_VAR_START_CHARACTER = '%'

private fun Int.toPrimitiveType(): TypeName = when (this) {
    Opcodes.T_CHAR -> PredefinedPrimitives.Char
    Opcodes.T_BOOLEAN -> PredefinedPrimitives.Boolean
    Opcodes.T_BYTE -> PredefinedPrimitives.Byte
    Opcodes.T_DOUBLE -> PredefinedPrimitives.Double
    Opcodes.T_FLOAT -> PredefinedPrimitives.Float
    Opcodes.T_INT -> PredefinedPrimitives.Int
    Opcodes.T_LONG -> PredefinedPrimitives.Long
    Opcodes.T_SHORT -> PredefinedPrimitives.Short
    else -> error("Unknown primitive type opcode: $this")
}.typeName()

private fun parsePrimitiveType(opcode: Int) = when (opcode) {
    0 -> TOP
    1 -> PredefinedPrimitives.Int.typeName()
    2 -> PredefinedPrimitives.Float.typeName()
    3 -> PredefinedPrimitives.Double.typeName()
    4 -> PredefinedPrimitives.Long.typeName()
    5 -> NULL
    6 -> UNINIT_THIS
    else -> error("Unknown opcode in primitive type parsing: $opcode")
}

private fun parseBsmHandleTag(tag: Int): BsmHandleTag = when (tag) {
    Opcodes.H_GETFIELD -> BsmHandleTag.FieldHandle.GET_FIELD
    Opcodes.H_GETSTATIC -> BsmHandleTag.FieldHandle.GET_STATIC
    Opcodes.H_PUTFIELD -> BsmHandleTag.FieldHandle.PUT_FIELD
    Opcodes.H_PUTSTATIC -> BsmHandleTag.FieldHandle.PUT_STATIC

    Opcodes.H_INVOKEVIRTUAL -> BsmHandleTag.MethodHandle.INVOKE_VIRTUAL
    Opcodes.H_INVOKESTATIC -> BsmHandleTag.MethodHandle.INVOKE_STATIC
    Opcodes.H_INVOKESPECIAL -> BsmHandleTag.MethodHandle.INVOKE_SPECIAL
    Opcodes.H_NEWINVOKESPECIAL -> BsmHandleTag.MethodHandle.NEW_INVOKE_SPECIAL
    Opcodes.H_INVOKEINTERFACE -> BsmHandleTag.MethodHandle.INVOKE_INTERFACE

    else -> error("Unknown tag in BSM handle: $tag")
}

private fun parseType(any: Any): TypeName = when (any) {
    is String -> any.typeName()
    is Int -> parsePrimitiveType(any)
    is LabelNode -> {
        val newNode: TypeInsnNode = any.run {
            var cur: AbstractInsnNode = this
            var typeInsnNode: TypeInsnNode?
            do {
                typeInsnNode = cur.next as? TypeInsnNode
                cur = cur.next
            } while (typeInsnNode == null)
            typeInsnNode
        }
        newNode.desc.typeName()
    }

    else -> error("Unexpected local type $any")
}

private infix fun TypeName.isCompatibleWith(type: TypeName): Boolean {
    val isPrimitiveLeft = isPrimitive
    val isPrimitiveRight = type.isPrimitive
    if (isPrimitiveLeft != isPrimitiveRight) {
        return false
    }
    if (!isPrimitiveLeft) { // both are reference types
        return true
    }
    // both are primitive types
    return isDWord == type.isDWord
}

private val OBJECT_TYPE_NAME = OBJECT_CLASS.typeName()

private fun typeLub(first: TypeName, second: TypeName): TypeName {
    if (first == second) return first

    if (first == TOP || second == TOP) return TOP

    if (first.isPrimitive) {
        return primitiveTypeLub(first, second)
    }

    if (second.isPrimitive) {
        return primitiveTypeLub(second, first)
    }

    return OBJECT_TYPE_NAME
}

private fun primitiveTypeLub(primitiveType: TypeName, other: TypeName): TypeName {
    if (primitiveType == NULL) {
        return if (other.isPrimitive) TOP else other
    }

    if (!other.isPrimitive) return TOP

    if (primitiveType.typeName == PredefinedPrimitives.Int) {
        return other
    }

    if (other.typeName == PredefinedPrimitives.Int) {
        return primitiveType
    }

    return TOP
}

private fun List<*>?.parseLocals(): Array<TypeName?> {
    if (this == null || isEmpty()) return emptyArray()

    var result = arrayOfNulls<TypeName>(16)
    var realSize = 0

    var index = 0
    for (any in this) {
        val type = parseType(any!!)

        result = result.add(index, type)
        realSize = index + 1

        when {
            type.isDWord -> index += 2
            else -> ++index
        }
    }

    return result.copyOf(realSize)
}

private fun List<*>?.parseStack(): List<TypeName> =
    this?.map { parseType(it!!) } ?: emptyList()

private val primitiveWeights = mapOf(
    PredefinedPrimitives.Boolean to 0,
    PredefinedPrimitives.Byte to 1,
    PredefinedPrimitives.Char to 1,
    PredefinedPrimitives.Short to 2,
    PredefinedPrimitives.Int to 3,
    PredefinedPrimitives.Long to 4,
    PredefinedPrimitives.Float to 5,
    PredefinedPrimitives.Double to 6
)

private fun maxOfPrimitiveTypes(first: String, second: String): String {
    val weight1 = primitiveWeights[first] ?: 0
    val weight2 = primitiveWeights[second] ?: 0
    return when {
        weight1 >= weight2 -> first
        else -> second
    }
}

private fun String.lessThen(anotherPrimitive: String): Boolean {
    val weight1 = primitiveWeights[anotherPrimitive] ?: 0
    val weight2 = primitiveWeights[this] ?: 0
    return weight2 <= weight1
}

private val Type.asTypeName: BsmArg
    get() = when (this.sort) {
        Type.VOID -> BsmTypeArg(PredefinedPrimitives.Void.typeName())
        Type.BOOLEAN -> BsmTypeArg(PredefinedPrimitives.Boolean.typeName())
        Type.CHAR -> BsmTypeArg(PredefinedPrimitives.Char.typeName())
        Type.BYTE -> BsmTypeArg(PredefinedPrimitives.Byte.typeName())
        Type.SHORT -> BsmTypeArg(PredefinedPrimitives.Short.typeName())
        Type.INT -> BsmTypeArg(PredefinedPrimitives.Int.typeName())
        Type.FLOAT -> BsmTypeArg(PredefinedPrimitives.Float.typeName())
        Type.LONG -> BsmTypeArg(PredefinedPrimitives.Long.typeName())
        Type.DOUBLE -> BsmTypeArg(PredefinedPrimitives.Double.typeName())
        Type.ARRAY -> BsmTypeArg((elementType.asTypeName as BsmTypeArg).typeName.asArray())
        Type.OBJECT -> BsmTypeArg(className.typeName())
        Type.METHOD -> BsmMethodTypeArg(
            this.argumentTypes.map { (it.asTypeName as BsmTypeArg).typeName },
            (this.returnType.asTypeName as BsmTypeArg).typeName
        )

        else -> error("Unknown type: $this")
    }

private val AbstractInsnNode.isBranchingInst
    get() = when (this) {
        is JumpInsnNode -> true
        is TableSwitchInsnNode -> true
        is LookupSwitchInsnNode -> true
        is InsnNode -> opcode == Opcodes.ATHROW
        else -> false
    }

private val AbstractInsnNode.isTerminateInst
    get() = this is InsnNode && (this.opcode == Opcodes.ATHROW || this.opcode in Opcodes.IRETURN..Opcodes.RETURN)

private val TryCatchBlockNode.typeOrDefault get() = this.type ?: THROWABLE_CLASS

private val Collection<TryCatchBlockNode>.commonTypeOrDefault
    get() = map { it.type }
        .distinct()
        .singleOrNull()
        ?: THROWABLE_CLASS

internal fun <K, V> identityMap(): MutableMap<K, V> = IdentityHashMap()

internal fun <K, V> Map<out K, V>.toIdentityMap(): Map<K, V> = toMap()

class RawInstListBuilder(
    val method: JcMethod,
    private val methodNode: MethodNode,
    private val keepLocalVariableNames: Boolean,
) {
    private val frames = identityMap<AbstractInsnNode, Frame>()
    private val labels = identityMap<LabelNode, JcRawLabelInst>()
    private val generatedLabels = identityMap<AbstractInsnNode, MutableMap<Int, JcRawLabelInst>>()
    private val generatedVars = identityMap<AbstractInsnNode, MutableList<Any?>>()

    private val ENTRY = InsnNode(-1)

    private val instructions = mutableListOf<AbstractInsnNode?>()
    private val instructionIndex = identityMap<AbstractInsnNode, Int>()

    private val tryCatchHandlers = identityMap<AbstractInsnNode, MutableList<TryCatchBlockNode>>()
    private val predecessors = identityMap<AbstractInsnNode, MutableList<AbstractInsnNode>>()
    private val instructionLists = identityMap<AbstractInsnNode, MutableList<JcRawInst>>()
    private val localTypeRefinement = hashMapOf<JcRawLocalVar, JcRawLocalVar>()
    private val blackListForTypeRefinement = listOf(TOP, NULL, UNINIT_THIS)

    private val localMergeAssignments = identityMap<AbstractInsnNode, MutableMap<Int, JcRawSimpleValue>>()
    private val stackMergeAssignments = identityMap<AbstractInsnNode, MutableMap<Int, JcRawSimpleValue>>()

    private var argCounter = 0
    private var generatedLocalVarsCounter = 0

    fun build(): JcInstList<JcRawInst> {
        buildGraph()

        buildInstructions()
        buildRequiredAssignments()
        buildRequiredGotos()

        val generatedInstructions = mutableListOf<JcRawInst>()
        instructionLists[ENTRY]?.let { generatedInstructions += it }
        for (inst in instructions) {
            if (inst == null) continue
            instructionLists[inst]?.let { generatedInstructions += it }
        }
        generatedInstructions.ensureFirstInstIsLineNumber()

        val originalInstructionList = JcInstListImpl(generatedInstructions)

        // after all the frame info resolution we can refine type info for some local variables,
        // so we replace all the old versions of the variables with the type refined ones
        @Suppress("UNCHECKED_CAST")
        val localTypeRefinementExprMap = localTypeRefinement as Map<JcRawExpr, JcRawExpr>
        val localsNormalizedInstructionList = originalInstructionList.map(ExprMapper(localTypeRefinementExprMap))

        val result = Simplifier().simplify(method.enclosingClass.classpath, localsNormalizedInstructionList)
        return result
    }

    private fun MutableList<JcRawInst>.ensureFirstInstIsLineNumber() {
        val firstLineNumber = indexOfFirst { it is JcRawLineNumberInst }
        if (firstLineNumber == -1 || firstLineNumber == 0) return
        if (firstLineNumber == 1 && this[0] is JcRawLabelInst) return

        val lineNumberInst = this[firstLineNumber] as JcRawLineNumberInst
        val label = generateFreshLabel()
        val lineNumberWithLabel = JcRawLineNumberInst(lineNumberInst.owner, lineNumberInst.lineNumber, label.ref)
        addAll(0, listOf(label, lineNumberWithLabel))
    }

    private fun nodeId(node: AbstractInsnNode): Int =
        if (node === ENTRY) 0 else (instructionIndex[node]!! + 1)

    private fun nodeIdsCount(): Int = instructionIndex.size + 1

    private fun BitSet.add(idx: Int): BitSet {
        if (get(idx)) return this

        val result = clone() as BitSet
        result.set(idx)
        return result
    }

    private fun BitSet.union(other: BitSet): BitSet {
        val result = clone() as BitSet
        result.or(other)
        return result
    }

    private fun BitSet.contains(other: BitSet): Boolean {
        val otherCopy = other.clone() as BitSet
        otherCopy.andNot(this)
        return otherCopy.isEmpty
    }

    private fun findBackEdges(
        successors: Map<AbstractInsnNode, List<AbstractInsnNode>>
    ): Map<AbstractInsnNode, Map<AbstractInsnNode, Unit>> {
        val backEdges = identityMap<AbstractInsnNode, MutableMap<AbstractInsnNode, Unit>>()

        val initialPath = BitSet().add(nodeId(ENTRY))
        val nodeState = identityMap<AbstractInsnNode, BitSet>()

        val unprocessed = mutableListOf<Pair<AbstractInsnNode, BitSet>>()
        unprocessed.add(ENTRY to initialPath)

        while (unprocessed.isNotEmpty()) {
            val (node, currentPath) = unprocessed.removeLast()

            val storedPath = nodeState[node]

            if (storedPath != null && storedPath.contains(currentPath)) {
                continue
            }

            val path = (storedPath?.union(currentPath) ?: currentPath).also { nodeState[node] = it }

            for (successor in successors[node].orEmpty()) {
                val updatedPath = path.add(nodeId(successor))
                if (updatedPath !== path) {
                    unprocessed.add(successor to updatedPath)
                    continue
                }

                backEdges.getOrPut(node, ::identityMap)[successor] = Unit
            }
        }

        return backEdges
    }

    private fun topSortNodes(
        successors: Map<AbstractInsnNode, List<AbstractInsnNode>>,
        backEdges: Map<AbstractInsnNode, Map<AbstractInsnNode, Unit>>
    ): List<AbstractInsnNode> {
        val result = mutableListOf<AbstractInsnNode>()

        val visited = IntArray(nodeIdsCount())
        for ((node, nodeSuccessors) in successors) {
            val nodeBackEdges = backEdges[node].orEmpty()
            for (successor in nodeSuccessors) {
                if (nodeBackEdges.contains(successor)) continue

                visited[nodeId(successor)]++
            }
        }

        check(visited[nodeId(ENTRY)] == 0)
        val unprocessed = mutableListOf<AbstractInsnNode>()
        unprocessed.add(ENTRY)

        while (unprocessed.isNotEmpty()) {
            val node = unprocessed.removeLast()
            result.add(node)

            val nodeBackEdges = backEdges[node].orEmpty()
            for (successor in successors[node].orEmpty()) {
                if (nodeBackEdges.contains(successor)) continue

                if (--visited[nodeId(successor)] == 0) {
                    unprocessed.add(successor)
                }
            }
        }

        return result
    }

    private fun buildInstructions() {
        val initialFrame = createInitialFrame()
        frames[ENTRY] = initialFrame

        val successors = identityMap<AbstractInsnNode, MutableList<AbstractInsnNode>>()
        for (node in instructions) {
            if (node == null) continue

            predecessors[node]?.forEach { predecessor ->
                val predecessorSuccessors = successors.getOrPut(predecessor, ::mutableListOf)
                predecessorSuccessors.add(node)
            }
        }

        val backEdges = findBackEdges(successors)

        val orderedInsnNodes = topSortNodes(successors, backEdges)
        for (insn in orderedInsnNodes) {
            if (insn === ENTRY) continue

            check(insn !in frames) { "Visit instruction multiple times" }

            val frame = when (insn) {
                is LabelNode -> buildLabelNode(insn)
                is FrameNode -> buildFrameNode(insn)
                else -> buildSimpleInstruction(insn)
            }

            frames[insn] = frame
        }
    }

    private fun buildSimpleInstruction(insn: AbstractInsnNode): Frame {
        val predecessor = predecessors[insn]?.singleOrNull()
            ?: error("Incorrect simple node predecessor")

        val predecessorFrame = frames[predecessor]
            ?: error("Incorrect frame processing order")

        val builderVars = generatedVars.getOrPut(insn, ::mutableListOf)
        val frameBuilder = SimpleInstBuilder(predecessorFrame, builderVars)

        when (insn) {
            is InsnNode -> buildInsnNode(insn, frameBuilder)
            is FieldInsnNode -> buildFieldInsnNode(insn, frameBuilder)
            is IincInsnNode -> buildIincInsnNode(insn, frameBuilder)
            is IntInsnNode -> buildIntInsnNode(insn, frameBuilder)
            is InvokeDynamicInsnNode -> buildInvokeDynamicInsn(insn, frameBuilder)
            is JumpInsnNode -> buildJumpInsnNode(insn, frameBuilder)
            is LineNumberNode -> buildLineNumberNode(insn, frameBuilder)
            is LdcInsnNode -> buildLdcInsnNode(insn, frameBuilder)
            is LookupSwitchInsnNode -> buildLookupSwitchInsnNode(insn, frameBuilder)
            is MethodInsnNode -> buildMethodInsnNode(insn, frameBuilder)
            is MultiANewArrayInsnNode -> buildMultiANewArrayInsnNode(insn, frameBuilder)
            is TableSwitchInsnNode -> buildTableSwitchInsnNode(insn, frameBuilder)
            is TypeInsnNode -> buildTypeInsnNode(insn, frameBuilder)
            is VarInsnNode -> buildVarInsnNode(insn, frameBuilder)

            else -> error("Unknown insn node ${insn::class}")
        }

        return frameBuilder.currentFrame
    }

    // `localMergeAssignments` and `stackMergeAssignments` are maps of variable assignments
    // that we need to add to the instruction list after the construction process to ensure
    // liveness of the variables on every step of the method. We cannot add them during the construction
    // because some of them are unknown at that stage (e.g. because of loops)
    private fun buildRequiredAssignments() {
        for ((mergeInst, unorderedLocalAssignments) in localMergeAssignments.orderByInst()) {
            if (unorderedLocalAssignments.isEmpty()) continue

            val predecessors = predecessors[mergeInst] ?: continue

            val localAssignments = unorderedLocalAssignments.orderByIndex()

            for (insn in predecessors) {
                val insnList = instructionList(insn)
                val frame = frames[insn] ?: error("No frame for inst")

                for ((variable, value) in localAssignments) {
                    val frameVariable = frame.findLocal(variable)
                    if (frameVariable != null && value != frameVariable) {
                        insertValueAssignment(insn, insnList, value, frameVariable)
                    }
                }
            }
        }

        for ((mergeInst, unorderedStackAssignments) in stackMergeAssignments.orderByInst()) {
            if (unorderedStackAssignments.isEmpty()) continue

            val predecessors = predecessors[mergeInst] ?: continue

            val stackAssignments = unorderedStackAssignments.orderByIndex()

            for (insn in predecessors) {
                val insnList = instructionList(insn)
                val frame = frames[insn] ?: error("No frame for inst")

                for ((index, value) in stackAssignments) {
                    val frameValue = frame.stack[index]
                    if (value != frameValue) {
                        insertValueAssignment(insn, insnList, value, frameValue)
                    }
                }
            }
        }
    }

    private fun <V> Map<AbstractInsnNode, V>.orderByInst() =
        entries.sortedBy { instructionIndex[it.key] ?: -1 }

    private fun <V> Map<Int, V>.orderByIndex() =
        entries.sortedBy { it.key }

    private fun insertValueAssignment(
        insn: AbstractInsnNode,
        insnList: MutableList<JcRawInst>,
        value: JcRawSimpleValue,
        expr: JcRawSimpleValue
    ) {
        val assignment = JcRawAssignInst(method, value, expr)
        if (insn.isTerminateInst) {
            insnList.addInst(assignment, insnList.lastIndex)
        } else if (insn.isBranchingInst) {
            val branchInstIdx = insnList.indexOfFirst { it is JcRawBranchingInst }
            val branchInst = insnList[branchInstIdx] as JcRawBranchingInst
            when (branchInst) {
                is JcRawGotoInst -> {
                    insnList.addInst(assignment, branchInstIdx)
                }

                is JcRawSwitchInst -> {
                    insnList.addInst(assignment, branchInstIdx)
                    if (branchInst.key.dependsOn(value)) {
                        val freshVar = generateFreshLocalVar(branchInst.key.typeName)
                        insnList.addInst(JcRawAssignInst(method, freshVar, branchInst.key), index = 0)
                        insnList[branchInstIdx + 2] = branchInst.copy(key = freshVar)
                    }
                }

                is JcRawIfInst -> {
                    insnList.addInst(assignment, branchInstIdx)
                    if (branchInst.condition.dependsOn(value)) {
                        val freshVar = generateFreshLocalVar(value.typeName)
                        insnList.addInst(JcRawAssignInst(method, freshVar, value), index = 0)

                        val updatedCondition = branchInst.condition.replace(fromValue = value, toValue = freshVar)
                        insnList[branchInstIdx + 2] = branchInst.copy(condition = updatedCondition)
                    }
                }
            }
        } else {
            insnList.addInst(assignment)
        }
    }

    private fun JcRawExpr.dependsOn(value: JcRawSimpleValue): Boolean =
        this == value || operands.any { it.dependsOn(value) }

    private fun JcRawConditionExpr.replace(
        fromValue: JcRawSimpleValue,
        toValue: JcRawSimpleValue
    ): JcRawConditionExpr =
        accept(ExprMapper(mapOf(fromValue to toValue))) as JcRawConditionExpr

    // adds the `goto` instructions to ensure consistency in the instruction list:
    // every jump is show explicitly with some branching instruction
    private fun buildRequiredGotos() {
        for (insn in instructions) {
            if (insn == null) continue

            if (tryCatchHandlers.contains(insn)) continue

            val predecessors = predecessors.getOrDefault(insn, emptyList())
            if (predecessors.size > 1) {
                for (predecessor in predecessors) {
                    if (!predecessor.isBranchingInst) {
                        val label = when (insn) {
                            is LabelNode -> labelRef(insn)
                            else -> {
                                val newLabel = generateFreshLabel()
                                addInstruction(insn, newLabel, 0)
                                newLabel.ref
                            }
                        }
                        addInstruction(predecessor, JcRawGotoInst(method, label))
                    }
                }
            }
        }
    }

    /**
     * represents a frame state: information about types of local variables and stack variables
     * needed to handle ASM FrameNode instructions
     */
    private data class FrameState(
        private val locals: Array<TypeName?>,
        val stack: List<TypeName>,
    ) {
        companion object {
            fun parseNew(insn: FrameNode): FrameState {
                return FrameState(insn.local.parseLocals(), insn.stack.parseStack())
            }
        }

        fun appendFrame(insn: FrameNode): FrameState {
            val lastType = locals.lastOrNull()
            val insertKey = when {
                lastType == null -> 0
                lastType.isDWord -> locals.lastIndex + 2
                else -> locals.lastIndex + 1
            }

            val appendedLocals = insn.local.parseLocals()

            val newLocals = locals.copyOf(insertKey + appendedLocals.size)
            for (index in appendedLocals.indices) {
                newLocals[insertKey + index] = appendedLocals[index]
            }

            return copy(locals = newLocals, stack = emptyList())
        }

        fun dropFrame(inst: FrameNode): FrameState {
            val newLocals = locals.copyOf(locals.size - inst.local.size).trimEndNulls()
            return copy(locals = newLocals, stack = emptyList())
        }

        fun copy0(): FrameState = this.copy(stack = emptyList())

        fun copy1(insn: FrameNode): FrameState {
            val newStack = insn.stack.parseStack()
            return this.copy(stack = newStack)
        }

        fun localsUnsafe(): Array<TypeName?> = locals
    }

    private fun refineWithFrameState(frame: Frame, frameState: FrameState): Frame {
        val localTypes = frameState.localsUnsafe()
        val refinedLocals = Array(localTypes.size) { variable ->
            val type = localTypes[variable]
            if (type == null || type == TOP) return@Array null

            val value = frame.findLocal(variable) ?: return@Array null

            if (value is JcRawLocalVar && value.typeName != type && type !in blackListForTypeRefinement) {
                JcRawLocalVar(value.index, value.name, type).also { newLocal ->
                    localTypeRefinement[value] = newLocal
                }
            } else {
                value
            }
        }

        val stackTypes = frameState.stack
        val refinedStack = frame.stack.withIndex()
            .filter { it.index in stackTypes.indices }
            .map { (index, value) ->
                val type = stackTypes[index]
                if (value is JcRawLocalVar && value.typeName != type && type !in blackListForTypeRefinement) {
                    JcRawLocalVar(value.index, value.name, type).also { newLocal ->
                        localTypeRefinement[value] = newLocal
                    }
                } else value
            }

        return Frame(refinedLocals.trimEndNulls(), refinedStack.toPersistentList())
    }

    /**
     * represents the bytecode Frame: a set of active local variables and stack variables
     * during the execution of the instruction
     */
    private data class Frame(
        val locals: Array<JcRawSimpleValue?>,
        val stack: PersistentList<JcRawSimpleValue>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Frame

            if (!locals.contentEquals(other.locals)) return false
            if (stack != other.stack) return false

            return true
        }

        override fun hashCode(): Int {
            var result = locals.contentHashCode()
            result = 31 * result + stack.hashCode()
            return result
        }

        fun putLocal(variable: Int, value: JcRawSimpleValue): Frame {
            val newLocals = locals.copyOf(maxOf(locals.size, variable + 1))
            newLocals[variable] = value
            return copy(locals = newLocals, stack = stack)
        }

        fun hasLocal(variable: Int): Boolean = findLocal(variable) != null

        fun maxLocal(): Int = locals.lastIndex

        fun findLocal(variable: Int): JcRawSimpleValue? = locals.getOrNull(variable)

        fun getLocal(variable: Int): JcRawSimpleValue = locals.getOrNull(variable)
            ?: error("No local variable $variable")

        fun push(value: JcRawSimpleValue) = copy(locals = locals, stack = stack.add(value))
        fun peek() = stack.last()
        fun pop(): Pair<Frame, JcRawSimpleValue> =
            copy(locals = locals, stack = stack.removeAt(stack.lastIndex)) to stack.last()
    }

    private open inner class InstBuilder(
        val instVars: MutableList<Any?>, // List<null | JcRawLocalVar | mark>
        var currentLocalVarIdx: Int = 0
    ) {
        fun nextRegister(typeName: TypeName): JcRawLocalVar {
            if (currentLocalVarIdx < instVars.size) {
                var currentVar = instVars[currentLocalVarIdx]

                if (currentVar == null) {
                    return generateFreshLocalVar(typeName).also {
                        instVars[currentLocalVarIdx] = it
                        currentLocalVarIdx++
                    }
                }

                check(currentVar is JcRawLocalVar) {
                    "Unexpected generator mark: $currentVar"
                }

                if (currentVar.typeName != typeName) {
                    currentVar = currentVar.copy(typeName = typeName).also { instVars[currentLocalVarIdx] = it }
                }

                currentLocalVarIdx++
                return currentVar
            }

            check(currentLocalVarIdx == instVars.size)
            currentLocalVarIdx++

            val freshVar = generateFreshLocalVar(typeName)
            instVars.add(freshVar)
            return freshVar
        }

        fun skipRegister() {
            if (currentLocalVarIdx < instVars.size) {
                currentLocalVarIdx++
                return
            }

            check(currentLocalVarIdx == instVars.size)
            currentLocalVarIdx++

            instVars.add(null)
        }

        fun skipAllBeforeMark(mark: Any) {
            while (currentLocalVarIdx < instVars.size) {
                if (mark === instVars[currentLocalVarIdx]) {
                    currentLocalVarIdx++
                    return
                }
                currentLocalVarIdx++
            }

            check(currentLocalVarIdx == instVars.size)
            currentLocalVarIdx++

            instVars.add(mark)
        }
    }

    private inner class SimpleInstBuilder(
        var currentFrame: Frame,
        instVars: MutableList<Any?>,
    ) : InstBuilder(instVars)

    private fun SimpleInstBuilder.pop(): JcRawSimpleValue {
        val (frame, value) = currentFrame.pop()
        currentFrame = frame
        return value
    }

    private fun SimpleInstBuilder.push(value: JcRawSimpleValue) {
        currentFrame = currentFrame.push(value)
    }

    private fun SimpleInstBuilder.peek(): JcRawSimpleValue = currentFrame.peek()

    private fun SimpleInstBuilder.local(variable: Int): JcRawSimpleValue {
        return currentFrame.getLocal(variable)
    }

    private fun Frame.valueUsedInStack(value: JcRawSimpleValue): Boolean =
        stack.any { it == value }

    private fun Frame.valueUsedInLocals(value: JcRawSimpleValue, variable: Int): Boolean {
        for (i in locals.indices) {
            if (i == variable) continue
            val localValue = locals[i] ?: continue
            if (localValue == value) return true
        }
        return false
    }

    private fun SimpleInstBuilder.local(
        variable: Int,
        expr: JcRawSimpleValue,
        insn: AbstractInsnNode,
    ): JcRawAssignInst? {
        val oldVar = currentFrame.findLocal(variable)?.let {
            val infoFromLocalVars = findLocalVariableWithInstruction(variable, insn)
            val isArg =
                variable < argCounter && infoFromLocalVars != null && infoFromLocalVars.start == firstLabelOrNull
            if (expr.typeName.isPrimitive.xor(it.typeName.isPrimitive)
                && it.typeName.typeName != PredefinedPrimitives.Null
                && !isArg
            ) {
                null
            } else {
                it
            }
        }
        return if (oldVar != null) {
            if (oldVar is JcRawArgument) {
                currentFrame = currentFrame.putLocal(variable, expr)
                null
            } else if (oldVar.typeName == expr.typeName || (expr is JcRawNullConstant && !oldVar.typeName.isPrimitive)) {
                if (!currentFrame.valueUsedInStack(oldVar) && !currentFrame.valueUsedInLocals(oldVar, variable)) {
                    // optimization: old variable has no other usages. So we can overwrite it
                    JcRawAssignInst(method, oldVar, expr)
                } else {
                    currentFrame = currentFrame.putLocal(variable, expr)
                    null
                }
            } else {
                val assignment = nextRegisterDeclaredVariable(expr.typeName, variable, insn)
                currentFrame = currentFrame.putLocal(variable, assignment)
                JcRawAssignInst(method, assignment, expr)
            }
        } else {
            // We have to get type if rhv expression is NULL
            val typeOfNewAssigment =
                if (expr.typeName.typeName == PredefinedPrimitives.Null) {
                    findLocalVariableWithInstruction(variable, insn)
                        ?.desc?.typeName()
                        ?: currentFrame.findLocal(variable)?.typeName
                        ?: "java.lang.Object".typeName()
                } else {
                    expr.typeName
                }
            val newLocal = nextRegisterDeclaredVariable(typeOfNewAssigment, variable, insn)
            val result = JcRawAssignInst(method, newLocal, expr)
            currentFrame = currentFrame.putLocal(variable, newLocal)
            result
        }
    }

    private fun label(insnNode: LabelNode): JcRawLabelInst = labels[insnNode]
        ?: error("No label for: $insnNode")

    private fun labelRef(insnNode: LabelNode): JcRawLabelRef = label(insnNode).ref

    private var generatedLabelIndex = 0
    private fun generateFreshLabel(): JcRawLabelInst {
        return JcRawLabelInst(method, "#${generatedLabelIndex++}")
    }

    private fun generateLabel(insn: AbstractInsnNode, labelId: Int): JcRawLabelInst {
        val insnLabels = generatedLabels.getOrPut(insn, ::hashMapOf)
        return insnLabels.getOrPut(labelId) { generateFreshLabel() }
    }

    private fun generateFreshLocalVar(typeName: TypeName): JcRawLocalVar {
        val freshVarIdx = generatedLocalVarsCounter++
        return JcRawLocalVar(freshVarIdx, "$LOCAL_VAR_START_CHARACTER${freshVarIdx}", typeName)
    }

    private fun instructionList(insn: AbstractInsnNode) = instructionLists.getOrPut(insn, ::mutableListOf)

    private fun addInstruction(insn: AbstractInsnNode, inst: JcRawInst, index: Int? = null) {
        instructionList(insn).addInst(inst, index)
    }

    private fun MutableList<JcRawInst>.addInst(inst: JcRawInst, index: Int? = null) {
        if (index != null) {
            add(index, inst)
        } else {
            add(inst)
        }
    }

    private fun SimpleInstBuilder.nextRegisterDeclaredVariable(
        typeName: TypeName,
        variable: Int,
        insn: AbstractInsnNode
    ): JcRawSimpleValue {
        val nextLabel = generateSequence(insn) { it.next }
            .filterIsInstance<LabelNode>()
            .firstOrNull()

        val lvNode = methodNode.localVariables
            .singleOrNull { it.index == variable && it.start == nextLabel }

        val declaredTypeName = lvNode?.desc?.typeName()
        val idx = nextRegister(typeName).index
        val lvName = lvNode?.name?.takeIf { keepLocalVariableNames } ?: "$LOCAL_VAR_START_CHARACTER$idx"

        return if (declaredTypeName != null && !declaredTypeName.isPrimitive && !typeName.isArray) {
            JcRawLocalVar(idx, lvName, declaredTypeName)
        } else {
            JcRawLocalVar(idx, lvName, typeName)
        }
    }

    private fun buildGraph() {
        val instructions = methodNode.instructions.toArray()
        instructions.firstOrNull()?.let {
            predecessors.getOrPut(it, ::mutableListOf).add(ENTRY)
        }
        for (insn in instructions) {
            if (insn is LabelNode) {
                labels[insn] = generateFreshLabel()
            }

            if (insn is JumpInsnNode) {
                predecessors.getOrPut(insn.label, ::mutableListOf).add(insn)
                if (insn.opcode != Opcodes.GOTO) {
                    predecessors.getOrPut(insn.next, ::mutableListOf).add(insn)
                }
            } else if (insn is TableSwitchInsnNode) {
                predecessors.getOrPut(insn.dflt, ::mutableListOf).add(insn)
                insn.labels.forEach {
                    predecessors.getOrPut(it, ::mutableListOf).add(insn)
                }
            } else if (insn is LookupSwitchInsnNode) {
                predecessors.getOrPut(insn.dflt, ::mutableListOf).add(insn)
                insn.labels.forEach {
                    predecessors.getOrPut(it, ::mutableListOf).add(insn)
                }
            } else if (insn.isTerminateInst) {
                continue
            } else if (insn.next != null) {
                predecessors.getOrPut(insn.next, ::mutableListOf).add(insn)
            }
        }

        for (tryCatchBlock in methodNode.tryCatchBlocks) {
            val handlers = tryCatchHandlers.getOrPut(tryCatchBlock.handler, ::mutableListOf)

            val blockStart = tryCatchBlock.start
            val blockEnd = tryCatchBlock.end
            val handler = tryCatchBlock.handler

            if (blockStart == handler) {
                continue
            }

            handlers.add(tryCatchBlock)

            val handlerPreds = predecessors.getOrPut(handler, ::mutableListOf)

            var current: AbstractInsnNode = blockStart
            while (current != blockEnd) {
                handlerPreds.add(current)
                current = current.next ?: error("Unexpected instruction")
            }

            for (startPredecessor in predecessors[blockStart].orEmpty()) {
                if (startPredecessor.isBetween(blockStart, blockEnd)) continue

                handlerPreds.add(startPredecessor)
            }
        }

        val deadInstructions = mutableSetOf<AbstractInsnNode>()
        for (insn in instructions) {
            val preds = predecessors[insn]
            if ((preds.isNullOrEmpty() || preds.all { it in deadInstructions })) {
                if (insn is LabelNode) {
                    labels.remove(insn)
                }

                deadInstructions += insn
                predecessors -= insn
            }
        }
        if (deadInstructions.isNotEmpty()) {
            predecessors.forEach { (_, preds) ->
                preds.removeAll { it in deadInstructions }
            }
        }

        instructions.mapIndexedTo(this.instructions) { index, insn ->
            instructionIndex[insn] = index
            insn.takeIf { it !in deadInstructions }
        }
    }

    private fun createInitialFrame(): Frame {
        var locals = arrayOfNulls<JcRawSimpleValue>(16)
        var localsRealSize = 0

        argCounter = 0
        var staticInc = 0
        if (!method.isStatic) {
            locals = locals.add(argCounter, thisRef())
            localsRealSize = argCounter + 1

            argCounter++
            staticInc = 1
        }
        val variables = methodNode.localVariables.orEmpty().sortedBy(LocalVariableNode::index)

        fun getName(parameter: JcParameter): String? {
            val idx = parameter.index + staticInc
            return if (idx < variables.size) {
                variables[idx].name
            } else {
                parameter.name
            }
        }

        for (parameter in method.parameters) {
            val argument = JcRawArgument.of(parameter.index, getName(parameter), parameter.type)

            locals = locals.add(argCounter, argument)
            localsRealSize = argCounter + 1

            if (argument.typeName.isDWord) argCounter += 2
            else argCounter++
        }

        return Frame(locals.copyOf(localsRealSize), persistentListOf())
    }

    private fun thisRef() = JcRawThis(method.enclosingClass.name.typeName())

    private fun buildInsnNode(insn: InsnNode, frame: SimpleInstBuilder) = with(frame) {
        when (insn.opcode) {
            Opcodes.NOP -> Unit
            in Opcodes.ACONST_NULL..Opcodes.DCONST_1 -> buildConstant(insn)
            in Opcodes.IALOAD..Opcodes.SALOAD -> buildArrayRead(insn)
            in Opcodes.IASTORE..Opcodes.SASTORE -> buildArrayStore(insn)
            in Opcodes.POP..Opcodes.POP2 -> buildPop(insn)
            in Opcodes.DUP..Opcodes.DUP2_X2 -> buildDup(insn)
            Opcodes.SWAP -> buildSwap()
            in Opcodes.IADD..Opcodes.DREM -> buildBinary(insn)
            in Opcodes.INEG..Opcodes.DNEG -> buildUnary(insn)
            in Opcodes.ISHL..Opcodes.LXOR -> buildBinary(insn)
            in Opcodes.I2L..Opcodes.I2S -> buildCast(insn)
            in Opcodes.LCMP..Opcodes.DCMPG -> buildCmp(insn)
            in Opcodes.IRETURN..Opcodes.RETURN -> buildReturn(insn)
            Opcodes.ARRAYLENGTH -> buildUnary(insn)
            Opcodes.ATHROW -> buildThrow(insn)
            in Opcodes.MONITORENTER..Opcodes.MONITOREXIT -> buildMonitor(insn)
            else -> error("Unknown insn opcode: ${insn.opcode}")
        }
    }

    private fun SimpleInstBuilder.buildConstant(insn: InsnNode) {
        val constant = when (val opcode = insn.opcode) {
            Opcodes.ACONST_NULL -> JcRawNull()
            Opcodes.ICONST_M1 -> JcRawInt(-1)
            in Opcodes.ICONST_0..Opcodes.ICONST_5 -> JcRawInt(opcode - Opcodes.ICONST_0)
            in Opcodes.LCONST_0..Opcodes.LCONST_1 -> JcRawLong((opcode - Opcodes.LCONST_0).toLong())
            in Opcodes.FCONST_0..Opcodes.FCONST_2 -> JcRawFloat((opcode - Opcodes.FCONST_0).toFloat())
            in Opcodes.DCONST_0..Opcodes.DCONST_1 -> JcRawDouble((opcode - Opcodes.DCONST_0).toDouble())
            else -> error("Unknown constant opcode: $opcode")
        }
        push(constant)
    }

    private fun SimpleInstBuilder.buildArrayRead(insn: InsnNode) {
        val index = pop()
        val arrayRef = pop()
        val read = JcRawArrayAccess(arrayRef, index, arrayRef.typeName.elementType())

        val assignment = nextRegister(read.typeName)
        addInstruction(insn, JcRawAssignInst(method, assignment, read))
        push(assignment)
    }

    private fun SimpleInstBuilder.buildArrayStore(insn: InsnNode) {
        val value = pop()
        val index = pop()
        val arrayRef = pop()
        addInstruction(
            insn, JcRawAssignInst(
                method,
                JcRawArrayAccess(arrayRef, index, arrayRef.typeName.elementType()),
                value
            )
        )
    }

    private fun SimpleInstBuilder.buildPop(insn: InsnNode) {
        when (val opcode = insn.opcode) {
            Opcodes.POP -> pop()
            Opcodes.POP2 -> {
                val top = pop()
                if (!top.typeName.isDWord) pop()
            }

            else -> error("Unknown pop opcode: $opcode")
        }
    }

    private fun SimpleInstBuilder.buildDup(insn: InsnNode) {
        when (val opcode = insn.opcode) {
            Opcodes.DUP -> push(peek())
            Opcodes.DUP_X1 -> {
                val top = pop()
                val prev = pop()
                push(top)
                push(prev)
                push(top)
            }

            Opcodes.DUP_X2 -> {
                val val1 = pop()
                val val2 = pop()
                if (val2.typeName.isDWord) {
                    push(val1)
                    push(val2)
                    push(val1)
                } else {
                    val val3 = pop()
                    push(val1)
                    push(val3)
                    push(val2)
                    push(val1)
                }
            }

            Opcodes.DUP2 -> {
                val top = pop()
                if (top.typeName.isDWord) {
                    push(top)
                    push(top)
                } else {
                    val bot = pop()
                    push(bot)
                    push(top)
                    push(bot)
                    push(top)
                }
            }

            Opcodes.DUP2_X1 -> {
                val val1 = pop()
                if (val1.typeName.isDWord) {
                    val val2 = pop()
                    push(val1)
                    push(val2)
                    push(val1)
                } else {
                    val val2 = pop()
                    val val3 = pop()
                    push(val2)
                    push(val1)
                    push(val3)
                    push(val2)
                    push(val1)
                }
            }

            Opcodes.DUP2_X2 -> {
                val val1 = pop()
                if (val1.typeName.isDWord) {
                    val val2 = pop()
                    if (val2.typeName.isDWord) {
                        push(val1)
                        push(val2)
                        push(val1)
                    } else {
                        val val3 = pop()
                        push(val1)
                        push(val3)
                        push(val2)
                        push(val1)
                    }
                } else {
                    val val2 = pop()
                    val val3 = pop()
                    if (val3.typeName.isDWord) {
                        push(val2)
                        push(val1)
                        push(val3)
                        push(val2)
                        push(val1)
                    } else {
                        val val4 = pop()
                        push(val2)
                        push(val1)
                        push(val4)
                        push(val3)
                        push(val2)
                        push(val1)
                    }
                }
            }

            else -> error("Unknown dup opcode: $opcode")
        }
    }

    private fun SimpleInstBuilder.buildSwap() {
        val top = pop()
        val bot = pop()
        push(top)
        push(bot)
    }

    private fun SimpleInstBuilder.buildBinary(insn: InsnNode) {
        val rhv = pop()
        val lhv = pop()
        val resolvedType = resolveType(lhv.typeName, rhv.typeName)
        val expr = when (val opcode = insn.opcode) {
            in Opcodes.IADD..Opcodes.DADD -> JcRawAddExpr(resolvedType, lhv, rhv)
            in Opcodes.ISUB..Opcodes.DSUB -> JcRawSubExpr(resolvedType, lhv, rhv)
            in Opcodes.IMUL..Opcodes.DMUL -> JcRawMulExpr(resolvedType, lhv, rhv)
            in Opcodes.IDIV..Opcodes.DDIV -> JcRawDivExpr(resolvedType, lhv, rhv)
            in Opcodes.IREM..Opcodes.DREM -> JcRawRemExpr(resolvedType, lhv, rhv)
            in Opcodes.ISHL..Opcodes.LSHL -> JcRawShlExpr(resolvedType, lhv, rhv)
            in Opcodes.ISHR..Opcodes.LSHR -> JcRawShrExpr(resolvedType, lhv, rhv)
            in Opcodes.IUSHR..Opcodes.LUSHR -> JcRawUshrExpr(resolvedType, lhv, rhv)
            in Opcodes.IAND..Opcodes.LAND -> JcRawAndExpr(resolvedType, lhv, rhv)
            in Opcodes.IOR..Opcodes.LOR -> JcRawOrExpr(resolvedType, lhv, rhv)
            in Opcodes.IXOR..Opcodes.LXOR -> JcRawXorExpr(resolvedType, lhv, rhv)
            else -> error("Unknown binary opcode: $opcode")
        }
        val assignment = nextRegister(resolvedType)
        addInstruction(insn, JcRawAssignInst(method, assignment, expr))
        push(assignment)
    }

    private fun resolveType(left: TypeName, right: TypeName): TypeName {
        val leftName = left.typeName
        val leftIsPrimitive = PredefinedPrimitives.matches(leftName)
        if (leftIsPrimitive) {
            val rightName = right.typeName
            val max = maxOfPrimitiveTypes(leftName, rightName)
            return when {
                max.lessThen(PredefinedPrimitives.Int) -> TypeNameImpl(PredefinedPrimitives.Int)
                else -> TypeNameImpl(max)
            }
        }
        return left
    }

    private fun SimpleInstBuilder.buildUnary(insn: InsnNode) {
        val operand = pop()
        val expr = when (val opcode = insn.opcode) {
            in Opcodes.INEG..Opcodes.DNEG -> {
                val resolvedType = maxOfPrimitiveTypes(operand.typeName.typeName, PredefinedPrimitives.Int)
                JcRawNegExpr(TypeNameImpl(resolvedType), operand)
            }

            Opcodes.ARRAYLENGTH -> JcRawLengthExpr(PredefinedPrimitives.Int.typeName(), operand)
            else -> error("Unknown unary opcode $opcode")
        }
        val assignment = nextRegister(expr.typeName)
        addInstruction(insn, JcRawAssignInst(method, assignment, expr))
        push(assignment)
    }

    private fun SimpleInstBuilder.buildCast(insn: InsnNode) {
        val operand = pop()
        val targetType = when (val opcode = insn.opcode) {
            Opcodes.I2L, Opcodes.F2L, Opcodes.D2L -> PredefinedPrimitives.Long.typeName()
            Opcodes.I2F, Opcodes.L2F, Opcodes.D2F -> PredefinedPrimitives.Float.typeName()
            Opcodes.I2D, Opcodes.L2D, Opcodes.F2D -> PredefinedPrimitives.Double.typeName()
            Opcodes.L2I, Opcodes.F2I, Opcodes.D2I -> PredefinedPrimitives.Int.typeName()
            Opcodes.I2B -> PredefinedPrimitives.Byte.typeName()
            Opcodes.I2C -> PredefinedPrimitives.Char.typeName()
            Opcodes.I2S -> PredefinedPrimitives.Short.typeName()
            else -> error("Unknown cast opcode $opcode")
        }
        val assignment = nextRegister(targetType)
        addInstruction(insn, JcRawAssignInst(method, assignment, JcRawCastExpr(targetType, operand)))
        push(assignment)
    }

    private fun SimpleInstBuilder.buildCmp(insn: InsnNode) {
        val rhv = pop()
        val lhv = pop()
        val expr = when (val opcode = insn.opcode) {
            Opcodes.LCMP -> JcRawCmpExpr(PredefinedPrimitives.Int.typeName(), lhv, rhv)
            Opcodes.FCMPL, Opcodes.DCMPL -> JcRawCmplExpr(PredefinedPrimitives.Int.typeName(), lhv, rhv)
            Opcodes.FCMPG, Opcodes.DCMPG -> JcRawCmpgExpr(PredefinedPrimitives.Int.typeName(), lhv, rhv)
            else -> error("Unknown cmp opcode $opcode")
        }
        val assignment = nextRegister(PredefinedPrimitives.Int.typeName())
        addInstruction(insn, JcRawAssignInst(method, assignment, expr))
        push(assignment)
    }

    private fun SimpleInstBuilder.buildReturn(insn: InsnNode) {
        addInstruction(
            insn, when (val opcode = insn.opcode) {
                Opcodes.RETURN -> JcRawReturnInst(method, null)
                in Opcodes.IRETURN..Opcodes.ARETURN -> JcRawReturnInst(method, pop())
                else -> error("Unknown return opcode: $opcode")
            }
        )
    }

    private fun SimpleInstBuilder.buildMonitor(insn: InsnNode) {
        val monitor = pop() as JcRawSimpleValue
        addInstruction(
            insn, when (val opcode = insn.opcode) {
                Opcodes.MONITORENTER -> {
                    JcRawEnterMonitorInst(method, monitor)
                }

                Opcodes.MONITOREXIT -> JcRawExitMonitorInst(method, monitor)
                else -> error("Unknown monitor opcode $opcode")
            }
        )
    }

    private fun SimpleInstBuilder.buildThrow(insn: InsnNode) {
        val throwable = pop()
        addInstruction(insn, JcRawThrowInst(method, throwable))
    }

    private fun buildFieldInsnNode(insnNode: FieldInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val fieldName = insnNode.name
        val fieldType = insnNode.desc.typeName()
        val declaringClass = insnNode.owner.typeName()
        when (insnNode.opcode) {
            Opcodes.GETFIELD -> {
                val assignment = nextRegister(fieldType)
                val field = JcRawFieldRef(frame.pop(), declaringClass, fieldName, fieldType)
                addInstruction(insnNode, JcRawAssignInst(method, assignment, field))
                frame.push(assignment)
            }

            Opcodes.PUTFIELD -> {
                val value = frame.pop()
                val instance = frame.pop()
                val fieldRef = JcRawFieldRef(instance, declaringClass, fieldName, fieldType)
                addInstruction(insnNode, JcRawAssignInst(method, fieldRef, value))
            }

            Opcodes.GETSTATIC -> {
                val assignment = nextRegister(fieldType)
                val field = JcRawFieldRef(declaringClass, fieldName, fieldType)
                addInstruction(insnNode, JcRawAssignInst(method, assignment, field))
                frame.push(assignment)
            }

            Opcodes.PUTSTATIC -> {
                val value = frame.pop()
                val fieldRef = JcRawFieldRef(declaringClass, fieldName, fieldType)
                addInstruction(insnNode, JcRawAssignInst(method, fieldRef, value))
            }
        }
    }

//    private fun copyLocalFromMethodArguments(curLabel: LabelNode, variable: Int, type: TypeName): JcRawSimpleValue? {
//        val actualLocalFromDebugInfo = findLocalVariableWithInstruction(variable, curLabel)
//        val isArg = if (actualLocalFromDebugInfo == null) {
//            variable < argCounter
//        } else {
//            actualLocalFromDebugInfo.start == firstLabelOrNull
//        }
//
//        if (variable < argCounter && isArg) {
//            frames.values.forEach {
//                val value = it.findLocal(variable)
//                if (value != null && ((value is JcRawArgument && value.typeName isCompatibleWith type) || value is JcRawThis)) {
//                    return value
//                }
//            }
//        }
//        return null
//    }

    private val firstLabelOrNull: AbstractInsnNode? get() = instructions.firstOrNull { it is LabelNode }

    private fun buildFrameNode(insnNode: FrameNode): Frame {
        val lastFrameState = when (insnNode.type) {
            Opcodes.F_NEW -> FrameState.parseNew(insnNode)
            Opcodes.F_FULL -> FrameState.parseNew(insnNode)

            // todo: complex frame nodes
            Opcodes.F_APPEND,
            Opcodes.F_CHOP,
            Opcodes.F_SAME,
            Opcodes.F_SAME1 -> null

            else -> error("Unknown frame node type: ${insnNode.type}")
        }

        val predecessor = predecessors[insnNode]?.singleOrNull()
            ?: error("Incorrect frame node predecessor")

        val predecessorFrame = frames[predecessor]
            ?: error("Incorrect frame processing order")

        if (lastFrameState == null) {
            return predecessorFrame
        }

        return refineWithFrameState(predecessorFrame, lastFrameState)
    }

    private fun buildIincInsnNode(insnNode: IincInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val variable = insnNode.`var`
        val local = local(variable)
        val rhv = JcRawInt(insnNode.incr)

        val resolvedType = resolveType(local.typeName, rhv.typeName)
        val expr = JcRawAddExpr(resolvedType, local, rhv)
        val assignment = nextRegister(resolvedType)
        addInstruction(insnNode, JcRawAssignInst(method, assignment, expr))

        val localAssign = local(variable, assignment, insnNode)
        if (localAssign != null) {
            addInstruction(insnNode, localAssign)
        }
    }

    private fun buildIntInsnNode(insnNode: IntInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val operand = insnNode.operand
        when (val opcode = insnNode.opcode) {
            Opcodes.BIPUSH -> push(JcRawInt(operand))
            Opcodes.SIPUSH -> push(JcRawInt(operand))
            Opcodes.NEWARRAY -> {
                val expr = JcRawNewArrayExpr(operand.toPrimitiveType().asArray(), pop())
                val assignment = nextRegister(expr.typeName)
                addInstruction(insnNode, JcRawAssignInst(method, assignment, expr))
                push(assignment)
            }

            else -> error("Unknown int insn opcode: $opcode")
        }
    }

    private val Handle.bsmHandleArg
        get() = BsmHandle(
            parseBsmHandleTag(tag),
            owner.typeName(),
            name,
            if (desc.contains("(")) {
                Type.getArgumentTypes(desc).map { it.descriptor.typeName() }
            } else {
                listOf()
            },
            if (desc.contains("(")) {
                Type.getReturnType(desc).descriptor.typeName()
            } else {
                Type.getReturnType("(;)$desc").descriptor.typeName()
            },
            isInterface
        )

    private fun bsmNumberArg(number: Number) = when (number) {
        is Int -> BsmIntArg(number)
        is Float -> BsmFloatArg(number)
        is Long -> BsmLongArg(number)
        is Double -> BsmDoubleArg(number)
        else -> error("Unknown number: $number")
    }

    private fun buildInvokeDynamicInsn(insnNode: InvokeDynamicInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val desc = insnNode.desc
        val bsmArgs = insnNode.bsmArgs.map {
            when (it) {
                is Number -> bsmNumberArg(it)
                is String -> BsmStringArg(it)
                is Type -> it.asTypeName
                is Handle -> it.bsmHandleArg
                else -> error("Unknown arg of bsm: $it")
            }
        }.reversed()
        val args = Type.getArgumentTypes(desc).map { pop() }.reversed()
        val bsmMethod = insnNode.bsm.bsmHandleArg
        val expr = JcRawDynamicCallExpr(
            bsmMethod,
            bsmArgs,
            insnNode.name,
            Type.getArgumentTypes(desc).map { it.descriptor.typeName() },
            Type.getReturnType(desc).descriptor.typeName(),
            args,
        )
        if (Type.getReturnType(desc) == Type.VOID_TYPE) {
            addInstruction(insnNode, JcRawCallInst(method, expr))
        } else {
            val result = nextRegister(Type.getReturnType(desc).descriptor.typeName())
            addInstruction(insnNode, JcRawAssignInst(method, result, expr))
            push(result)
        }
    }

    private fun buildJumpInsnNode(insnNode: JumpInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val target = labelRef(insnNode.label)
        when (val opcode = insnNode.opcode) {
            Opcodes.GOTO -> addInstruction(insnNode, JcRawGotoInst(method, target))
            else -> {
                val falseTarget = (insnNode.next as? LabelNode)?.let { label(it) } ?: generateLabel(insnNode, labelId = 0)
                val rhv = pop()
                val boolTypeName = PredefinedPrimitives.Boolean.typeName()
                val expr = when (opcode) {
                    Opcodes.IFNULL -> JcRawEqExpr(boolTypeName, rhv, JcRawNull())
                    Opcodes.IFNONNULL -> JcRawNeqExpr(boolTypeName, rhv, JcRawNull())
                    Opcodes.IFEQ -> JcRawEqExpr(boolTypeName, rhv, JcRawZero(rhv.typeName))
                    Opcodes.IFNE -> JcRawNeqExpr(boolTypeName, rhv, JcRawZero(rhv.typeName))
                    Opcodes.IFLT -> JcRawLtExpr(boolTypeName, rhv, JcRawZero(rhv.typeName))
                    Opcodes.IFGE -> JcRawGeExpr(boolTypeName, rhv, JcRawZero(rhv.typeName))
                    Opcodes.IFGT -> JcRawGtExpr(boolTypeName, rhv, JcRawZero(rhv.typeName))
                    Opcodes.IFLE -> JcRawLeExpr(boolTypeName, rhv, JcRawZero(rhv.typeName))
                    Opcodes.IF_ICMPEQ -> JcRawEqExpr(boolTypeName, pop(), rhv)
                    Opcodes.IF_ICMPNE -> JcRawNeqExpr(boolTypeName, pop(), rhv)
                    Opcodes.IF_ICMPLT -> JcRawLtExpr(boolTypeName, pop(), rhv)
                    Opcodes.IF_ICMPGE -> JcRawGeExpr(boolTypeName, pop(), rhv)
                    Opcodes.IF_ICMPGT -> JcRawGtExpr(boolTypeName, pop(), rhv)
                    Opcodes.IF_ICMPLE -> JcRawLeExpr(boolTypeName, pop(), rhv)
                    Opcodes.IF_ACMPEQ -> JcRawEqExpr(boolTypeName, pop(), rhv)
                    Opcodes.IF_ACMPNE -> JcRawNeqExpr(boolTypeName, pop(), rhv)
                    else -> error("Unknown jump opcode $opcode")
                }

                addInstruction(insnNode, JcRawIfInst(method, expr, target, falseTarget.ref))
                if (insnNode.next !is LabelNode) {
                    addInstruction(insnNode, falseTarget)
                }
            }
        }
    }

    private fun InstBuilder.mergeFrames(frames: List<Pair<AbstractInsnNode, Frame?>>, curInsn: LabelNode): Frame {
        val frameSet = frames.mapNotNull { it.second }
        val maxLocalVar = frameSet.minOf { it.maxLocal() }
        val maxStackIndex = frameSet.minOf { it.stack.lastIndex }

        val localTypes = Array(maxLocalVar + 1) { local ->
            resolveFrameVariableType(frameSet, local, curInsn)
        }

        val stackTypes = List(maxStackIndex + 1) {
            resolveStackVariableType(frameSet, it)
        }

        if (frameSet.size == frames.size) {
            @Suppress("UNCHECKED_CAST")
            return mergeWithPresentFrames(frames as List<Pair<AbstractInsnNode, Frame>>, curInsn, localTypes, stackTypes)
        } else {
            return mergeWithMissedFrames(curInsn, localTypes, stackTypes)
        }
    }

    private fun InstBuilder.mergeWithMissedFrames(
        curNode: LabelNode,
        localTypes: Array<TypeName?>,
        stackTypes: List<TypeName>,
    ): Frame {
        val localMergeAssignments = localMergeAssignments.getOrPut(curNode, ::hashMapOf)
        val stackMergeAssignments = stackMergeAssignments.getOrPut(curNode, ::hashMapOf)

        val mergedStack = stackTypes.mapIndexed { index, type ->
            nextRegister(type).also { stackMergeAssignments[index] = it }
        }

        skipAllBeforeMark(StackMark)

        val mergedLocals = Array<JcRawSimpleValue?>(localTypes.size) { variable ->
            val type = localTypes[variable]
            if (type == null || type == TOP) {
                skipRegister()
                return@Array null
            }

            nextRegister(type).also { localMergeAssignments[variable] = it }
        }

        return Frame(mergedLocals.trimEndNulls(), mergedStack.toPersistentList())
    }

    private fun InstBuilder.mergeWithPresentFrames(
        frames: List<Pair<AbstractInsnNode, Frame>>,
        curNode: LabelNode,
        localTypes: Array<TypeName?>,
        stackTypes: List<TypeName>,
    ): Frame {
        val localMergeAssignments = localMergeAssignments.getOrPut(curNode, ::hashMapOf)
        val stackMergeAssignments = stackMergeAssignments.getOrPut(curNode, ::hashMapOf)

        val mergedStack = stackTypes.mapIndexed { index, type ->
            val allFramesSameValue = framesStackSameValue(frames, index)
            if (allFramesSameValue != null) {
                stackMergeAssignments.remove(index)
                skipRegister()
                return@mapIndexed allFramesSameValue
            }

            nextRegister(type).also { stackMergeAssignments[index] = it }
        }

        skipAllBeforeMark(StackMark)

        val mergedLocals = Array(localTypes.size) { variable ->
            val type = localTypes[variable]
            if (type == null || type == TOP) {
                localMergeAssignments.remove(variable)
                skipRegister()
                return@Array null
            }

            val allFramesSameValue = framesVariableSameValue(frames, variable)
            if (allFramesSameValue != null) {
                localMergeAssignments.remove(variable)
                skipRegister()
                return@Array allFramesSameValue
            }

            nextRegister(type).also { localMergeAssignments[variable] = it }
        }

        return Frame(mergedLocals.trimEndNulls(), mergedStack.toPersistentList())
    }

    private fun resolveStackVariableType(frames: Iterable<Frame>, stackIndex: Int): TypeName {
        var type: TypeName? = null
        for (frame in frames) {
            val frameType = frame.stack[stackIndex].typeName

            if (type == null) {
                type = frameType
                continue
            }

            type = typeLub(type, frameType)
        }

        check(type != null && type != TOP) {
            "Incorrect stack types"
        }

        return type
    }

    private fun resolveFrameVariableType(frames: Iterable<Frame>, variable: Int, curLabel: LabelNode): TypeName? {
        var type: TypeName? = null
        for (frame in frames) {
            if (!frame.hasLocal(variable)) return null

            val frameType = frame.getLocal(variable).typeName
            if (type == null) {
                type = frameType
                continue
            }

            type = typeLub(type, frameType)
        }

        if (type == TOP) return TOP

        // If we have several variables types for one register we have to search right type in debug info otherwise we cannot guarantee anything
        val debugType = findLocalVariableWithInstruction(variable, curLabel)
                ?.let { Type.getType(it.desc) }
                ?.descriptor?.typeName()

        if (debugType != null) return debugType

        return type ?: error("No type")
    }

    private fun framesVariableSameValue(frames: Iterable<Pair<AbstractInsnNode, Frame>>, variable: Int): JcRawSimpleValue? =
        frames.sameOrNull { second.getLocal(variable) }

    private fun framesStackSameValue(frames: Iterable<Pair<AbstractInsnNode, Frame>>, index: Int): JcRawSimpleValue? =
        frames.sameOrNull { second.stack[index] }

    private inline fun <R : Any, T> Iterable<T>.sameOrNull(getter: T.() -> R): R? {
        var result: R? = null
        for (element in this) {
            val elementValue = getter(element)
            if (result == null) {
                result = elementValue
                continue
            }

            if (elementValue != result) return null
        }
        return result
    }

    private fun buildLabelNode(insnNode: LabelNode): Frame {
        val labelInst = label(insnNode)
        addInstruction(insnNode, labelInst)

        val predecessors = predecessors[insnNode].orEmpty()
        val predecessorFrames = predecessors.map { frames[it] }

        val builderVars = generatedVars.getOrPut(insnNode, ::mutableListOf)
        val builder = InstBuilder(builderVars)

        var throwable: JcRawLocalVar? = null

        val catchEntries = tryCatchHandlers[insnNode].orEmpty()
        if (catchEntries.isNotEmpty()) {
            throwable = builder.nextRegister(catchEntries.commonTypeOrDefault.typeName())

            val entries = catchEntries.mapIndexed { index, node ->
                buildCatchEntry(insnNode, node, index)
            }

            val catchInst = JcRawCatchInst(
                method,
                throwable,
                labelRef(insnNode),
                entries
            )

            addInstruction(insnNode, catchInst)
        }

        val singleFrame = predecessorFrames.singleOrNull()
        var currentFrame = if (singleFrame != null) {
            singleFrame
        } else {
            builder.mergeFrames(predecessors.zip(predecessorFrames), insnNode)
        }

        if (throwable != null) {
            currentFrame = currentFrame.push(throwable)
        }

        return currentFrame
    }

    private fun buildCatchEntry(insn: LabelNode, node: TryCatchBlockNode, entryIdx: Int): JcRawCatchEntry {
        var startLabel = labels[node.start]
        if (startLabel == null) {
            startLabel = generateLabel(insn, labelId = entryIdx * 2)
            ensureLabelInitialized(node.start, startLabel)
        }

        var endLabel = labels[node.end]
        if (endLabel == null) {
            endLabel = generateLabel(insn, labelId = entryIdx * 2 + 1)
            ensureLabelInitialized(node.end, endLabel)
        }

        return JcRawCatchEntry(node.typeOrDefault.typeName(), startLabel.ref, endLabel.ref)
    }

    private fun ensureLabelInitialized(node: AbstractInsnNode, label: JcRawLabelInst) {
        val nodeIdx = instructionIndex[node] ?: error("No label node index")
        instructions[nodeIdx] = node

        val nodeInst = instructionList(node)
        if (label !in nodeInst) {
            nodeInst.add(label)
        }
    }

    private fun buildLineNumberNode(insnNode: LineNumberNode, frame: SimpleInstBuilder) = with(frame) {
        addInstruction(insnNode, JcRawLineNumberInst(method, insnNode.line, labelRef(insnNode.start)))
    }

    private fun ldcValue(cst: Any): JcRawSimpleValue {
        return when (cst) {
            is Int -> JcRawInt(cst)
            is Float -> JcRawFloat(cst)
            is Double -> JcRawDouble(cst)
            is Long -> JcRawLong(cst)
            is String -> JcRawStringConstant(cst, STRING_CLASS.typeName())
            is Type -> JcRawClassConstant(cst.descriptor.typeName(), CLASS_CLASS.typeName())
            is Handle -> {
                JcRawMethodConstant(
                    cst.owner.typeName(),
                    cst.name,
                    Type.getArgumentTypes(cst.desc).map { it.descriptor.typeName() },
                    Type.getReturnType(cst.desc).descriptor.typeName(),
                    METHOD_HANDLE_CLASS.typeName()
                )
            }

            else -> error("Can't convert LDC value: $cst of type ${cst::class.java.name}")
        }
    }

    private fun buildLdcInsnNode(insnNode: LdcInsnNode, frame: SimpleInstBuilder) = with(frame) {
        when (val cst = insnNode.cst) {
            is Int -> push(ldcValue(cst))
            is Float -> push(ldcValue(cst))
            is Double -> push(ldcValue(cst))
            is Long -> push(ldcValue(cst))
            is String -> push(JcRawStringConstant(cst, STRING_CLASS.typeName()))
            is Type -> {
                val assignment = nextRegister(CLASS_CLASS.typeName())
                addInstruction(
                    insnNode, JcRawAssignInst(
                        method,
                        assignment,
                        when (cst.sort) {
                            Type.METHOD -> JcRawMethodType(
                                cst.argumentTypes.map { it.descriptor.typeName() },
                                cst.returnType.descriptor.typeName(),
                                METHOD_TYPE_CLASS.typeName()
                            )

                            else -> ldcValue(cst)
                        }
                    )
                )
                push(assignment)
            }

            is Handle -> {
                val assignment = nextRegister(CLASS_CLASS.typeName())
                addInstruction(
                    insnNode, JcRawAssignInst(
                        method,
                        assignment,
                        ldcValue(cst)
                    )
                )
                push(assignment)
            }

            is ConstantDynamic -> {
                val methodHande = cst.bootstrapMethod
                val assignment = nextRegister(CLASS_CLASS.typeName())
                val exprs = arrayListOf<JcRawValue>()
                repeat(cst.bootstrapMethodArgumentCount) {
                    exprs.add(
                        ldcValue(cst.getBootstrapMethodArgument(it - 1))
                    )
                }
                val methodCall: JcRawCallExpr = when (cst.bootstrapMethod.tag) {
                    Opcodes.INVOKESPECIAL -> JcRawSpecialCallExpr(
                        methodHande.owner.typeName(),
                        cst.name,
                        Type.getArgumentTypes(methodHande.desc).map { it.descriptor.typeName() },
                        Type.getReturnType(methodHande.desc).descriptor.typeName(),
                        thisRef(),
                        exprs
                    )

                    else -> {
                        val lookupAssignment = nextRegister(METHOD_HANDLES_LOOKUP_CLASS.typeName())
                        addInstruction(
                            insnNode, JcRawAssignInst(
                                method,
                                lookupAssignment,
                                JcRawStaticCallExpr(
                                    METHOD_HANDLES_CLASS.typeName(),
                                    "lookup",
                                    emptyList(),
                                    METHOD_HANDLES_LOOKUP_CLASS.typeName(),
                                    emptyList()
                                )
                            )
                        )
                        JcRawStaticCallExpr(
                            methodHande.owner.typeName(),
                            methodHande.name,
                            Type.getArgumentTypes(methodHande.desc).map { it.descriptor.typeName() },
                            Type.getReturnType(methodHande.desc).descriptor.typeName(),
                            listOf(
                                lookupAssignment,
                                JcRawStringConstant(cst.name, STRING_CLASS.typeName()),
                                JcRawClassConstant(cst.descriptor.typeName(), CLASS_CLASS.typeName())
                            ) + exprs,
                            methodHande.isInterface
                        )
                    }
                }
                addInstruction(insnNode, JcRawAssignInst(method, assignment, methodCall))
                push(assignment)
            }

            else -> error("Unknown LDC constant: $cst and type ${cst::class.java.name}")
        }
    }

    private fun buildLookupSwitchInsnNode(insnNode: LookupSwitchInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val key = pop()
        val default = labelRef(insnNode.dflt)
        val branches = insnNode.keys
            .zip(insnNode.labels)
            .associate { (JcRawInt(it.first) as JcRawValue) to labelRef(it.second) }
        addInstruction(insnNode, JcRawSwitchInst(method, key, branches, default))
    }

    private fun buildMethodInsnNode(insnNode: MethodInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val owner = when {
            insnNode.owner.typeName().isArray -> OBJECT_TYPE_NAME
            else -> insnNode.owner.typeName()
        }
        val methodName = insnNode.name
        val argTypes = Type.getArgumentTypes(insnNode.desc).map { it.descriptor.typeName() }
        val returnType = Type.getReturnType(insnNode.desc).descriptor.typeName()

        val args = Type.getArgumentTypes(insnNode.desc).map { pop() }.reversed()

        val expr = when (val opcode = insnNode.opcode) {
            Opcodes.INVOKESTATIC -> JcRawStaticCallExpr(
                owner,
                methodName,
                argTypes,
                returnType,
                args,
                insnNode.itf
            )

            else -> {
                val instance = pop()
                when (opcode) {
                    Opcodes.INVOKEVIRTUAL -> JcRawVirtualCallExpr(
                        owner,
                        methodName,
                        argTypes,
                        returnType,
                        instance,
                        args
                    )

                    Opcodes.INVOKESPECIAL -> JcRawSpecialCallExpr(
                        owner,
                        methodName,
                        argTypes,
                        returnType,
                        instance,
                        args
                    )

                    Opcodes.INVOKEINTERFACE -> JcRawInterfaceCallExpr(
                        owner,
                        methodName,
                        argTypes,
                        returnType,
                        instance,
                        args
                    )

                    else -> error("Unknown method insn opcode: ${insnNode.opcode}")
                }
            }
        }
        if (Type.getReturnType(insnNode.desc) == Type.VOID_TYPE) {
            addInstruction(insnNode, JcRawCallInst(method, expr))
        } else {
            val result = nextRegister(Type.getReturnType(insnNode.desc).descriptor.typeName())
            addInstruction(insnNode, JcRawAssignInst(method, result, expr))
            push(result)
        }
    }

    private fun buildMultiANewArrayInsnNode(insnNode: MultiANewArrayInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val dimensions = mutableListOf<JcRawValue>()
        repeat(insnNode.dims) {
            dimensions += pop()
        }
        val expr = JcRawNewArrayExpr(insnNode.desc.typeName(), dimensions.reversed())
        val assignment = nextRegister(expr.typeName)
        addInstruction(insnNode, JcRawAssignInst(method, assignment, expr))
        push(assignment)
    }

    private fun buildTableSwitchInsnNode(insnNode: TableSwitchInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val index = pop()
        val default = labelRef(insnNode.dflt)
        val branches = (insnNode.min..insnNode.max)
            .zip(insnNode.labels)
            .associate { (JcRawInt(it.first) as JcRawValue) to labelRef(it.second) }
        addInstruction(insnNode, JcRawSwitchInst(method, index, branches, default))
    }

    private fun buildTypeInsnNode(insnNode: TypeInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val type = insnNode.desc.typeName()
        when (insnNode.opcode) {
            Opcodes.NEW -> {
                val assignment = nextRegister(type)
                addInstruction(insnNode, JcRawAssignInst(method, assignment, JcRawNewExpr(type)))
                push(assignment)
            }

            Opcodes.ANEWARRAY -> {
                val length = pop()
                val assignment = nextRegister(type.asArray())
                addInstruction(
                    insnNode, JcRawAssignInst(
                        method,
                        assignment,
                        JcRawNewArrayExpr(type.asArray(), length)
                    )
                )
                push(assignment)
            }

            Opcodes.CHECKCAST -> {
                val assignment = nextRegister(type)
                addInstruction(insnNode, JcRawAssignInst(method, assignment, JcRawCastExpr(type, pop())))
                push(assignment)
            }

            Opcodes.INSTANCEOF -> {
                val assignment = nextRegister(PredefinedPrimitives.Boolean.typeName())
                addInstruction(
                    insnNode, JcRawAssignInst(
                        method,
                        assignment,
                        JcRawInstanceOfExpr(PredefinedPrimitives.Boolean.typeName(), pop(), type)
                    )
                )
                push(assignment)
            }

            else -> error("Unknown opcode ${insnNode.opcode} in TypeInsnNode")
        }
    }

    private fun buildVarInsnNode(insnNode: VarInsnNode, frame: SimpleInstBuilder) = with(frame) {
        val variable = insnNode.`var`
        when (insnNode.opcode) {
            in Opcodes.ISTORE..Opcodes.ASTORE -> {
                val inst = local(variable, pop(), insnNode)
                if (inst != null) {
                    addInstruction(insnNode, inst)
                }
            }

            in Opcodes.ILOAD..Opcodes.ALOAD -> {
                push(local(variable))
            }

            else -> error("Unknown opcode ${insnNode.opcode} in VarInsnNode")
        }
    }

    private fun findLocalVariableWithInstruction(variable: Int, insn: AbstractInsnNode): LocalVariableNode? =
        methodNode.localVariables.find { it.index == variable && insn.isBetween(it.start, it.end) }

    private fun AbstractInsnNode.isBetween(labelStart: AbstractInsnNode, labelEnd: AbstractInsnNode): Boolean =
        methodNode.instructions.let {
            it.indexOf(this) in it.indexOf(labelStart)..it.indexOf(labelEnd)
        }
}

private data object StackMark

private fun <T> Array<T?>.trimEndNulls(): Array<T?> {
    var realSize = size

    while (realSize > 0 && this[realSize - 1] == null) {
        realSize--
    }

    if (realSize == size) {
        return this
    }

    return this.copyOf(realSize)
}

private fun <T> Array<T?>.add(index: Int, value: T): Array<T?> {
    if (index < size) {
        this[index] = value
        return this
    }

    val result = copyOf(size * 2)
    result[index] = value
    return result
}

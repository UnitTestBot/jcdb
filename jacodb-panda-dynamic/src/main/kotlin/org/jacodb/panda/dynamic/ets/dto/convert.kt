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

package org.jacodb.panda.dynamic.ets.dto

import org.jacodb.panda.dynamic.ets.base.EtsAnyType
import org.jacodb.panda.dynamic.ets.base.EtsArrayAccess
import org.jacodb.panda.dynamic.ets.base.EtsArrayLiteral
import org.jacodb.panda.dynamic.ets.base.EtsAssignStmt
import org.jacodb.panda.dynamic.ets.base.EtsBinaryOperation
import org.jacodb.panda.dynamic.ets.base.EtsBooleanConstant
import org.jacodb.panda.dynamic.ets.base.EtsBooleanType
import org.jacodb.panda.dynamic.ets.base.EtsCallExpr
import org.jacodb.panda.dynamic.ets.base.EtsCallStmt
import org.jacodb.panda.dynamic.ets.base.EtsCastExpr
import org.jacodb.panda.dynamic.ets.base.EtsConditionExpr
import org.jacodb.panda.dynamic.ets.base.EtsConstant
import org.jacodb.panda.dynamic.ets.base.EtsDeleteStmt
import org.jacodb.panda.dynamic.ets.base.EtsEntity
import org.jacodb.panda.dynamic.ets.base.EtsFieldRef
import org.jacodb.panda.dynamic.ets.base.EtsGotoStmt
import org.jacodb.panda.dynamic.ets.base.EtsIfStmt
import org.jacodb.panda.dynamic.ets.base.EtsInstLocation
import org.jacodb.panda.dynamic.ets.base.EtsInstanceCallExpr
import org.jacodb.panda.dynamic.ets.base.EtsInstanceFieldRef
import org.jacodb.panda.dynamic.ets.base.EtsInstanceOfExpr
import org.jacodb.panda.dynamic.ets.base.EtsLValue
import org.jacodb.panda.dynamic.ets.base.EtsLengthExpr
import org.jacodb.panda.dynamic.ets.base.EtsLocal
import org.jacodb.panda.dynamic.ets.base.EtsNeverType
import org.jacodb.panda.dynamic.ets.base.EtsNewArrayExpr
import org.jacodb.panda.dynamic.ets.base.EtsNewExpr
import org.jacodb.panda.dynamic.ets.base.EtsNopStmt
import org.jacodb.panda.dynamic.ets.base.EtsNullConstant
import org.jacodb.panda.dynamic.ets.base.EtsNullType
import org.jacodb.panda.dynamic.ets.base.EtsNumberConstant
import org.jacodb.panda.dynamic.ets.base.EtsNumberType
import org.jacodb.panda.dynamic.ets.base.EtsObjectLiteral
import org.jacodb.panda.dynamic.ets.base.EtsParameterRef
import org.jacodb.panda.dynamic.ets.base.EtsPhiExpr
import org.jacodb.panda.dynamic.ets.base.EtsRelationOperation
import org.jacodb.panda.dynamic.ets.base.EtsReturnStmt
import org.jacodb.panda.dynamic.ets.base.EtsStaticCallExpr
import org.jacodb.panda.dynamic.ets.base.EtsStaticFieldRef
import org.jacodb.panda.dynamic.ets.base.EtsStmt
import org.jacodb.panda.dynamic.ets.base.EtsStringConstant
import org.jacodb.panda.dynamic.ets.base.EtsStringType
import org.jacodb.panda.dynamic.ets.base.EtsSwitchStmt
import org.jacodb.panda.dynamic.ets.base.EtsThis
import org.jacodb.panda.dynamic.ets.base.EtsThrowStmt
import org.jacodb.panda.dynamic.ets.base.EtsType
import org.jacodb.panda.dynamic.ets.base.EtsTypeOfExpr
import org.jacodb.panda.dynamic.ets.base.EtsUnaryOperation
import org.jacodb.panda.dynamic.ets.base.EtsUnclearRefType
import org.jacodb.panda.dynamic.ets.base.EtsUndefinedConstant
import org.jacodb.panda.dynamic.ets.base.EtsUndefinedType
import org.jacodb.panda.dynamic.ets.base.EtsUnknownType
import org.jacodb.panda.dynamic.ets.base.EtsValue
import org.jacodb.panda.dynamic.ets.base.EtsVoidType
import org.jacodb.panda.dynamic.ets.base.BinaryOp
import org.jacodb.panda.dynamic.ets.base.UnaryOp
import org.jacodb.panda.dynamic.ets.graph.EtsCfg
import org.jacodb.panda.dynamic.ets.model.EtsClass
import org.jacodb.panda.dynamic.ets.model.EtsClassImpl
import org.jacodb.panda.dynamic.ets.model.EtsClassSignature
import org.jacodb.panda.dynamic.ets.model.EtsField
import org.jacodb.panda.dynamic.ets.model.EtsFieldImpl
import org.jacodb.panda.dynamic.ets.model.EtsFieldSignature
import org.jacodb.panda.dynamic.ets.model.EtsFieldSubSignature
import org.jacodb.panda.dynamic.ets.model.EtsFile
import org.jacodb.panda.dynamic.ets.model.EtsMethod
import org.jacodb.panda.dynamic.ets.model.EtsMethodImpl
import org.jacodb.panda.dynamic.ets.model.EtsMethodParameter
import org.jacodb.panda.dynamic.ets.model.EtsMethodSignature
import org.jacodb.panda.dynamic.ets.model.EtsMethodSubSignature

class EtsMethodBuilder(
    signature: EtsMethodSignature,
) {
    private val etsMethod = EtsMethodImpl(signature)
    private var freeLocal: Int = 0
    private val currentStmts: MutableList<EtsStmt> = mutableListOf()

    private fun loc(): EtsInstLocation {
        return EtsInstLocation(etsMethod, currentStmts.size)
    }

    private var built: Boolean = false

    fun build(cfgDto: CfgDto): EtsMethod {
        require(!built) { "Method has already been built" }
        val cfg = cfg2cfg(cfgDto)
        etsMethod.cfg = cfg
        built = true
        return etsMethod
    }

    fun convertToEtsStmt(stmt: StmtDto) {
        val newStmt = when (stmt) {
            is UnknownStmtDto -> object : EtsStmt {
                override val location: EtsInstLocation = loc()

                override fun toString(): String = "UNKNOWN"

                override fun <R> accept(visitor: EtsStmt.Visitor<R>): R {
                    error("UnknownStmt is not supported")
                }
            }

            is NopStmtDto -> EtsNopStmt(location = loc())

            is AssignStmtDto -> EtsAssignStmt(
                location = loc(),
                lhv = convertToEtsEntity(stmt.left) as EtsLValue,
                rhv = convertToEtsEntity(stmt.right),
            )

            is CallStmtDto -> EtsCallStmt(
                location = loc(),
                expr = convertToEtsEntity(stmt.expr) as EtsCallExpr,
            )

            is DeleteStmtDto -> EtsDeleteStmt(
                location = loc(),
                arg = convertToEtsFieldRef(stmt.arg),
            )

            is ReturnStmtDto -> {
                val etsEntity = convertToEtsEntity(stmt.arg)
                val etsValue = if (etsEntity is EtsValue) {
                    etsEntity
                } else {
                    val newLocal = EtsLocal("_tmp${freeLocal++}", EtsUnknownType)
                    currentStmts += EtsAssignStmt(
                        location = loc(),
                        lhv = newLocal,
                        rhv = etsEntity,
                    )
                    newLocal
                }
                EtsReturnStmt(
                    location = loc(),
                    returnValue = etsValue,
                )
            }

            is ReturnVoidStmtDto -> EtsReturnStmt(
                location = loc(),
                returnValue = null,
            )

            is ThrowStmtDto -> EtsThrowStmt(
                location = loc(),
                arg = convertToEtsEntity(stmt.arg),
            )

            is GotoStmtDto -> EtsGotoStmt(location = loc())

            is IfStmtDto -> EtsIfStmt(
                location = loc(),
                condition = convertToEtsEntity(stmt.condition) as EtsConditionExpr,
            )

            is SwitchStmtDto -> EtsSwitchStmt(
                location = loc(),
                arg = convertToEtsEntity(stmt.arg),
                cases = stmt.cases.map { convertToEtsEntity(it) },
            )

            // else -> error("Unknown Stmt: $stmt")
        }
        currentStmts += newStmt
    }

    fun convertToEtsEntity(value: ValueDto): EtsEntity {
        return when (value) {
            is UnknownValueDto -> object : EtsEntity {
                override val type: EtsType
                    get() = EtsUnknownType

                override fun toString(): String = "UNKNOWN"

                override fun <R> accept(visitor: EtsEntity.Visitor<R>): R {
                    if (visitor is EtsEntity.Visitor.Default<R>) {
                        return visitor.defaultVisit(this)
                    }
                    error("UnknownEntity is not supported")
                }
            }

            is LocalDto -> EtsLocal(
                name = value.name,
                type = convertToEtsType(value.type),
            )

            is ConstantDto -> convertToEtsConstant(value)

            is NewExprDto -> EtsNewExpr(
                type = convertToEtsType(value.type) // as ClassType
            )

            is NewArrayExprDto -> EtsNewArrayExpr(
                elementType = convertToEtsType(value.type),
                size = convertToEtsEntity(value.size),
            )

            is TypeOfExprDto -> EtsTypeOfExpr(
                arg = convertToEtsEntity(value.arg)
            )

            is InstanceOfExprDto -> EtsInstanceOfExpr(
                arg = convertToEtsEntity(value.arg),
                checkType = convertToEtsType(value.checkType),
            )

            is LengthExprDto -> EtsLengthExpr(
                arg = convertToEtsEntity(value.arg)
            )

            is CastExprDto -> EtsCastExpr(
                arg = convertToEtsEntity(value.arg),
                type = convertToEtsType(value.type),
            )

            is PhiExprDto -> EtsPhiExpr(
                args = value.args.map { convertToEtsEntity(it) },
                argToBlock = emptyMap(), // TODO
                type = convertToEtsType(value.type),
            )

            is ArrayLiteralDto -> EtsArrayLiteral(
                elements = value.elements.map { convertToEtsEntity(it) },
                type = convertToEtsType(value.type), // TODO: as EtsArrayType,
            )

            is ObjectLiteralDto -> EtsObjectLiteral(
                properties = emptyList(), // TODO
                type = convertToEtsType(value.type),
            )

            is UnaryOperationDto -> EtsUnaryOperation(
                op = convertToEtsUnaryOp(value.op),
                arg = convertToEtsEntity(value.arg),
            )

            is BinaryOperationDto -> EtsBinaryOperation(
                op = convertToEtsBinaryOp(value.op),
                left = convertToEtsEntity(value.left),
                right = convertToEtsEntity(value.right),
            )

            is RelationOperationDto -> EtsRelationOperation(
                relop = value.op,
                left = convertToEtsEntity(value.left),
                right = convertToEtsEntity(value.right),
            )

            is InstanceCallExprDto -> EtsInstanceCallExpr(
                instance = convertToEtsEntity(value.instance),
                method = convertToEtsMethodSignature(value.method),
                args = value.args.map {
                    val etsEntity = convertToEtsEntity(it)
                    if (etsEntity is EtsValue) return@map etsEntity
                    val newLocal = EtsLocal("_tmp${freeLocal++}", EtsUnknownType)
                    currentStmts += EtsAssignStmt(
                        location = loc(),
                        lhv = newLocal,
                        rhv = etsEntity,
                    )
                    newLocal
                },
            )

            is StaticCallExprDto -> EtsStaticCallExpr(
                method = convertToEtsMethodSignature(value.method),
                args = value.args.map {
                    val etsEntity = convertToEtsEntity(it)
                    if (etsEntity is EtsValue) return@map etsEntity
                    TODO()
                },
            )

            is ThisRefDto -> EtsThis(
                type = convertToEtsType(value.type) // TODO: as ClassType
            )

            is ParameterRefDto -> EtsParameterRef(
                index = value.index,
                type = convertToEtsType(value.type),
            )

            is ArrayRefDto -> EtsArrayAccess(
                array = convertToEtsEntity(value.array),
                index = convertToEtsEntity(value.index),
                type = convertToEtsType(value.type),
            )

            is FieldRefDto -> convertToEtsFieldRef(value)

            // else -> error("Unknown Value: $value")
        }
    }

    fun convertToEtsFieldRef(fieldRef: FieldRefDto): EtsFieldRef {
        val field = convertToEtsFieldSignature(fieldRef.field)
        return when (fieldRef) {
            is InstanceFieldRefDto -> EtsInstanceFieldRef(
                instance = convertToEtsEntity(fieldRef.instance), // as Local
                field = field
            )

            is StaticFieldRefDto -> EtsStaticFieldRef(
                field = field
            )
        }
    }

    fun cfg2cfg(cfg: CfgDto): EtsCfg {
        // val stmts: MutableList<EtsStmt> = mutableListOf()
        val blocks = cfg.blocks.associateBy { it.id }
        val visited: MutableSet<Int> = hashSetOf()
        val queue: ArrayDeque<Int> = ArrayDeque()
        queue.add(0)
        val blockStart: MutableMap<Int, Int> = hashMapOf()
        val blockEnd: MutableMap<Int, Int> = hashMapOf()
        while (queue.isNotEmpty()) {
            val block = blocks[queue.removeFirst()]!!
            if (block.stmts.isNotEmpty()) {
                blockStart[block.id] = currentStmts.size
            }
            for (stmt in block.stmts) {
                convertToEtsStmt(stmt)
            }
            if (block.stmts.isNotEmpty()) {
                blockEnd[block.id] = currentStmts.lastIndex
            }
            for (next in block.successors) {
                if (visited.add(next)) {
                    queue.addLast(next)
                }
            }
        }
        val successorMap: MutableMap<EtsStmt, List<EtsStmt>> = hashMapOf()
        for (block in cfg.blocks) {
            if (block.stmts.isEmpty()) {
                continue
            }
            val startId = blockStart[block.id]!!
            val endId = blockEnd[block.id]!!
            for (i in startId until endId) {
                successorMap[currentStmts[i]] = listOf(currentStmts[i + 1])
            }
            successorMap[currentStmts[endId]] = block.successors.mapNotNull { blockId ->
                blockStart[blockId]?.let { currentStmts[it] }
            }
        }
        return EtsCfg(currentStmts, successorMap)
    }
}

fun convertToEtsType(type: String): EtsType {
    return when (type) {
        "any" -> EtsAnyType
        "unknown" -> EtsUnknownType
        // "union" -> UnionType
        // "tuple" -> TupleType
        "boolean" -> EtsBooleanType
        "number" -> EtsNumberType
        "string" -> EtsStringType
        "null" -> EtsNullType
        "undefined" -> EtsUndefinedType
        "void" -> EtsVoidType
        "never" -> EtsNeverType
        // "literal" -> LiteralType
        // "class" -> ClassType
        // "array" -> ArrayType
        // "object" -> ArrayObjectType
        else -> EtsUnclearRefType(type)
    }
}

fun convertToEtsConstant(value: ConstantDto): EtsConstant {
    return when (value.type) {
        "string" -> EtsStringConstant(
            value = value.value
        )

        "boolean" -> EtsBooleanConstant(
            value = value.value.toBoolean()
        )

        "number" -> EtsNumberConstant(
            value = value.value.toDouble()
        )

        "null" -> EtsNullConstant

        "undefined" -> EtsUndefinedConstant

        "unknown" -> object : EtsConstant {
            override val type: EtsType
                get() = EtsUnknownType

            override fun toString(): String = "UnknownConstant(${value.value})"

            override fun <R> accept(visitor: EtsConstant.Visitor<R>): R {
                TODO("UnknownConstant is not supported")
            }
        }

        else -> error("Unknown Constant: $value")
    }
}

fun convertToEtsUnaryOp(op: String): UnaryOp {
    return when (op) {
        "+" -> UnaryOp.Plus
        "-" -> UnaryOp.Minus
        "!" -> UnaryOp.Bang
        "~" -> UnaryOp.Tilde
        "typeof" -> UnaryOp.Typeof
        "void" -> UnaryOp.Void
        "delete" -> UnaryOp.Delete
        "MinusToken" -> UnaryOp.Minus
        "PlusToken" -> UnaryOp.Plus
        else -> error("Unknown UnaryOp: $op")
    }
}

fun convertToEtsBinaryOp(op: String): BinaryOp {
    return when (op) {
        "+" -> BinaryOp.Add
        "-" -> BinaryOp.Sub
        "*" -> BinaryOp.Mul
        "/" -> BinaryOp.Div
        "%" -> BinaryOp.Mod
        "==" -> BinaryOp.EqEq
        "!=" -> BinaryOp.NotEq
        "===" -> BinaryOp.EqEqEq
        "!==" -> BinaryOp.NotEqEq
        "<" -> BinaryOp.Lt
        "<=" -> BinaryOp.LtEq
        ">" -> BinaryOp.Gt
        ">=" -> BinaryOp.GtEq
        "<<" -> BinaryOp.LShift
        ">>" -> BinaryOp.RShift
        ">>>" -> BinaryOp.ZeroFillRShift
        "&" -> BinaryOp.BitAnd
        "|" -> BinaryOp.BitOr
        "^" -> BinaryOp.BitXor
        "&&" -> BinaryOp.LogicalAnd
        "||" -> BinaryOp.LogicalOr
        "in" -> BinaryOp.In
        "instanceof" -> BinaryOp.InstanceOf
        "**" -> BinaryOp.Exp
        "??" -> BinaryOp.NullishCoalescing

        else -> error("Unknown BinaryOp: $op")
    }
}

fun convertToEtsClassSignature(clazz: ClassSignatureDto): EtsClassSignature {
    return EtsClassSignature(
        name = clazz.name,
        namespace = null, // TODO
        file = null, // TODO
    )
}

fun convertToEtsFieldSignature(field: FieldSignatureDto): EtsFieldSignature {
    return EtsFieldSignature(
        enclosingClass = convertToEtsClassSignature(field.enclosingClass),
        sub = EtsFieldSubSignature(
            name = field.name,
            type = convertToEtsType(field.fieldType),
        )
    )
}

fun convertToEtsMethodSignature(method: MethodSignatureDto): EtsMethodSignature {
    return EtsMethodSignature(
        enclosingClass = convertToEtsClassSignature(method.enclosingClass),
        sub = EtsMethodSubSignature(
            name = method.name,
            parameters = method.parameters.mapIndexed { index, param ->
                EtsMethodParameter(
                    index = index,
                    name = param.name,
                    type = convertToEtsType(param.type),
                    isOptional = param.isOptional
                )
            },
            returnType = convertToEtsType(method.returnType),
        )
    )
}

fun convertToEtsMethod(method: MethodDto): EtsMethod {
    val signature = convertToEtsMethodSignature(method.signature)
    // Note: locals are not used in the current implementation
    // val locals = method.body.locals.map {
    //     convertToEtsEntity(it) as EtsLocal  // safe cast
    // }
    val builder = EtsMethodBuilder(signature)
    val etsMethod = builder.build(method.body.cfg)
    return etsMethod
}

fun convertToEtsField(field: FieldDto): EtsField {
    return EtsFieldImpl(
        signature = EtsFieldSignature(
            enclosingClass = convertToEtsClassSignature(field.signature.enclosingClass),
            sub = EtsFieldSubSignature(
                name = field.signature.name,
                type = convertToEtsType(field.signature.fieldType)
            )
        ),
        // TODO: decorators = field.modifiers...
        isOptional = field.isOptional,
        isDefinitelyAssigned = field.isDefinitelyAssigned,
        initializer = null, // TODO: handle initializer - assign in constructor
    )
}

fun convertToEtsClass(clazz: ClassDto): EtsClass {
    return EtsClassImpl(
        signature = EtsClassSignature(
            name = clazz.signature.name,
            namespace = null, // TODO
            file = null, // TODO
        ),
        fields = clazz.fields.map { convertToEtsField(it) },
        methods = clazz.methods.map { convertToEtsMethod(it) },
    )
}

fun convertToEtsFile(file: EtsFileDto): EtsFile {
    val classesFromNamespaces = file.namespaces.flatMap { it.classes }
    val allClasses = file.classes + classesFromNamespaces
    val convertedClasses = allClasses.map { convertToEtsClass(it) }
    return EtsFile(
        name = file.name,
        path = file.absoluteFilePath,
        classes = convertedClasses,
    )
}

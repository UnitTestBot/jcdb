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

package org.jacodb.panda.dynamic.api

import org.jacodb.api.common.cfg.CommonArrayAccess
import org.jacodb.api.common.cfg.CommonFieldRef
import org.jacodb.api.common.cfg.CommonThis
import org.jacodb.api.common.cfg.CommonValue

interface PandaValue : PandaExpr, CommonValue

interface PandaSimpleValue : PandaValue {
    override val operands: List<PandaValue>
        get() = emptyList()
}

interface PandaLocal : PandaSimpleValue {
    // TODO: val name: String
}

class PandaLocalVar(
    val index: Int,
    override val type: PandaType = PandaAnyType, // TODO: remove default value
) : PandaLocal {

    override fun toString(): String = "%$index"

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaLocalVar(this)
    }
}

class PandaThis(
    override val type: PandaType,
) : PandaLocal, CommonThis {

    override fun toString(): String = "this"

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaThis(this)
    }
}

class PandaArgument(val id: Int) : PandaSimpleValue {
    override val type: PandaType
        get() = PandaAnyType

    override fun toString() = "arg $id"

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaArgument(this)
    }
}

interface PandaConstant : PandaSimpleValue

class TODOConstant(val value: String?) : PandaConstant {
    override val type: PandaType
        get() = PandaAnyType

    override fun toString() = value?.let { "\"$it\"" } ?: "null"

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaTODOConstant(this)
    }
}

class PandaNumberConstant(val value: Int) : PandaConstant {
    override val type: PandaType
        get() = PandaNumberType

    override fun toString() = value.toString()

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaNumberConstant(this)
    }
}

class PandaStringConstant(val value: String) : PandaConstant {
    override val type: PandaType
        get() = PandaAnyType

    override fun toString() = "\"$value\""

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaStringConstant(this)
    }
}

object PandaUndefinedConstant : PandaConstant {
    override val type: PandaType
        get() = PandaUndefinedType

    override fun toString(): String = "undefined"

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaUndefinedConstant(this)
    }
}

object PandaNullConstant : PandaConstant {
    override val operands: List<PandaValue>
        get() = emptyList()

    // actually "typeof null" is "object", so consider smth like PandaClassType but PandaObjectType
    override val type: PandaType
        get() = PandaAnyType

    override fun toString(): String = "null"

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaNullConstant(this)
    }
}

interface PandaComplexValue : PandaValue

data class PandaFieldRef(
    override val instance: PandaValue?, // null for static fields
    override val classField: PandaField,
    override val type: PandaType,
) : PandaComplexValue, CommonFieldRef {
    override val operands: List<PandaValue>
        get() = listOfNotNull(instance)

    override fun toString(): String = "${instance ?: classField.enclosingClass.simpleName}.${classField.name}"

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaFieldRef(this)
    }
}

data class PandaArrayAccess(
    override val array: PandaValue,
    override val index: PandaValue,
    override val type: PandaType,
) : PandaComplexValue, CommonArrayAccess {
    override val operands: List<PandaValue>
        get() = listOf(array, index)

    override fun toString(): String = "$array[$index]"

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaArrayAccess(this)
    }
}

interface PandaInstanceCallValue : PandaComplexValue {

    val instance: PandaValue

    val obj: PandaValue

    fun getClassAndMethodName(): List<String>
}

// used to connect loaded object to instance
class PandaLoadedValue(
    override val instance: PandaValue,
    override val obj: PandaValue
) : PandaInstanceCallValue {

    override val type: PandaType
        get() = PandaAnyType
    override val operands: List<PandaValue>
        get() = listOf(instance, obj)

    override fun getClassAndMethodName(): List<String> {
        assert(instance is PandaStringConstant)
        assert(obj is PandaStringConstant)

        return listOf((instance as PandaStringConstant).value, (obj as PandaStringConstant).value)
    }

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        return visitor.visitPandaLoadedValue(this)
    }

    override fun toString() = "Loaded[$instance.$obj]"
}

class PandaInstanceCallValueImpl(
    override val instance: PandaValue,
    override val obj: PandaValue
) : PandaInstanceCallValue {
    override val type: PandaType
        get() = PandaAnyType
    override val operands: List<PandaValue>
        get() = listOf(instance, obj)

    override fun getClassAndMethodName(): List<String> {
        assert(obj is PandaStringConstant)

        return listOf(instance.resolve(), (obj as PandaStringConstant).value)
    }

    private fun PandaValue.resolve(): String {
        return when(this) {
            is PandaStringConstant -> this.value
            is PandaLocalVar -> (this.type as PandaClassType).typeName
            is PandaThis -> (this.type as PandaClassType).typeName
            else -> throw IllegalArgumentException("couldn't resolve $this")
        }
    }

    override fun <T> accept(visitor: PandaExprVisitor<T>): T {
        TODO("Not yet implemented")
    }
}
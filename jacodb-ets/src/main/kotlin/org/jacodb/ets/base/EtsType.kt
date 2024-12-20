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

package org.jacodb.ets.base

import org.jacodb.api.common.CommonType
import org.jacodb.api.common.CommonTypeName
import org.jacodb.ets.model.EtsClassSignature
import org.jacodb.ets.model.EtsLocalSignature
import org.jacodb.ets.model.EtsMethodSignature
import org.jacodb.ets.model.EtsNamespaceSignature

interface EtsType : CommonType, CommonTypeName {
    override val typeName: String

    override val nullable: Boolean?
        get() = false

    interface Visitor<out R> {
        fun visit(type: EtsAnyType): R
        fun visit(type: EtsUnknownType): R
        fun visit(type: EtsUnionType): R
        fun visit(type: EtsTupleType): R
        fun visit(type: EtsBooleanType): R
        fun visit(type: EtsNumberType): R
        fun visit(type: EtsStringType): R
        fun visit(type: EtsNullType): R
        fun visit(type: EtsUndefinedType): R
        fun visit(type: EtsVoidType): R
        fun visit(type: EtsNeverType): R
        fun visit(type: EtsLiteralType): R
        fun visit(type: EtsClassType): R
        fun visit(type: EtsFunctionType): R
        fun visit(type: EtsArrayType): R
        fun visit(type: EtsArrayObjectType): R
        fun visit(type: EtsUnclearRefType): R
        fun visit(type: EtsGenericType): R
        fun visit(type: EtsAliasType): R
        fun visit(type: EtsAnnotationNamespaceType): R
        fun visit(type: EtsAnnotationTypeQueryType): R

        interface Default<R> : Visitor<R> {
            override fun visit(type: EtsAnyType): R = defaultVisit(type)
            override fun visit(type: EtsUnknownType): R = defaultVisit(type)
            override fun visit(type: EtsUnionType): R = defaultVisit(type)
            override fun visit(type: EtsTupleType): R = defaultVisit(type)
            override fun visit(type: EtsBooleanType): R = defaultVisit(type)
            override fun visit(type: EtsNumberType): R = defaultVisit(type)
            override fun visit(type: EtsStringType): R = defaultVisit(type)
            override fun visit(type: EtsNullType): R = defaultVisit(type)
            override fun visit(type: EtsUndefinedType): R = defaultVisit(type)
            override fun visit(type: EtsVoidType): R = defaultVisit(type)
            override fun visit(type: EtsNeverType): R = defaultVisit(type)
            override fun visit(type: EtsLiteralType): R = defaultVisit(type)
            override fun visit(type: EtsClassType): R = defaultVisit(type)
            override fun visit(type: EtsFunctionType): R = defaultVisit(type)
            override fun visit(type: EtsArrayType): R = defaultVisit(type)
            override fun visit(type: EtsArrayObjectType): R = defaultVisit(type)
            override fun visit(type: EtsUnclearRefType): R = defaultVisit(type)
            override fun visit(type: EtsGenericType): R = defaultVisit(type)
            override fun visit(type: EtsAliasType): R = defaultVisit(type)
            override fun visit(type: EtsAnnotationNamespaceType): R = defaultVisit(type)
            override fun visit(type: EtsAnnotationTypeQueryType): R = defaultVisit(type)

            fun defaultVisit(type: EtsType): R
        }
    }

    fun <R> accept(visitor: Visitor<R>): R
}

object EtsAnyType : EtsType {
    override val typeName: String
        get() = "any"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

object EtsUnknownType : EtsType {
    override val typeName: String
        get() = "unknown"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsUnionType(
    val types: List<EtsType>,
) : EtsType {
    override val typeName: String
        get() = types.joinToString(separator = " | ") { it.typeName }

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsTupleType(
    val types: List<EtsType>,
) : EtsType {
    override val typeName: String
        get() = types.joinToString(prefix = "[", postfix = "]") { it.typeName }

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

interface EtsPrimitiveType : EtsType

object EtsBooleanType : EtsPrimitiveType {
    override val typeName: String
        get() = "boolean"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

object EtsNumberType : EtsPrimitiveType {
    override val typeName: String
        get() = "number"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

object EtsStringType : EtsPrimitiveType {
    override val typeName: String
        get() = "string"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

object EtsNullType : EtsPrimitiveType {
    override val typeName: String
        get() = "null"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

object EtsUndefinedType : EtsPrimitiveType {
    override val typeName: String
        get() = "undefined"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

object EtsVoidType : EtsPrimitiveType {
    override val typeName: String
        get() = "void"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

object EtsNeverType : EtsPrimitiveType {
    override val typeName: String
        get() = "never"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsLiteralType(
    val literalTypeName: String,
) : EtsPrimitiveType {
    override val typeName: String
        get() = "literal"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

interface EtsRefType : EtsType

data class EtsClassType(
    val signature: EtsClassSignature,
    val typeParameters: List<EtsType> = emptyList(),
) : EtsRefType {
    override val typeName: String
        get() = if (typeParameters.isNotEmpty()) {
            val generics = typeParameters.joinToString()
            "${signature.name}<$generics>"
        } else {
            signature.name
        }

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsFunctionType(
    val method: EtsMethodSignature,
    val typeParameters: List<EtsType> = emptyList(),
) : EtsRefType {
    override val typeName: String
        get() = if (typeParameters.isNotEmpty()) {
            val generics = typeParameters.joinToString()
            "${method.name}<$generics>"
        } else {
            method.name
        }

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsArrayType(
    val elementType: EtsType,
    val dimensions: Int,
) : EtsRefType {
    override val typeName: String
        get() = elementType.typeName + "[]".repeat(dimensions)

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsArrayObjectType(
    val elementType: EtsType,
) : EtsRefType {
    override val typeName: String
        get() = "Array<${elementType.typeName}>"

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsUnclearRefType(
    val name: String,
    val typeParameters: List<EtsType> = emptyList(),
) : EtsRefType {
    override val typeName: String
        get() = if (typeParameters.isNotEmpty()) {
            val generics = typeParameters.joinToString()
            "$name<$generics>"
        } else {
            name
        }

    override fun toString(): String = typeName

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsGenericType(
    val name: String,
    val defaultType: EtsType? = null,
    val constraint: EtsType? = null,
) : EtsRefType {
    override val typeName: String
        get() = name

    override fun toString(): String {
        return name + (constraint?.let { " extends $it" } ?: "") + (defaultType?.let { " = $it" } ?: "")
    }

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsAliasType(
    val name: String,
    val originalType: EtsType,
    val signature: EtsLocalSignature,
) : EtsType {
    override val typeName: String
        get() = name

    override fun toString(): String {
        return "$name = $originalType"
    }

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsAnnotationNamespaceType(
    val originType: String,
    val namespaceSignature: EtsNamespaceSignature,
) : EtsType {
    override val typeName: String
        get() = originType

    override fun toString(): String {
        return originType
    }

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

data class EtsAnnotationTypeQueryType(
    val originType: String,
) : EtsType {
    override val typeName: String
        get() = originType

    override fun toString(): String {
        return originType
    }

    override fun <R> accept(visitor: EtsType.Visitor<R>): R {
        return visitor.visit(this)
    }
}

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

package org.jacodb.ets.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("_")
sealed interface TypeDto

@Serializable
@SerialName("AnyType")
object AnyTypeDto : TypeDto {
    override fun toString(): String {
        return "any"
    }
}

@Serializable
@SerialName("UnknownType")
object UnknownTypeDto : TypeDto {
    override fun toString(): String {
        return "unknown"
    }
}

@Serializable
@SerialName("VoidType")
object VoidTypeDto : TypeDto {
    override fun toString(): String {
        return "void"
    }
}

@Serializable
@SerialName("NeverType")
object NeverTypeDto : TypeDto {
    override fun toString(): String {
        return "never"
    }
}

@Serializable
@SerialName("UnionType")
data class UnionTypeDto(
    val types: List<TypeDto>,
) : TypeDto {
    override fun toString(): String {
        return types.joinToString(" | ")
    }
}

@Serializable
@SerialName("TupleType")
data class TupleTypeDto(
    val types: List<TypeDto>,
) : TypeDto {
    override fun toString(): String {
        return "[${types.joinToString()}]"
    }
}

@Serializable
sealed interface PrimitiveTypeDto : TypeDto {
    val name: String
}

@Serializable
@SerialName("BooleanType")
object BooleanTypeDto : PrimitiveTypeDto {
    override val name: String
        get() = "boolean"

    override fun toString(): String {
        return name
    }
}

@Serializable
@SerialName("NumberType")
object NumberTypeDto : PrimitiveTypeDto {
    override val name: String
        get() = "number"

    override fun toString(): String {
        return name
    }
}

@Serializable
@SerialName("StringType")
object StringTypeDto : PrimitiveTypeDto {
    override val name: String
        get() = "string"

    override fun toString(): String {
        return name
    }
}

@Serializable
@SerialName("NullType")
object NullTypeDto : PrimitiveTypeDto {
    override val name: String
        get() = "null"

    override fun toString(): String {
        return name
    }
}

@Serializable
@SerialName("UndefinedType")
object UndefinedTypeDto : PrimitiveTypeDto {
    override val name: String
        get() = "undefined"

    override fun toString(): String {
        return name
    }
}

@Serializable
@SerialName("LiteralType")
data class LiteralTypeDto(
    val literal: PrimitiveLiteralDto,
) : PrimitiveTypeDto {
    override val name: String
        get() = literal.toString()

    override fun toString(): String {
        return name
    }
}

@Serializable(with = PrimitiveLiteralSerializer::class)
sealed class PrimitiveLiteralDto {
    data class StringLiteral(val value: String) : PrimitiveLiteralDto() {
        override fun toString(): String = value
    }

    data class NumberLiteral(val value: Double) : PrimitiveLiteralDto() {
        override fun toString(): String = value.toString()
    }

    data class BooleanLiteral(val value: Boolean) : PrimitiveLiteralDto() {
        override fun toString(): String = value.toString()
    }
}

@Serializable
@SerialName("ClassType")
data class ClassTypeDto(
    val signature: ClassSignatureDto,
    val typeParameters: List<TypeDto> = emptyList(),
) : TypeDto {
    override fun toString(): String {
        return if (typeParameters.isNotEmpty()) {
            val generics = typeParameters.joinToString()
            "$signature<$generics>"
        } else {
            signature.toString()
        }
    }
}

@Serializable
@SerialName("FunctionType")
data class FunctionTypeDto(
    val signature: MethodSignatureDto,
    val typeParameters: List<TypeDto> = emptyList(),
) : TypeDto {
    override fun toString(): String {
        val params = signature.parameters.joinToString()
        return if (typeParameters.isNotEmpty()) {
            val generics = typeParameters.joinToString()
            "${signature.name}<$generics>($params): ${signature.returnType}"
        } else {
            "${signature.name}($params): ${signature.returnType}"
        }
    }
}

@Serializable
@SerialName("ArrayType")
data class ArrayTypeDto(
    val elementType: TypeDto,
    val dimensions: Int,
) : TypeDto {
    override fun toString(): String {
        return "$elementType" + "[]".repeat(dimensions)
    }
}

@Serializable
@SerialName("UnclearReferenceType")
data class UnclearReferenceTypeDto(
    val name: String,
    val typeParameters: List<TypeDto> = emptyList(),
) : TypeDto {
    override fun toString(): String {
        return if (typeParameters.isNotEmpty()) {
            val generics = typeParameters.joinToString()
            "$name<$generics>"
        } else {
            name
        }
    }
}

@Serializable
@SerialName("GenericType")
data class GenericTypeDto(
    val name: String,
    val defaultType: TypeDto? = null,
    val constraint: TypeDto? = null,
) : TypeDto {
    override fun toString(): String {
        return name + (constraint?.let { " extends $it" } ?: "") + (defaultType?.let { " = $it" } ?: "")
    }
}

@Serializable
@SerialName("AliasType")
data class AliasTypeDto(
    val name: String,
    val originalType: TypeDto,
    val signature: LocalSignatureDto,
) : TypeDto {
    override fun toString(): String {
        return "$name = $originalType"
    }
}

@Serializable
@SerialName("AnnotationNamespaceType")
data class AnnotationNamespaceTypeDto(
    val originType: String,
    val namespaceSignature: NamespaceSignatureDto,
) : TypeDto {
    override fun toString(): String {
        return originType
    }
}

@Serializable
@SerialName("AnnotationTypeQueryType")
data class AnnotationTypeQueryTypeDto(
    val originType: String,
) : TypeDto {
    override fun toString(): String {
        return originType
    }
}

@Serializable
@SerialName("LexicalEnvType")
data class LexicalEnvTypeDto(
    val nestedMethod: MethodSignatureDto,
    val closures: List<LocalDto>,
) : TypeDto {
    override fun toString(): String {
        return closures.joinToString(prefix = "[", postfix = "]")
    }
}

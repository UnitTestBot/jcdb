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

package org.utbot.jacodb.impl.types.signature

import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter
import org.utbot.jacodb.api.JcAnnotation
import org.utbot.jacodb.impl.bytecode.isNullable

internal fun JvmType.copyWith(nullability: Boolean?, annotations: List<JcAnnotation> = this.annotations): JvmType =
    when (this) {
        is JvmArrayType -> JvmArrayType(elementType, nullability, annotations)
        is JvmClassRefType -> JvmClassRefType(name, nullability, annotations)
        is JvmParameterizedType.JvmNestedType -> JvmParameterizedType.JvmNestedType(name, parameterTypes, ownerType, nullability, annotations)
        is JvmParameterizedType -> JvmParameterizedType(name, parameterTypes, nullability, annotations)

        is JvmTypeVariable -> JvmTypeVariable(symbol, nullability, annotations).also {
            it.declaration = declaration
        }

        is JvmWildcard -> {
            if (nullability != true)
                error("Attempting to make wildcard not-nullable, which are always nullable by convention")
            if (annotations.isNotEmpty())
                error("Annotations on wildcards are not supported")
            this
        }

        is JvmPrimitiveType -> {
            if (nullability != false)
                error("Attempting to make a nullable primitive")
            JvmPrimitiveType(ref, annotations)
        }
    }

internal fun JvmType.relaxWithKmType(kmType: KmType): JvmType =
    when (this) {
        is JvmArrayType -> {
            // NB: kmType may have zero (for primitive arrays) or one (for object arrays) argument
            val updatedElementType = kmType.arguments.singleOrNull()?.type?.let {
                elementType.relaxWithKmType(it)
            } ?: elementType

            JvmArrayType(updatedElementType, kmType.isNullable, annotations.toList())
        }

        is JvmParameterizedType.JvmNestedType -> {
            val relaxedParameterTypes = parameterTypes.relaxAll(kmType.arguments.map { it.type })
            JvmParameterizedType.JvmNestedType(name, relaxedParameterTypes, ownerType, kmType.isNullable, annotations.toList())
        }

        is JvmParameterizedType -> {
            val relaxedParameterTypes = parameterTypes.relaxAll(kmType.arguments.map { it.type })
            JvmParameterizedType(name, relaxedParameterTypes, kmType.isNullable, annotations.toList())
        }

        is JvmBoundWildcard.JvmUpperBoundWildcard -> {
            // Kotlin metadata is constructed in terms of projections => there is no explicit type for wildcard.
            // Therefore, we don't look for kmType.arguments and relax bound with kmType directly, not with kmType.arguments.single()
            // Same applies to JvmLowerBoundWildcard.relaxWithKmType
            JvmBoundWildcard.JvmUpperBoundWildcard(bound.relaxWithKmType(kmType))
        }

        is JvmBoundWildcard.JvmLowerBoundWildcard ->
            JvmBoundWildcard.JvmLowerBoundWildcard(bound.relaxWithKmType(kmType))

        else -> copyWith(kmType.isNullable) // default implementation for many of JvmTypes
    }

internal fun JvmTypeParameterDeclarationImpl.relaxWithKmTypeParameter(kmTypeParameter: KmTypeParameter): JvmTypeParameterDeclaration {
    val newBounds = bounds?.zip(kmTypeParameter.upperBounds) { bound, kmType ->
        bound.relaxWithKmType(kmType)
    }
    return JvmTypeParameterDeclarationImpl(symbol, owner, newBounds)
}

private fun Iterable<JvmType>.relaxAll(kmTypes: List<KmType?>): List<JvmType> =
    zip(kmTypes) { type, kmType ->
        kmType?.let {
            type.relaxWithKmType(it)
        } ?: type
    }
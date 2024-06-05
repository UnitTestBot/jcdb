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

package org.jacodb.panda.dynamic.ark.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClassSignature(
    val name: String,
)

@Serializable
data class FieldSignature(
    val enclosingClass: ClassSignature,
    val name: String,
    val fieldType: String,
    val optional: Boolean = false,
)

@Serializable
data class MethodSignature(
    val enclosingClass: ClassSignature,
    val name: String,
    val parameters: List<MethodParameter>,
    val returnType: String,
)

@Serializable
data class MethodParameter(
    val name: String,
    val type: String,
    val optional: Boolean = false,
)

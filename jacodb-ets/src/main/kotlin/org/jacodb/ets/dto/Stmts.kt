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
import kotlinx.serialization.json.JsonObject

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("_")
sealed interface StmtDto

@Serializable(with = RawStmtSerializer::class)
@SerialName("RawStmt")
@OptIn(ExperimentalSerializationApi::class)
data class RawStmtDto(
    val kind: String, // constructor name
    val extra: JsonObject,
) : StmtDto

@Serializable
@SerialName("NopStmt")
data object NopStmtDto : StmtDto

@Serializable
@SerialName("AssignStmt")
data class AssignStmtDto(
    val left: ValueDto,
    val right: ValueDto,
) : StmtDto

@Serializable
@SerialName("CallStmt")
data class CallStmtDto(
    val expr: CallExprDto,
) : StmtDto

@Serializable
sealed interface TerminatingStmtDto : StmtDto

@Serializable
@SerialName("ReturnVoidStmt")
data object ReturnVoidStmtDto : TerminatingStmtDto

@Serializable
@SerialName("ReturnStmt")
data class ReturnStmtDto(
    val arg: ValueDto,
) : TerminatingStmtDto

@Serializable
@SerialName("ThrowStmt")
data class ThrowStmtDto(
    val arg: ValueDto,
) : TerminatingStmtDto

@Serializable
sealed interface BranchingStmtDto : StmtDto

@Serializable
@SerialName("GotoStmt")
data object GotoStmtDto : BranchingStmtDto

@Serializable
@SerialName("IfStmt")
data class IfStmtDto(
    val condition: ConditionExprDto,
) : BranchingStmtDto

@Serializable
@SerialName("SwitchStmt")
data class SwitchStmtDto(
    val arg: ValueDto,
    val cases: List<ValueDto>,
) : BranchingStmtDto

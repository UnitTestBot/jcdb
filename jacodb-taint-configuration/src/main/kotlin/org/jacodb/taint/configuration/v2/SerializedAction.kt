package org.jacodb.taint.configuration.v2

import kotlinx.serialization.Serializable

@Serializable
data class SerializedTaintAssignAction(
    val kind: String,
    val annotatedWith: SerializedNameMatcher? = null,
    val pos: PositionBaseWithModifiers,
)

@Serializable
data class SerializedTaintCleanAction(
    val taintKind: String? = null,
    val pos: PositionBaseWithModifiers,
)

@Serializable
data class SerializedTaintPassAction(
    val taintKind: String? = null,
    val from: PositionBaseWithModifiers,
    val to: PositionBaseWithModifiers,
)

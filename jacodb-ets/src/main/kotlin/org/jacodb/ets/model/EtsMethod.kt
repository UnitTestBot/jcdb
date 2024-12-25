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

@file:Suppress("PropertyName")

package org.jacodb.ets.model

import org.jacodb.api.common.CommonMethod
import org.jacodb.ets.base.EtsLocal
import org.jacodb.ets.base.EtsType
import org.jacodb.ets.graph.EtsCfg

// TODO: typeParameters
interface EtsMethod : EtsBaseModel, CommonMethod {
    val signature: EtsMethodSignature
    val typeParameters: List<EtsType>
    val locals: List<EtsLocal>
    val cfg: EtsCfg

    val enclosingClass: EtsClassSignature
        get() = signature.enclosingClass

    // If not specified, entity is public if not private and not protected
    override val isPublic: Boolean
        get() = super.isPublic || (!isPrivate && !isProtected)

    override val name: String
        get() = signature.name

    override val parameters: List<EtsMethodParameter>
        get() = signature.parameters

    override val returnType: EtsType
        get() = signature.returnType

    override fun flowGraph(): EtsCfg {
        return cfg
    }
}

class EtsMethodImpl(
    override val signature: EtsMethodSignature,
    override val typeParameters: List<EtsType> = emptyList(),
    override val locals: List<EtsLocal> = emptyList(),
    override val modifiers: EtsModifiers = EtsModifiers.EMPTY,
    override val decorators: List<EtsDecorator> = emptyList(),
) : EtsMethod {
    var _cfg: EtsCfg? = null

    override val cfg: EtsCfg
        get() = _cfg ?: EtsCfg.empty()

    override fun toString(): String {
        return signature.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EtsMethodImpl

        return signature == other.signature
    }

    override fun hashCode(): Int {
        return signature.hashCode()
    }
}

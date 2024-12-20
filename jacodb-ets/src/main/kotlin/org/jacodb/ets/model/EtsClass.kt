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

package org.jacodb.ets.model

import org.jacodb.ets.base.EtsType

interface EtsClass : EtsBaseModel {
    val signature: EtsClassSignature
    val typeParameters: List<EtsType>
    val fields: List<EtsField>
    val methods: List<EtsMethod>
    val ctor: EtsMethod
    val superClass: EtsClassSignature?
    val implementedInterfaces: List<EtsClassSignature>

    val name: String
        get() = signature.name
}

class EtsClassImpl(
    override val signature: EtsClassSignature,
    override val fields: List<EtsField>,
    override val methods: List<EtsMethod>,
    override val ctor: EtsMethod,
    override val superClass: EtsClassSignature? = null,
    override val implementedInterfaces: List<EtsClassSignature> = emptyList(),
    override val typeParameters: List<EtsType> = emptyList(),
    override val modifiers: EtsModifiers = EtsModifiers.EMPTY,
    override val decorators: List<EtsDecorator> = emptyList(),
) : EtsClass {
    init {
        require(ctor !in methods)
    }

    override fun toString(): String {
        return signature.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EtsClassImpl

        return signature == other.signature
    }

    override fun hashCode(): Int {
        return signature.hashCode()
    }
}

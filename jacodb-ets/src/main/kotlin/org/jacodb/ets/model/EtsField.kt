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

// for '!' field marker ("definitely assigned field"), see https://www.typescriptlang.org/docs/handbook/2/classes.html#--strictpropertyinitialization

interface EtsField {
    val enclosingClass: EtsClass?
    val signature: EtsFieldSignature

    val name: String
        get() = signature.name

    val type: EtsType
        get() = signature.type
}

class EtsFieldImpl(
    override val signature: EtsFieldSignature,
    val modifiers: EtsModifiers = EtsModifiers.EMPTY,
    val isOptional: Boolean = false,  // '?'
    val isDefinitelyAssigned: Boolean = false, // '!'
) : EtsField {

    override var enclosingClass: EtsClass? = null

    override fun toString(): String {
        return signature.toString()
    }
}

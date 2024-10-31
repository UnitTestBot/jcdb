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

interface EtsBaseModel {
    val modifiers: EtsModifiers
    val decorators: List<EtsDecorator>

    val isPrivate: Boolean get() = modifiers.isPrivate
    val isProtected: Boolean get() = modifiers.isProtected
    val isPublic: Boolean get() = modifiers.isPublic
    val isExport: Boolean get() = modifiers.isExport
    val isStatic: Boolean get() = modifiers.isStatic
    val isAbstract: Boolean get() = modifiers.isAbstract
    val isAsync: Boolean get() = modifiers.isAsync
    val isConst: Boolean get() = modifiers.isConst
    val isAccessor: Boolean get() = modifiers.isAccessor
    val isDefault: Boolean get() = modifiers.isDefault
    val isIn: Boolean get() = modifiers.isIn
    val isReadonly: Boolean get() = modifiers.isReadonly
    val isOut: Boolean get() = modifiers.isOut
    val isOverride: Boolean get() = modifiers.isOverride
    val isDeclare: Boolean get() = modifiers.isDeclare

    fun hasModifier(modifier: EtsModifier): Boolean = modifiers.hasModifier(modifier)
    fun hasDecorator(decorator: EtsDecorator): Boolean = decorators.contains(decorator)
}

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

package org.jacodb.ets.utils

import org.jacodb.ets.dto.EtsFileDto
import org.jacodb.ets.model.EtsFile

fun EtsFileDto.toText(): String {
    val lines: MutableList<String> = mutableListOf()
    lines += "EtsFileDto '${signature}':"
    classes.forEach { clazz ->
        lines += "= CLASS '${clazz.signature}':"
        lines += "  superClass = '${clazz.superClassName}'"
        lines += "  typeParameters = ${clazz.typeParameters}"
        lines += "  modifiers = ${clazz.modifiers}"
        lines += "  decorators = ${clazz.decorators}"
        lines += "  fields: ${clazz.fields.size}"
        clazz.fields.forEach { field ->
            lines += "  - FIELD '${field.signature}'"
            lines += "    modifiers = ${field.modifiers}"
            lines += "    decorators = ${field.decorators}"
            lines += "    isOptional = ${field.isOptional}"
            lines += "    isDefinitelyAssigned = ${field.isDefinitelyAssigned}"
        }
        lines += "  methods: ${clazz.methods.size}"
        clazz.methods.forEach { method ->
            lines += "  - METHOD '${method.signature}'"
            lines += "    modifiers = ${method.modifiers}"
            lines += "    decorators = ${method.decorators}"
            lines += "    typeParameters = ${method.typeParameters}"
            if (method.body != null) {
                lines += "    locals = ${method.body.locals}"
                lines += "    blocks: ${method.body.cfg.blocks.size}"
                method.body.cfg.blocks.forEach { block ->
                    lines += "    - BLOCK ${block.id}"
                    lines += "      successors = ${block.successors}"
                    lines += "      predecessors = ${block.predecessors}"
                    lines += "      statements: ${block.stmts.size}"
                    block.stmts.forEachIndexed { i, stmt ->
                        lines += "      ${i + 1}. $stmt"
                    }
                }
            }
        }
    }
    return lines.joinToString("\n")
}

fun EtsFile.toText(): String {
    val lines: MutableList<String> = mutableListOf()
    lines += "EtsFile '${signature}':"
    classes.forEach { clazz ->
        lines += "= CLASS '${clazz.signature}':"
        lines += "  typeParameters = ${clazz.typeParameters}"
        lines += "  modifiers = ${clazz.modifiers}"
        lines += "  decorators = ${clazz.decorators}"
        lines += "  superClass = '${clazz.superClass}'"
        lines += "  fields: ${clazz.fields.size}"
        clazz.fields.forEach { field ->
            lines += "  - FIELD '${field.signature}'"
        }
        lines += "  constructor = '${clazz.ctor.signature}'"
        lines += "    typeParameters = ${clazz.ctor.typeParameters}"
        lines += "    modifiers = ${clazz.ctor.modifiers}"
        lines += "    decorators = ${clazz.ctor.decorators}"
        lines += "    locals: ${clazz.ctor.locals.size}"
        lines += "    stmts: ${clazz.ctor.cfg.stmts.size}"
        clazz.ctor.cfg.stmts.forEach { stmt ->
            lines += "    ${stmt.location.index}. $stmt"
            val pad = " ".repeat("${stmt.location.index}".length + 2) // number + dot + space
            lines += "    ${pad}successors = ${clazz.ctor.cfg.successors(stmt).map { it.location.index }}"
        }
        lines += "  methods: ${clazz.methods.size}"
        clazz.methods.forEach { method ->
            lines += "  - METHOD '${method.signature}':"
            lines += "    typeParameters = ${method.typeParameters}"
            lines += "    modifiers = ${method.modifiers}"
            lines += "    decorators = ${method.decorators}"
            lines += "    locals: ${method.locals.size}"
            lines += "    stmts: ${method.cfg.stmts.size}"
            method.cfg.stmts.forEach { stmt ->
                lines += "    ${stmt.location.index}. $stmt"
                val pad = " ".repeat("${stmt.location.index}".length + 2) // number + dot + space
                lines += "    ${pad}successors = ${method.cfg.successors(stmt).map { it.location.index }}"
            }
        }
    }
    return lines.joinToString("\n")
}

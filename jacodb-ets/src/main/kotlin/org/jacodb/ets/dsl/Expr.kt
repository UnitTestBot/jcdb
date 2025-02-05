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

package org.jacodb.ets.dsl

sealed interface Expr

sealed interface Value : Expr

data class Local(val name: String) : Value {
    override fun toString() = name
}

data class Parameter(val index: Int) : Value {
    override fun toString() = "param($index)"
}

object ThisRef : Value {
    override fun toString() = "this"
}

data class Constant(val value: Double) : Value {
    override fun toString() = "const($value)"
}

enum class BinaryOperator {
    AND, OR,
    EQ, NEQ, LT, LTE, GT, GTE,
    ADD, SUB, MUL, DIV
}

data class BinaryExpr(
    val operator: BinaryOperator,
    val left: Expr,
    val right: Expr,
) : Expr {
    override fun toString() = "${operator.name.lowercase()}($left, $right)"
}

enum class UnaryOperator {
    NOT, NEG
}

data class UnaryExpr(
    val operator: UnaryOperator,
    val expr: Expr,
) : Expr {
    override fun toString() = "${operator.name.lowercase()}($expr)"
}

sealed interface CallExpr : Expr {
    val name: String
    val args: List<Expr>
}

data class FnCall(
    override val name: String,
    override val args: List<Value>,
) : CallExpr {
    override fun toString(): String {
        return "call($name, $args)"
    }
}

data class InstanceCall(
    val instance: Local,
    override val name: String,
    override val args: List<Value>,
) : CallExpr {
    override fun toString(): String {
        return "$instance.call($name, $args)"
    }
}

data class StaticCall(
    val className: String,
    override val name: String,
    override val args: List<Value>,
) : CallExpr {
    override fun toString(): String {
        return "$className.call($name, $args)"
    }
}

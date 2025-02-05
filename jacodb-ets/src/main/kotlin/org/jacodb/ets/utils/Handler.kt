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

import org.jacodb.ets.base.EtsAddExpr
import org.jacodb.ets.base.EtsAndExpr
import org.jacodb.ets.base.EtsArrayAccess
import org.jacodb.ets.base.EtsAssignStmt
import org.jacodb.ets.base.EtsAwaitExpr
import org.jacodb.ets.base.EtsBitAndExpr
import org.jacodb.ets.base.EtsBitNotExpr
import org.jacodb.ets.base.EtsBitOrExpr
import org.jacodb.ets.base.EtsBitXorExpr
import org.jacodb.ets.base.EtsBooleanConstant
import org.jacodb.ets.base.EtsCallStmt
import org.jacodb.ets.base.EtsCastExpr
import org.jacodb.ets.base.EtsCommaExpr
import org.jacodb.ets.base.EtsDeleteExpr
import org.jacodb.ets.base.EtsDivExpr
import org.jacodb.ets.base.EtsEntity
import org.jacodb.ets.base.EtsEqExpr
import org.jacodb.ets.base.EtsExpExpr
import org.jacodb.ets.base.EtsGotoStmt
import org.jacodb.ets.base.EtsGtEqExpr
import org.jacodb.ets.base.EtsGtExpr
import org.jacodb.ets.base.EtsIfStmt
import org.jacodb.ets.base.EtsInExpr
import org.jacodb.ets.base.EtsInstanceCallExpr
import org.jacodb.ets.base.EtsInstanceFieldRef
import org.jacodb.ets.base.EtsInstanceOfExpr
import org.jacodb.ets.base.EtsLeftShiftExpr
import org.jacodb.ets.base.EtsLengthExpr
import org.jacodb.ets.base.EtsLocal
import org.jacodb.ets.base.EtsLtEqExpr
import org.jacodb.ets.base.EtsLtExpr
import org.jacodb.ets.base.EtsMulExpr
import org.jacodb.ets.base.EtsNegExpr
import org.jacodb.ets.base.EtsNewArrayExpr
import org.jacodb.ets.base.EtsNewExpr
import org.jacodb.ets.base.EtsNopStmt
import org.jacodb.ets.base.EtsNotEqExpr
import org.jacodb.ets.base.EtsNotExpr
import org.jacodb.ets.base.EtsNullConstant
import org.jacodb.ets.base.EtsNullishCoalescingExpr
import org.jacodb.ets.base.EtsNumberConstant
import org.jacodb.ets.base.EtsOrExpr
import org.jacodb.ets.base.EtsParameterRef
import org.jacodb.ets.base.EtsPostDecExpr
import org.jacodb.ets.base.EtsPostIncExpr
import org.jacodb.ets.base.EtsPreDecExpr
import org.jacodb.ets.base.EtsPreIncExpr
import org.jacodb.ets.base.EtsPtrCallExpr
import org.jacodb.ets.base.EtsRemExpr
import org.jacodb.ets.base.EtsReturnStmt
import org.jacodb.ets.base.EtsRightShiftExpr
import org.jacodb.ets.base.EtsStaticCallExpr
import org.jacodb.ets.base.EtsStaticFieldRef
import org.jacodb.ets.base.EtsStmt
import org.jacodb.ets.base.EtsStrictEqExpr
import org.jacodb.ets.base.EtsStrictNotEqExpr
import org.jacodb.ets.base.EtsStringConstant
import org.jacodb.ets.base.EtsSubExpr
import org.jacodb.ets.base.EtsSwitchStmt
import org.jacodb.ets.base.EtsTernaryExpr
import org.jacodb.ets.base.EtsThis
import org.jacodb.ets.base.EtsThrowStmt
import org.jacodb.ets.base.EtsTypeOfExpr
import org.jacodb.ets.base.EtsUnaryPlusExpr
import org.jacodb.ets.base.EtsUndefinedConstant
import org.jacodb.ets.base.EtsUnsignedRightShiftExpr
import org.jacodb.ets.base.EtsVoidExpr
import org.jacodb.ets.base.EtsYieldExpr

abstract class AbstractHandler : EtsEntity.Visitor.Default<Unit>, EtsStmt.Visitor.Default<Unit> {

    abstract fun handle(value: EtsEntity)
    abstract fun handle(stmt: EtsStmt)

    final override fun defaultVisit(value: EtsEntity) {
        handle(value)
    }

    final override fun defaultVisit(stmt: EtsStmt) {
        handle(stmt)
    }

    final override fun visit(stmt: EtsNopStmt) {
        handle(stmt)
    }

    final override fun visit(stmt: EtsAssignStmt) {
        handle(stmt)
        stmt.lhv.accept(this)
        stmt.rhv.accept(this)
    }

    final override fun visit(stmt: EtsCallStmt) {
        handle(stmt)
        stmt.expr.accept(this)
    }

    final override fun visit(stmt: EtsReturnStmt) {
        handle(stmt)
        stmt.returnValue?.accept(this)
    }

    final override fun visit(stmt: EtsThrowStmt) {
        handle(stmt)
        stmt.arg.accept(this)
    }

    final override fun visit(stmt: EtsGotoStmt) {
        handle(stmt)
    }

    final override fun visit(stmt: EtsIfStmt) {
        handle(stmt)
        stmt.condition.accept(this)
    }

    final override fun visit(stmt: EtsSwitchStmt) {
        error("deprecated")
    }

    final override fun visit(value: EtsLocal) {
        handle(value)
    }

    final override fun visit(value: EtsStringConstant) {
        handle(value)
    }

    final override fun visit(value: EtsBooleanConstant) {
        handle(value)
    }

    final override fun visit(value: EtsNumberConstant) {
        handle(value)
    }

    final override fun visit(value: EtsNullConstant) {
        handle(value)
    }

    final override fun visit(value: EtsUndefinedConstant) {
        handle(value)
    }

    final override fun visit(value: EtsThis) {
        handle(value)
    }

    final override fun visit(value: EtsParameterRef) {
        handle(value)
    }

    final override fun visit(value: EtsArrayAccess) {
        handle(value)
        value.array.accept(this)
        value.index.accept(this)
    }

    final override fun visit(value: EtsInstanceFieldRef) {
        handle(value)
        value.instance.accept(this)
    }

    final override fun visit(value: EtsStaticFieldRef) {
        handle(value)
    }

    final override fun visit(expr: EtsNewExpr) {
        handle(expr)
    }

    final override fun visit(expr: EtsNewArrayExpr) {
        handle(expr)
        expr.size.accept(this)
    }

    final override fun visit(expr: EtsLengthExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsCastExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsInstanceOfExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsDeleteExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsAwaitExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsYieldExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsTypeOfExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsVoidExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsNotExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsBitNotExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsNegExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsUnaryPlusExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsPreIncExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsPreDecExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsPostIncExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsPostDecExpr) {
        handle(expr)
        expr.arg.accept(this)
    }

    final override fun visit(expr: EtsEqExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsNotEqExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsStrictEqExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsStrictNotEqExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsLtExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsLtEqExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsGtExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsGtEqExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsInExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsAddExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsSubExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsMulExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsDivExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsRemExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsExpExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsBitAndExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsBitOrExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsBitXorExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsLeftShiftExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsRightShiftExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsUnsignedRightShiftExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsAndExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsOrExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsNullishCoalescingExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsInstanceCallExpr) {
        handle(expr)
        expr.instance.accept(this)
        expr.args.forEach { it.accept(this) }
    }

    final override fun visit(expr: EtsStaticCallExpr) {
        handle(expr)
        expr.args.forEach { it.accept(this) }
    }

    final override fun visit(expr: EtsPtrCallExpr) {
        handle(expr)
        expr.ptr.accept(this)
        expr.args.forEach { it.accept(this) }
    }

    final override fun visit(expr: EtsCommaExpr) {
        handle(expr)
        expr.left.accept(this)
        expr.right.accept(this)
    }

    final override fun visit(expr: EtsTernaryExpr) {
        handle(expr)
        expr.condition.accept(this)
        expr.thenExpr.accept(this)
        expr.elseExpr.accept(this)
    }
}

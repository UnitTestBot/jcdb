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

package org.jacodb.analysis.npe

import org.jacodb.analysis.config.CallPositionToValueResolver
import org.jacodb.analysis.config.FactAwareConditionEvaluator
import org.jacodb.analysis.ifds.Analyzer
import org.jacodb.analysis.ifds.Reason
import org.jacodb.analysis.taint.EdgeForOtherRunner
import org.jacodb.analysis.taint.NewSummaryEdge
import org.jacodb.analysis.taint.NewVulnerability
import org.jacodb.analysis.taint.TaintDomainFact
import org.jacodb.analysis.taint.TaintEdge
import org.jacodb.analysis.taint.TaintEvent
import org.jacodb.analysis.taint.TaintVertex
import org.jacodb.analysis.taint.TaintVulnerability
import org.jacodb.analysis.taint.Tainted
import org.jacodb.analysis.util.Traits
import org.jacodb.api.common.CommonMethod
import org.jacodb.api.common.analysis.ApplicationGraph
import org.jacodb.api.common.cfg.CommonInst
import org.jacodb.api.jvm.JcMethod
import org.jacodb.taint.configuration.TaintConfigurationFeature
import org.jacodb.taint.configuration.TaintMark
import org.jacodb.taint.configuration.TaintMethodSink

private val logger = mu.KotlinLogging.logger {}

context(Traits<Method, Statement>)
class NpeAnalyzer<Method, Statement>(
    private val graph: ApplicationGraph<Method, Statement>,
) : Analyzer<TaintDomainFact, TaintEvent<Statement>, Method, Statement>
    where Method : CommonMethod,
          Statement : CommonInst {

    override val flowFunctions: ForwardNpeFlowFunctions<Method, Statement> by lazy {
        ForwardNpeFlowFunctions(graph)
    }

    private val taintConfigurationFeature: TaintConfigurationFeature?
        get() = flowFunctions.taintConfigurationFeature

    private fun isExitPoint(statement: Statement): Boolean {
        return statement in graph.exitPoints(graph.methodOf(statement))
    }

    override fun handleNewEdge(
        edge: TaintEdge<Statement>,
    ): List<TaintEvent<Statement>> = buildList {
        if (isExitPoint(edge.to.statement)) {
            add(NewSummaryEdge(edge))
        }

        if (edge.to.fact is Tainted && edge.to.fact.mark == TaintMark.NULLNESS) {
            if (edge.to.fact.variable.isDereferencedAt(edge.to.statement)) {
                val message = "NPE" // TODO
                val vulnerability = TaintVulnerability(message, sink = edge.to)
                logger.info {
                    val m = graph.methodOf(vulnerability.sink.statement)
                    "Found sink=${vulnerability.sink} in $m"
                }
                add(NewVulnerability(vulnerability))
            }
        }

        run {
            val callExpr = edge.to.statement.getCallExpr() ?: return@run
            val callee = callExpr.callee

            val config = taintConfigurationFeature?.let { feature ->
                if (callee is JcMethod) {
                    logger.trace { "Extracting config for $callee" }
                    feature.getConfigForMethod(callee)
                } else {
                    error("Cannot extract config for $callee")
                }
            } ?: return@run

            // TODO: not always we want to skip sinks on Zero facts.
            //  Some rules might have ConstantTrue or just true (when evaluated with Zero fact) condition.
            if (edge.to.fact !is Tainted) {
                return@run
            }

            // Determine whether 'edge.to' is a sink via config:
            val conditionEvaluator = FactAwareConditionEvaluator(
                edge.to.fact,
                CallPositionToValueResolver(edge.to.statement),
            )
            for (item in config.filterIsInstance<TaintMethodSink>()) {
                if (item.condition.accept(conditionEvaluator)) {
                    val message = item.ruleNote
                    val vulnerability = TaintVulnerability(message, sink = edge.to, rule = item)
                    logger.trace {
                        val m = graph.methodOf(vulnerability.sink.statement)
                        "Found sink=${vulnerability.sink} in $m on $item"
                    }
                    add(NewVulnerability(vulnerability))
                }
            }
        }
    }

    override fun handleCrossUnitCall(
        caller: TaintVertex<Statement>,
        callee: TaintVertex<Statement>,
    ): List<TaintEvent<Statement>> = buildList {
        add(EdgeForOtherRunner(TaintEdge(callee, callee), Reason.CrossUnitCall(caller)))
    }
}

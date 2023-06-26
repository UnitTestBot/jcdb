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

package org.jacodb.analysis.impl

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.jacodb.analysis.JcNaiveDevirtualizerFactory
import org.jacodb.analysis.JcSimplifiedGraphFactory
import org.jacodb.analysis.analyzers.NpeAnalyzer
import org.jacodb.analysis.analyzers.NpePrecalcBackwardAnalyzer
import org.jacodb.analysis.analyzers.UnusedVariableAnalyzer
import org.jacodb.analysis.engine.ClassUnitResolver
import org.jacodb.analysis.engine.IfdsInstanceFactory
import org.jacodb.analysis.engine.IfdsUnitInstance
import org.jacodb.analysis.engine.IfdsUnitTraverser
import org.jacodb.analysis.engine.IfdsWithBackwardPreSearch
import org.jacodb.analysis.engine.MethodUnitResolver
import org.jacodb.analysis.engine.UnitResolver
import org.jacodb.analysis.toDumpable
import org.jacodb.api.ext.findClass
import org.jacodb.impl.features.InMemoryHierarchy
import org.jacodb.impl.features.Usages
import org.jacodb.testing.BaseTest
import org.jacodb.testing.WithDB
import org.joda.time.DateTime
import org.junit.jupiter.api.Test

class JodaDateTimeAnalysisTest : BaseTest() {
    companion object : WithDB(Usages, InMemoryHierarchy)

    private fun testOne(unitResolver: UnitResolver<*>, ifdsInstanceFactory: IfdsInstanceFactory) {
        val clazz = cp.findClass<DateTime>()

        val graph = JcSimplifiedGraphFactory().createGraph(cp)
        val devirtualizer = JcNaiveDevirtualizerFactory.createDevirtualizer(graph)
        val engine = IfdsUnitTraverser(graph, unitResolver, devirtualizer, ifdsInstanceFactory)
        clazz.declaredMethods.forEach { engine.addStart(it) }
        val result = engine.analyze()
        val kek = result.toDumpable()

        println("Vulnerabilities found: ${kek.foundVulnerabilities.size}")
        val json = Json { prettyPrint = true }
        json.encodeToStream(kek, System.out)
    }

    @Test
    fun `test Unused variable analysis`() {
        testOne(ClassUnitResolver(false), IfdsUnitInstance.createProvider(UnusedVariableAnalyzer(graph)))
    }

    @Test
    fun `test NPE analysis`() {
//        testOne(MethodUnitResolver) { graph, devirtualizer, context, unitResolver, unit ->
//            val forward = BidiIfdsForTaintAnalysis.createProvider(
//                NpeAnalyzer(graph),
//                NpeFlowdroidBackwardAnalyzer(graph)
//            ).createInstance(graph, devirtualizer, context, unitResolver, unit)
//            val backward = IfdsUnitInstance.createProvider(NpePrecalcBackwardAnalyzer(graph))
//                .createInstance(graph.reversed, devirtualizer, context, unitResolver, unit)
//            IfdsWithBackwardPreSearch(forward, backward)
//        }
         testOne(MethodUnitResolver, IfdsWithBackwardPreSearch.createProvider(NpeAnalyzer(graph), NpePrecalcBackwardAnalyzer(graph)))
    }

    private val graph = JcSimplifiedGraphFactory().createGraph(cp)
}
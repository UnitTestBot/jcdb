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

import org.jacodb.analysis.ifds.SingletonUnitResolver
import org.jacodb.analysis.npe.NpeManager
import org.jacodb.analysis.taint.TaintManager
import org.jacodb.analysis.unused.UnusedVariableManager
import org.jacodb.api.jvm.ext.findClass
import org.jacodb.testing.WithGlobalDB
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

private val logger = mu.KotlinLogging.logger {}

class JodaDateTimeAnalysisTest : BaseAnalysisTest() {

    companion object : WithGlobalDB()

    @Test
    fun `test taint analysis`() {
        val clazz = cp.findClass<DateTime>()
        val methods = clazz.declaredMethods
        val unitResolver = SingletonUnitResolver
        val manager = with(traits) {
            TaintManager(graph, unitResolver)
        }
        val sinks = manager.analyze(methods, timeout = 60.seconds)
        logger.info { "Vulnerabilities found: ${sinks.size}" }
    }

    @Test
    fun `test NPE analysis`() {
        val clazz = cp.findClass<DateTime>()
        val methods = clazz.declaredMethods
        val unitResolver = SingletonUnitResolver
        val manager = with(traits) {
            NpeManager(graph, unitResolver)
        }
        val sinks = manager.analyze(methods, timeout = 60.seconds)
        logger.info { "Vulnerabilities found: ${sinks.size}" }
    }

    @Test
    fun `test unused variables analysis`() {
        val clazz = cp.findClass<DateTime>()
        val methods = clazz.declaredMethods
        val unitResolver = SingletonUnitResolver
        val manager = with(traits) {
            UnusedVariableManager(graph, unitResolver)
        }
        val sinks = manager.analyze(methods, timeout = 60.seconds)
        logger.info { "Unused variables found: ${sinks.size}" }
    }
}

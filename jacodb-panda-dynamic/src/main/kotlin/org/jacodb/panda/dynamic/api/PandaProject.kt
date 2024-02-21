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

package org.jacodb.panda.dynamic.api

import org.jacodb.api.core.Project

class PandaProject(
    val classes: List<PandaClass>
) : Project<PandaType> {

    private val std = PandaStdLib

    init {
        classes.forEach { clazz ->
            clazz.methods.forEach { method ->
                method.setProject(this)
            }
            clazz.setProject(this)
        }
    }

    override fun findTypeOrNull(name: String): PandaType? {
        return null
    }

    fun getGlobalClass(): PandaClass {
        return findClassOrNull("L_GLOBAL")
            ?: throw IllegalStateException("no global class")
    }

//    fun findObject(name: String, currentClassName: String): PandaField {
//        findClassOrNull(currentClassName)?.let { clazz ->
//            return clazz.fields.find { it.name == name } ?: findObject(name, clazz.superClassName)
//        }
//
//        throw IllegalStateException("couldn't find object $name starting from class $currentClassName")
//    }

    fun findInstanceMethodInStd(instanceName: String, methodName: String): PandaStdMethod {
        std.fields.find { it.name == instanceName }?.let { obj ->
            return (obj.methods.find { it.name == methodName }
                ?: throw IllegalStateException("no method $methodName for $instanceName"))
                .also {
                    it.setProject(this)
                }
        } ?: throw IllegalStateException("no std field $instanceName")
    }

    fun findClassOrNull(name: String): PandaClass? = classes.find { it.name == name }

    fun findMethodOrNull(name: String, currentClassName: String): PandaMethod? =
        findClassOrNull(currentClassName)?.methods?.find {
            it.name == name
        }

    override fun close() {}

    companion object {

        fun empty(): PandaProject = PandaProject(emptyList())
    }

}

class PandaClass(
    val name: String,
    val superClassName: String,
    val methods: List<PandaMethod>,
) {

    private var _project: PandaProject = PandaProject.empty()
    val project: PandaProject get() = _project

    fun setProject(value: PandaProject) {
        _project = value
    }
}

class PandaObject(
    val name: String,
    val methods: List<PandaStdMethod>
)

object PandaStdLib {

    val fields = listOf(
        PandaObject(
            "console",
            listOf(PandaStdMethod("log", PandaAnyType()))
        )
    )

}

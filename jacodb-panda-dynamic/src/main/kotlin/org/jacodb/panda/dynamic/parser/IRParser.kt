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

package org.jacodb.panda.dynamic.parser

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jacodb.panda.dynamic.*
import java.io.File

class IRParser(jsonPath: String) {

    data class ProgramIR(val classes: List<ProgramClass>)

    data class ProgramClass(
        val is_interface: Boolean,
        val methods: List<ProgramMethod>?,
        val name: String,
        val simple_name: String?,
        val super_class: String?,
        val fields: List<ProgramField>?
    )

    data class ProgramMethod(
        val basic_blocks: List<ProgramBasicBlock>?,
        val is_class_initializer: Boolean,
        val is_constructor: Boolean,
        val is_native: Boolean,
        val is_synchronized: Boolean,
        val name: String,
        val return_type: String,
        val signature: String
    ) {

        val idToMappable: MutableMap<Int, Mappable> = mutableMapOf()

        val insts: MutableList<PandaInst> = mutableListOf()

        // ArkTS id -> Panda input
        val idToInputs: MutableMap<Int, MutableList<PandaExpr>> = mutableMapOf()

        val idToIRInputs: MutableMap<Int, MutableList<ProgramInst>> = mutableMapOf()

        // ArkTS bb id -> bb
        val idToBB: MutableMap<Int, PandaBasicBlock> = mutableMapOf()

        val pandaMethod: PandaMethod = PandaMethod()

        fun inputsViaOp(op: ProgramInst): List<Mappable> = idToInputs.getOrDefault(op.id(), emptyList())

        var currentLocalVarId = 0

        var currentId = 0

        init {
            basic_blocks?.forEach { it.method = this }
        }
    }

    data class ProgramBasicBlock(
        val id: Int,
        val insts: List<ProgramInst>?,
        val successors: List<Int>?,
        val predecessors: List<Int>?
    ) {

        lateinit var method: ProgramMethod

        init {
            insts?.forEach { it.basicBlock = this }
        }
    }

    data class ProgramInst(
        val id: String,
        val inputs: List<String>?,
        val opcode: String,
        val type: String?,
        val users: List<String>?,
        val value: Any?,
        val visit: String
    ) {

        lateinit var basicBlock: ProgramBasicBlock

        private fun String.trimId() = this.drop(1).toInt()

        private val _id: Int
            get() = id.trimId()

        fun id() = _id

        fun inputs(): List<Int> = inputs?.map { it.trimId() } ?: emptyList()

        fun outputs(): List<Int> = users?.map { it.trimId() } ?: emptyList()
    }

    data class ProgramField(
        val name: String,
        val type: String
    )

    private val jsonFile: File = File(jsonPath)

    private val json = jsonFile.readText()

    private fun ProgramInst.currentMethod() = this.basicBlock.method

    private fun ProgramInst.currentBB() = this.basicBlock

    private fun inputsViaOp(op: ProgramInst) = op.currentMethod().inputsViaOp(op)


    fun getProgramIR(): ProgramIR {
        val gson = Gson()
        val programIRType = object : TypeToken<ProgramIR>() {}.type
        val programIR: ProgramIR = gson.fromJson(json, programIRType)
//        mapProgramIR(programIR)
        return programIR
    }

    fun mapProgramIR(programIR: ProgramIR) {

        programIR.classes.forEach { clazz ->
            clazz.methods?.forEach { method ->
                method.basic_blocks?.forEach { bb ->
                    bb.insts?.forEach { inst ->
                        when {
                            // TODO: Удалишь -- плакать буду
//                            inst.opcode.startsWith("IfImm") -> {
//                                val successors = bb.successors ?: throw IllegalStateException("No bb succ after IfImm op")
//                                val trueBB = method.basic_blocks.find { it.id == successors[0] }!!
//                                val falseBB = method.basic_blocks.find { it.id == successors[1] }!!
//                                listOfNotNull(
//                                    trueBB.insts?.minBy { it.id() }?.id(),
//                                    falseBB.insts?.minBy { it.id() }?.id()
//                                ).forEach { output ->
//                                    method.idToIRInputs.getOrPut(output) { mutableListOf() }.add(inst)
//                                }
//                            }
                            else -> inst.outputs().forEach { output ->
                                method.idToIRInputs.getOrPut(output) { mutableListOf() }.add(inst)
                            }
                        }
                    }
                }
            }
        }

        val programInstructions = programIR.classes
            .flatMap { it.methods ?: emptyList() }
            .flatMap { it.basic_blocks ?: emptyList() }
            .flatMap { it.insts ?: emptyList() }

        programInstructions.forEach { programInst ->
            val currentMethod: ProgramMethod = programInst.currentMethod()
            mapOpcode(programInst, currentMethod)
//            currentMethod.idToInputs[programInst.id()] = programInst.inputs()
            currentMethod.idToBB[programInst.currentBB().id] = mapBasicBlock(programInst.currentBB())
        }

        val programMethods = programIR.classes
            .flatMap { it.methods ?: emptyList() }

        programMethods.forEach { programMethod ->
            programMethod.pandaMethod.initBlocks(
                programMethod.idToBB.values.toList(),
            )
        }

    }

    private fun mapBasicBlock(bb: ProgramBasicBlock): PandaBasicBlock {
        val start = bb.insts?.first()?.id()
        val end = bb.insts?.last()?.id()
        val successors = bb.successors?.toSet()
        val predecessors = bb.predecessors?.toSet()

        return PandaBasicBlock(
            bb.id,
            successors ?: emptySet(),
            predecessors ?: emptySet(),
            PandaInstRef(start ?: -1),
            PandaInstRef(end ?: -1)
        )
    }

    private fun addInput(method: ProgramMethod, inputId: Int, outputId: Int, input: PandaExpr) {
        method.idToIRInputs.getOrDefault(outputId, mutableListOf()).forEachIndexed { index, programInst ->
            if (inputId == programInst.id()) {
                method.idToInputs.getOrPut(outputId) { mutableListOf() }.add(index, input)
            }
        }
    }

    private fun mapOpcode(op: ProgramInst, method: ProgramMethod) = with(op) {
        val inputs = inputsViaOp(this)
        val outputs = op.outputs()

        when {
            opcode == "Parameter" -> {
                val arg = PandaArgument(emptyList())
                outputs.forEach { output ->
                    addInput(method, op.id(), output, arg)
                }
            }

            opcode == "Constant" -> {
                val c = mapConstant(this)
                outputs.forEach { output ->
                    addInput(method, op.id(), output, c)
                }
            }

            opcode == "Intrinsic.typeof" -> {
                val lv = PandaLocalVar(method.currentLocalVarId++)
                val assign = PandaAssignInst(locationFromOp(this), lv, PandaTypeofExpr(inputs[0] as PandaValue))
                outputs.forEach { output ->
                    addInput(method, op.id(), output, lv)
                }
                method.insts.add(assign)
            }

            opcode == "Intrinsic.noteq" -> {
                val lv = PandaLocalVar(method.currentLocalVarId++)
                val assign = PandaAssignInst(
                    locationFromOp(this), lv, PandaNeqExpr(inputs[0] as PandaValue, inputs[1] as PandaValue)
                )
                outputs.forEach { output ->
                    addInput(method, op.id(), output, lv)
                }
                method.insts.add(assign)
            }

            opcode.startsWith("Compare") -> {
                val cmpOp = PandaCmpOp.valueOf(
                    Regex("Compare [.*] .*").find(opcode)!!.groups[1].toString()
                )
                val cmp = PandaCmpExpr(cmpOp, inputs[0] as PandaValue, inputs[1] as PandaValue)
                val lv = PandaLocalVar(method.currentLocalVarId++)
                val assign = PandaAssignInst(locationFromOp(this), lv, cmp)
                outputs.forEach { output ->
                    addInput(method, op.id(), output, lv)
                }
                method.insts.add(assign)
            }

            opcode.startsWith("IfImm") -> mapIfInst(this, inputs)
            opcode == "LoadString" -> {
                val sc = PandaStringConstant()
                outputs.forEach { output ->
                    addInput(method, op.id(), output, sc)
                }
            }

            opcode == "CastValueToAnyType" -> TODO()
//            opcode == "Intrinsic.newobjrange" -> PandaNewExpr(inputs[0] as PandaValue)
            opcode == "SaveState" -> TODO()
            else -> TODO()
        }
    }

    private fun getInstType(op: ProgramInst): Mappable = with(op) {
        val operands = inputsViaOp(op).mapNotNull { it as? PandaValue }
        return when (opcode) {
            "Intrinsic.tryldglobalbyname" -> TODOExpr(opcode, operands)
            "Intrinsic.newobjrange" -> TODOExpr(opcode, operands)
            "Intrinsic.throw" -> TODOInst(opcode, locationFromOp(op), operands)
            "Intrinsic.add2" -> TODOExpr(opcode, operands)
            "Intrinsic.return" -> TODOExpr(opcode, operands)
            else -> TODO()
        }
    }

    private fun mapIfInst(op: ProgramInst, inputs: List<Mappable>): PandaIfInst {
        val cmpOp = PandaCmpOp.valueOf(
            Regex("IfImm [.*] .*").find(op.opcode)!!.groups[1].toString()
        )
        val condExpr: PandaConditionExpr = when (cmpOp) {
            PandaCmpOp.NE -> PandaNeqExpr(inputs[0] as PandaValue, inputs[1] as PandaValue)
            PandaCmpOp.EQ -> PandaEqExpr(inputs[0] as PandaValue, inputs[1] as PandaValue)
        }

        val trueBranch = lazy {
            op.currentMethod().idToBB[op.basicBlock.successors!![0]]!!.start
        }

        val falseBranch = lazy {
            op.currentMethod().idToBB[op.basicBlock.successors!![1]]!!.start
        }

        return PandaIfInst(locationFromOp(op), condExpr, trueBranch, falseBranch)
    }

    private fun locationFromOp(op: ProgramInst): PandaInstLocation {
        val method = op.currentMethod()
        return PandaInstLocation(
            method.pandaMethod,
            method.currentId++,
            0
        )
    }


    private fun mapConstant(op: ProgramInst): PandaConstant = when (op.type) {
        "i64" -> PandaNumberConstant(op.value!!.toString().toInt(radix = 16))
        else -> TODOConstant()
    }

    fun printProgramInfo(programIR: ProgramIR) {
        programIR.classes.forEach { programClass ->
            println("Class Name: ${programClass.name}")

            programClass.methods?.forEach { programMethod ->
                println("  Method Name: ${programMethod.name}")
                programMethod.basic_blocks?.forEach { programBlock ->
                    println("    Basic Block ID: ${programBlock.id}")
                    programBlock.insts?.forEach { programInst ->
                        println("      Inst ID: ${programInst.id()}, Opcode: ${programInst.opcode}")
                        println("        Type: ${programInst.type}, Users: ${programInst.users}, Value: ${programInst.value}, Visit: ${programInst.visit}")
                    }
                }
            }

            programClass.fields?.forEach { programField ->
                println("  Field Name: ${programField.name}, Type: ${programField.type}")
            }
        }
    }

    fun printSetOfProgramOpcodes(programIR: ProgramIR) {
        val opcodes = mutableSetOf<String>()
        programIR.classes.forEach { programClass ->
            programClass.methods?.forEach { programMethod ->
                programMethod.basic_blocks?.forEach { programBlock ->
                    programBlock.insts?.forEach { programInst ->
                        opcodes.add(programInst.opcode)
                    }
                }
            }
        }
        println(opcodes)
    }

}

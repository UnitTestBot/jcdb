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

/*
 * This file is generated by jOOQ.
 */
package org.utbot.jacodb.impl.storage.jooq.tables


import kotlin.collections.List

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Index
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row7
import org.jooq.Schema
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import org.utbot.jacodb.impl.storage.jooq.DefaultSchema
import org.utbot.jacodb.impl.storage.jooq.indexes.CALLSSEARCH
import org.utbot.jacodb.impl.storage.jooq.keys.FK_CALLS_BYTECODELOCATIONS_1
import org.utbot.jacodb.impl.storage.jooq.keys.FK_CALLS_SYMBOLS_1
import org.utbot.jacodb.impl.storage.jooq.tables.records.CallsRecord


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Calls(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, CallsRecord>?,
    aliased: Table<CallsRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<CallsRecord>(
    alias,
    DefaultSchema.DEFAULT_SCHEMA,
    child,
    path,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table()
) {
    companion object {

        /**
         * The reference instance of <code>Calls</code>
         */
        val CALLS = Calls()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<CallsRecord> = CallsRecord::class.java

    /**
     * The column <code>Calls.callee_class_symbol_id</code>.
     */
    val CALLEE_CLASS_SYMBOL_ID: TableField<CallsRecord, Long?> = createField(DSL.name("callee_class_symbol_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>Calls.callee_name_symbol_id</code>.
     */
    val CALLEE_NAME_SYMBOL_ID: TableField<CallsRecord, Long?> = createField(DSL.name("callee_name_symbol_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>Calls.callee_desc_hash</code>.
     */
    val CALLEE_DESC_HASH: TableField<CallsRecord, Long?> = createField(DSL.name("callee_desc_hash"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>Calls.opcode</code>.
     */
    val OPCODE: TableField<CallsRecord, Int?> = createField(DSL.name("opcode"), SQLDataType.INTEGER, this, "")

    /**
     * The column <code>Calls.caller_class_symbol_id</code>.
     */
    val CALLER_CLASS_SYMBOL_ID: TableField<CallsRecord, Long?> = createField(DSL.name("caller_class_symbol_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>Calls.caller_method_offsets</code>.
     */
    val CALLER_METHOD_OFFSETS: TableField<CallsRecord, ByteArray?> = createField(DSL.name("caller_method_offsets"), SQLDataType.BLOB, this, "")

    /**
     * The column <code>Calls.location_id</code>.
     */
    val LOCATION_ID: TableField<CallsRecord, Long?> = createField(DSL.name("location_id"), SQLDataType.BIGINT.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<CallsRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<CallsRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>Calls</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>Calls</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>Calls</code> table reference
     */
    constructor(): this(DSL.name("Calls"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, CallsRecord>): this(Internal.createPathAlias(child, key), child, key, CALLS, null)
    override fun getSchema(): Schema = DefaultSchema.DEFAULT_SCHEMA
    override fun getIndexes(): List<Index> = listOf(CALLSSEARCH)
    override fun getReferences(): List<ForeignKey<CallsRecord, *>> = listOf(FK_CALLS_SYMBOLS_1, FK_CALLS_BYTECODELOCATIONS_1)

    private lateinit var _symbols: Symbols
    private lateinit var _bytecodelocations: Bytecodelocations
    fun symbols(): Symbols {
        if (!this::_symbols.isInitialized)
            _symbols = Symbols(this, FK_CALLS_SYMBOLS_1)

        return _symbols;
    }
    fun bytecodelocations(): Bytecodelocations {
        if (!this::_bytecodelocations.isInitialized)
            _bytecodelocations = Bytecodelocations(this, FK_CALLS_BYTECODELOCATIONS_1)

        return _bytecodelocations;
    }
    override fun `as`(alias: String): Calls = Calls(DSL.name(alias), this)
    override fun `as`(alias: Name): Calls = Calls(alias, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Calls = Calls(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Calls = Calls(name, null)

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row7<Long?, Long?, Long?, Int?, Long?, ByteArray?, Long?> = super.fieldsRow() as Row7<Long?, Long?, Long?, Int?, Long?, ByteArray?, Long?>
}

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


import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row6
import org.jooq.Schema
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import org.utbot.jacodb.impl.storage.jooq.DefaultSchema
import org.utbot.jacodb.impl.storage.jooq.keys.FK_BYTECODELOCATIONS_BYTECODELOCATIONS_1
import org.utbot.jacodb.impl.storage.jooq.keys.PK_BYTECODELOCATIONS
import org.utbot.jacodb.impl.storage.jooq.tables.records.BytecodelocationsRecord


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Bytecodelocations(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, BytecodelocationsRecord>?,
    aliased: Table<BytecodelocationsRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<BytecodelocationsRecord>(
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
         * The reference instance of <code>BytecodeLocations</code>
         */
        val BYTECODELOCATIONS = Bytecodelocations()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<BytecodelocationsRecord> = BytecodelocationsRecord::class.java

    /**
     * The column <code>BytecodeLocations.id</code>.
     */
    val ID: TableField<BytecodelocationsRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>BytecodeLocations.path</code>.
     */
    val PATH: TableField<BytecodelocationsRecord, String?> = createField(DSL.name("path"), SQLDataType.VARCHAR(1024).nullable(false), this, "")

    /**
     * The column <code>BytecodeLocations.uniqueId</code>.
     */
    val UNIQUEID: TableField<BytecodelocationsRecord, String?> = createField(DSL.name("uniqueId"), SQLDataType.VARCHAR(1024).nullable(false), this, "")

    /**
     * The column <code>BytecodeLocations.runtime</code>.
     */
    val RUNTIME: TableField<BytecodelocationsRecord, Boolean?> = createField(DSL.name("runtime"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("0", SQLDataType.BOOLEAN)), this, "")

    /**
     * The column <code>BytecodeLocations.state</code>.
     */
    val STATE: TableField<BytecodelocationsRecord, Int?> = createField(DSL.name("state"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.field("0", SQLDataType.INTEGER)), this, "")

    /**
     * The column <code>BytecodeLocations.updated_id</code>.
     */
    val UPDATED_ID: TableField<BytecodelocationsRecord, Long?> = createField(DSL.name("updated_id"), SQLDataType.BIGINT, this, "")

    private constructor(alias: Name, aliased: Table<BytecodelocationsRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<BytecodelocationsRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>BytecodeLocations</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>BytecodeLocations</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>BytecodeLocations</code> table reference
     */
    constructor(): this(DSL.name("BytecodeLocations"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, BytecodelocationsRecord>): this(Internal.createPathAlias(child, key), child, key, BYTECODELOCATIONS, null)
    override fun getSchema(): Schema = DefaultSchema.DEFAULT_SCHEMA
    override fun getPrimaryKey(): UniqueKey<BytecodelocationsRecord> = PK_BYTECODELOCATIONS
    override fun getKeys(): List<UniqueKey<BytecodelocationsRecord>> = listOf(PK_BYTECODELOCATIONS)
    override fun getReferences(): List<ForeignKey<BytecodelocationsRecord, *>> = listOf(FK_BYTECODELOCATIONS_BYTECODELOCATIONS_1)

    private lateinit var _bytecodelocations: Bytecodelocations
    fun bytecodelocations(): Bytecodelocations {
        if (!this::_bytecodelocations.isInitialized)
            _bytecodelocations = Bytecodelocations(this, FK_BYTECODELOCATIONS_BYTECODELOCATIONS_1)

        return _bytecodelocations;
    }
    override fun `as`(alias: String): Bytecodelocations = Bytecodelocations(DSL.name(alias), this)
    override fun `as`(alias: Name): Bytecodelocations = Bytecodelocations(alias, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Bytecodelocations = Bytecodelocations(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Bytecodelocations = Bytecodelocations(name, null)

    // -------------------------------------------------------------------------
    // Row6 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row6<Long?, String?, String?, Boolean?, Int?, Long?> = super.fieldsRow() as Row6<Long?, String?, String?, Boolean?, Int?, Long?>
}

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
import org.jooq.Name
import org.jooq.Record
import org.jooq.Row5
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
import org.utbot.jacodb.impl.storage.jooq.keys.FK_OUTERCLASSES_SYMBOLS_1
import org.utbot.jacodb.impl.storage.jooq.keys.PK_OUTERCLASSES
import org.utbot.jacodb.impl.storage.jooq.tables.records.OuterclassesRecord


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class Outerclasses(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, OuterclassesRecord>?,
    aliased: Table<OuterclassesRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<OuterclassesRecord>(
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
         * The reference instance of <code>OuterClasses</code>
         */
        val OUTERCLASSES = Outerclasses()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<OuterclassesRecord> = OuterclassesRecord::class.java

    /**
     * The column <code>OuterClasses.id</code>.
     */
    val ID: TableField<OuterclassesRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT, this, "")

    /**
     * The column <code>OuterClasses.outer_class_name_id</code>.
     */
    val OUTER_CLASS_NAME_ID: TableField<OuterclassesRecord, Long?> = createField(DSL.name("outer_class_name_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>OuterClasses.name</code>.
     */
    val NAME: TableField<OuterclassesRecord, String?> = createField(DSL.name("name"), SQLDataType.VARCHAR(256), this, "")

    /**
     * The column <code>OuterClasses.method_name</code>.
     */
    val METHOD_NAME: TableField<OuterclassesRecord, String?> = createField(DSL.name("method_name"), SQLDataType.CLOB, this, "")

    /**
     * The column <code>OuterClasses.method_desc</code>.
     */
    val METHOD_DESC: TableField<OuterclassesRecord, String?> = createField(DSL.name("method_desc"), SQLDataType.CLOB, this, "")

    private constructor(alias: Name, aliased: Table<OuterclassesRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<OuterclassesRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>OuterClasses</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>OuterClasses</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>OuterClasses</code> table reference
     */
    constructor(): this(DSL.name("OuterClasses"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, OuterclassesRecord>): this(Internal.createPathAlias(child, key), child, key, OUTERCLASSES, null)
    override fun getSchema(): Schema = DefaultSchema.DEFAULT_SCHEMA
    override fun getPrimaryKey(): UniqueKey<OuterclassesRecord> = PK_OUTERCLASSES
    override fun getKeys(): List<UniqueKey<OuterclassesRecord>> = listOf(PK_OUTERCLASSES)
    override fun getReferences(): List<ForeignKey<OuterclassesRecord, *>> = listOf(FK_OUTERCLASSES_SYMBOLS_1)

    private lateinit var _symbols: Symbols
    fun symbols(): Symbols {
        if (!this::_symbols.isInitialized)
            _symbols = Symbols(this, FK_OUTERCLASSES_SYMBOLS_1)

        return _symbols;
    }
    override fun `as`(alias: String): Outerclasses = Outerclasses(DSL.name(alias), this)
    override fun `as`(alias: Name): Outerclasses = Outerclasses(alias, this)

    /**
     * Rename this table
     */
    override fun rename(name: String): Outerclasses = Outerclasses(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): Outerclasses = Outerclasses(name, null)

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row5<Long?, Long?, String?, String?, String?> = super.fieldsRow() as Row5<Long?, Long?, String?, String?, String?>
}

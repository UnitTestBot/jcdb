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

package org.jacodb.impl

import org.jacodb.api.jvm.JcPersistenceImplSettings
import org.jacodb.api.storage.ers.EmptyErsSettings
import org.jacodb.api.storage.ers.ErsSettings
import org.jacodb.impl.storage.SQLITE_DATABASE_PERSISTENCE_SPI
import org.jacodb.impl.storage.ers.ERS_DATABASE_PERSISTENCE_SPI
import org.jacodb.impl.storage.ers.kv.KV_ERS_SPI
import org.jacodb.impl.storage.ers.ram.RAM_ERS_SPI
import org.jacodb.impl.storage.ers.sql.SQL_ERS_SPI
import org.jacodb.impl.storage.kv.rocks.ROCKS_KEY_VALUE_STORAGE_SPI
import org.jacodb.impl.storage.kv.xodus.XODUS_KEY_VALUE_STORAGE_SPI

object JcSQLitePersistenceSettings : JcPersistenceImplSettings {
    override val persistenceId: String
        get() = SQLITE_DATABASE_PERSISTENCE_SPI
}

open class JcErsSettings(
    val ersId: String,
    val ersSettings: ErsSettings = EmptyErsSettings
) : JcPersistenceImplSettings {

    override val persistenceId: String
        get() = ERS_DATABASE_PERSISTENCE_SPI
}

object JcRamErsSettings : JcErsSettings(RAM_ERS_SPI, RamErsSettings())

object JcSqlErsSettings : JcErsSettings(SQL_ERS_SPI)

object JcXodusKvErsSettings : JcErsSettings(KV_ERS_SPI, JcKvErsSettings(XODUS_KEY_VALUE_STORAGE_SPI))

object JcRocksKvErsSettings : JcErsSettings(KV_ERS_SPI, JcKvErsSettings(ROCKS_KEY_VALUE_STORAGE_SPI))

object JcLmdbKvErsSettings : JcErsSettings(KV_ERS_SPI, JcLmdbErsSettings()) {

    fun withMapSize(mapSize: Long): JcErsSettings {
        return JcErsSettings(KV_ERS_SPI, JcLmdbErsSettings(mapSize))
    }
}
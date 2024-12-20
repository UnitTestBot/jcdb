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

package org.jacodb.impl.storage.ers.ram

import org.jacodb.util.ByteArrayBuilder
import java.io.InputStream

internal fun PropertiesMutable.toImmutable(builder: ByteArrayBuilder): PropertiesImmutable {
    return PropertiesImmutable(props.beginRead().toAttributesImmutable(builder))
}

internal fun InputStream.readPropertiesImmutable(): PropertiesImmutable = PropertiesImmutable(
    attributes = readAttributesImmutable()
)
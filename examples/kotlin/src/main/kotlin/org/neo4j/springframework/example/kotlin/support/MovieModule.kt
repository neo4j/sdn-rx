/*
 * Copyright (c) 2019-2020 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neo4j.springframework.example.kotlin.support

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.module.SimpleModule
import org.neo4j.springframework.example.kotlin.domain.MovieEntity
import org.neo4j.springframework.example.kotlin.domain.PersonEntity
import org.neo4j.springframework.example.kotlin.domain.Roles
import org.springframework.stereotype.Component
import java.io.IOException

/**
 * @author Gerrit Meier
 */
@Component
class MovieModule : SimpleModule() {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    internal abstract class MovieEntityMixin @JsonCreator constructor(@JsonProperty("title") title: String?,
                                                                      @JsonProperty("description") description: String?) {
        @get:JsonSerialize(keyUsing = PersonEntityAsKeySerializer::class, contentUsing = RolesAsContentSerializer::class)
        @get:JsonDeserialize(keyUsing = PersonEntityAsKeyDeSerializer::class)
        abstract val actorsAndRoles: Map<Any?, Any?>?
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    internal abstract class PersonEntityMixin @JsonCreator constructor(@JsonProperty("name") name: String?, @JsonProperty("born") born: Long?)
    internal class RoleDeserializer : JsonDeserializer<Roles?>() {
        @Throws(IOException::class)
        override fun deserialize(jsonParser: JsonParser,
                                 deserializationContext: DeserializationContext): Roles {
            return Roles(jsonParser.readValueAs<List<String>>(object : TypeReference<List<String?>?>() {}))
        }
    }

    internal class PersonEntityAsKeyDeSerializer : KeyDeserializer() {
        override fun deserializeKey(name: String, ctxt: DeserializationContext): Any {
            return PersonEntity(name, null)
        }
    }

    internal class PersonEntityAsKeySerializer : JsonSerializer<PersonEntity>() {
        @Throws(IOException::class)
        override fun serialize(personEntity: PersonEntity, jsonGenerator: JsonGenerator,
                               serializerProvider: SerializerProvider) {
            jsonGenerator.writeFieldName(personEntity.name)
        }
    }

    internal class RolesAsContentSerializer : JsonSerializer<Roles>() {
        @Throws(IOException::class)
        override fun serialize(roles: Roles, jsonGenerator: JsonGenerator,
                               serializerProvider: SerializerProvider) {
            jsonGenerator.writeObject(roles.roles)
        }
    }

    init {
        setMixInAnnotation(MovieEntity::class.java, MovieEntityMixin::class.java)
        setMixInAnnotation(PersonEntity::class.java, PersonEntityMixin::class.java)
        addDeserializer(Roles::class.java, RoleDeserializer())
    }
}

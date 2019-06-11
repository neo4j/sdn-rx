/*
 * Copyright (c) 2019 "Neo4j,"
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

package org.neo4j.springframework.data.integration.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.springframework.data.config.AbstractNeo4jConfig
import org.neo4j.springframework.data.integration.shared.KotlinPerson
import org.neo4j.springframework.data.repository.config.EnableNeo4jRepositories
import org.neo4j.springframework.data.test.Neo4jExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * @author Gerrit Meier
 */
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [KotlinIT.Config::class])
class KotlinIT(@Autowired val repository: KotlinRepository, @Autowired val driver: Driver) {

    private val personName = "test"

    fun setup() {
        val session = driver.session()
        val transaction = session.beginTransaction()
        transaction.run("MATCH (n) detach delete n")
        transaction.run("CREATE (n:KotlinPerson) SET n.name = '$personName'")
        transaction.success()
        transaction.close()
        session.close()
    }

    @Test
    fun findAllKotlinPersons() {
        setup(); // no beforeAll or similar here because it does not read nice in Kotlin
        val person = repository.findAll()
        assertThat(person.first().name).isEqualTo(personName)
    }

    @Configuration
    @EnableNeo4jRepositories
    @EnableTransactionManagement
    internal open class Config : AbstractNeo4jConfig() {

        @Bean
        override fun driver(): Driver {
            val adapter = Neo4jExtension.ContainerAdapter()
            adapter.start()
            val neo4jConnectionSupport = Neo4jExtension.Neo4jConnectionSupport(adapter.getBoltUrl(), AuthTokens.none())
            return neo4jConnectionSupport.openConnection()
        }

        override fun getMappingBasePackages(): Collection<String> {
            return listOf(KotlinPerson::class.java.getPackage().name)
        }
    }
}

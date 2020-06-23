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

package org.neo4j.springframework.example.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.neo4j.driver.Driver
import org.neo4j.springframework.example.kotlin.domain.Average
import org.neo4j.springframework.example.kotlin.domain.MovieEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * @author Gerrit Meier
 */
@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
class KotlinReactiveWebApplicationTest(@Autowired val driver: Driver,
                                       @Autowired val client: WebTestClient) {


    companion object {

        @Container
        @JvmStatic
        private val neo4jContainer = Neo4jContainer<Nothing>("neo4j:4.0")

        @DynamicPropertySource
        @JvmStatic
        fun neo4jProperties(registry: DynamicPropertyRegistry) {
            registry.add("org.neo4j.driver.uri") { neo4jContainer.boltUrl }
            registry.add("org.neo4j.driver.authentication.username") { "neo4j" }
            registry.add("org.neo4j.driver.authentication.password") { neo4jContainer.adminPassword }
        }
    }

    @BeforeEach
    @Throws(IOException::class)
    fun setupData() {
        BufferedReader(
            InputStreamReader(this.javaClass.getResourceAsStream("/movies.cypher"))).use { moviesReader ->
            driver.session().use { session ->
                session.writeTransaction<Any> { tx ->
                    tx.run("MATCH (n) DETACH DELETE n").consume()
                    val moviesCypher = moviesReader.lines().collect(Collectors.joining(" "))
                    tx.run(moviesCypher).consume()
                    null
                }
            }
        }
    }

    @Test
    fun listAllMovies() {
        client.get().uri("/movies").exchange()
            .expectStatus().isOk
            .expectBodyList(MovieEntity::class.java).hasSize(38)
    }

    @Test
    fun movieByTitle() {
        client.get().uri("/movies/by-title?title=The Matrix").exchange()
            .expectStatus().isOk
            .expectBody<MovieEntity>()
            .consumeWith { result ->
                val movie: MovieEntity = result.responseBody!!
                assertThat(movie.title).isEqualTo("The Matrix")
            }
    }

    @Test
    fun createMovie() {
        val newMovie = MovieEntity("Aeon Flux", "Reactive is the new cool", Average.Good)
        client.put().uri("/movies").bodyValue(newMovie).exchange()
            .expectStatus().isOk
        driver.session().use { session ->
            val tagline = session.run("MATCH (m:Movie{title:'Aeon Flux'}) return m")
                .single()["m"].asNode()["tagline"].asString()
            assertThat(tagline).isEqualTo("Reactive is the new cool")
        }
    }

    @Test
    fun deleteMovie() {
        client.delete().uri("/movies/The Matrix") // disclaimer: you should never delete The Matrix
            .exchange()
            .expectStatus().isOk
        driver.session().use { session ->
            val movieCount = session.run("MATCH (m:Movie) return count(m) as movieCount")
                .single()["movieCount"].asLong()
            assertThat(movieCount).isEqualTo(37L)
        }
    }
}

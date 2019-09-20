package org.neo4j.springframework.data.integration.imperative

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.neo4j.driver.Driver
import org.neo4j.driver.Values
import org.neo4j.springframework.data.config.AbstractNeo4jConfig
import org.neo4j.springframework.data.core.Neo4jClient
import org.neo4j.springframework.data.core.fetchAs
import org.neo4j.springframework.data.test.Neo4jExtension
import org.neo4j.springframework.data.test.Neo4jIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement

@Neo4jIntegrationTest
class Neo4jClientKotlinInteropIT @Autowired constructor(
		private val driver: Driver,
		private val neo4jClient: Neo4jClient
) {

	companion object {
		@JvmStatic
		private lateinit var neo4jConnectionSupport: Neo4jExtension.Neo4jConnectionSupport
	}

	@BeforeEach
	fun prepareData() {

		driver.session().use {
			val bands = mapOf(
					Pair("Queen", listOf("Brian", "Roger", "John", "Freddie")),
					Pair("Die Ärzte", listOf("Farin", "Rod", "Bela"))
			)

			bands.forEach { b, m ->
				val summary = it.run("""
					CREATE (b:Band {name: ${'$'}band}) 
					WITH b
					UNWIND ${'$'}names AS name CREATE (n:Member {name: name}) <- [:HAS_MEMBER] - (b)
					""".trimIndent(), Values.parameters("band", b, "names", m)).summary()
				assertThat(summary.counters().nodesCreated()).isGreaterThan(0)
			}
		}
	}

	@AfterEach
	fun purgeData() {

		driver.session().use { it.run("MATCH (n) DETACH DELETE n").consume() }
	}

	data class Artist(val name: String)

	data class Band(val name: String, val member: Collection<Artist>)

	@Test
	fun `The Neo4j client should be usable from idiomatic Kotlin code`() {

		val dieAerzte = neo4jClient
				.query("""
					MATCH (b:Band {name: ${'$'}name}) - [:HAS_MEMBER] -> (m) 
					RETURN b as band, collect(m.name) as members
				""".trimIndent())
				.bind("Die Ärzte").to("name")
				.fetchAs<Band>()
				.mappedBy { _, r ->
					val members = r["members"].asList { v -> Artist(v.asString()) }
					Band(r["band"]["name"].asString(), members)
				}.one()

		assertThat(dieAerzte).isNotNull
		assertThat(dieAerzte!!.member).hasSize(3)

		if (neo4jClient.query("MATCH (n:IDontExists) RETURN id(n)").fetchAs<Long>().one() != null) {
			Assertions.fail<String>("The record does not exist, the optional had to be null")
		}
	}

	@Configuration
	@EnableTransactionManagement
	open class Config : AbstractNeo4jConfig() {

		@Bean
		override fun driver(): Driver {
			return neo4jConnectionSupport.driver
		}
	}
}
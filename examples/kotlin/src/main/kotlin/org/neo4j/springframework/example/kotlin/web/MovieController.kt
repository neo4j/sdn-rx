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

package org.neo4j.springframework.example.kotlin.web

import org.neo4j.springframework.example.kotlin.domain.MovieEntity
import org.neo4j.springframework.example.kotlin.domain.MovieRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author Gerrit Meier
 */
@RestController
@RequestMapping("/movies")
class MovieController(val movieRepository: MovieRepository) {

    @PutMapping
    fun createOrUpdateMovie(@RequestBody newMovie: MovieEntity): Mono<MovieEntity> {
        return movieRepository.save(newMovie)
    }

    @GetMapping(value = ["", "/"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getMovies(): Flux<MovieEntity> {
        return movieRepository
            .findAll()
    }

    @GetMapping("/by-title")
    fun byTitle(@RequestParam title: String): Mono<MovieEntity> {
        return movieRepository.findOneByTitle(title)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): Mono<Void> {
        return movieRepository.deleteById(id)
    }
}

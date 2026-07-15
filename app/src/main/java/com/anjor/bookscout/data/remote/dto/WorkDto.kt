package com.anjor.bookscout.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Response of the Open Library work endpoint:
 * `https://openlibrary.org/works/{workId}.json`
 *
 * The `description` field is polymorphic in the API: it can be a plain string OR an
 * object of the shape `{"type": "...", "value": "..."}`. We capture it as a raw
 * [JsonElement] and resolve it later in the mapper so we never fail to deserialize.
 */
@Serializable
data class WorkDto(
    @SerialName("key") val key: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: JsonElement? = null,
    @SerialName("subjects") val subjects: List<String>? = null,
    @SerialName("subject_places") val subjectPlaces: List<String>? = null,
    @SerialName("authors") val authors: List<AuthorRefDto>? = null,
    @SerialName("first_publish_date") val firstPublishDate: String? = null,
    @SerialName("covers") val covers: List<Int>? = null,
    @SerialName("created") val created: ValueDto? = null,
    @SerialName("last_modified") val lastModified: ValueDto? = null
)

@Serializable
data class AuthorRefDto(
    @SerialName("key") val key: String? = null,
    @SerialName("name") val name: String? = null
)

@Serializable
data class ValueDto(
    @SerialName("value") val value: String? = null
)

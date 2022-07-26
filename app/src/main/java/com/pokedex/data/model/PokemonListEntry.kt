package com.pokedex.data.model

data class PokemonListEntry(
    val pokemonName: String,
    val imageUrl: String,
    val number: Int,
    var fav: Boolean
)
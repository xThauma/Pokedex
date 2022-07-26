package com.pokedex.presentation.viewmodel

import com.plcoding.jetpackcomposepokedex.data.remote.responses.Pokemon
import com.pokedex.data.model.PokemonListEntry


data class PokedexState(
    var items: List<PokemonListEntry> = emptyList(),
    var currPokemon: Pokemon? = null,
    var cachedItems: List<PokemonListEntry> = emptyList(),
    var lastSearchedWord: String = "",
    var isFavOnly: Boolean = false,
    var isLoading: Boolean = false,
    var loadError: String = "",
    var endReached: Boolean = false,
    var currPage: Int = 0
) {
}
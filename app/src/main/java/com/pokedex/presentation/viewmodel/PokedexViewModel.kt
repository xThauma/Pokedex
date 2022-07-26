package com.pokedex.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.pokedex.data.model.PokemonListEntry
import com.pokedex.data.repository.PokedexRepository
import com.pokedex.util.Constants.PAGE_SIZE
import com.pokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.Collections.addAll
import javax.inject.Inject

@HiltViewModel
class PokedexViewModel @Inject constructor(
    private val repository: PokedexRepository
) : ViewModel() {

    var state by mutableStateOf(PokedexState())

    init {
        loadPokemonPaginated()
    }

    fun loadPokemonPaginated() {
        viewModelScope.launch {
            if (state.isLoading) {
                return@launch
            }
            repository.getPokemonList(PAGE_SIZE, state.currPage * PAGE_SIZE).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val newItems = result.data?.results?.mapIndexed { index, entry ->
                            val number = if (entry.url.endsWith("/")) {
                                entry.url.dropLast(1).takeLastWhile { it.isDigit() }
                            } else {
                                entry.url.takeLastWhile { it.isDigit() }
                            }
                            val url =
                                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${number}.png"
                            PokemonListEntry(entry.name.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.ROOT
                                ) else it.toString()
                            }, url, number.toInt(), Random().nextBoolean())
                        } ?: emptyList()
                        state = state.copy(
                            endReached = state.currPage * PAGE_SIZE >= result.data!!.count,
                            items = state.items + newItems,
                            loadError = "",
                            isLoading = false,
                            currPage = state.currPage + 1,
                        )

                    }
                    is Resource.Error -> {
                        state = state.copy(
                            loadError = result.message ?: "Null error"
                        )
                    }
                    is Resource.Loading -> {
                        state = state.copy(
                            isLoading = result.isLoading
                        )
                    }
                }
            }
        }
    }

    fun getPokemon(pokemonName: String) {
        viewModelScope.launch {
            repository.getPokemonInfo(pokemonName.lowercase(Locale.ROOT)).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        state = state.copy(
                            currPokemon = result.data!!,
                            loadError = "",
                            isLoading = false,
                        )
                    }
                    is Resource.Error -> {
                        state = state.copy(
                            loadError = result.message ?: "Null error"
                        )
                    }
                    is Resource.Loading -> {
                        state = state.copy(
                            isLoading = result.isLoading
                        )
                    }
                }
            }
        }
    }

    fun setFavouritePokemon(entry: PokemonListEntry) {
        state = state.copy(
            items = state.items.map { item ->
                if (item == entry) {
                    item.copy(fav = !entry.fav)
                } else {
                    item
                }
            },
            cachedItems = state.cachedItems.map { item ->
                if (item == entry) {
                    item.copy(fav = !entry.fav)
                } else {
                    item
                }
            }
        )
        if (state.isFavOnly) {
            searchPokemon(state.lastSearchedWord)
        }
    }

    fun setFavouriteOnly() {
        state = state.copy(
            isFavOnly = !state.isFavOnly
        )
        searchPokemon(state.lastSearchedWord)
    }

    fun searchPokemon(pokemonName: String) {
        Log.e("LOG", "searchPokemon")
        if (state.items.size > state.cachedItems.size) {
            state = state.copy(
                cachedItems = state.items
            )
        }

        if (pokemonName.length <= state.lastSearchedWord.length) {
            state = state.copy(
                items = state.cachedItems
            )
        }
        state = state.copy(
            lastSearchedWord = pokemonName
        )

        viewModelScope.launch(Dispatchers.Default) {
            if (pokemonName.isEmpty() && !state.isFavOnly) {
                state = state.copy(
                    items = state.cachedItems,
                )
                return@launch
            }

            state = state.copy(
                items = state.items.filter {
                    it.pokemonName.contains(other = pokemonName, ignoreCase = true)
                            && if (state.isFavOnly) it.fav else true

                }
            )
        }
    }

    fun calcDominantColor(drawable: Drawable, onFinish: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }
}
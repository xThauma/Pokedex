package com.pokedex.data.repository

import com.plcoding.jetpackcomposepokedex.data.remote.responses.Pokemon
import com.plcoding.jetpackcomposepokedex.data.remote.responses.PokemonList
import com.pokedex.data.remote.PokeApi
import com.pokedex.util.Resource
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@ActivityScoped
class PokedexRepository @Inject constructor(
    private val api: PokeApi
) {
    suspend fun getPokemonList(limit: Int, offset: Int): Flow<Resource<PokemonList>> {
        return flow {
            emit(Resource.Loading(true))

            try {
                val response = api.getPokemonList(limit, offset)
                emit(Resource.Success(response))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }

            emit(Resource.Loading(false))
            return@flow
        }
    }

    suspend fun getPokemonInfo(pokemonName: String): Flow<Resource<Pokemon>> {
        return flow {
            emit(Resource.Loading(true))

            try {
                val response = api.getPokemonInfo(pokemonName)
                emit(Resource.Success(response))
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }

            emit(Resource.Loading(false))
            return@flow
        }
    }


}
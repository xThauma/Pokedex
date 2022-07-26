package com.pokedex

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.pokedex.data.model.PokemonListEntry
import com.pokedex.destinations.PokemonInfoFragmentDestination
import com.pokedex.presentation.viewmodel.PokedexViewModel
import com.pokedex.ui.theme.BackgroundColor
import com.pokedex.ui.theme.DisabledColor
import com.pokedex.ui.theme.RedColor
import com.pokedex.ui.theme.SearchBarColor
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@RootNavGraph(start = true)
@Destination
@Composable
fun PokedexFragment(
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
    viewModel: PokedexViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(15.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            Text(
                text = "Your Pokédex",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_heart),
                tint = if(state.isFavOnly) RedColor else DisabledColor,
                contentDescription = "menu",
                modifier = modifier
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        viewModel.setFavouriteOnly()
                    }
            )
        }
        Spacer(modifier = modifier.height(15.dp))
        Text(
            text = "Who are you looking for? Search for a Pokédex by name or using its National Pokédex number.",
            style = MaterialTheme.typography.body1,
            color = Color.DarkGray
        )
        Spacer(modifier = modifier.height(8.dp))
        SearchBar(
            modifier = modifier.fillMaxWidth(),
            hint = "Search for a Pokémon",
            onSearch = {
                viewModel.searchPokemon(it)
            },
            onType = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }
        )
        Spacer(modifier = modifier.height(8.dp))
        PokemonList(
            scrollState = scrollState,
            onClick = { url, name, isFav ->
                navigator.navigate(
                    PokemonInfoFragmentDestination(
                        url = url,
                        pokemonName = name,
                        isFavourite = isFav
                    )
                )
            }
        )
    }

}

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    viewModel: PokedexViewModel = hiltViewModel(),
    onClick: (String, String, Boolean) -> Unit,
    scrollState: LazyListState
) {
    val state = viewModel.state

    LazyColumn(
        contentPadding = PaddingValues(10.dp),
        modifier = modifier
            .fillMaxWidth(),
        state = scrollState
    ) {
        val itemCount = if (state.items.size % 2 == 0) {
            state.items.size / 2
        } else {
            state.items.size / 2 + 1
        }
        items(itemCount) {
            if (it >= itemCount - 1 && !state.endReached) {
                viewModel.loadPokemonPaginated()
//                coroutineScope.launch {
//                    scrollState.animateScrollToItem(index = it)
//                }
            }
            PokemonListRow(rowIndex = it, entries = state.items, onClick = onClick)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        if (state.loadError.isNotEmpty()) {
            Text(
                text = "Loading failed.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = modifier
                    .align(Alignment.Center)
                    .clickable {
                        viewModel.loadPokemonPaginated()
                    }
            )
        }
    }
}

@Composable
fun PokemonListRow(
    rowIndex: Int,
    entries: List<PokemonListEntry>,
    onClick: (String, String, Boolean) -> Unit
) {

    Column {
        Row {
            PokemonEntry(
                entry = entries[rowIndex * 2],
                modifier = Modifier.weight(1f),
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(16.dp))
            if (entries.size >= rowIndex * 2 + 2) {
                PokemonEntry(
                    entry = entries[rowIndex * 2 + 1],
                    modifier = Modifier.weight(1f),
                    onClick = onClick
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PokemonEntry(
    entry: PokemonListEntry,
    modifier: Modifier = Modifier,
    viewModel: PokedexViewModel = hiltViewModel(),
    onClick: (String, String, Boolean) -> Unit
) {
    val painter = rememberAsyncImagePainter(
        model = entry.imageUrl
    )

    val searchingState = viewModel.state
    val interactionSource = remember { MutableInteractionSource() }
    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember {
        mutableStateOf(defaultDominantColor)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        defaultDominantColor,
                        dominantColor
                    )
                )
            )
            .clickable {
                onClick(
                    entry.imageUrl,
                    entry.pokemonName,
                    entry.fav
                )
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (painter.state is AsyncImagePainter.State.Success) {
                LaunchedEffect(key1 = searchingState) {
                    val image = painter.imageLoader.execute(painter.request).drawable
                    if (image != null) {
                        viewModel.calcDominantColor(image) {
                            dominantColor = it
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = entry.pokemonName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart),
                        contentDescription = "like",
                        tint = if (entry.fav) RedColor else DisabledColor,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                viewModel.setFavouritePokemon(entry)
                            }
                    )
                }
            }


            Image(
                painter = painter,
                contentDescription = entry.pokemonName,
                modifier = Modifier
                    .size(126.dp)
            )
        }

        if (painter.state is AsyncImagePainter.State.Loading || painter.state is AsyncImagePainter.State.Error)
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
    }
}


@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    onSearch: (String) -> Unit = {},
    onType: () -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }

    var isHintDisplayed by remember {
        mutableStateOf(hint != "")
    }

    Row(
        modifier = modifier
            .shadow(
                3.dp,
                shape = RoundedCornerShape(25.dp)
            )
            .background(
                color = SearchBarColor,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(7.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "search",
        )
        Box(
            modifier = modifier
        ) {
            BasicTextField(
                value = text,
                onValueChange = {
                    onType()
                    if (it.length <= 10) {
                        text = it
                        onSearch(it)
                    }
                },
                maxLines = 1,
                singleLine = true,
                textStyle = TextStyle(color = Color.Black),
                modifier = modifier
                    .onFocusChanged {
                        isHintDisplayed = !it.isFocused && text.isEmpty()
                    }
            )
            if (isHintDisplayed) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }
        }
    }
}
package com.pokedex

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.plcoding.jetpackcomposepokedex.data.remote.responses.Pokemon
import com.plcoding.jetpackcomposepokedex.data.remote.responses.Sprites
import com.plcoding.jetpackcomposepokedex.data.remote.responses.Stat
import com.pokedex.presentation.viewmodel.PokedexViewModel
import com.pokedex.ui.theme.*
import com.pokedex.util.Helpers
import com.pokedex.util.Parsers
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import java.util.*

@Destination
@Composable
fun PokemonInfoFragment(
    url: String,
    pokemonName: String,
    modifier: Modifier = Modifier,
    isFavourite: Boolean = false,
    navigator: DestinationsNavigator,
    viewModel: PokedexViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = true) {
        viewModel.getPokemon(pokemonName)
    }

    val state = viewModel.state
    val pokemon = state.currPokemon

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (!state.isLoading && state.loadError == "" && pokemon != null) {
            TopSection(
                isFav = isFavourite,
                onClickBack = {
                    navigator.popBackStack()
                },
                url = url,
                pokemon = pokemon,
                viewModel = viewModel
            )
            Spacer(modifier = modifier.height(15.dp))
            pagerSection(pokemon)
        } else {
            CircularProgressIndicator(
                modifier = modifier
                    .align(CenterHorizontally)
                    .width(36.dp)
                    .height(36.dp)
                    .padding(top = 10.dp)
            )
        }
    }
}

@Composable
fun ImageSection(
    url: String,
    pokemonName: String,
    pokemonId: Int,
    modifier: Modifier = Modifier,
    onLoad: (AsyncImagePainter) -> Unit,
    color: Color
) {
    val painter = rememberAsyncImagePainter(model = url)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = pokemonName,
                fontSize = 18.sp,
                color = color,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace

            )
            Text(
                text = "#$pokemonId",
                fontSize = 18.sp,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = modifier.height(10.dp))
        Image(
            painter = painter,
            contentDescription = pokemonName,
            modifier = modifier
                .size(240.dp)
                .align(CenterHorizontally),
            alignment = Alignment.Center
        )
        onLoad(painter)
    }

}

@Composable
fun TopSection(
    isFav: Boolean,
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit,
    url: String,
    pokemon: Pokemon,
    viewModel: PokedexViewModel
) {
    val scope = rememberCoroutineScope()
    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember {
        mutableStateOf(defaultDominantColor)
    }
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominantColor,
                        defaultDominantColor
                    )
                )
            )
            .padding(10.dp)
    ) {


        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                tint = if (Helpers.getLightness(dominantColor) < 0.1) Color.White else Color.Black,
                contentDescription = "back",
                modifier = modifier
                    .size(36.dp)
                    .clickable {
                        onClickBack()
                    }
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = "liked",
                tint = if (isFav) RedColor else DisabledColor,
                modifier = modifier
                    .size(36.dp)
            )
        }
        Spacer(modifier = modifier.height(15.dp))
        ImageSection(
            url = url,
            pokemonName = pokemon.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            },
            color = if (Helpers.getLightness(dominantColor) < 0.1) Color.White else Color.Black,
            pokemonId = pokemon.id,
            onLoad = { painter ->
                if (painter.state is AsyncImagePainter.State.Success) {
                    scope.launch {
                        val image = painter.imageLoader.execute(painter.request).drawable
                        if (image != null) {
                            viewModel.calcDominantColor(image) {
                                dominantColor = it
                            }
                        }
                    }

                }
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun pagerSection(
    pokemon: Pokemon,
    modifier: Modifier = Modifier
) {
    val pages = listOf("About", "Stats", "Sprites")
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
        },
        backgroundColor = Color.White,
        divider = {},
    ) {
        pages.forEachIndexed { index, title ->
            Tab(
                text = {
                    Text(
                        text = title,

                        )
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                modifier = modifier
                    .background(Color.White)
                    .padding(5.dp)
                    .background(DisabledColor),
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.LightGray
            )
        }
    }

    HorizontalPager(
        count = pages.size,
        state = pagerState,
        modifier = modifier
            .padding(top = 10.dp, start = 15.dp, end = 15.dp)
    ) { page ->
        when (page) {
            0 -> {
                AboutSection(
                    experience = pokemon.baseExperience,
                    height = pokemon.height,
                    weight = pokemon.weight,
                    abilities = pokemon.abilities.size
                )
            }
            1 -> {
                StatsSection(
                    pokemonStats = pokemon.stats
                )
            }
            2 -> {
                SpritesSection(pokemon.sprites)
            }
        }
    }
}

@Composable
fun SpritesSection(
    sprites: Sprites,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (sprites.frontDefault != null) {
            SpriteImage(url = sprites.frontDefault)
        } else if (sprites.frontFemale != null) {
            SpriteImage(url = sprites.frontFemale)
        } else if (sprites.frontShiny != null) {
            SpriteImage(url = sprites.frontShiny)
        } else if (sprites.frontShinyFemale != null) {
            SpriteImage(url = sprites.frontShinyFemale)
        }

        if (sprites.backDefault != null) {
            SpriteImage(url = sprites.backDefault)
        } else if (sprites.backFemale != null) {
            SpriteImage(url = sprites.backFemale)
        } else if (sprites.backShiny != null) {
            SpriteImage(url = sprites.backShiny)
        } else if (sprites.backShinyFemale != null) {
            SpriteImage(url = sprites.backShinyFemale)
        }
    }
}

@Composable
fun SpriteImage(
    modifier: Modifier = Modifier,
    url: String
) {
    val painter = rememberAsyncImagePainter(model = url)
    Log.e("LOG", "Url: $url")
    Image(
        painter = painter,
        contentDescription = "image",
        modifier = modifier
            .size(160.dp)
    )
    if (painter.state is AsyncImagePainter.State.Loading || painter.state is AsyncImagePainter.State.Error) {
        CircularProgressIndicator()
    }
}

@Composable
fun StatsSection(
    modifier: Modifier = Modifier,
    pokemonStats: List<Stat>
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        pokemonStats.forEach { stat ->
            StatsSectionRow(
                statName = Parsers.statToAbbr(stat),
                statValue = stat.baseStat,
                statColor = Parsers.statToColor(stat),
            )
        }
    }
}

@Composable
fun StatsSectionRow(
    modifier: Modifier = Modifier,
    maxValue: Int = 100,
    statName: String,
    statValue: Int,
    statColor: Color,
    animDuration: Int = 1000,
    animDelay: Int = 0
) {
    var animationPlayed by remember {
        mutableStateOf(false)
    }
    val curPercent = animateFloatAsState(
        targetValue = if (animationPlayed) {
            statValue / maxValue.toFloat()
        } else 0f,
        animationSpec = tween(
            animDuration,
            animDelay
        )
    )
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth(curPercent.value)
            .clip(CircleShape)
            .background(statColor)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = statName,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = (curPercent.value * maxValue).toInt().toString(),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AboutSection(
    experience: Int = 1000,
    height: Int = 100,
    weight: Int = 200,
    abilities: Int = 3,
    modifier: Modifier = Modifier
) {
    val newHeight = Helpers.formatNumber((height / 10f))
    val newWeight = Helpers.formatNumber((weight / 10f))

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        AboutRow(
            titleLeft = "Height",
            contentLeft = "$newHeight m (${Helpers.heightMetersToFeet(newHeight)})",
            titleRight = "Weight",
            contentRight = "$newWeight kg (${Helpers.weightKgToLb(newWeight)} lbs)"
        )
        Spacer(modifier = modifier.height(10.dp))
        AboutRow(
            titleLeft = "Experience",
            contentLeft = "$experience",
            titleRight = "Abilities",
            contentRight = "$abilities"
        )
    }
}

@Composable
fun AboutRow(
    titleLeft: String,
    contentLeft: String,
    titleRight: String,
    contentRight: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = modifier
                .weight(1f)
        ) {
            Text(
                text = titleLeft,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = contentLeft
            )
        }

        Column(
            modifier = modifier
                .weight(1f)
        ) {
            Text(
                text = titleRight,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = contentRight
            )
        }
    }
}
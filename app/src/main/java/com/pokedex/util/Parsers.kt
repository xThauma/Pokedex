package com.pokedex.util

import androidx.compose.ui.graphics.Color
import com.plcoding.jetpackcomposepokedex.data.remote.responses.Stat
import com.plcoding.jetpackcomposepokedex.data.remote.responses.Type
import com.pokedex.ui.theme.*
import java.util.*

class Parsers {
    companion object {
        fun typeToColor(type: Type): Color {
            return when (type.type.name.toLowerCase(Locale.ROOT)) {
                "normal" -> TypeNormal
                "fire" -> TypeFire
                "water" -> TypeWater
                "electric" -> TypeElectric
                "grass" -> TypeGrass
                "ice" -> TypeIce
                "fighting" -> TypeFighting
                "poison" -> TypePoison
                "ground" -> TypeGround
                "flying" -> TypeFlying
                "psychic" -> TypePsychic
                "bug" -> TypeBug
                "rock" -> TypeRock
                "ghost" -> TypeGhost
                "dragon" -> TypeDragon
                "dark" -> TypeDark
                "steel" -> TypeSteel
                "fairy" -> TypeFairy
                else -> Color.Black
            }
        }

        fun statToColor(stat: Stat): Color {
            return when (stat.stat.name.toLowerCase()) {
                "hp" -> HPColor
                "attack" -> AtkColor
                "defense" -> DefColor
                "special-attack" -> SpAtkColor
                "special-defense" -> SpDefColor
                "speed" -> SpdColor
                else -> Color.White
            }
        }

        fun statToAbbr(stat: Stat): String {
            return when (stat.stat.name.toLowerCase()) {
                "hp" -> "HP"
                "attack" -> "Atk"
                "defense" -> "Def"
                "special-attack" -> "SpAtk"
                "special-defense" -> "SpDef"
                "speed" -> "Spd"
                else -> ""
            }
        }
    }
}
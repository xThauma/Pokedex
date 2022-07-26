package com.pokedex.util

import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import java.util.*
import kotlin.random.Random


class Helpers {

    companion object {
        fun weightKgToLb(weightInKg: String) =
            String.format(Locale.ENGLISH, "%.1f", weightInKg.toFloat() * 2.20462262f)

        fun heightMetersToFeet(heightInMeters: String): String {
            val inches = String.format(Locale.ENGLISH, "%.2f", heightInMeters.toFloat() * 3.93700787)
            val dotIndex = inches.indexOf(".")
            val inchesPre = inches.substring(0, dotIndex)
            val inchesPost = inches.substring(dotIndex + 1)
            return "$inchesPre'$inchesPost\""
        }

        fun formatNumber(num: Float) =
            if (num % 1.0 == 0.0) {
                num.toInt().toString()
            } else {
                num.toString()
            }

        fun getLightness(colorObj: androidx.compose.ui.graphics.Color): Float {
            val color = colorObj.toArgb()
            val red: Int = Color.red(color)
            val green: Int = Color.green(color)
            val blue: Int = Color.blue(color)
            val hsl = FloatArray(3)
            ColorUtils.RGBToHSL(red, green, blue, hsl)
            return hsl[2]
        }

        fun getFreeColor(statColors: List<androidx.compose.ui.graphics.Color>, usedColors: MutableList<androidx.compose.ui.graphics.Color>) : androidx.compose.ui.graphics.Color {
            val randomColor = statColors[Random.nextInt(statColors.size)]
            return if (!usedColors.contains(randomColor)) {
                usedColors += randomColor
                randomColor
            } else {
                getFreeColor(statColors, usedColors)
            }
        }
    }
}
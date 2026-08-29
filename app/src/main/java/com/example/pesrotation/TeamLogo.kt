package com.example.pesrotation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TeamLogo(
    team: Team,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val isNationalTeam = team.category != "CLB"
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, team.color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(team.imageRes),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .then(if (isNationalTeam) Modifier else Modifier.padding(10.dp)),
            contentScale = if (isNationalTeam) ContentScale.Crop else ContentScale.Fit,
        )
    }
}

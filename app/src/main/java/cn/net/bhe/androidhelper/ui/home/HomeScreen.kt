package cn.net.bhe.androidhelper.ui.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable


@Composable
fun HomeScreen() {
    val cardList = listOf(
        CardData("Card 1", "Description for Card 1"),
        CardData("Card 2", "Description for Card 2"),
        CardData("Card 3", "Description for Card 3"),
    )

    LazyColumn {
        items(cardList.size) { index ->
            CardItem(card = cardList[index])
        }
    }
}
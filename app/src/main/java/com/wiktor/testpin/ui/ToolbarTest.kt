@Composable
fun GomToolbar(
    title: String,
    leftmostClickableItem: GomScaffoldTopBarItem?,
    rightmostClickableItems: List<GomScaffoldTopBarItem> = emptyList(),
    isToolbarDividerEnabled: Boolean = true,
) {
    val sidePadding = if (leftmostClickableItem != null || rightmostClickableItems.isNotEmpty()) {
        56.dp        // przestrzeń materialowa dla ikon (ikonka + minimalny margines)
    } else {
        16.dp        // standardowy padding gdy ikon brak
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {

            // LEFT ICON
            leftmostClickableItem?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                ) {
                    it.draw(modifier = Modifier.size(24.dp))
                }
            }

            // RIGHT ICONS
            if (rightmostClickableItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rightmostClickableItems.forEach { item ->
                        item.draw(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 16.dp)
                        )
                    }
                }
            }

            // TITLE
            Text(
                text = title,
                style = GomTheme.typography.headingH3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = sidePadding)   // klucz!
                    .wrapContentWidth(Alignment.CenterStart)
                    .offset(y = 1.dp) // optyczne wycentrowanie pionowe
                    .align(Alignment.Center)
            )
        }

        if (isToolbarDividerEnabled) {
            BnpDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

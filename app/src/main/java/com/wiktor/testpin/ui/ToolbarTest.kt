@Composable
fun GomToolbar(
    title: String,
    leftmostClickableItem: GomScaffoldTopBarItem?,
    rightmostClickableItems: List<GomScaffoldTopBarItem> = emptyList(),
    isToolbarDividerEnabled: Boolean = true,
    iconSize: Dp = 24.dp,
    sideContainerWidthIfPresent: Dp = 56.dp, // przestrzeń rezerwowana gdy są ikony
    sideContainerWidthIfAbsent: Dp = 16.dp,  // minimalny gap gdy brakuje ikon
    appBarHeight: Dp = 56.dp,
    titleVerticalOffset: Dp = 1.dp // optyczne przesunięcie w dół
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(appBarHeight)
                .background(GomTheme.colors.panel)
        ) {
            // LEFT container - ma stałą minimalną szerokość (zapobiega przesuwaniu/ściskaniu)
            val leftWidth = if (leftmostClickableItem != null) sideContainerWidthIfPresent else sideContainerWidthIfAbsent
            Box(
                modifier = Modifier
                    .width(leftWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.CenterStart
            ) {
                leftmostClickableItem?.draw(
                    modifier = Modifier
                        .padding(start = GomTheme.spacings.base)
                        .size(iconSize)
                )
            }

            // RIGHT container - stała minimalna szerokość, ikony nie będą się ściskać
            val rightWidth = if (rightmostClickableItems.isNotEmpty()) sideContainerWidthIfPresent else sideContainerWidthIfAbsent
            Box(
                modifier = Modifier
                    .width(rightWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.padding(end = GomTheme.spacings.base)
                ) {
                    rightmostClickableItems.forEach { item ->
                        // każda ikona ma stały rozmiar; padding między ikonami jest również stały
                        item.draw(
                            modifier = Modifier
                                .size(iconSize)
                                .padding(start = GomTheme.spacings.base)
                        )
                    }
                }
            }

            // TITLE - matematycznie wycentrowany w całym pasku,
            // jednocześnie ograniczony przez obecność stałych lewych/prawych kontenerów
            Text(
                text = title,
                style = GomTheme.typography.headingH3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth() // zajmuje całą szerokość, by center odnosiło się do całego paska
                    .wrapContentWidth(Alignment.CenterHorizontally) // centrowanie wewnątrz tej szer.
                    .offset(y = titleVerticalOffset) // optyczne wycentrowanie pionowe
                    .align(Alignment.Center)
            )
        }

        if (isToolbarDividerEnabled) {
            BnpDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

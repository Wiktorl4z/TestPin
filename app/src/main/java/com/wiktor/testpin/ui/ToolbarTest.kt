@Composable
fun GomToolbar(
    title: String,
    leftmostClickableItem: GomScaffoldTopBarItem?,
    rightmostClickableItems: List<GomScaffoldTopBarItem> = emptyList(),
    isToolbarDividerEnabled: Boolean = true,
) {
    TopAppBar(
        windowInsets = AppBarDefaults.topAppBarWindowInsets,
        backgroundColor = GomTheme.colors.panel,
        elevation = 0.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GomTheme.colors.panel),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ---- LEFT AREA ----
                val leftWidth = if (leftmostClickableItem != null) {
                    24.dp + GomTheme.spacings.base + 8.dp
                } else {
                    8.dp // minimalny odstęp gdy brak ikony
                }

                Box(
                    modifier = Modifier
                        .width(leftWidth),
                    contentAlignment = Alignment.CenterStart
                ) {
                    leftmostClickableItem?.draw(
                        modifier = Modifier
                            .padding(start = GomTheme.spacings.base)
                            .size(24.dp)
                    )
                }


                // ---- CENTER AREA (CENTERED TITLE) ----
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = GomTheme.typography.headingH3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 8.dp) // safety padding
                            .wrapContentWidth()         // 👈 kluczowe: NIE fillMaxWidth
                    )
                }


                // ---- RIGHT AREA ----
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = GomTheme.spacings.base)
                ) {
                    rightmostClickableItems.forEach { item ->
                        item.draw(
                            modifier = Modifier
                                .padding(start = GomTheme.spacings.base)
                                .size(24.dp)
                        )
                    }
                }
            }

            if (isToolbarDividerEnabled) {
                BnpDivider()
            }
        }
    }
}

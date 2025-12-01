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
                    .background(GomTheme.colors.panel)
                    .padding(horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ---- LEFT AREA ----
                if (leftmostClickableItem != null) {
                    Box(
                        modifier = Modifier
                            .width(24.dp + GomTheme.spacings.base + 8.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        leftmostClickableItem.draw(
                            modifier = Modifier
                                .padding(start = GomTheme.spacings.base)
                                .size(24.dp)
                        )
                    }
                } else {
                    // brak ikony -> zostawiamy minimalny padding 8dp
                    Spacer(Modifier.width(8.dp))
                }


                // ---- CENTER TEXT ----
                Text(
                    text = title,
                    style = GomTheme.typography.headingH3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 0.dp, end = 8.dp)
                )


                // ---- RIGHT ICONS ----
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

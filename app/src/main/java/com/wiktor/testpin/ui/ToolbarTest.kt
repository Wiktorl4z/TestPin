@Composable
fun GomToolbar(
    title: String,
    leftmostClickableItem: GomScaffoldTopBarItem?,
    rightmostClickableItems: List<GomScaffoldTopBarItem> = emptyList(),
    isToolbarDividerEnabled: Boolean = true
) {
    // Standardowa szerokość miejsca na jedną ikonę
    val leftSectionWidth = 56.dp

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(GomTheme.colors.panel),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --------------------------
            //       LEFT SECTION
            // --------------------------
            Box(
                modifier = Modifier
                    .width(leftSectionWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                leftmostClickableItem?.draw(
                    modifier = Modifier
                        .padding(start = GomTheme.spacings.base)
                        .size(24.dp)
                )
            }

            // --------------------------
            //       CENTER SECTION
            // --------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = GomTheme.typography.headingH3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset(y = 1.dp)
                )
            }

            // --------------------------
            //       RIGHT SECTION
            // --------------------------
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .padding(end = GomTheme.spacings.base),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
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
            BnpDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

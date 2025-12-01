@Composable
fun GomToolbar(
    title: String,
    leftmostClickableItem: GomScaffoldTopBarItem?,
    rightmostClickableItems: List<GomScaffoldTopBarItem> = emptyList(),
    isToolbarDividerEnabled: Boolean = true
) {
    val sectionWidth = 48.dp // stała przestrzeń jak w Material AppBar

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(GomTheme.colors.panel),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // LEFT SECTION — stała szerokość, ikonka się nie zmniejszy
            Box(
                modifier = Modifier
                    .width(sectionWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                leftmostClickableItem?.draw(
                    modifier = Modifier.size(24.dp)
                )
            }

            // CENTER SECTION — tekst zawsze w centrum całego TopAppBar
            Box(
                modifier = Modifier
                    .weight(1f)                    // <-- czysta magia centrowania
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = GomTheme.typography.headingH3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset(y = 1.dp) // optyczne wyśrodkowanie pionowe
                )
            }

            // RIGHT SECTION — stała szerokość, ikony się nie zmniejszą
            Box(
                modifier = Modifier
                    .width(sectionWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    rightmostClickableItems.forEach { item ->
                        item.draw(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        if (isToolbarDividerEnabled) {
            BnpDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

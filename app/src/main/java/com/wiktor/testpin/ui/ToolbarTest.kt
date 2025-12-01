@Composable
fun GomToolbar(
    title: String,
    leftmostClickableItem: GomScaffoldTopBarItem?,
    rightmostClickableItems: List<GomScaffoldTopBarItem> = emptyList(),
    isToolbarDividerEnabled: Boolean = true,
    minSideGap: Dp = 8.dp,         // minimalny odstęp między ikoną a tekstem
    iconSize: Dp = 24.dp,
    appBarHeight: Dp = 56.dp,
    titleVerticalOffset: Dp = 1.dp // optyczne przesunięcie w dół, dopasuj jeśli trzeba
) {
    val density = LocalDensity.current

    Column {
        // SubcomposeLayout daje nam kontrolę nad pomiarem sekcji
        SubcomposeLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(appBarHeight)
                .background(GomTheme.colors.panel)
        ) { constraints ->
            // przelicz Dp -> px
            val minGapPx = with(density) { minSideGap.roundToPx() }
            val iconSizePx = with(density) { iconSize.roundToPx() }
            val startPaddingPx = with(density) { GomTheme.spacings.base.roundToPx() } // jeśli używacie takiego paddingu

            // 1) zmierz LEWĄ sekcję (jeśli jest)
            val leftPlaceables = if (leftmostClickableItem != null) {
                val measurables = subcompose("left") {
                    Box(
                        modifier = Modifier
                            .padding(start = GomTheme.spacings.base)
                    ) {
                        leftmostClickableItem.draw(
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
                measurables.map { it.measure(constraints.copy(maxWidth = constraints.maxWidth)) }
            } else {
                emptyList()
            }
            val leftWidthPx = if (leftPlaceables.isNotEmpty()) {
                // leftPlaceables sum width (should be one)
                leftPlaceables.maxOf { it.width } + minGapPx // dodajemy minimalny gap po ikonie
            } else {
                // brak ikony => minimum gap od lewej krawędzi
                minGapPx
            }

            // 2) zmierz PRAWĄ sekcję (może mieć kilka ikon)
            val rightPlaceables = if (rightmostClickableItems.isNotEmpty()) {
                val measurables = subcompose("right") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = GomTheme.spacings.base)
                    ) {
                        rightmostClickableItems.forEach { item ->
                            item.draw(
                                modifier = Modifier
                                    .padding(start = GomTheme.spacings.base)
                                    .size(iconSize)
                            )
                        }
                    }
                }
                measurables.map { it.measure(constraints.copy(maxWidth = constraints.maxWidth)) }
            } else {
                emptyList()
            }
            val rightWidthPx = if (rightPlaceables.isNotEmpty()) {
                rightPlaceables.maxOf { it.width } + minGapPx
            } else {
                minGapPx
            }

            // 3) ile miejsca możemy dać tytułowi, tak żeby był wycentrowany w całym pasku,
            //    ale nie nachodził na ikony:
            val maxReserved = maxOf(leftWidthPx, rightWidthPx)
            val availableForTitle = (constraints.maxWidth - 2 * maxReserved).coerceAtLeast(0)

            // 4) zmierz tytuł z ograniczeniem szerokości
            val titleMeasurables = subcompose("title") {
                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text(
                        text = title,
                        style = GomTheme.typography.headingH3,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(horizontal = minSideGap)
                            .offset(y = titleVerticalOffset) // optyczne centrowanie
                    )
                }
            }

            val titleConstraints = Constraints(
                maxWidth = availableForTitle,
                minWidth = 0,
                minHeight = 0,
                maxHeight = constraints.maxHeight
            )
            val titlePlaceables = titleMeasurables.map { it.measure(titleConstraints) }
            val titleWidthPx = titlePlaceables.maxOfOrNull { it.width } ?: 0
            val titleHeightPx = titlePlaceables.maxOfOrNull { it.height } ?: 0

            // 5) wybierz największe wysokości (na wypadek różnic)
            val leftHeight = leftPlaceables.maxOfOrNull { it.height } ?: 0
            val rightHeight = rightPlaceables.maxOfOrNull { it.height } ?: 0
            val contentHeight = maxOf(titleHeightPx, leftHeight, rightHeight, constraints.minHeight)

            // 6) finalne ułożenie: title na środku całego width, left na starcie, right na końcu
            layout(width = constraints.maxWidth, height = contentHeight) {
                // left
                if (leftPlaceables.isNotEmpty()) {
                    val lp = leftPlaceables.first()
                    val leftY = (contentHeight - lp.height) / 2
                    lp.placeRelative(0, leftY)
                }

                // right (umieszczamy na końcu)
                if (rightPlaceables.isNotEmpty()) {
                    val rp = rightPlaceables.first()
                    val rightX = constraints.maxWidth - rp.width
                    val rightY = (contentHeight - rp.height) / 2
                    rp.placeRelative(rightX, rightY)
                }

                // title — centrowany absolutnie względem całej szerokości
                if (titlePlaceables.isNotEmpty()) {
                    val tp = titlePlaceables.first()
                    val titleX = (constraints.maxWidth - tp.width) / 2
                    val titleY = (contentHeight - tp.height) / 2
                    tp.placeRelative(titleX, titleY)
                }
            }
        }

        // Divider NA DOLE paska (poza TopAppBar content)
        if (isToolbarDividerEnabled) {
            BnpDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

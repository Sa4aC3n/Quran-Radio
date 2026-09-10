package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.magicnav.MagicNavItem
import com.example.magicnav.MagicNavigationMenu
import com.example.magicnav.defaultAppNavItems

@Composable
fun MagicNavBar(
    modifier: Modifier = Modifier,
    items: List<MagicNavItem> = defaultAppNavItems,
    currentRoute: String? = null,
    onItemSelected: (MagicNavItem) -> Unit = {}
) {
    MagicNavigationMenu(
        modifier = modifier,
        items = items,
        currentRoute = currentRoute,
        onItemSelected = onItemSelected
    )
}

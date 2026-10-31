package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalAppLanguage
import com.example.localization.StringKey
import com.example.localization.appString
import com.example.localization.localizedName
import com.example.model.Country
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesScreen(
    viewModel: RadioViewModel,
    onCountryClick: (Country) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val countries by viewModel.countries.collectAsState()
    val allStations by viewModel.allStations.collectAsState()
    val gridColumns by viewModel.countriesGridColumns.collectAsState()

    var isLayoutSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Normalize columns to either 3 or 4
    val columnsCount = if (gridColumns == 4) 4 else 3

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                title = {
                    Text(
                        text = appString(StringKey.COUNTRIES_TITLE),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(
                        onClick = { isLayoutSheetOpen = true },
                        modifier = Modifier.testTag("btn_toggle_grid_layout")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "طريقة عرض الدول",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { innerPadding ->
        val horizontalSpacing = if (columnsCount == 4) 6.dp else 8.dp
        val verticalSpacing = if (columnsCount == 4) 6.dp else 8.dp
        val contentPaddingHorizontal = if (columnsCount == 4) 8.dp else 12.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .testTag("countries_grid"),
            contentPadding = PaddingValues(horizontal = contentPaddingHorizontal, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            items(countries, key = { it.code }) { country ->
                val countryStations = allStations.filter { it.countryCode.equals(country.code, ignoreCase = true) }
                val stationCount = countryStations.size

                CompactCountryCard(
                    country = country,
                    stationCount = stationCount,
                    isFourColumns = (columnsCount == 4),
                    onClick = { onCountryClick(country) }
                )
            }
        }
    }

    // Grid Layout Selection Bottom Sheet
    if (isLayoutSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isLayoutSheetOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkyBluePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "طريقة عرض الدول",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "اختر عدد البطاقات المعروضة في كل صف",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: 3 cards per row
                GridLayoutOptionItem(
                    title = "3 بطاقات في الصف",
                    description = "عرض متوازن وواضح لأسماء وتفاصيل الدول",
                    isSelected = (columnsCount == 3),
                    onClick = {
                        viewModel.setCountriesGridColumns(3)
                        isLayoutSheetOpen = false
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: 4 cards per row
                GridLayoutOptionItem(
                    title = "4 بطاقات في الصف",
                    description = "عرض مدمج ومضغوط يسمح بمشاهدة عدد أكبر من الدول",
                    isSelected = (columnsCount == 4),
                    onClick = {
                        viewModel.setCountriesGridColumns(4)
                        isLayoutSheetOpen = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CompactCountryCard(
    country: Country,
    stationCount: Int,
    isFourColumns: Boolean,
    onClick: () -> Unit
) {
    val cardCornerRadius = if (isFourColumns) 14.dp else 16.dp
    val flagBoxSize = if (isFourColumns) 32.dp else 40.dp
    val flagCornerRadius = if (isFourColumns) 9.dp else 12.dp
    val flagFontSize = if (isFourColumns) 16.sp else 22.sp
    val verticalCardPadding = if (isFourColumns) 6.dp else 8.dp
    val horizontalCardPadding = if (isFourColumns) 2.dp else 4.dp
    val nameFontSize = if (isFourColumns) 10.5.sp else 12.5.sp
    val countFontSize = if (isFourColumns) 8.5.sp else 10.sp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cardCornerRadius))
            .clickable { onClick() }
            .testTag("country_item_${country.code}"),
        shape = RoundedCornerShape(cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalCardPadding, horizontal = horizontalCardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flag Box
            Box(
                modifier = Modifier
                    .size(flagBoxSize)
                    .clip(RoundedCornerShape(flagCornerRadius))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = country.flagEmoji,
                    fontSize = flagFontSize
                )
            }

            Spacer(modifier = Modifier.height(if (isFourColumns) 4.dp else 6.dp))

            // Country Name
            val currentLang = LocalAppLanguage.current
            val countryDisplayName = country.localizedName(currentLang)
            Text(
                text = countryDisplayName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = nameFontSize
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            Spacer(modifier = Modifier.height(1.dp))

            // Stations count
            Text(
                text = appString(StringKey.COUNTRIES_STATIONS_COUNT, stationCount),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SkyBluePrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = countFontSize
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GridLayoutOptionItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SkyBluePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = SkyBluePrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

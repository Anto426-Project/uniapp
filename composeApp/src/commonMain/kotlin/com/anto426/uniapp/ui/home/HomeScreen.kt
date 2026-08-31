package com.anto426.uniapp.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.uniapp.home.presentation.HomeUiState
import com.anto426.uniapp.model.home.dashboard.HomeDashboardData
import com.anto426.uniapp.ui.home.components.GlassCard
import com.anto426.uniapp.ui.home.components.HeroCareerCard
import com.anto426.uniapp.ui.home.components.QuickActionPills
import com.anto426.uniapp.ui.home.components.QuickStatsGrid
import com.anto426.uniapp.ui.home.components.UpcomingDeadlineCard
import com.anto426.uniapp.ui.theme.UniColors
import com.anto426.uniapp.ui.theme.UniTheme

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onNavigateToRoute: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    UniTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(UniColors.BackgroundDark)
        ) {
            // Ambient Radiant Background Glow (Monet / AGSL simulation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(UniColors.AmbientGlowGradient)
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    HomeTopBar(
                        uiState = uiState,
                        onProfileClick = onNavigateToProfile
                    )
                },
                bottomBar = {
                    HomeBottomNavigationBar(
                        currentTab = "home",
                        onTabSelected = { tab ->
                            when (tab) {
                                "home" -> {}
                                "didattica" -> onNavigateToRoute("didattica")
                                "servizi" -> onNavigateToRoute("servizi")
                                "profilo" -> onNavigateToProfile()
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (val state = uiState) {
                        is HomeUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = UniColors.PrimaryMagenta,
                                    strokeWidth = 3.dp
                                )
                            }
                        }

                        is HomeUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = 24.dp
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = "⚠️", fontSize = 32.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = state.message,
                                            fontSize = 14.sp,
                                            color = UniColors.TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        GlassCard(
                                            shape = RoundedCornerShape(16.dp),
                                            backgroundColor = UniColors.PrimaryMagenta.copy(alpha = 0.3f),
                                            contentPadding = 12.dp,
                                            onClick = onRetry
                                        ) {
                                            Text(
                                                text = "Riprova",
                                                fontWeight = FontWeight.Bold,
                                                color = UniColors.TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is HomeUiState.Success -> {
                            HomeContent(
                                data = state.data,
                                onNavigateToRoute = onNavigateToRoute
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    uiState: HomeUiState,
    onProfileClick: () -> Unit
) {
    val studentName = (uiState as? HomeUiState.Success)?.data?.profile?.firstName ?: "Studente"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(UniColors.PrimaryPurple.copy(alpha = 0.3f))
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏛️",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Home",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = UniColors.TextPrimary
                )
                Text(
                    text = "Ciao, $studentName ✨",
                    fontSize = 13.sp,
                    color = UniColors.TextSecondary
                )
            }
        }

        // Notification / Search Pill
        GlassCard(
            shape = CircleShape,
            contentPadding = 10.dp,
            backgroundColor = Color(0x3326173D),
            onClick = onProfileClick
        ) {
            Text(
                text = "🔔",
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun HomeContent(
    data: HomeDashboardData,
    onNavigateToRoute: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Career Overview Card
        item {
            HeroCareerCard(
                career = data.career,
                onClick = { onNavigateToRoute("didattica/carriera") }
            )
        }

        // Next Deadline Alert Card (if any)
        if (data.nextDeadline != null) {
            item {
                UpcomingDeadlineCard(
                    deadline = data.nextDeadline,
                    onClick = { onNavigateToRoute("servizi/tasse") }
                )
            }
        }

        // 2x2 Mini Stats Grid (Exams, Taxes, etc.)
        item {
            QuickStatsGrid(
                stats = data.stats,
                onExamsClick = { onNavigateToRoute("didattica/appelli") },
                onTaxesClick = { onNavigateToRoute("servizi/tasse") }
            )
        }

        // Quick Actions Grid
        item {
            QuickActionPills(
                actions = data.quickActions,
                onActionClick = { action -> onNavigateToRoute(action.routeKey) }
            )
        }
    }
}

@Composable
private fun HomeBottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            shape = RoundedCornerShape(32.dp),
            backgroundColor = Color(0x4D1A102A),
            borderBrush = UniColors.GlassBorderGradient,
            contentPadding = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = "🏛️",
                    label = "Home",
                    isSelected = currentTab == "home",
                    onClick = { onTabSelected("home") }
                )
                BottomNavItem(
                    icon = "🎓",
                    label = "Didattica",
                    isSelected = currentTab == "didattica",
                    onClick = { onTabSelected("didattica") }
                )
                BottomNavItem(
                    icon = "🌐",
                    label = "Servizi",
                    isSelected = currentTab == "servizi",
                    onClick = { onTabSelected("servizi") }
                )
                BottomNavItem(
                    icon = "⚙️",
                    label = "Impostazioni",
                    isSelected = currentTab == "profilo",
                    onClick = { onTabSelected("profilo") }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) UniColors.PrimaryMagenta.copy(alpha = 0.25f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = if (isSelected) 22.sp else 18.sp
        )
    }
}

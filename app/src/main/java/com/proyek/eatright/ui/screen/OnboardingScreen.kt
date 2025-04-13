package com.proyek.eatright.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.proyek.eatright.R
import com.proyek.eatright.ui.theme.DarkBlue
import com.proyek.eatright.ui.theme.DarkBlue2
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    onboardingComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val isLastPage = remember {
        derivedStateOf { pagerState.currentPage == 2 }
    }

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal Pager for Onboarding Screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPage(page)
            }

            // Page Indicator
            PageIndicator(
                pageCount = 3,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Navigation Buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (isLastPage.value) {
                            onboardingComplete()
                            navController.navigate("login") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLastPage.value) "Mulai" else "Lanjut",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isLastPage.value) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = if (isLastPage.value) "Mulai" else "Lanjut"
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustrations and Text based on page
        when (page) {
            0 -> {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_1),
                    contentDescription = "Nutrition Tracking",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(bottom = 32.dp)
                )
                Text(
                    text = "Lacak Nutrisi Anda",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue2
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Pantau asupan gizi harian Anda dengan mudah dan akurat. EatRight membantu Anda memahami kebutuhan nutrisi tubuh.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
            1 -> {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_2),
                    contentDescription = "Meal Planning",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(bottom = 32.dp)
                )
                Text(
                    text = "Rencana Makanan Pintar",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue2
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Buat rencana makan sehat yang disesuaikan dengan kebutuhan dan tujuan kesehatan Anda. Dapatkan rekomendasi makanan terbaik.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
            2 -> {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_3),
                    contentDescription = "Health Progress",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(bottom = 32.dp)
                )
                Text(
                    text = "Pantau Kemajuan Anda",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue2
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Lihat perkembangan kesehatan Anda secara real-time. Dapatkan wawasan mendalam tentang perjalanan nutrisi dan kebugaran Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { page ->
            val width = animateDpAsState(
                targetValue = if (page == currentPage) 32.dp else 12.dp,
                label = "Indicator Width"
            )
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(width.value)
                    .clip(CircleShape)
                    .background(
                        if (page == currentPage) DarkBlue
                        else Color.LightGray
                    )
            )
        }
    }
}

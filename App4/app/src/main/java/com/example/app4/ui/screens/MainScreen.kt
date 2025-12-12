package com.example.app4.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app4.features.files.FileScreen
import com.example.app4.features.media.MediaScreen
import com.example.app4.features.pdf.PdfScreen
import com.example.app4.features.security.SecurityScreen
import kotlinx.coroutines.launch

enum class DemoTab(val title: String, val emoji: String) {
    FILES("Fichiers", "📁"),
    MEDIA("Photos", "📷"),
    PDF("PDF", "📄"),
    SECURITY("Sécurité", "🛡️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val tabs = DemoTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Demo 3 - Fichiers & Médias",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = tab.emoji, fontSize = 16.sp)
                                Text(text = tab.title)
                            }
                        }
                    )
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (tabs[page]) {
                    DemoTab.FILES -> FileScreen()
                    DemoTab.MEDIA -> MediaScreen()
                    DemoTab.PDF -> PdfScreen()
                    DemoTab.SECURITY -> SecurityScreen()
                }
            }
        }
    }
}

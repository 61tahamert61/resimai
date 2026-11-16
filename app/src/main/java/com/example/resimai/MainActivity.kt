package com.example.resimai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import okhttp3.OkHttpClient
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

// Görsel servisi: değiştirmek isterseniz sadece bu iki satırı güncelleyin.
const val API = "https://image.pollinations.ai/prompt/"
fun imageUrl(prompt: String, seed: Int) =
    API + URLEncoder.encode(prompt, "UTF-8").replace("+", "%20") +
        "?width=512&height=512&nologo=true&seed=$seed"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { App() } }
    }
}

@Composable
fun App() {
    val ctx = LocalContext.current
    val loader = remember {
        ImageLoader.Builder(ctx).okHttpClient {
            OkHttpClient.Builder().readTimeout(120, TimeUnit.SECONDS).build()
        }.build()
    }
    var prompt by remember { mutableStateOf("") }
    var urls by remember { mutableStateOf(listOf<String>()) }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.statusBarsPadding().padding(16.dp)) {
            Text("ResimAI", style = MaterialTheme.typography.headlineMedium)
            Text("Ne çizmemi istersin? 9 farklı resim üretirim.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = prompt, onValueChange = { prompt = it },
                label = { Text("örn. gün batımında uzaylı kedi") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val base = (0..900000).random()
                    urls = List(9) { imageUrl(prompt.trim(), base + it) }
                },
                enabled = prompt.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Çiz") }
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(urls, key = { it }) { u ->
                    SubcomposeAsyncImage(
                        model = u, imageLoader = loader, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        loading = { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp)) } },
                        error = { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("⚠") } },
                        modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

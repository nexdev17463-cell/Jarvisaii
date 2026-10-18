package com.nexdev.jarvis

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val Background = Color(0xFF050B18)
private val Panel = Color(0xFF0D1930)
private val Cyan = Color(0xFF43E8FF)
private val Blue = Color(0xFF168BFF)
private val Purple = Color(0xFF845BFF)
private val Muted = Color(0xFF8CA5C8)
private val NexFont = FontFamily(Font(R.font.poppins_regular, FontWeight.Normal))

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "IN")
            }
        }

        setContent {
            JarvisApp(
                onSpeak = { text ->
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis")
                }
            )
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun JarvisApp(onSpeak: (String) -> Unit) {
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1400); showSplash = false }
    var tab by remember { mutableStateOf("Home") }
    var prompt by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var speakReplies by remember { mutableStateOf(true) }
    var confirmAccessibilityActions by remember { mutableStateOf(true) }
    var selectedTheme by remember { mutableStateOf("Midnight") }
    val messages = remember {
        mutableStateListOf(
            "JARVIS" to "Hello! Main JARVIS hoon. Hindi ya English mein baat karo."
        )
    }
    val scope = rememberCoroutineScope()

    val speech = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            prompt = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull().orEmpty()
        }
    }

    fun listen() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS")
            }
            speech.launch(intent)
        } catch (_: Exception) {
            messages.add("JARVIS" to "Speech recognition is not available.")
            tab = "Chat"
        }
    }

    fun send() {
        val question = prompt.trim()
        if (question.isEmpty() || loading) return

        messages.add("YOU" to question)
        prompt = ""
        tab = "Chat"
        loading = true

        scope.launch {
            val answer = withContext(Dispatchers.IO) {
                askJarvis(question)
            }
            messages.add("JARVIS" to answer)
            loading = false
        }
    }

    MaterialTheme(typography = Typography().run { copy(bodyLarge = bodyLarge.copy(fontFamily = NexFont), bodyMedium = bodyMedium.copy(fontFamily = NexFont), bodySmall = bodySmall.copy(fontFamily = NexFont), titleLarge = titleLarge.copy(fontFamily = NexFont), titleMedium = titleMedium.copy(fontFamily = NexFont), titleSmall = titleSmall.copy(fontFamily = NexFont), headlineSmall = headlineSmall.copy(fontFamily = NexFont), labelLarge = labelLarge.copy(fontFamily = NexFont), labelMedium = labelMedium.copy(fontFamily = NexFont), labelSmall = labelSmall.copy(fontFamily = NexFont)) }) {
        if (showSplash) NexSplashScreen() else Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        when (selectedTheme) {
                            "Ocean" -> listOf(Color(0xFF073B4C), Background, Color(0xFF020B12))
                            "Violet" -> listOf(Color(0xFF28164B), Background, Color(0xFF080510))
                            else -> listOf(Color(0xFF101F3C), Background, Color(0xFF030711))
                        }
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "N E X D E V   S Y S T E M S",
                        color = Cyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "J.A.R.V.I.S",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "PERSONAL AI ASSISTANT",
                        color = Muted,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                }
                Box(
                    Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(listOf(Blue, Purple)),
                            CircleShape
                        )
                        .padding(1.dp)
                        .background(Panel, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("N", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                }
            }

            when (tab) {
                "Chat" -> {
                    Text("AI CONVERSATION", color = Cyan, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { item ->
                            val user = item.first == "YOU"
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (user) Color(0xFF103451) else Panel)
                                    .border(
                                        1.dp,
                                        if (user) Blue.copy(alpha = 0.65f)
                                        else Cyan.copy(alpha = 0.25f),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Text(
                                    item.first,
                                    color = if (user) Color(0xFFFFC66D) else Cyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(item.second, color = Color.White, fontSize = 15.sp)
                                if (!user && speakReplies) {
                                    TextButton(onClick = { onSpeak(item.second) }) {
                                        Text("▶  SPEAK", color = Cyan)
                                    }
                                }
                            }
                        }
                        if (loading) {
                            item {
                                Text("JARVIS is thinking...", color = Cyan)
                            }
                        }
                    }
                }

                "Explore" -> {
                    Text("DISCOVER AI", color = Cyan, fontSize = 13.sp)
                    Spacer(Modifier.height(14.dp))
                    ToolCard("✦", "AI Assistant", "Ask questions in Hindi or English") {
                        tab = "Chat"
                    }
                    ToolCard("◉", "Voice Assistant", "Speak with JARVIS") {
                        listen()
                    }
                    ToolCard("⌘", "Smart Chat", "Continue your AI conversation") {
                        tab = "Chat"
                    }
                    Spacer(Modifier.weight(1f))
                }

                "Settings" -> {
                    Text("PREFERENCES & PRIVACY", color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    SettingsCard("Appearance", "Choose the JARVIS interface theme") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Midnight", "Ocean", "Violet").forEach { theme ->
                                FilterChip(
                                    selected = selectedTheme == theme,
                                    onClick = { selectedTheme = theme },
                                    label = { Text(theme) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Blue.copy(alpha = 0.25f),
                                        selectedLabelColor = Cyan
                                    )
                                )
                            }
                        }
                    }
                    SettingsToggle("Voice replies", "Allow JARVIS to speak replies when you tap Speak", speakReplies) { speakReplies = it }
                    SettingsToggle("Confirm sensitive actions", "Preference for future phone-control actions; no actions are auto-run in this build", confirmAccessibilityActions) { confirmAccessibilityActions = it }
                    SettingsCard("Permissions", "Microphone is requested only when you tap voice input. Accessibility is optional and must be enabled manually in Android Settings; phone-control actions are not wired into this UI yet.") {
                        Text("Review permissions in Android Settings", color = Cyan, fontSize = 12.sp)
                    }
                    SettingsCard("AI privacy", "This build no longer embeds a Gemini API key. Connect a secure backend before enabling cloud AI chat.") {
                        Text("API key protection: backend required", color = Color(0xFFFFC66D), fontSize = 12.sp)
                    }
                    SettingsCard("About JARVIS", "Version 1.1 • Personal AI Assistant") {
                        Text("Built for transparent, user-controlled assistance.", color = Muted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                }

                "Profile" -> {
                    Spacer(Modifier.height(30.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Panel)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Orb()
                            Spacer(Modifier.height(16.dp))
                            Text("JARVIS AI", color = Color.White, fontSize = 22.sp)
                            Text("Personal AI Assistant", color = Muted)
                            Spacer(Modifier.height(12.dp))
                            Text("Version 1.0", color = Cyan)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(5.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(25.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF0C2143), Color(0xFF050B18))
                                    )
                                )
                                .border(
                                    1.dp,
                                    Blue.copy(alpha = 0.30f),
                                    RoundedCornerShape(25.dp)
                                )
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Orb()
                                Spacer(Modifier.height(14.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(7.dp)
                                            .background(Color(0xFF37FF9B), CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "SYSTEM ONLINE",
                                        color = Cyan,
                                        fontSize = 12.sp,
                                        letterSpacing = 2.sp
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "How can I help you today?",
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DISCOVER AI TOOLS", color = Cyan, fontSize = 12.sp)
                            Spacer(Modifier.weight(1f))
                            Text("VIEW ALL  ›", color = Muted, fontSize = 10.sp,
                                modifier = Modifier.clickable { tab = "Explore" })
                        }
                        Spacer(Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            MiniTool("✦", "AI Chat", Modifier.weight(1f)) { tab = "Chat" }
                            MiniTool("◉", "Voice AI", Modifier.weight(1f)) { listen() }
                            MiniTool("⌘", "Explore", Modifier.weight(1f)) { tab = "Explore" }
                        }

                        Spacer(Modifier.height(16.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF0A1428))
                                .border(
                                    1.dp,
                                    Cyan.copy(alpha = 0.20f),
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { tab = "Chat" }
                                .padding(15.dp)
                        ) {
                            Column {
                                Text("YOUR AI ASSISTANT", color = Cyan, fontSize = 11.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Chat, ask questions, or use your voice to get started.",
                                    color = Muted,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(10.dp))
                                Text("Tap here to open conversation  →", color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0D1930))
                    .border(1.dp, Blue.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(39.dp)
                        .background(Brush.linearGradient(listOf(Blue, Purple)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎙", fontSize = 17.sp)
                }
                Spacer(Modifier.width(9.dp))
                BasicTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Cyan),
                    decorationBox = { inner ->
                        Box {
                            if (prompt.isEmpty()) {
                                Text("Ask JARVIS anything...", color = Muted, fontSize = 13.sp)
                            }
                            inner()
                        }
                    }
                )
                TextButton(
                    onClick = { listen() },
                    contentPadding = PaddingValues(horizontal = 7.dp)
                ) {
                    Text("MIC", color = Cyan, fontSize = 12.sp)
                }
                Button(
                    onClick = { send() },
                    enabled = !loading,
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("➤", fontSize = 17.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(9.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xF20C1930), Color(0xF2152342))))
                    .border(1.dp, Cyan.copy(alpha = 0.24f), RoundedCornerShape(26.dp))
                    .padding(horizontal = 2.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem("⌂", "Home", tab == "Home") { tab = "Home" }
                NavItem("◌", "Chat", tab == "Chat") { tab = "Chat" }
                NavItem("✧", "Explore", tab == "Explore") { tab = "Explore" }
                NavItem("◎", "Profile", tab == "Profile") { tab = "Profile" }
                NavItem("⚙", "Settings", tab == "Settings") { tab = "Settings" }
            }
        }
    }
}


@Composable
private fun NexSplashScreen() {
    val transition = rememberInfiniteTransition(label = "nexSplash")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "splashPulse"
    )
    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF101F3C), Color(0xFF050B18), Color(0xFF030711))))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(124.dp).scale(pulse)
                    .background(Brush.linearGradient(listOf(Cyan, Blue, Purple)), CircleShape)
                    .padding(3.dp).background(Background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("N", color = Color.White, fontSize = 66.sp, fontWeight = FontWeight.Light)
            }
            Spacer(Modifier.height(25.dp))
            Text("NEX AI", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Spacer(Modifier.height(7.dp))
            Text("YOUR PERSONAL AI COMPANION", color = Cyan, fontSize = 10.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Cyan,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.height(10.dp))
            Text("INITIALIZING SYSTEM", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun SettingsCard(title: String, description: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp)).background(Panel)
            .border(1.dp, Blue.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text(description, color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SettingsToggle(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(16.dp))
            .background(Panel).border(1.dp, Blue.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = Muted, fontSize = 11.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun Orb() {
    val infinite = rememberInfiniteTransition(label = "orb")
    val pulse by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        Modifier
            .size(132.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(128.dp)
                .border(1.dp, Cyan.copy(alpha = 0.25f), CircleShape)
        )
        Box(
            Modifier
                .size(108.dp)
                .background(
                    Brush.linearGradient(listOf(Cyan, Blue, Purple)),
                    CircleShape
                )
                .padding(5.dp)
                .background(Color(0xFF08142C), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "N",
                color = Color.White,
                fontSize = 52.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun MiniTool(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, Blue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, color = Cyan, fontSize = 25.sp)
        Spacer(Modifier.height(7.dp))
        Text(label, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun ToolCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Panel)
            .border(1.dp, Blue.copy(alpha = 0.3f), RoundedCornerShape(17.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, color = Cyan, fontSize = 28.sp)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(description, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun NavItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Cyan.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                if (selected) 1.dp else 0.dp,
                if (selected) Cyan.copy(alpha = 0.28f) else Color.Transparent,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 3.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            icon,
            color = if (selected) Cyan else Muted,
            fontSize = 21.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            label,
            color = if (selected) Color.White else Muted,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

private fun askJarvis(question: String): String {
    // Intentionally no API key is embedded in the APK. Configure a trusted backend
    // endpoint here before enabling cloud requests in a public release.
    return "JARVIS is ready, but secure AI connection is not configured yet. For your safety, this build does not store a Gemini API key inside the APK. Connect a trusted backend to enable AI responses."

    /*
    var connection: HttpURLConnection? = null

    return try {
        val url = URL("https://YOUR-SECURE-BACKEND.example/api/chat")
        connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        val body = JSONObject()
            .put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put(
                            "text",
                            "You are JARVIS, a helpful personal AI assistant. Reply in the language the user uses. Be clear, friendly and concise."
                        )
                    )
                )
            )
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", question)))
                )
            )

        connection.outputStream.use {
            it.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader().use { it.readText() }

        if (code !in 200..299) {
            "AI server error ($code). API key ya quota check karo."
        } else {
            JSONObject(response)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        }
    } catch (e: Exception) {
        "Connection error: ${e.localizedMessage ?: "Please try again."}"
    } finally {
        connection?.disconnect()
    }
    */
}

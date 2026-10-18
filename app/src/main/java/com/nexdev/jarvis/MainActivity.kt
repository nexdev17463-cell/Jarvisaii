package com.nexdev.jarvis

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

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
            JarvisScreen(
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
private fun JarvisScreen(onSpeak: (String) -> Unit) {
    val bg = Color(0xFF070B18)
    val cyan = Color(0xFF53E7FF)
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val messages = remember {
        mutableStateListOf(
            "JARVIS" to "Hello! Main JARVIS hoon. Mujhse Hindi ya English mein baat karo."
        )
    }
    val scope = rememberCoroutineScope()

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) input = spoken
        }
    }

    fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS se baat karein")
            }
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            messages.add("JARVIS" to "Speech recognition is not available on this device.")
        }
    }

    fun sendMessage() {
        val question = input.trim()
        if (question.isEmpty() || busy) return

        messages.add("YOU" to question)
        input = ""
        busy = true

        scope.launch {
            val answer = withContext(Dispatchers.IO) {
                askJarvis(question, BuildConfig.GEMINI_API_KEY)
            }
            messages.add("JARVIS" to answer)
            busy = false
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF101B35), bg, Color(0xFF080817))
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("NEXDEV SYSTEMS", color = cyan, fontSize = 11.sp)
                    Text(
                        "J.A.R.V.I.S",
                        color = Color.White,
                        fontSize = 27.sp
                    )
                    Text("PERSONAL AI ASSISTANT", color = Color.LightGray, fontSize = 10.sp)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(cyan.copy(alpha = 0.13f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AI", color = cyan, fontSize = 19.sp)
                }
            }

            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF155C86), Color(0xFF10152C), bg)
                        ),
                        RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(cyan, Color(0xFF765BFF))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFF10152C), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("J", color = cyan, fontSize = 34.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (busy) "PROCESSING..." else "SYSTEM ONLINE",
                        color = cyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("CONVERSATION", color = cyan, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { item ->
                    val isUser = item.first == "YOU"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isUser) Color(0xFF17334B) else Color(0xFF141A2D),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            item.first,
                            color = if (isUser) Color(0xFFFFC66D) else cyan,
                            fontSize = 10.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(item.second, color = Color.White, fontSize = 14.sp)
                        if (!isUser) {
                            TextButton(onClick = { onSpeak(item.second) }) {
                                Text("▶ Speak", color = cyan)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF151D32), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(cyan),
                    decorationBox = { inner ->
                        Box {
                            if (input.isEmpty()) {
                                Text("Ask JARVIS anything...", color = Color.Gray)
                            }
                            inner()
                        }
                    }
                )
                TextButton(onClick = { startListening() }) {
                    Text("MIC", color = cyan)
                }
                Button(
                    onClick = { sendMessage() },
                    enabled = !busy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF176D8D)
                    )
                ) {
                    Text("SEND")
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "VOICE INPUT • AI CHAT • VOICE OUTPUT",
                color = Color.Gray,
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private fun askJarvis(question: String, apiKey: String): String {
    if (apiKey.isBlank()) {
        return "API key abhi set nahi hai. Google AI Studio se Gemini API key lekar GitHub Secrets mein GEMINI_API_KEY naam se add karni hogi."
    }

    var connection: HttpURLConnection? = null

    return try {
        val url = URL(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        )
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
                            "You are JARVIS, a helpful personal AI assistant. Reply naturally in the language the user uses. Be clear and concise."
                        )
                    )
                )
            )
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", question))
                        )
                )
            )

        connection.outputStream.use {
            it.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val response = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) {
            "AI server error ($code). API key ya quota check karo."
        } else {
            val json = JSONObject(response)
            json.getJSONArray("candidates")
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
}

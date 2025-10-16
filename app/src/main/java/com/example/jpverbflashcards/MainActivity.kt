package com.example.jpverbflashcards

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
// import java.io.*
import kotlin.random.Random
import android.util.Log


// ---------------------------------------------------------
//  Data Models
// ---------------------------------------------------------
data class VerbCard(
    val dictionary: String,
    val hiragana: String,
    val meaningEn: String,
    val masu: String,
    val masuHiragana: String,
    val mashita: String,
    val mashitaHiragana: String,
    val masen: String,
    val masenHiragana: String
)

@Serializable
data class VerbStats(
    var rightCount: Int = 0,
    var wrongCount: Int = 0
)

typealias VerbStatsTable = MutableMap<String, VerbStats>

// ---------------------------------------------------------
//  JSON load/save for stats
// ---------------------------------------------------------
fun loadVerbStats(context: Context): VerbStatsTable {
    val file = File(context.filesDir, "verb_stats.json")
    if (!file.exists()) return mutableMapOf()
    return try {
        val text = file.readText()
        val decoded = Json.decodeFromString<Map<String, VerbStats>>(text)
        decoded.toMutableMap()
    } catch (e: Exception) {
        e.printStackTrace()
        mutableMapOf()
    }
}

fun saveVerbStats(context: Context, table: VerbStatsTable) {
    val text = Json.encodeToString(table)
    File(context.filesDir, "verb_stats.json").writeText(text)
}

// ---------------------------------------------------------
//  Weighted random draw
// ---------------------------------------------------------
fun weightedRandomIndex(deck: List<VerbCard>, table: VerbStatsTable): Int {
    val weights = deck.map { card ->
        val s = table[card.dictionary]
        val right = s?.rightCount ?: 0
        val wrong = s?.wrongCount ?: 0
        val total = right + wrong
        // Higher weight for cards you miss often
        1f + 3f * (wrong.toFloat() / (total + 1))
    }

    val totalWeight = weights.sum()
    // val probabilities = weights.map { it / totalWeight }
    // val sumP = probabilities.sum()    
    
    // // Print a readable log entry for analysis
    // val logSummary = deck.mapIndexed { i, card ->
    // 	"${card.dictionary}: w=${"%.2f".format(weights[i])}, p=${"%.2f".format(probabilities[i])}"
    // }.joinToString(" | ")
    
    // Log.d("Weights", logSummary)
    // Log.d("Weights", "Σp = ${"%.3f".format(sumP)} (should be ~1.000)")
    var r = Random.nextFloat() * totalWeight
    for ((i, w) in weights.withIndex()) {
        r -= w
        if (r <= 0f) return i
    }
    return deck.lastIndex // fallback
}

// ---------------------------------------------------------
//  Load CSV deck
// ---------------------------------------------------------
fun loadVerbDeck(context: Context): List<VerbCard> {
    val verbs = mutableListOf<VerbCard>()
    val inputStream = context.resources.openRawResource(R.raw.verbs)
    val reader = BufferedReader(InputStreamReader(inputStream))
    reader.readLine() // skip header
    reader.forEachLine { line ->
        val parts = line.split(",")
        if (parts.size >= 9) {
            verbs.add(
                VerbCard(
                    dictionary = parts[0].trim(),
                    hiragana = parts[1].trim(),
                    meaningEn = parts[2].trim(),
                    masu = parts[3].trim(),
                    masuHiragana = parts[4].trim(),
                    mashita = parts[5].trim(),
                    mashitaHiragana = parts[6].trim(),
                    masen = parts[7].trim(),
                    masenHiragana = parts[8].trim()
                )
            )
        }
    }
    reader.close()
    return verbs
}

// ---------------------------------------------------------
//  Main Activity
// ---------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deck = loadVerbDeck(this)
        setContent { App(deck) }
    }
}

// ---------------------------------------------------------
//  UI Composables
// ---------------------------------------------------------
@Composable
fun App(deck: List<VerbCard>) {
    val context = LocalContext.current
    val statsTable = remember { loadVerbStats(context) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            FlashcardScreen(deck, statsTable)
        }
    }
}

@Composable
fun FlashcardScreen(deck: List<VerbCard>, statsTable: VerbStatsTable) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(Random.nextInt(deck.size)) }
    var isFlipped by remember { mutableStateOf(false) }
    var globalSeen by remember { mutableStateOf(0) }

    fun recordSwipe(correct: Boolean) {
        val card = deck[currentIndex]
        val entry = statsTable.getOrPut(card.dictionary) { VerbStats() }
        if (correct) entry.rightCount++ else entry.wrongCount++
        globalSeen++
        saveVerbStats(context, statsTable)
    }

    fun nextWeightedCard(correct: Boolean) {
        recordSwipe(correct)
        currentIndex = weightedRandomIndex(deck, statsTable)
        isFlipped = false
    }

    val card = deck[currentIndex]
    val swipeThresholdPx = with(LocalDensity.current) { 64.dp.toPx() }
    var dragX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E11))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount
                    },
		    onDragEnd = {
			// Log.d("SwipeTest", "dragX = $dragX");
			when {
			    dragX > swipeThresholdPx -> nextWeightedCard(true)    // 👉 swipe right = correct
							dragX < -swipeThresholdPx -> nextWeightedCard(false)  // 👈 swipe left  = incorrect
			}
			
			dragX = 0f
		    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Flashcard(card = card, isFlipped = isFlipped, onTap = { isFlipped = !isFlipped })

        // Bottom overlay with info
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val s = statsTable[card.dictionary]
            val right = s?.rightCount ?: 0
            val wrong = s?.wrongCount ?: 0
            val total = right + wrong
            // Text(
            //     text = "✅$right ❌$wrong  •  Seen $total×  •  Global $globalSeen",
            //     color = Color(0xFF9AA0A6),
            //     fontSize = 14.sp
            // )
            Text(
                text = "Tap = flip • Swipe 👉 = correct • Swipe 👈 = incorrect",
                color = Color(0xFF607D8B),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun Flashcard(card: VerbCard, isFlipped: Boolean, onTap: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "flip"
    )
    val cameraDistance = with(LocalDensity.current) { 16.dp.toPx() }

    Box(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .aspectRatio(0.66f)
            .clip(RoundedCornerShape(24.dp))
            .graphicsLayer {
                rotationY = rotation
                this.cameraDistance = cameraDistance
            }
            .background(Color(0xFF1C1F24))
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            SelectionContainer { FrontFace(card) }
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                SelectionContainer { BackFace(card) }
            }
        }
    }
}

@Composable
fun FrontFace(card: VerbCard) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = card.hiragana,
            color = Color(0xFFB0BEC5),
            fontSize = 22.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        AutoResizeText(
            text = card.dictionary,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxFontSize = 56.sp,
            minFontSize = 28.sp,
            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun BackFace(card: VerbCard) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF21262C))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = card.meaningEn,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(24.dp))
        EntryFurigana("ます形", card.masu, card.masuHiragana)
        EntryFurigana("ました形", card.mashita, card.mashitaHiragana)
        EntryFurigana("ません形", card.masen, card.masenHiragana)
    }
}

@Composable
fun AutoResizeText(
    text: String,
    color: Color,
    fontWeight: FontWeight,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    var textSize by remember { mutableStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }
    Text(
        text = text,
        color = color,
        fontSize = textSize,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        onTextLayout = { result ->
            if (result.didOverflowWidth && textSize > minFontSize) textSize *= 0.9f
            else readyToDraw = true
        }
    )
}

@Composable
fun EntryFurigana(label: String, kanji: String, hiragana: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF9AA0A6), fontSize = 18.sp)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = hiragana,
                color = Color(0xFFB0BEC5),
                fontSize = 16.sp,
                lineHeight = 16.sp
            )
            Text(
                text = kanji,
                color = Color(0xFFECEFF1),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

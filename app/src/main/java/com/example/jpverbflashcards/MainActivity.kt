package com.example.jpverbflashcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import kotlin.random.Random
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.util.Log


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
	val deck = loadVerbDeck(this)
	setContent { App(deck) }
        // setContent { App() }
    }
}

// -------------------- Data Model --------------------
data class VerbCard(
    val dictionary: String,   // Kanji dictionary form
    val hiragana: String,     // Hiragana for dictionary form
    val meaningEn: String,    // English meaning
    val masu: String,         // Kanji ます form
    val masuHiragana: String, // Hiragana ます form
    val mashita: String,      // Kanji ました form
    val mashitaHiragana: String, // Hiragana ました form
    val masen: String,        // Kanji ません form
    val masenHiragana: String, // Hiragana ません form
    var rightCount: Int = 0,
    var wrongCount: Int = 0,
    var totalSeen: Int = 0
)


fun saveProgress(context: Context, cards: List<VerbCard>) {
    val prefs = context.getSharedPreferences("verb_progress", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    cards.forEachIndexed { i, card ->
        editor.putInt("right_$i", card.rightCount)
        editor.putInt("wrong_$i", card.wrongCount)
        editor.putInt("seen_$i", card.totalSeen)
    }
    editor.apply()
}

fun loadProgress(context: Context, cards: MutableList<VerbCard>) {
    val prefs = context.getSharedPreferences("verb_progress", Context.MODE_PRIVATE)
    cards.forEachIndexed { i, card ->
        card.rightCount = prefs.getInt("right_$i", 0)
        card.wrongCount = prefs.getInt("wrong_$i", 0)
        card.totalSeen = prefs.getInt("seen_$i", 0)
    }
}


fun loadVerbDeck(context: Context): List<VerbCard> {
    val verbs = mutableListOf<VerbCard>()
    val inputStream = context.resources.openRawResource(R.raw.verbs)
    val reader = BufferedReader(InputStreamReader(inputStream))

    // Skip header
    reader.readLine()

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

// -------------------- UI --------------------
@Composable
fun FuriganaText(
    kanji: String,
    hiragana: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = hiragana,
            color = Color(0xFFB0BEC5),
            fontSize = 22.sp,
            lineHeight = 22.sp
        )
        Text(
            text = kanji,
            color = Color.White,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 64.sp
        )
    }
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
        Text(
            label,
            color = Color(0xFF9AA0A6),
            fontSize = 18.sp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = hiragana,
                color = Color(0xFFB0BEC5),
                fontSize = 16.sp, // slightly smaller
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


@Composable
fun App(deck: List<VerbCard>) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            FlashcardScreen(deck = deck)
        }
    }
}

// -------------------- Flashcard Screen with Swipe Tracking --------------------

@Composable
fun FlashcardScreen(deck: List<VerbCard>) {
    val context = LocalContext.current

    // 🧠 Persist deck state across recompositions & configuration changes
    val cards = rememberSaveable(
        saver = listSaver(
            save = { list ->
                list.map {
                    "${it.dictionary}|${it.rightCount}|${it.wrongCount}|${it.totalSeen}"
                }
            },
            restore = { saved ->
                deck.map { original ->
                    val entry = saved.find { it.startsWith(original.dictionary) }
                    val parts = entry?.split("|") ?: listOf(original.dictionary, "0", "0", "0")
                    original.copy(
                        rightCount = parts[1].toInt(),
                        wrongCount = parts[2].toInt(),
                        totalSeen = parts[3].toInt()
                    )
                }.toMutableStateList()
            }
        )
    ) {
        deck.map { it.copy() }.toMutableStateList()
    }

    // 🧾 Load persisted progress only once (if any)
    LaunchedEffect(Unit) { loadProgress(context, cards) }

    var currentIndex by remember { mutableStateOf(weightedRandomIndex(cards)) }
    var isFlipped by remember { mutableStateOf(false) }

    val swipeThresholdPx = with(LocalDensity.current) { 64.dp.toPx() }
    var dragX by remember { mutableStateOf(0f) }

    fun nextCard(correct: Boolean) {
        val card = cards[currentIndex]
        card.totalSeen++
        if (correct) card.rightCount++ else card.wrongCount++
        saveProgress(context, cards)

        Log.d("Flashcards", "Card: ${card.dictionary}, right=${card.rightCount}, wrong=${card.wrongCount}, total=${card.totalSeen}")
        Log.d("Flashcards", "Object hash: ${card.hashCode()}")

        currentIndex = weightedRandomIndex(cards)
        isFlipped = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E11))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount.x
                    },
                    onDragEnd = {
                        when {
                            dragX > swipeThresholdPx -> nextCard(true)
                            dragX < -swipeThresholdPx -> nextCard(false)
                        }
                        dragX = 0f
                    },
                    onDragCancel = { dragX = 0f }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Flashcard(
            card = cards[currentIndex],
            isFlipped = isFlipped,
            onTap = { isFlipped = !isFlipped }
        )

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tap = flip • Swipe → = correct • Swipe ← = incorrect",
                color = Color(0xFF9AA0A6),
                fontSize = 14.sp
            )
            Text(
                text = "✅${cards[currentIndex].rightCount} ❌${cards[currentIndex].wrongCount} • Seen ${cards[currentIndex].totalSeen}",
                color = Color(0xFF607D8B),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun Flashcard(
    card: VerbCard,
    isFlipped: Boolean,
    onTap: () -> Unit
) {
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
            .clickable(onClick = onTap), // ✅ replaces detectTapGestures
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            FrontFace(card)
        } else {
            Box(
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f
                }
            ) {
                BackFace(card)
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
        // Hiragana ABOVE the kanji
        Text(
            text = card.hiragana,
            color = Color(0xFFB0BEC5),
            fontSize = 22.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Kanji (dictionary form) centered, auto-resizing
        AutoResizeText(
            text = card.dictionary,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxFontSize = 56.sp,
            minFontSize = 28.sp,
            modifier = Modifier
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
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
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { result ->
            if (result.didOverflowWidth && textSize > minFontSize) {
                textSize *= 0.9f
            } else {
                readyToDraw = true
            }
        }
    )
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
private fun Entry(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF9AA0A6), fontSize = 18.sp)
        Text(value, color = Color(0xFFECEFF1), fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

fun weightedRandomIndex(cards: List<VerbCard>): Int {
    val weights = cards.map { card ->
        1f + 3f * (card.wrongCount.toFloat() / maxOf(1, card.totalSeen))
    }
    val totalWeight = weights.sum()
    var r = Random.nextFloat() * totalWeight
    for ((i, w) in weights.withIndex()) {
        r -= w
        if (r <= 0f) return i
    }
    return cards.lastIndex
}

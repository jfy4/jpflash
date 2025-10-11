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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

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
    val masenHiragana: String // Hiragana ません form
)
// data class VerbCard(
//     val dictionary: String,   // Kanji
//     val hiragana: String,     // Hiragana reading
//     val meaningEn: String,    // English translation
//     val masu: String,
//     val mashita: String,
//     val masen: String
// )

// val SampleDeck = listOf(
//     VerbCard("行く", "いく", "to go", "行きます", "行きました", "行きません"),
//     VerbCard("来る", "くる", "to come", "来ます", "来ました", "来ません"),
//     VerbCard("する", "する", "to do", "します", "しました", "しません"),
//     VerbCard("食べる", "たべる", "to eat", "食べます", "食べました", "食べません"),
//     VerbCard("見る", "みる", "to see / watch", "見ます", "見ました", "見ません"),
//     VerbCard("読む", "よむ", "to read", "読みます", "読みました", "読みません"),
//     VerbCard("書く", "かく", "to write", "書きます", "書きました", "書きません"),
//     VerbCard("話す", "はなす", "to speak", "話します", "話しました", "話しません"),
//     VerbCard("聞く", "きく", "to listen / ask", "聞きます", "聞きました", "聞きません"),
//     VerbCard("飲む", "のむ", "to drink", "飲みます", "飲みました", "飲みません"),
// )

// val SampleDeck = listOf(
//     VerbCard("行く", "行き", "to go", "行きます", "行って", "行った", "行かない"),
//     VerbCard("来る", "来（き）", "to come", "来ます", "来て", "来た", "来ない"),
//     VerbCard("する", "し", "to do", "します", "して", "した", "しない"),
//     VerbCard("食べる", "食べ", "to eat", "食べます", "食べて", "食べた", "食べない"),
//     VerbCard("見る", "見", "to see / watch", "見ます", "見て", "見た", "見ない"),
//     VerbCard("読む", "読み", "to read", "読みます", "読んで", "読んだ", "読まない"),
//     VerbCard("書く", "書き", "to write", "書きます", "書いて", "書いた", "書かない"),
//     VerbCard("話す", "話し", "to speak", "話します", "話して", "話した", "話さない"),
//     VerbCard("聞く", "聞き", "to listen / ask", "聞きます", "聞いて", "聞いた", "聞かない"),
//     VerbCard("飲む", "飲み", "to drink", "飲みます", "飲んで", "飲んだ", "飲まない"),
//     VerbCard("買う", "買い", "to buy", "買います", "買って", "買った", "買わない"),
//     VerbCard("会う", "会い", "to meet", "会います", "会って", "会った", "会わない"),
//     VerbCard("歩く", "歩き", "to walk", "歩きます", "歩いて", "歩いた", "歩かない"),
//     VerbCard("走る", "走り", "to run", "走ります", "走って", "走った", "走らない"),
//     VerbCard("寝る", "寝", "to sleep", "寝ます", "寝て", "寝た", "寝ない"),
//     VerbCard("起きる", "起き", "to wake up", "起きます", "起きて", "起きた", "起きない"),
//     VerbCard("入る", "入り", "to enter", "入ります", "入って", "入った", "入らない"),
//     VerbCard("出る", "出", "to leave / go out", "出ます", "出て", "出た", "出ない"),
//     VerbCard("使う", "使い", "to use", "使います", "使って", "使った", "使わない"),
// )

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

// fun loadVerbDeck(context: Context): List<VerbCard> {
//     val verbs = mutableListOf<VerbCard>()
//     val inputStream = context.resources.openRawResource(R.raw.verbs)
//     val reader = BufferedReader(InputStreamReader(inputStream))

//     // Skip the header line
//     reader.readLine()

//     reader.forEachLine { line ->
//         val parts = line.split(",")
//         if (parts.size >= 6) {
//             verbs.add(
//                 VerbCard(
//                     dictionary = parts[0].trim(),
//                     hiragana = parts[1].trim(),
//                     meaningEn = parts[2].trim(),
//                     masu = parts[3].trim(),
//                     mashita = parts[4].trim(),
//                     masen = parts[5].trim()
//                 )
//             )
//         }
//     }

//     reader.close()
//     return verbs
// }


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

// @Composable
// fun App() {
//     MaterialTheme(colorScheme = MaterialTheme.colorScheme) {
//         Surface(modifier = Modifier.fillMaxSize()) {
//             FlashcardScreen(deck = SampleDeck)
//         }
//     }
// }

@Composable
fun FlashcardScreen(deck: List<VerbCard>) {
    var currentIndex by remember { mutableStateOf(Random.nextInt(deck.size)) }
    var previousIndex by remember { mutableStateOf<Int?>(null) }
    var isFlipped by remember { mutableStateOf(false) }

    fun nextRandomCard() {
        previousIndex = currentIndex
        currentIndex = Random.nextInt(deck.size)
        isFlipped = false
    }

    fun goPreviousCard() {
        previousIndex?.let { prev ->
            val tmp = currentIndex
            currentIndex = prev
            previousIndex = tmp
            isFlipped = false
        }
    }

    val card = deck[currentIndex]
    val swipeThresholdPx = with(LocalDensity.current) { 64.dp.toPx() }
    var dragX by remember { mutableStateOf(0f) }

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
                            dragX > swipeThresholdPx -> nextRandomCard()
                            dragX < -swipeThresholdPx -> goPreviousCard()
                        }
                        dragX = 0f
                    },
                    onDragCancel = { dragX = 0f }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Flashcard(
            card = card,
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
                text = "Tap = flip • Swipe → = next • Swipe ← = previous",
                color = Color(0xFF9AA0A6),
                fontSize = 14.sp
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
            .background(Color(0xFF28303A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FuriganaText(kanji = card.dictionary, hiragana = card.hiragana)
    }
}

// @Composable
// fun FrontFace(card: VerbCard) {
//     Column(
//         Modifier
//             .fillMaxSize()
//             .background(Color(0xFF28303A))
//             .padding(24.dp),
//         horizontalAlignment = Alignment.CenterHorizontally,
//         verticalArrangement = Arrangement.Center
//     ) {
//         Text("ます-stem", color = Color(0xFF9AA0A6), fontSize = 18.sp)
//         Spacer(Modifier.height(8.dp))
//         Text(
//             text = card.stem,
//             color = Color.White,
//             fontSize = 64.sp,
//             lineHeight = 64.sp,
//             textAlign = TextAlign.Center,
//             fontWeight = FontWeight.Bold
//         )
//         Spacer(Modifier.height(16.dp))
//         Text(
//             text = "（${card.dictionary}）",
//             color = Color(0xFFB0BEC5),
//             fontSize = 22.sp
//         )
//     }
// }

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

// @Composable
// fun BackFace(card: VerbCard) {
//     Column(
//         modifier = Modifier
//             .fillMaxSize()
//             .background(Color(0xFF21262C))
//             .padding(24.dp),
//         verticalArrangement = Arrangement.Center,
//         horizontalAlignment = Alignment.Start
//     ) {
//         Text(
//             text = card.meaningEn,
//             color = Color.White,
//             fontSize = 28.sp,
//             fontWeight = FontWeight.SemiBold
//         )
//         Spacer(Modifier.height(20.dp))
//         Entry("ます形", card.masu)
//         Entry("ました形", card.mashita)
//         Entry("ません形", card.masen)
//     }
// }

// @Composable
// fun BackFace(card: VerbCard) {
//     Column(
//         Modifier
//             .fillMaxSize()
//             .background(Color(0xFF21262C))
//             .padding(24.dp),
//         verticalArrangement = Arrangement.Center
//     ) {
//         Text(
//             text = card.meaningEn,
//             color = Color.White,
//             fontSize = 26.sp,
//             fontWeight = FontWeight.SemiBold
//         )
//         Spacer(Modifier.height(16.dp))
//         Entry("辞書形", card.dictionary)
//         Entry("ます形", card.masu)
//         Entry("て形", card.te)
//         Entry("た形", card.ta)
//         Entry("ない形", card.nai)
//     }
// }

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

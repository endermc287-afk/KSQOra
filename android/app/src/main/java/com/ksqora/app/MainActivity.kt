package com.ksqora.app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private val Ink = Color(0xFF100D19)
private val Panel = Color(0xFF211A31)
private val Purple = Color(0xFF9B6DFF)
private val Yellow = Color(0xFFFFD34E)

private val Subjects = listOf(
    "Azərbaycan dili", "Riyaziyyat", "Ədəbiyyat", "Fizika",
    "Texnologiya", "Kimya", "İnformatika", "Biologiya",
    "İngilis dili", "Rus dili"
)

data class Question(val text: String, val options: List<String>, val answer: Int)
data class Exam(val id: String, val title: String, val subject: String, val questions: List<Question>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KSQoraApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KSQoraApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("ksqora", 0) }

    val exams = remember {
        mutableStateListOf<Exam>().apply {
            addAll(loadExams(prefs.getString("exams", "[]") ?: "[]"))
        }
    }

    var tab by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(Subjects.first()) }
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "", "", "") }
    var answer by remember { mutableIntStateOf(0) }
    val draftQuestions = remember { mutableStateListOf<Question>() }
    var importCode by remember { mutableStateOf("") }
    var activeExam by remember { mutableStateOf<Exam?>(null) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    var resultText by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }

    fun persist() {
        prefs.edit().putString("exams", saveExams(exams)).apply()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            secondary = Yellow,
            background = Ink,
            surface = Panel,
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Ink,
            topBar = {
                Column(
                    Modifier.fillMaxWidth().background(Ink).padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "KSQ",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "ora",
                            color = Purple,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.weight(1f))
                        Surface(
                            color = Color(0xFF3B2D12),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                "🔨  Yaradıcı",
                                color = Yellow,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 7.dp
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        "Azərbaycan məktəbləri üçün KSQ platforması",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton("Müəllim", tab == 0) { tab = 0 }
                    TabButton("Şagird", tab == 1) { tab = 1 }
                    TabButton("KSQ-larım", tab == 2) { tab = 2 }
                }

                Column(
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (notice.isNotBlank()) {
                        Text(notice, color = Yellow)
                    }

                    when (tab) {
                        0 -> {
                            Text(
                                "Yeni KSQ yarat",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                title,
                                { title = it },
                                label = { Text("KSQ adı") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    subject,
                                    {},
                                    readOnly = true,
                                    label = { Text("Fənn") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    Subjects.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                subject = item
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.DarkGray)
                            Text("Sual əlavə et", fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                question,
                                { question = it },
                                label = { Text("Sualın mətni") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            options.forEachIndexed { index, value ->
                                OutlinedTextField(
                                    value,
                                    { options[index] = it },
                                    label = { Text("Variant ${'A' + index}") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            Text("Düzgün cavab", color = Color.LightGray)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                options.indices.forEach { index ->
                                    FilterChip(
                                        selected = answer == index,
                                        onClick = { answer = index },
                                        label = { Text("${'A' + index}") }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (question.isBlank() || options.any { it.isBlank() }) {
                                        notice = "Sualı və 4 variantı doldur."
                                    } else {
                                        draftQuestions.add(
                                            Question(
                                                question.trim(),
                                                options.map { it.trim() },
                                                answer
                                            )
                                        )
                                        question = ""
                                        options.indices.forEach { options[it] = "" }
                                        answer = 0
                                        notice = "Sual əlavə edildi. Cəmi: ${draftQuestions.size}"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sualı əlavə et")
                            }

                            if (draftQuestions.isNotEmpty()) {
                                Text("Əlavə edilmiş suallar: ${draftQuestions.size}")
                                draftQuestions.forEachIndexed { i, q ->
                                    Text("${i + 1}. ${q.text}", color = Color.LightGray)
                                }
                            }

                            Button(
                                onClick = {
                                    if (title.isBlank() || draftQuestions.isEmpty()) {
                                        notice = "KSQ adı yaz və ən azı bir sual əlavə et."
                                    } else {
                                        val exam = Exam(
                                            System.currentTimeMillis().toString(),
                                            title.trim(),
                                            subject,
                                            draftQuestions.toList()
                                        )

                                        exams.add(0, exam)
                                        persist()

                                        val code = encodeExam(exam)
                                        val send = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "KSQora imtahan kodu:\n$code"
                                            )
                                        }

                                        context.startActivity(
                                            Intent.createChooser(send, "KSQ paylaş")
                                        )

                                        title = ""
                                        draftQuestions.clear()
                                        notice = "KSQ yadda saxlanıldı. Paylaşım menyusu açıldı."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Purple
                                )
                            ) {
                                Icon(Icons.Default.Share, null)
                                Spacer(Modifier.width(8.dp))
                                Text("KSQ yarat və paylaş")
                            }

                            Text(
                                "Qeyd: paylaşım kodla işləyir. Bu versiyada onlayn hesab və mərkəzi nəticə bazası yoxdur.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        1 -> {
                            Text(
                                "KSQ-ya qoşul",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Müəllimin göndərdiyi KSQora kodunu bura yapışdır.",
                                color = Color.LightGray
                            )

                            OutlinedTextField(
                                importCode,
                                { importCode = it },
                                label = { Text("KSQ kodu") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4
                            )

                            Button(
                                onClick = {
                                    val imported = decodeExam(importCode)
                                    if (imported == null) {
                                        notice = "Kod yanlışdır və ya tam köçürülməyib."
                                    } else {
                                        activeExam = imported
                                        selectedAnswers.clear()
                                        resultText = ""
                                        notice = "KSQ açıldı: ${imported.title}"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("KSQ-nı aç")
                            }

                            activeExam?.let { exam ->
                                HorizontalDivider(color = Color.DarkGray)

                                Text(
                                    exam.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(exam.subject, color = Purple)

                                exam.questions.forEachIndexed { index, q ->
                                    Text(
                                        "${index + 1}. ${q.text}",
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    q.options.forEachIndexed { optionIndex, option ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedAnswers[index] == optionIndex,
                                                onClick = {
                                                    selectedAnswers[index] = optionIndex
                                                }
                                            )
                                            Text("${'A' + optionIndex}. $option")
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val score = exam.questions.indices.count {
                                            selectedAnswers[it] == exam.questions[it].answer
                                        }
                                        resultText =
                                            "Nəticə: $score / ${exam.questions.size} (${score * 100 / exam.questions.size}%)"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cavabları yoxla")
                                }

                                if (resultText.isNotBlank()) {
                                    Text(
                                        resultText,
                                        color = Yellow,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        else -> {
                            Text(
                                "Yadda saxlanılan KSQ-lar",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            if (exams.isEmpty()) {
                                Text("Hələ KSQ yaratmamısan.", color = Color.LightGray)
                            }

                            exams.forEach { exam ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Panel
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Text(
                                            exam.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            "${exam.subject} • ${exam.questions.size} sual",
                                            color = Color.LightGray
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    val code = encodeExam(exam)
                                                    val send = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, code)
                                                    }
                                                    context.startActivity(
                                                        Intent.createChooser(send, "KSQ paylaş")
                                                    )
                                                }
                                            ) {
                                                Icon(Icons.Default.Share, null)
                                                Text(" Paylaş")
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    exams.remove(exam)
                                                    persist()
                                                    notice = "KSQ silindi."
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, null)
                                                Text(" Sil")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Purple else Panel,
            contentColor = Color.White
        )
    ) {
        Text(label)
    }
}

private fun encodeExam(exam: Exam): String {
    val root = JSONObject()
        .put("id", exam.id)
        .put("title", exam.title)
        .put("subject", exam.subject)

    val questions = JSONArray()

    exam.questions.forEach { q ->
        val opts = JSONArray()
        q.options.forEach { opts.put(it) }

        questions.put(
            JSONObject()
                .put("text", q.text)
                .put("options", opts)
                .put("answer", q.answer)
        )
    }

    root.put("questions", questions)

    val encoded = Base64.encodeToString(
        root.toString().toByteArray(Charsets.UTF_8),
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    )

    return "KSQORA1:$encoded"
}

private fun decodeExam(raw: String): Exam? {
    return try {
        val code = raw.trim().substringAfter("KSQORA1:", "")
        if (code.isBlank()) return null

        val root = JSONObject(
            String(
                Base64.decode(code, Base64.URL_SAFE or Base64.NO_WRAP),
                Charsets.UTF_8
            )
        )

        val array = root.getJSONArray("questions")
        val questions = mutableListOf<Question>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val optsJson = item.getJSONArray("options")
            val opts = (0 until optsJson.length()).map {
                optsJson.getString(it)
            }

            val correct = item.getInt("answer")
            if (opts.size != 4 || correct !in 0..3) return null

            questions.add(
                Question(item.getString("text"), opts, correct)
            )
        }

        if (questions.isEmpty()) return null

        Exam(
            root.optString("id", System.currentTimeMillis().toString()),
            root.getString("title"),
            root.getString("subject"),
            questions
        )
    } catch (_: Exception) {
        null
    }
}

private fun saveExams(exams: List<Exam>): String {
    val array = JSONArray()
    exams.forEach { array.put(encodeExam(it)) }
    return array.toString()
}

private fun loadExams(raw: String): List<Exam> {
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull {
            decodeExam(array.getString(it))
        }
    } catch (_: Exception) {
        emptyList()
    }
}

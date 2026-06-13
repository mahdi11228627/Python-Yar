package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.GeminiManager
import com.example.data.PythonLesson
import com.example.data.PythonLessonsProvider
import com.example.data.PythonSimulator
import kotlinx.coroutines.launch

// Color tokens for our custom Premium Dark Concept ("Cosmic Slate Theme")
val CozyBg = Color(0xFF0F172A)        // Deep slate background
val CardBg = Color(0xFF1E293B)        // Lighter slate cards
val NeonCyan = Color(0xFF38BDF8)      // Primary Python Blue accent
val GoldAmber = Color(0xFFFBBF24)     // Secondary Python Yellow accent
val MintGreen = Color(0xFF34D399)     // Live Code output/success
val TermBg = Color(0xFF020617)        // Pure dark for Terminal console
val CharcoalBorder = Color(0xFF334155) // Sleek border lines
val PastelPurple = Color(0xFFC084FC)   // AI message outline accent
val GlassyText = Color(0xFF94A3B8)     // Soft gray readability text

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // Explicitly enforce Right-To-Left (RTL) Layout Direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        
        // Active Main Tab: 0 = Lessons/Simulator, 1 = AI Companion Chat
        var activeTab by remember { mutableStateOf(0) }
        
        // Selected Python Lesson
        val lessons = PythonLessonsProvider.lessons
        var selectedLesson by remember { mutableStateOf(lessons[0]) }
        
        // Active code inside the interactive editor
        var activeCodeText by remember { mutableStateOf(selectedLesson.codeSnippet) }
        
        // Synced reset when lesson changes
        LaunchedEffect(selectedLesson) {
            activeCodeText = selectedLesson.codeSnippet
        }
        
        // Simulator Output Console State
        var simulatorOutput by remember { mutableStateOf("") }
        var isRunningCode by remember { mutableStateOf(false) }

        // AI Chat Engine States
        var chatInput by remember { mutableStateOf("") }
        var isAiResponding by remember { mutableStateOf(false) }
        val chatMessages = remember {
            mutableStateListOf(
                ChatMessage(
                    id = "welcome_msg",
                    text = "سلام دوست من! 🐍 خوش اومدی به پایتون‌یار. من اینجام تا در مورد پایتون بهت یاد بدم. هر سوالی داری بنویس یا یکی از کدهای بخش آموزش رو برام بفرست تا خط‌به‌خط تحلیل کنم!",
                    isUser = false,
                    timestamp = "الان"
                )
            )
        }
        
        val chatListState = rememberLazyListState()
        val keyboardController = LocalSoftwareKeyboardController.current

        // Trigger AI Explanation for current code
        val triggerAiExplain: (String) -> Unit = { codeToExplain ->
            coroutineScope.launch {
                activeTab = 1 // Switch to AI Chat tab
                val userPrompt = "لطفاً این کد پایتون ابتدایی رو به زبان خیلی ساده و با لحن جذاب برام توضیح بده و بگو چیکار میکنه:\n\n```python\n$codeToExplain\n```"
                
                // Add user message mock-up
                chatMessages.add(
                    ChatMessage(
                        id = "user_${System.currentTimeMillis()}",
                        text = "می‌شه این کد از بخش «${selectedLesson.title}» رو برام موشکافی کنی؟ 😊\n\n```python\n${codeToExplain.trim()}\n```",
                        isUser = true,
                        timestamp = "الان"
                    )
                )
                
                isAiResponding = true
                chatListState.animateScrollToItem(chatMessages.size - 1)
                
                val aiResponse = GeminiManager.askGemini(userPrompt)
                
                isAiResponding = false
                chatMessages.add(
                    ChatMessage(
                        id = "ai_${System.currentTimeMillis()}",
                        text = aiResponse,
                        isUser = false,
                        timestamp = "الان"
                    )
                )
                chatListState.animateScrollToItem(chatMessages.size - 1)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CozyBg
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Top Custom Header Accent with Minimal Glow & Pulsing status indicator
                HeaderBar()

                // Modern Pill Tab Layout with Horizontal Transitions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .background(CardBg, RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabData = listOf(
                        "🎓 درس‌ها و شبیه‌ساز" to 0,
                        "💬 گفتگو و عیب‌یاب هوشمند" to 1
                    )
                    
                    tabData.forEach { (title, index) ->
                        val isSelected = activeTab == index
                        val bgBrush = if (isSelected) {
                            Brush.horizontalGradient(listOf(NeonCyan, PastelPurple))
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgBrush)
                                .clickable {
                                    activeTab = index
                                    keyboardController?.hide()
                                }
                                .testTag("tab_button_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CozyBg else GlassyText,
                                style = TextStyle(textAlign = TextAlign.Center)
                            )
                        }
                    }
                }

                // Interactive Content Switcher
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        label = "tab_animation"
                    ) { currentTab ->
                        when (currentTab) {
                            0 -> {
                                // Tab 0: Lessons and simulator
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(bottom = 16.dp)
                                ) {
                                    // Categories & Horizontal Lesson Selector
                                    CategoryLessonsRow(
                                        lessons = lessons,
                                        selectedLesson = selectedLesson,
                                        onLessonSelected = { lesson ->
                                            selectedLesson = lesson
                                            simulatorOutput = "" // Reset output
                                        }
                                    )

                                    // Descriptive Lesson Context Card
                                    LessonExplanationCard(selectedLesson)

                                    // Code Editor Section (Interactive block)
                                    CodeEditorSection(
                                        code = activeCodeText,
                                        onCodeChange = { activeCodeText = it },
                                        onRunClick = {
                                            coroutineScope.launch {
                                                isRunningCode = true
                                                simulatorOutput = "🔄 در حال اجرای مفسر شبیه‌ساز..."
                                                kotlinx.coroutines.delay(400) // Small delay for high-fidelity response
                                                simulatorOutput = PythonSimulator.executeCode(activeCodeText)
                                                isRunningCode = false
                                                Toast.makeText(context, "کد با موفقیت پردازش شد", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onResetClick = {
                                            activeCodeText = selectedLesson.codeSnippet
                                            simulatorOutput = ""
                                        },
                                        onExplainClick = {
                                            triggerAiExplain(activeCodeText)
                                        }
                                    )

                                    // Console Terminal View Panel
                                    TerminalConsolePanel(
                                        output = simulatorOutput,
                                        expectedOutput = selectedLesson.expectedOutput,
                                        isRunning = isRunningCode
                                    )
                                }
                            }
                            1 -> {
                                // Tab 1: AI Assistant
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // Chat Messages list
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        LazyColumn(
                                            state = chatListState,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp),
                                            contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(chatMessages, key = { it.id }) { message ->
                                                ChatBubbleItem(message)
                                            }
                                            
                                            if (isAiResponding) {
                                                item {
                                                    AiThinkingIndicator()
                                                }
                                            }
                                        }
                                    }

                                    // Quick questions chips row to quickly prompt Gemini
                                    QuickChipsRow(
                                        onChipClicked = { question ->
                                            chatInput = question
                                        }
                                    )

                                    // Chat input bottom panel (RTL aligned)
                                    ChatInputPanel(
                                        inputValue = chatInput,
                                        onValueChange = { chatInput = it },
                                        onSendClick = {
                                            if (chatInput.isNotBlank()) {
                                                val userQuestion = chatInput.trim()
                                                chatInput = ""
                                                keyboardController?.hide()
                                                
                                                coroutineScope.launch {
                                                    chatMessages.add(
                                                        ChatMessage(
                                                            id = "user_${System.currentTimeMillis()}",
                                                            text = userQuestion,
                                                            isUser = true,
                                                            timestamp = "الان"
                                                        )
                                                    )
                                                    isAiResponding = true
                                                    chatListState.animateScrollToItem(chatMessages.size - 1)
                                                    
                                                    val aiAnswer = GeminiManager.askGemini(userQuestion)
                                                    
                                                    isAiResponding = false
                                                    chatMessages.add(
                                                        ChatMessage(
                                                            id = "ai_${System.currentTimeMillis()}",
                                                            text = aiAnswer,
                                                            isUser = false,
                                                            timestamp = "الان"
                                                        )
                                                    )
                                                    chatListState.animateScrollToItem(chatMessages.size - 1)
                                                }
                                            }
                                        },
                                        isAiResponding = isAiResponding
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- UI COMPONENTS ----------------

@Composable
fun HeaderBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .shadow(0.dp)
            .drawBehind {
                // Subtle line divider underneath
                drawLine(
                    color = CharcoalBorder,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🐍 پایتون‌یار",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "نسخه ۱.۰",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }
            Text(
                text = "کدنویسی آسان و گفتگو با دستیار هوشمند فارسی",
                fontSize = 11.sp,
                color = GlassyText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        // Status indicator representing Gemini Engine connectivity
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(CardBg, RoundedCornerShape(16.dp))
                .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MintGreen.copy(alpha = alphaAnim))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "موتور Gemini فعال",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun CategoryLessonsRow(
    lessons: List<PythonLesson>,
    selectedLesson: PythonLesson,
    onLessonSelected: (PythonLesson) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = "📬 سرفصل‌های پایه‌ای پایتون",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = GoldAmber,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(lessons) { lesson ->
                val isSelected = lesson.id == selectedLesson.id
                val borderCol = if (isSelected) NeonCyan else CharcoalBorder
                val cardElev = if (isSelected) 8.dp else 0.dp
                
                Box(
                    modifier = Modifier
                        .shadow(cardElev, RoundedCornerShape(16.dp), spotColor = NeonCyan)
                        .background(CardBg, RoundedCornerShape(16.dp))
                        .border(1.2.dp, borderCol, RoundedCornerShape(16.dp))
                        .clickable { onLessonSelected(lesson) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .widthIn(min = 140.dp)
                        .testTag("lesson_tab_${lesson.id}")
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = lesson.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) NeonCyan else GlassyText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lesson.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${lesson.difficulty} | ${lesson.duration}",
                                fontSize = 10.sp,
                                color = GlassyText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonExplanationCard(lesson: PythonLesson) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CharcoalBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lesson.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                
                Text(
                    text = lesson.englishTitle,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = GlassyText
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = lesson.description,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Fast Tips Highlight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CozyBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "💡 نکات طلایی:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAmber
                )
                Spacer(modifier = Modifier.height(6.dp))
                lesson.tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•",
                            fontSize = 15.sp,
                            color = GoldAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Text(
                            text = tip,
                            fontSize = 11.5.sp,
                            color = GlassyText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorSection(
    code: String,
    onCodeChange: (String) -> Unit,
    onRunClick: () -> Unit,
    onResetClick: () -> Unit,
    onExplainClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💻 ویرایشگر تعاملی کدهای پایتون:",
                fontSize = 13.3.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Reset Button
                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier
                        .size(28.dp)
                        .background(CardBg, CircleShape)
                        .border(1.dp, CharcoalBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "بازنشانی",
                        tint = GoldAmber,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        
        // Editable Code Block Container styling the lines
        TextField(
            value = code,
            onValueChange = onCodeChange,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = NeonCyan,
                lineHeight = 20.sp,
                textDirection = TextDirection.Ltr
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .testTag("code_editor_field"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TermBg,
                unfocusedContainerColor = TermBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = GoldAmber
            ),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Interactive Console Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // RUN Button
            Button(
                onClick = onRunClick,
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                modifier = Modifier
                    .weight(1.2f)
                    .height(44.dp)
                    .testTag("run_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CozyBg,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "اجرای فوری کد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = CozyBg
                )
            }

            // AI EXPLAIN Button
            Button(
                onClick = onExplainClick,
                colors = ButtonDefaults.buttonColors(containerColor = PastelPurple),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("explain_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CozyBg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "کالبدشکافی هوشمند",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CozyBg
                )
            }
        }
    }
}

@Composable
fun TerminalConsolePanel(
    output: String,
    expectedOutput: String,
    isRunning: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "🪵 ترمینال خروجی پایتون:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = GlassyText,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TermBg, RoundedCornerShape(16.dp))
                .border(1.2.dp, CharcoalBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
                .heightIn(min = 90.dp)
        ) {
            if (output.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = GlassyText.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "برای دیدن نتایج اجرا روی دکمه «اجرای فوری کد» ضربه بزنید.",
                        fontSize = 11.5.sp,
                        color = GlassyText.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = output,
                    color = if (output.startsWith("🚨")) Color.Red else MintGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("terminal_output_text")
                )
            }
            
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd),
                    color = NeonCyan,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val isUser = message.isUser
    val bubbleBg = if (isUser) NeonCyan else CardBg
    val textColor = if (isUser) CozyBg else Color.White
    val borderStroke = if (isUser) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, CharcoalBorder)
    
    // Bubble Alignment wrapper
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
    ) {
        // AI icon avatar
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PastelPurple.copy(alpha = 0.2f))
                    .border(1.dp, PastelPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🧠", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (isUser) 4.dp else 16.dp, 
                bottomEnd = if (isUser) 16.dp else 4.dp
            ),
            color = bubbleBg,
            border = borderStroke,
            modifier = Modifier
                .widthIn(max = 290.dp)
                .shadow(if (isUser) 2.dp else 0.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // If message has python syntax formatting, enhance it
                val parsedText = highlightMarkdownCodes(message.text, isUser)
                
                Text(
                    text = parsedText,
                    fontSize = 13.sp,
                    color = textColor,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.2f))
                    .border(1.dp, NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun AiThinkingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PastelPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🖲️", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBg,
            border = BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.widthIn(max = 240.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = PastelPurple,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "پایتون‌یار در حال بررسی کدهای شماست...",
                    fontSize = 11.5.sp,
                    color = GlassyText
                )
            }
        }
    }
}

@Composable
fun QuickChipsRow(onChipClicked: (String) -> Unit) {
    val quickQuestions = listOf(
        "🧠 چطور یه حلقه For بنویسم؟",
        "❓ فرق متغیر عددی با رشته‌ای چیه؟",
        "🕷️ لیست‌ها چطور کار میکنند؟",
        "🧩 چطور شرط بنویسم؟"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "⚡ کلیدهای کمکی پرسش تندفهم:",
            fontSize = 11.sp,
            color = GlassyText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { question ->
                Box(
                    modifier = Modifier
                        .background(CardBg, RoundedCornerShape(12.dp))
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                        .clickable { onChipClicked(question.substring(2)) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = question,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputPanel(
    inputValue: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isAiResponding: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .border(1.dp, CharcoalBorder)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chat text container
        OutlinedTextField(
            value = inputValue,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = "مثلاً: دستور if چطور نوشته می‌شود؟ ...",
                    fontSize = 12.5.sp,
                    color = GlassyText
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CharcoalBorder,
                focusedContainerColor = CozyBg,
                unfocusedContainerColor = CozyBg
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("chat_input_field"),
            maxLines = 3,
            textStyle = TextStyle(
                fontSize = 13.sp,
                color = Color.White,
                textDirection = TextDirection.Rtl // Align input to RTL
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (!isAiResponding) onSendClick() })
        )

        // Floating action send button
        IconButton(
            onClick = onSendClick,
            enabled = !isAiResponding && inputValue.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (inputValue.isNotBlank() && !isAiResponding) NeonCyan else CharcoalBorder,
                    RoundedCornerShape(14.dp)
                )
                .testTag("chat_send_button")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "فرستادن",
                tint = if (inputValue.isNotBlank() && !isAiResponding) CozyBg else GlassyText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Helper to highlight python code inside chat bubbles markdown format e.g. ```python ... ```
fun highlightMarkdownCodes(text: String, isUser: Boolean): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("```")
        for (index in parts.indices) {
            val segment = parts[index]
            if (index % 2 == 1) {
                // inside code block
                val cleanCode = if (segment.startsWith("python")) segment.substring(6) else segment
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = if (isUser) CozyBg else NeonCyan,
                        background = (if (isUser) Color.Black.copy(alpha = 0.15f) else TermBg).copy(alpha = 0.8f)
                    )
                ) {
                    append(cleanCode.trim())
                }
            } else {
                // outside markdown
                append(segment)
            }
        }
    }
}

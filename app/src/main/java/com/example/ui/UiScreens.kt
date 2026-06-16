package com.example.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Dynamic Color Scheme (Professional Polish Theme)
val DarkBackground = Color(0xFF0F0D0E) // Luxurious matte black-brown
val CardBackground = Color(0xFF1C1B1F) // Premium matte charcoal gray
val NeonGreen = Color(0xFFE50914)      // Bold Netflix Red accent
val CyanAcc = Color(0xFF6366F1)        // Indigo accent for Kids/secondary tabs
val GoldYellow = Color(0xFFFFD700)     // Amber / Gold contrast for stars & admin
val TextMuted = Color(0xFF94A3B8)      // Slate 400

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppMainScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeTab by viewModel.selectedTab.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsState()
    val isMuted by viewModel.isMusicMuted.collectAsState()
    val footMusicUrl by viewModel.footMusicUrl.collectAsState()
    var showMusicUrlDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var showAuthDialog by remember { mutableStateOf(false) }
    var activeWatchMovie by remember { mutableStateOf<MovieEntity?>(null) }
    var activeWatchMatch by remember { mutableStateOf<MatchEntity?>(null) }
    var activeJoinPaywall by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        if (currentUser == null) {
            AuthScreen(viewModel = viewModel)
        } else {
            Scaffold(
                bottomBar = {
                    BottomNavBar(
                        selectedTab = activeTab,
                        lang = lang,
                        onTabSelected = { viewModel.navigateTo(it) },
                        isAdmin = currentUser?.role == "admin"
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
                        }
                    ) { targetTab ->
                        when (targetTab) {
                            "home" -> HomeTab(viewModel, onWatchMovie = { movie ->
                                if (currentUser?.subscriptionExpires != null && currentUser!!.subscriptionExpires!! > System.currentTimeMillis()) {
                                    activeWatchMovie = movie
                                } else {
                                    activeJoinPaywall = true
                                }
                            })
                            "kids" -> KidsTab(viewModel, onWatchMovie = { movie ->
                                if (currentUser?.subscriptionExpires != null && currentUser!!.subscriptionExpires!! > System.currentTimeMillis()) {
                                    activeWatchMovie = movie
                                } else {
                                    activeJoinPaywall = true
                                }
                            })
                            "foot" -> FootTab(viewModel, onWatchMatch = { match ->
                                if (currentUser?.subscriptionExpires != null && currentUser!!.subscriptionExpires!! > System.currentTimeMillis()) {
                                    activeWatchMatch = match
                                } else {
                                    activeJoinPaywall = true
                                }
                            })
                            "community" -> CommunityTab(viewModel)
                            "settings" -> SettingsTab(viewModel)
                            "admin" -> {
                                if (currentUser?.role == "admin") {
                                    var adminSecuredAccess by remember { mutableStateOf(false) }
                                    if (adminSecuredAccess) {
                                        AdminTab(viewModel)
                                    } else {
                                        var codeInput by remember { mutableStateOf("") }
                                        var errorMessage by remember { mutableStateOf("") }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(DarkBackground)
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = CardBackground),
                                                border = BorderStroke(1.dp, NeonGreen.copy(0.3f)),
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("admin_secure_gate")
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(24.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    Text(
                                                        text = "🔒 خادم الـ Admin محمي",
                                                        color = Color.White,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "الرجاء إدخال كود المرور الخاص بالأدمن للدخول:",
                                                        color = TextMuted,
                                                        fontSize = 12.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    OutlinedTextField(
                                                        value = codeInput,
                                                        onValueChange = {
                                                            codeInput = it
                                                            errorMessage = ""
                                                        },
                                                        label = { Text("كود الدخول (Admin Code)") },
                                                        singleLine = true,
                                                        visualTransformation = PasswordVisualTransformation(),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = NeonGreen,
                                                            unfocusedBorderColor = Color.White.copy(0.12f),
                                                            focusedLabelColor = NeonGreen
                                                        ),
                                                        modifier = Modifier.fillMaxWidth().testTag("admin_code_input")
                                                    )
                                                    if (errorMessage.isNotEmpty()) {
                                                        Text(errorMessage, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Button(
                                                        onClick = {
                                                            if (codeInput.trim() == "BILLKPLLH") {
                                                                adminSecuredAccess = true
                                                            } else {
                                                                errorMessage = "الكود غير صحيح! حاول مجدداً."
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.White),
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("verify_admin_code_button")
                                                    ) {
                                                        Text("دخول الخادم", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("غير مصرح لك بدخول لوحة التحكم.", color = Color.Red, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Floating Sound controller inside FOOT tab or as floating overlay globally
                    if (activeTab == "foot") {
                        FloatingAudioController(
                            isPlaying = isMusicPlaying,
                            isMuted = isMuted,
                            onTogglePlay = { viewModel.togglePlayPauseMusic() },
                            onToggleMute = { viewModel.toggleFootMusicMute() },
                            onEditUrl = { showMusicUrlDialog = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        )
                    }

                    // Custom dynamic sound URL selection dialog popup
                    if (showMusicUrlDialog) {
                        var tempUrl by remember { mutableStateOf(footMusicUrl) }
                        AlertDialog(
                            onDismissRequest = { showMusicUrlDialog = false },
                            containerColor = CardBackground,
                            titleContentColor = Color.White,
                            textContentColor = TextMuted,
                            title = {
                                Text(
                                    text = "تغيير بث الأغنية 🎶",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "أدخل رابط بث ملف صوتي مباشراً (MP3/WAV/etc.) لتشغيله كخلفية صوتية في قسم المباريات:",
                                        fontSize = 12.sp
                                    )
                                    OutlinedTextField(
                                        value = tempUrl,
                                        onValueChange = { tempUrl = it },
                                        label = { Text("رابط الصوت المباشر") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonGreen,
                                            unfocusedBorderColor = Color.White.copy(0.12f),
                                            focusedLabelColor = NeonGreen
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("custom_audio_url_input")
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.updateFootMusicUrl(tempUrl)
                                        showMusicUrlDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.White)
                                ) {
                                    Text("حفظ وتعديل", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showMusicUrlDialog = false }
                                ) {
                                    Text("إلغاء", color = Color.White)
                                }
                            }
                        )
                    }

                    // Video Watch Dialog
                    activeWatchMovie?.let { movie ->
                        MoviePlayerDialog(
                            movie = movie,
                            viewModel = viewModel,
                            onDismiss = { activeWatchMovie = null }
                        )
                    }

                    // Live football streaming dialog
                    activeWatchMatch?.let { match ->
                        MatchPlayerDialog(
                            match = match,
                            onDismiss = { activeWatchMatch = null }
                        )
                    }

                    // Paywall code dialog
                    if (activeJoinPaywall) {
                        PaywallActivationDialog(
                            viewModel = viewModel,
                            onDismiss = { activeJoinPaywall = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    selectedTab: String,
    lang: String,
    onTabSelected: (String) -> Unit,
    isAdmin: Boolean
) {
    NavigationBar(
        containerColor = CardBackground,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = selectedTab == "home",
            onClick = { onTabSelected("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(Localization.getText("home", lang), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                selectedTextColor = NeonGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_home_button")
        )
        NavigationBarItem(
            selected = selectedTab == "kids",
            onClick = { onTabSelected("kids") },
            icon = { Icon(Icons.Default.Face, contentDescription = null) },
            label = { Text(Localization.getText("kids", lang), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAcc,
                selectedTextColor = CyanAcc,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_kids_button")
        )
        NavigationBarItem(
            selected = selectedTab == "foot",
            onClick = { onTabSelected("foot") },
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text(Localization.getText("foot", lang), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                selectedTextColor = NeonGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_foot_button")
        )
        NavigationBarItem(
            selected = selectedTab == "community",
            onClick = { onTabSelected("community") },
            icon = { Icon(Icons.Default.Send, contentDescription = null) },
            label = { Text(Localization.getText("community", lang), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                selectedTextColor = NeonGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_community_button")
        )
        NavigationBarItem(
            selected = selectedTab == "settings",
            onClick = { onTabSelected("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(Localization.getText("settings", lang), fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                selectedTextColor = NeonGreen,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_settings_button")
        )
        if (isAdmin) {
            NavigationBarItem(
                selected = selectedTab == "admin",
                onClick = { onTabSelected("admin") },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldYellow) },
                label = { Text("الأدمن", fontSize = 11.sp, color = GoldYellow) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GoldYellow,
                    selectedTextColor = GoldYellow,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag("nav_admin_button")
            )
        }
    }
}

// ---------------- AUTH SCREEN ----------------
@Composable
fun AuthScreen(viewModel: MainViewModel) {
    var isLoginMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BILLK+",
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            color = NeonGreen,
            style = MaterialTheme.typography.headlineLarge.copy(
                shadow = Shadow(color = NeonGreen.copy(0.25f), offset = Offset(0f, 4f), blurRadius = 15f)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (isLoginMode) "تسجيل الدخول للمنصة" else "إنشاء حساب بيلك بلس جديد",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isLoginMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedLabelColor = NeonGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("لقب المستخدم") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedLabelColor = NeonGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("auth_field_nickname")
                )

                if (!isLoginMode) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedLabelColor = NeonGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedLabelColor = NeonGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = avatarUrl,
                        onValueChange = { avatarUrl = it },
                        label = { Text("رابط الصورة الشخصية (اختياري)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedLabelColor = NeonGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("كلمة المرور") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedLabelColor = NeonGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("auth_field_password")
                )

                Button(
                    onClick = {
                        if (isLoginMode) {
                            viewModel.login(nickname, pass) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.register(name, nickname, phone, email, pass, avatarUrl) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("auth_submit_button")
                ) {
                    Text(
                        text = if (isLoginMode) "تسجيل الدخول" else "إنشاء الحساب وتأكيد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                TextButton(
                    onClick = { isLoginMode = !isLoginMode },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(
                        text = if (isLoginMode) "ليس لديك حساب؟ أنشئ واحداً الآن" else "لديك حساب بالفعل؟ سجل الدخول",
                        color = NeonGreen
                    )
                }
            }
        }
    }
}

// ---------------- HOME TAB (BIG FILM) ----------------
@Composable
fun HomeTab(viewModel: MainViewModel, onWatchMovie: (MovieEntity) -> Unit) {
    val movies by viewModel.allMovies.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val nonKids = movies.filter { !it.isKids }
    val latestMovie = nonKids.lastOrNull() // Pick latest movie for high-profile hero banner

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Modern Premium Brand Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "BILLK",
                        color = NeonGreen, // Brand Red Accent!
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = (-1).sp
                        )
                    )
                    Text(
                        text = "PREMIUM STREAMING",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp
                        )
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search icon inside circle border
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(0.05f), CircleShape)
                            .border(1.dp, Color.White.copy(0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // User Profile image
                    AsyncImage(
                        model = currentUser?.avatarUrl ?: "https://ui-avatars.com/api/?name=User",
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, NeonGreen.copy(0.5f), CircleShape)
                    )
                }
            }
        }

        // High-profile dynamic Netflix Hero Banner of latest film/series
        latestMovie?.let { hero ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    AsyncImage(
                        model = hero.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // High quality linear dark fade overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DarkBackground),
                                    startY = 200f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(NeonGreen.copy(0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("أحدث إضافة لـ بيلك فيلم 🔥", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = hero.title,
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = hero.description,
                            color = Color.White.copy(0.7f),
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Button(
                                onClick = { onWatchMovie(hero) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مشاهدة الآن", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section standard films
        item {
            Text(
                text = "الأفلام المميزة 🎬",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            val films = nonKids.filter { !it.isSeries }
            if (films.isEmpty()) {
                Text("لا توجد أفلام حالياً", color = TextMuted, modifier = Modifier.padding(16.dp))
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(films) { film ->
                        MovieCard(movie = film, onClick = { onWatchMovie(film) })
                    }
                }
            }
        }

        // Section series
        item {
            Text(
                text = "المسلسلات الحصرية 📺",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
            val series = nonKids.filter { it.isSeries }
            if (series.isEmpty()) {
                Text("لا توجد مسلسلات حالياً", color = TextMuted, modifier = Modifier.padding(16.dp))
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(series) { ser ->
                        MovieCard(movie = ser, onClick = { onWatchMovie(ser) })
                    }
                }
            }
        }

        // Bottom space spacer
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ---------------- KIDS TAB ----------------
@Composable
fun KidsTab(viewModel: MainViewModel, onWatchMovie: (MovieEntity) -> Unit) {
    val movies by viewModel.allMovies.collectAsState()
    val kidsMovies = movies.filter { it.isKids }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            ),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BILLK KIDS 🧸",
                    color = CyanAcc,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        shadow = Shadow(color = CyanAcc.copy(0.4f), offset = Offset(0f, 2f), blurRadius = 10f)
                    )
                )
                Text(
                    text = "عالم مليء بالمرح والرسوم المخصصة للأطفال الآمنة!",
                    color = Color.White.copy(0.8f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        item {
            Text(
                text = "أفلام الكارتون المفضلة 🌟",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
            )
        }

        if (kidsMovies.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("سيتم إضافة الكارتون والرسوم المتحركة للأطفال قريباً! 🧸", color = Color.White.copy(0.6f), textAlign = TextAlign.Center)
                }
            }
        } else {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(kidsMovies) { cartoon ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground.copy(0.85f)),
                            shape = RoundedCornerShape(24.dp),
                            onClick = { onWatchMovie(cartoon) },
                            modifier = Modifier
                                .width(170.dp)
                                .border(2.dp, CyanAcc.copy(0.3f), RoundedCornerShape(24.dp))
                        ) {
                            Column {
                                AsyncImage(
                                    model = cartoon.coverUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                )
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = cartoon.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .background(CyanAcc.copy(0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(cartoon.ageRating, color = CyanAcc, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
fun MovieCard(movie: MovieEntity, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .testTag("movie_item_card_${movie.id}")
    ) {
        Column {
            AsyncImage(
                model = movie.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = movie.ageRating,
                    color = NeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ---------------- FOOT TAB (BILLK FOOT) ----------------
@Composable
fun FootTab(viewModel: MainViewModel, onWatchMatch: (MatchEntity) -> Unit) {
    val matches by viewModel.allMatches.collectAsState()
    var activeSubTab by remember { mutableStateOf("all") } // "all", "live", "upcoming", "ended", "game"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Football Header Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(CardBackground)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "all" to "الكل ⚽",
                "live" to "🔴 مباشر",
                "upcoming" to "⏰ قادمة",
                "ended" to "منتهية ✅",
                "game" to "🎮 ألعاب وتحديات"
            )
            tabs.forEach { (key, label) ->
                Button(
                    onClick = { activeSubTab = key },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == key) NeonGreen else Color.White.copy(0.05f),
                        contentColor = if (activeSubTab == key) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activeSubTab == "game") {
            MiniGamesContainer(viewModel)
        } else {
            val filteredMatches = matches.filter {
                activeSubTab == "all" || it.status == activeSubTab
            }

            if (filteredMatches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد مباريات في الوقت الحالي كوره 🥅", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMatches) { m ->
                        MatchCard(match = m, onWatch = { onWatchMatch(m) })
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(match: MatchEntity, onWatch: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp),
        onClick = onWatch,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header showing Match state and commentator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            when (match.status) {
                                "live" -> Color.Red
                                "ended" -> Color.Gray
                                else -> NeonGreen
                            }.copy(0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (match.status) {
                            "live" -> "🔴 مباشر الآن"
                            "ended" -> "انتهت"
                            else -> "قادمة"
                        },
                        color = when (match.status) {
                            "live" -> Color.Red
                            "ended" -> Color.White
                            else -> NeonGreen
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("المعلق: " + match.commentator, color = TextMuted, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central VS View
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1 (On the right/home)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    AsyncImage(
                        model = match.team1LogoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        match.team1Name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Text(
                    text = "VS",
                    color = TextMuted,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )

                // Team 2 (On the left/away)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    AsyncImage(
                        model = match.team2LogoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        match.team2Name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = match.description,
                color = Color.White.copy(0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onWatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (match.status == "live") Color.Red else NeonGreen,
                    contentColor = if (match.status == "live") Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (match.status == "ended") "تقرير المباراة" else "اضغط للمشاهدة البث",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------- MINI FOOTBALL GAMES ----------------
@Composable
fun MiniGamesContainer(viewModel: MainViewModel) {
    var activeGame by remember { mutableStateOf("menu") } // "menu", "penalty", "quiz"
    val context = LocalContext.current

    AnimatedContent(targetState = activeGame) { game ->
        when (game) {
            "menu" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ألعاب كرة القدم — بيلك بلس 🏆", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("العب مع أصدقائك واجمع النقاط لرفع مستواك!", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            onClick = { activeGame = "penalty" },
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("⚽", fontSize = 38.sp)
                                Text("ضربات الجزاء", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                Text("أصعب ركلات!", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp),
                            onClick = { activeGame = "quiz" },
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .border(1.dp, CyanAcc.copy(0.2f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("💡", fontSize = 38.sp)
                                Text("تحدي المعلومات", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                Text("ثقافة التيك تيك!", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            "penalty" -> {
                PenaltyShootoutGame(
                    onBack = { activeGame = "menu" },
                    onAwardPoints = { pts ->
                        viewModel.awardGamePoints(pts)
                        Toast.makeText(context, "+${pts * 10} نقطة مضافة إلى رصيدك!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            "quiz" -> {
                FootballQuizGame(
                    onBack = { activeGame = "menu" },
                    onAwardPoints = { score ->
                        viewModel.awardGamePoints(score)
                        Toast.makeText(context, "أحسنت! +${score * 10} نقطة إجابات صحيحة!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun PenaltyShootoutGame(onBack: () -> Unit, onAwardPoints: (Int) -> Unit) {
    var roundNum by remember { mutableStateOf(1) }
    var scoredCount by remember { mutableStateOf(0) }
    var resultText by remember { mutableStateOf("اختر اتجاه ركلتك وهز الشباك! 🥅") }
    var animatingBall by remember { mutableStateOf(false) }
    var shootDir by remember { mutableStateOf("") }
    var goalieDir by remember { mutableStateOf("") }
    var isGameOver by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Smooth physics layout state variables
    val ballOffset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.08f))) {
                Text("← رجوع", color = Color.White)
            }
            Text("الجولة $roundNum / 5", color = Color.White, fontWeight = FontWeight.Bold)
            Text("الأهداف: $scoredCount", color = NeonGreen, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Virtual Pitch Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF1B5E20), RoundedCornerShape(16.dp))
                .border(2.dp, Color.White.copy(0.4f), RoundedCornerShape(16.dp))
        ) {
            // Draw Goalposts
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .width(180.dp)
                    .height(60.dp)
                    .border(4.dp, Color.White, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(Color.White.copy(0.02f))
            )

            // Draw Goalkeeper
            Text(
                text = "🧤🏃",
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .offset(
                        x = when (goalieDir) {
                            "left" -> (-50).dp
                            "right" -> 50.dp
                            else -> 0.dp
                        }
                    )
            )

            // Draw Soccer Ball
            Text(
                text = "⚽",
                fontSize = 28.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .offset(
                        x = if (animatingBall) {
                            when (shootDir) {
                                "left" -> (-60).dp
                                "right" -> 60.dp
                                else -> 0.dp
                            }
                        } else 0.dp,
                        y = if (animatingBall) (-110).dp else 0.dp
                    )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = resultText, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        if (!isGameOver) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val directions = listOf("left" to "← يسار", "center" to "↑ وسط", "right" to "يمين →")
                directions.forEach { (dir, label) ->
                    Button(
                        onClick = {
                            if (animatingBall) return@Button
                            shootDir = dir
                            animatingBall = true
                            goalieDir = listOf("left", "center", "right").random()

                            coroutineScope.launch {
                                delay(700)
                                if (shootDir == goalieDir) {
                                    resultText = "🧤 الحارس طار وصد الكرة بغرابة!"
                                } else {
                                    resultText = "🎯 هدف أسطوري داخل الشباك!"
                                    scoredCount++
                                }
                                delay(1200)
                                if (roundNum < 5) {
                                    roundNum++
                                    shootDir = ""
                                    goalieDir = ""
                                    animatingBall = false
                                    resultText = "ركلة غامضة تالية! اختر اتجاهك..."
                                } else {
                                    isGameOver = true
                                    resultText = "انتهت اللعبة! أحرزت $scoredCount أهداف من تيك تاك."
                                    onAwardPoints(scoredCount)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    roundNum = 1
                    scoredCount = 0
                    resultText = "اختر اتجاه ركلتك وهز الشباك! 🥅"
                    animatingBall = false
                    shootDir = ""
                    goalieDir = ""
                    isGameOver = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إعادة التحدي والمرح 🔁", fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class QuizQuestion(val text: String, val options: List<String>, val correctIndex: Int)

@Composable
fun FootballQuizGame(onBack: () -> Unit, onAwardPoints: (Int) -> Unit) {
    val questions = remember {
        listOf(
            QuizQuestion(
                "أين أقيم أول كأس عالم في التاريخ سنة 1930؟",
                listOf("البرازيل", "الأرجنتين", "الأوروغواي", "فرنسا"),
                2
            ),
            QuizQuestion(
                "أي لاعب كرة قدم فاز بـ بيلوك الذهب الكرات كأكثر تاريخي؟",
                listOf("كريستيانو رونالدو", "ليونيل ميسي", "رونالدينيو", "بيليه"),
                1
            ),
            QuizQuestion(
                "ما هو النادي حامل الرقم القياسي بالتتويج بدوري أبطال أوروبا؟",
                listOf("ريال مدريد", "ميلان", "ليفربول", "برشلونة"),
                0
            )
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var isDone by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.08f))) {
                Text("← رجوع", color = Color.White)
            }
            Text("السؤال ${currentIndex + 1} / ${questions.size}", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isDone) {
            val q = questions[currentIndex]
            Text(
                text = q.text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                q.options.forEachIndexed { i, opt ->
                    val isSelected = selectedAnswerIndex == i
                    Button(
                        onClick = {
                            if (selectedAnswerIndex != null) return@Button
                            selectedAnswerIndex = i
                            if (i == q.correctIndex) {
                                correctCount++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedAnswerIndex != null) {
                                if (i == q.correctIndex) Color.Green else if (isSelected) Color.Red else CardBackground
                            } else CardBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isSelected) NeonGreen else Color.White.copy(0.08f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(opt, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            if (selectedAnswerIndex != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (currentIndex + 1 < questions.size) {
                            currentIndex++
                            selectedAnswerIndex = null
                        } else {
                            isDone = true
                            onAwardPoints(correctCount)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("السؤال التالي ➡️", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text("انتهى التحدي الذكي! 🎉", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "أجبت بشكل صحيح على $correctCount من أصل ${questions.size} أسئلة.",
                color = TextMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            Button(
                onClick = {
                    currentIndex = 0
                    correctCount = 0
                    selectedAnswerIndex = null
                    isDone = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تكرار المحاولة 🔁", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------- COMMUNITY TAB ----------------
@Composable
fun CommunityTab(viewModel: MainViewModel) {
    val comments by viewModel.activeEntityComments.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadComments("community")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "قسم مجتمع بيلك بلس 💬",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "اكتب اقتراحاتك، اطلب إضافة فيلم، أو شارك رأيك بالتحسينات!",
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Comments list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (comments.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("لا يوجد منشورات اقتراحات بعد، كن الأول في المحادثة!", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                items(comments) { comment ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "@" + comment.username, color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val dt = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault()).format(comment.timestamp)
                                Text(text = dt, color = TextMuted, fontSize = 9.sp)
                            }
                            Text(
                                text = comment.text,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text input to post suggestion
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("اكتب اقتراحك هنا...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color.White.copy(0.12f),
                    focusedLabelColor = NeonGreen
                ),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        viewModel.postComment("community", commentText) {
                            commentText = ""
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(54.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
            }
        }
    }
}

// ---------------- SETTINGS TAB ----------------
@Composable
fun SettingsTab(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()
    val friends by viewModel.friendsList.collectAsState()
    val context = LocalContext.current

    var friendNicknameQuery by remember { mutableStateOf("") }
    var showLangSelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = currentUser?.avatarUrl ?: "https://ui-avatars.com/api/?name=User",
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = currentUser?.name ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "@" + (currentUser?.nickname ?: ""), color = NeonGreen, fontSize = 13.sp)
                    Text(text = "رقم الهاتف: " + (currentUser?.phone ?: ""), color = TextMuted, fontSize = 11.sp)
                }
                Box(
                    modifier = Modifier
                        .background(GoldYellow.copy(0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = (currentUser?.points ?: 0).toString(), color = GoldYellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Text(text = "نقطة", color = GoldYellow, fontSize = 10.sp)
                    }
                }
            }
        }

        // Subscription Status Card (as per theme requirement)
        val expirationTime = currentUser?.subscriptionExpires ?: 0L
        val isSubscribed = expirationTime > System.currentTimeMillis()
        val daysRemaining = if (isSubscribed) {
            val diff = expirationTime - System.currentTimeMillis()
            (diff / (24L * 60 * 60 * 1000)).toInt()
        } else {
            0
        }

        var showPromoDialogInSettings by remember { mutableStateOf(false) }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(0.3f)),
            border = BorderStroke(1.dp, Color(0xFF334155).copy(0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().testTag("subscription_status_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF59E0B).copy(0.2f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "حالة الاشتراك المميز",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isSubscribed) {
                                if (daysRemaining > 0) "متبقي $daysRemaining يوم • نشط" else "ينتهي اليوم • نشط"
                            } else {
                                "منتهي أو غير نشط • تفعيل الآن"
                            },
                            color = if (isSubscribed) Color.Green else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Button(
                    onClick = { showPromoDialogInSettings = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CardBackground, contentColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("renew_subscription_button")
                ) {
                    Text(text = "تمديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showPromoDialogInSettings) {
            PaywallActivationDialog(
                viewModel = viewModel,
                onDismiss = { showPromoDialogInSettings = false }
            )
        }

        // Language Select Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تغيير لغة العرض والبلاد / العروبة 🌍",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showLangSelector = !showLangSelector },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val activeLangObj = Localization.supportedLanguages.find { it.code == lang }
                    Text(
                        text = "اللغة النشطة: " + (activeLangObj?.flag ?: "") + " " + (activeLangObj?.name ?: "العربية"),
                        color = Color.White
                    )
                }

                if (showLangSelector) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        items(Localization.supportedLanguages) { l ->
                            FilterChip(
                                selected = lang == l.code,
                                onClick = {
                                    viewModel.changeLanguage(l.code)
                                    showLangSelector = false
                                },
                                label = { Text(l.flag + " " + l.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonGreen,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }

        // Friends Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "إدارة الأصدقاء والمباريات 👥",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "البحث بلقب الصديق الحقيقي وإضافته للعب معاً ضربات جزاء!",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = friendNicknameQuery,
                        onValueChange = { friendNicknameQuery = it },
                        placeholder = { Text("لقب الصديق (e.g. admin)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(0.12f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (friendNicknameQuery.isNotBlank()) {
                                viewModel.addFriendByUsername(friendNicknameQuery) { ok, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (ok) friendNicknameQuery = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إضافة")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (friends.isEmpty()) {
                    Text("ليس لديك أصدقاء مضافون بعد.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                } else {
                    friends.forEach { fr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color.White.copy(0.04f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(fr.friendName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("@" + fr.friendNickname, color = NeonGreen, fontSize = 11.sp)
                            }
                            IconButton(onClick = { viewModel.removeFriend(fr.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Logout
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.2f), contentColor = Color.White),
            border = BorderStroke(1.dp, Color.Red),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚪 تسجيل خروج الحساب", fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------- FLOATING FOOT MEDIA CONTROLLER ----------------
@Composable
fun FloatingAudioController(
    isPlaying: Boolean,
    isMuted: Boolean,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onEditUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(50),
        modifier = modifier
            .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(50))
            .shadow(12.dp, RoundedCornerShape(50))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🎶", fontSize = 12.sp)
            IconButton(onClick = onTogglePlay, modifier = Modifier.size(32.dp)) {
                Text(
                    text = if (isPlaying) "⏸" else "▶",
                    color = NeonGreen,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onToggleMute, modifier = Modifier.size(32.dp)) {
                Text(
                    text = if (isMuted) "🔇" else "🔊",
                    color = NeonGreen,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onEditUrl, modifier = Modifier.size(32.dp)) {
                Text(
                    text = "⚙️",
                    color = NeonGreen,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ---------------- VIDEO PLAYERS IN DIALOG ----------------
@Composable
fun MoviePlayerDialog(
    movie: MovieEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var isSeriesLayout = movie.isSeries
    val episodes by viewModel.getEpisodesForMovie(movie.id).collectAsState(initial = emptyList())
    var currentWatchUrl by remember { mutableStateOf(movie.videoUrl) }
    var selectedEpisodeId by remember { mutableStateOf("") }
    val comments by viewModel.activeEntityComments.collectAsState()
    var writeCommentText by remember { mutableStateOf("") }

    LaunchedEffect(movie.id) {
        viewModel.loadComments(movie.id)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(movie.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }

                // Native Youtube WebView Embed
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black)
                ) {
                    val embeddedUrl = currentWatchUrl.replace("watch?v=", "embed/")
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webViewClient = WebViewClient()
                                loadUrl(embeddedUrl)
                            }
                        },
                        update = { webView ->
                            webView.loadUrl(embeddedUrl)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "عن المحتوى 🎬",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = movie.description,
                        color = Color.White.copy(0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    if (isSeriesLayout && episodes.isNotEmpty()) {
                        Text(
                            text = "حلقات المسلسل 📺",
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        episodes.forEach { ep ->
                            Button(
                                onClick = {
                                    currentWatchUrl = ep.videoUrl
                                    selectedEpisodeId = ep.id
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedEpisodeId == ep.id) NeonGreen else CardBackground
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    "الحلقة ${ep.episodeNumber}: ${ep.episodeTitle}",
                                    color = if (selectedEpisodeId == ep.id) Color.Black else Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reviews / Comments Section inside movies
                    Text(
                        text = "تقييمات وآراء المشاهدين ⭐",
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = writeCommentText,
                            onValueChange = { writeCommentText = it },
                            placeholder = { Text("أضف تعليقك ورأيك في الفيلم...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color.White.copy(0.12f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (writeCommentText.isNotBlank()) {
                                    viewModel.postComment(movie.id, writeCommentText) {
                                        writeCommentText = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Text("نشر")
                        }
                    }

                    comments.forEach { comm ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(CardBackground, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("@" + comm.username, color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                val dt = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault()).format(comm.timestamp)
                                Text(dt, color = TextMuted, fontSize = 9.sp)
                            }
                            Text(comm.text, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchPlayerDialog(match: MatchEntity, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(match.team1Name + " VS " + match.team2Name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black)
                ) {
                    val streamEmbedded = match.streamUrl.replace("watch?v=", "embed/")
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webViewClient = WebViewClient()
                                loadUrl(streamEmbedded)
                            }
                        },
                        update = { webView ->
                            webView.loadUrl(streamEmbedded)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// ---------------- ACTIVATION CODE PAYWALL ----------------
@Composable
fun PaywallActivationDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var promoCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("تفعيل الاشتراك المالي 💳", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "لمشاهدة هذا الفيلم أو كورة البث، يجب أن يكون لديك اشتراك نشط. يرجى الحصول على كود وإدخاله هنا.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it.uppercase() },
                    placeholder = { Text("كود بيلك بلس (e.g. BILLKYEAR)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = Color.White.copy(0.12f),
                        focusedLabelColor = NeonGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (promoCode.isNotBlank()) {
                        viewModel.activateSubscriptionCode(promoCode) { ok, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (ok) {
                                onDismiss()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
            ) {
                Text("تفعيل وتأكيد ✅", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.White)
            }
        }
    )
}

// ---------------- ADMIN PANEL TAB ----------------
@Composable
fun AdminTab(viewModel: MainViewModel) {
    var activeAdminSubTab by remember { mutableStateOf("analytics") } // "analytics", "matches", "movies", "users", "codes"
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TabRow(
            selectedTabIndex = listOf("analytics", "matches", "movies", "users", "codes").indexOf(activeAdminSubTab),
            containerColor = CardBackground,
            contentColor = NeonGreen
        ) {
            val tabs = listOf(
                "analytics" to "إحصائيات",
                "matches" to "⚽ مباريات",
                "movies" to "🎬 أفلام",
                "users" to "👥 الأعضاء",
                "codes" to "🔑 أكواد"
            )
            tabs.forEach { (key, label) ->
                Tab(
                    selected = activeAdminSubTab == key,
                    onClick = { activeAdminSubTab = key },
                    text = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    selectedContentColor = NeonGreen,
                    unselectedContentColor = TextMuted
                )
            }
        }

        when (activeAdminSubTab) {
            "analytics" -> AdminAnalyticsView(viewModel)
            "matches" -> AdminMatchesFormView(viewModel)
            "movies" -> AdminMoviesFormView(viewModel)
            "users" -> AdminMembersListView(viewModel)
            "codes" -> AdminCodesView(viewModel)
        }
    }
}

@Composable
fun AdminAnalyticsView(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val movies by viewModel.allMovies.collectAsState()
    val matches by viewModel.allMatches.collectAsState()
    val codes by viewModel.allCodes.collectAsState()

    val totalLogins = users.size
    val totalStayDuration = users.sumOf { it.stayDurationMinutes }
    val totalEarnings = codes.filter { it.isUsed }.size * 10 // e.g. $10 per code used
    val topViewsMovie = movies.maxByOrNull { it.views }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("الأرقام اللحظية للمنصة 📊", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsStatCard(label = "إجمالي المستخدمين", value = users.size.toString(), color = NeonGreen, modifier = Modifier.weight(1f))
            AnalyticsStatCard(label = "الأرباح التقديرية", value = "$$totalEarnings", color = GoldYellow, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsStatCard(label = "مجموع دقائق البقاء", value = "${totalStayDuration}m", color = CyanAcc, modifier = Modifier.weight(1f))
            AnalyticsStatCard(label = "الأفلام والمحتوى", value = movies.size.toString(), color = Color.White, modifier = Modifier.weight(1f))
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("المحتوى الأكثر مشاهدة تيك تاك 📺", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                if (topViewsMovie != null) {
                    Text(topViewsMovie.title + " (${topViewsMovie.views} مشاهدة)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Text("لا يوجد بيانات مشاهدة بعد.", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun AdminMatchesFormView(viewModel: MainViewModel) {
    val matches by viewModel.allMatches.collectAsState()

    var team1 by remember { mutableStateOf("") }
    var team2 by remember { mutableStateOf("") }
    var logo1 by remember { mutableStateOf("") }
    var logo2 by remember { mutableStateOf("") }
    var streamUrl by remember { mutableStateOf("") }
    var cover by remember { mutableStateOf("") }
    var comm by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("upcoming") } // "live", "upcoming", "ended"

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("إضافة مباراة كرة قدم جديدة ⚽", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = team1, onValueChange = { team1 = it }, label = { Text("الفريق الأول (اليمين)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = logo1, onValueChange = { logo1 = it }, label = { Text("رابط شعار الفريق الأول") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = team2, onValueChange = { team2 = it }, label = { Text("الفريق الثاني (اليسار)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = logo2, onValueChange = { logo2 = it }, label = { Text("رابط شعار الفريق الثاني") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = streamUrl, onValueChange = { streamUrl = it }, label = { Text("رابط يوتيوب أو سيرفر البث المباشر") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cover, onValueChange = { cover = it }, label = { Text("رابط صورة خلفية المباراة") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = comm, onValueChange = { comm = it }, label = { Text("المعلق الرياضي") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف القصير للمباراة") }, modifier = Modifier.fillMaxWidth())

        Text("حالة البث الحالية:", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("upcoming" to "قادمة ⏰", "live" to "مباشر 🔴", "ended" to "منتهية ✅").forEach { (st, label) ->
                FilterChip(
                    selected = status == st,
                    onClick = { status = st },
                    label = { Text(label) }
                )
            }
        }

        Button(
            onClick = {
                if (team1.isBlank() || team2.isBlank() || streamUrl.isBlank()) {
                    Toast.makeText(context, "الرجاء ملء الفريقين ورابط البث!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.adminAddMatch(team1, team2, logo1, logo2, streamUrl, cover, comm, desc, status, System.currentTimeMillis())
                    Toast.makeText(context, "تمت إضافة وحفظ المباراة بنجاح! ✅", Toast.LENGTH_SHORT).show()
                    team1 = ""
                    team2 = ""
                    logo1 = ""
                    logo2 = ""
                    streamUrl = ""
                    cover = ""
                    comm = ""
                    desc = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إضافة المباراة الآن ⚽", fontWeight = FontWeight.Bold)
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        Text("المباريات والفعاليات النشطة:", color = Color.White, fontWeight = FontWeight.Bold)
        matches.forEach { m ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(m.team1Name + " VS " + m.team2Name, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("المعلق: " + m.commentator + " | " + m.status, color = TextMuted, fontSize = 11.sp)
                }
                IconButton(onClick = { viewModel.adminDeleteMatch(m.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AdminMoviesFormView(viewModel: MainViewModel) {
    val movies by viewModel.allMovies.collectAsState()

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var cover by remember { mutableStateOf("") }
    var video by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("+16") }
    var showInKids by remember { mutableStateOf(false) } // true for BILLK KIDS, false for BILLK FILM
    var isSeries by remember { mutableStateOf(false) }

    var epMovieId by remember { mutableStateOf("") }
    var epTitle by remember { mutableStateOf("") }
    var epNum by remember { mutableStateOf(1) }
    var epVideo by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("إضافة فيلم أو مسلسل أو كارتون جديد 🎬", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان العمل") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cover, onValueChange = { cover = it }, label = { Text("رابط البوستر / الصورة") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = video, onValueChange = { video = it }, label = { Text("رابط الفيديو أو التريلر") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = rating, onValueChange = { rating = it }, label = { Text("التصنيف العمري (e.g. +16)") }, modifier = Modifier.fillMaxWidth())

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showInKids, onCheckedChange = { showInKids = it })
                Text("إضافة إلى قسم بيلك كيدز 🧸", color = Color.White, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSeries, onCheckedChange = { isSeries = it })
                Text("هل هو مسلسل؟ 📺", color = Color.White, fontSize = 12.sp)
            }
        }

        Button(
            onClick = {
                if (title.isBlank() || video.isBlank()) {
                    Toast.makeText(context, "الرجاء كتابة العنوان ورابط البث!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.adminAddMovie(title, desc, cover, video, rating, showInKids, isSeries)
                    Toast.makeText(context, "تمت إضافة المحتوى للمنصة بنجاح! ✅", Toast.LENGTH_SHORT).show()
                    title = ""
                    desc = ""
                    cover = ""
                    video = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ العمل الفني ونشره 💾", fontWeight = FontWeight.Bold)
        }

        if (movies.any { it.isSeries }) {
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Text("إضافة حلقات للمسلسلات 📺", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            // Select series
            Text("اختر المسلسل:", color = TextMuted)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(movies.filter { it.isSeries }) { m ->
                    FilterChip(
                        selected = epMovieId == m.id,
                        onClick = { epMovieId = m.id },
                        label = { Text(m.title) }
                    )
                }
            }

            OutlinedTextField(value = epTitle, onValueChange = { epTitle = it }, label = { Text("عنوان الحلقة (e.g. الحلقة 1)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = epNum.toString(),
                onValueChange = { epNum = it.toIntOrNull() ?: 1 },
                label = { Text("رقم الحلقة") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = epVideo, onValueChange = { epVideo = it }, label = { Text("رابط فيديو الحلقة") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (epMovieId.isBlank() || epTitle.isBlank() || epVideo.isBlank()) {
                        Toast.makeText(context, "املأ جميع حقول الحلقة!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.adminAddEpisode(epMovieId, epTitle, epNum, epVideo)
                        Toast.makeText(context, "تمت إضافة الحلقة بنجاح! ✅", Toast.LENGTH_SHORT).show()
                        epTitle = ""
                        epVideo = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAcc, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إضافة الحلقة للمسلسل 📺", fontWeight = FontWeight.Bold)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))
        Text("الأعمال المعروضة حالياً:", color = Color.White, fontWeight = FontWeight.Bold)
        movies.forEach { m ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(m.title, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(if (m.isKids) "قسم الأطفال 🧸" else "القسم العام | " + if (m.isSeries) "مسلسل" else "فيلم", color = TextMuted, fontSize = 11.sp)
                }
                IconButton(onClick = { viewModel.adminDeleteMovie(m.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AdminMembersListView(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("إدارة المشتركين والأعضاء (Profile Ti) 👥", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (users.isEmpty()) {
            item {
                Text("لا يوجد أعضاء مشتركين في هذا السيرفر.", color = TextMuted)
            }
        } else {
            items(users) { u ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(u.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("@" + u.nickname + " | " + u.role, color = NeonGreen, fontSize = 12.sp)
                                Text("رقم الهاتف: " + u.phone, color = TextMuted, fontSize = 11.sp)
                                Text(
                                    text = "الحالة الحالية: " + when (u.status) {
                                        "banned" -> "🚫 محظور نهائياً"
                                        "temp_banned" -> "⏱️ محظور مؤقت"
                                        "warned" -> "⚠️ محذر"
                                        else -> "🟢 نشط"
                                    },
                                    color = if (u.status == "banned") Color.Red else NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            AsyncImage(
                                model = u.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.adminWarnUser(u.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldYellow, contentColor = Color.Black),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("تنبيه", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.adminBanUserTemp(u.id, 5) }, // Ban for 5 minutes
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta, contentColor = Color.White),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("حظر مؤقت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.adminBanUserPermanent(u.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("حظر نهائي", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            if (u.status != "active") {
                                Button(
                                    onClick = { viewModel.adminUnbanUser(u.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("تفعيل", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
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
fun AdminCodesView(viewModel: MainViewModel) {
    val codes by viewModel.allCodes.collectAsState()
    
    var selectedPlanName by remember { mutableStateOf("سنة كاملة 👑") }
    var selectedPlanDays by remember { mutableStateOf(365) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("توليد أكواد الاشتراكات وإدارتها 🔑", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Text("اختر باقة التوليد:", color = Color.White)
        val plans = listOf(
            Triple("سنة كاملة 👑", 365, "سنة"),
            Triple("3 أشهر 🌟", 90, "3 أشهر"),
            Triple("شهر كامل ⭐", 30, "شهر"),
            Triple("7 أيام 💚", 7, "7 أيام"),
            Triple("يوم مجاناً 🆓", 1, "يوم")
        )
        plans.forEach { (plan, days, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedPlanName = plan
                        selectedPlanDays = days
                    }
                    .background(
                        if (selectedPlanName == plan) NeonGreen.copy(0.12f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedPlanName == plan, onClick = {
                    selectedPlanName = plan
                    selectedPlanDays = days
                })
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, color = Color.White)
            }
        }

        Button(
            onClick = {
                viewModel.adminGenerateCode(selectedPlanName, selectedPlanDays)
                Toast.makeText(context, "تم توليد الكود بنجاح! 🔑", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("توليد كود بيلك بلس عشوائي ⚙️", fontWeight = FontWeight.Bold)
        }

        Divider()

        Text("الأكواد اللحظية وحالتها:", color = Color.White, fontWeight = FontWeight.Bold)
        codes.forEach { co ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(co.code, color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("الباقة: " + co.planName + " (${co.durationDays} يوم)", color = Color.White, fontSize = 12.sp)
                    Text(
                        text = if (co.isUsed) "مستخدم من طرف @" + co.usedBy else "جاهز للاستخدام",
                        color = if (co.isUsed) GoldYellow else TextMuted,
                        fontSize = 11.sp
                    )
                }
                Row {
                    if (co.isUsed) {
                        IconButton(onClick = { viewModel.adminResetCode(co.code) }) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldYellow)
                        }
                    }
                    IconButton(onClick = { viewModel.adminDeleteCode(co.code) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                }
            }
        }
    }
}

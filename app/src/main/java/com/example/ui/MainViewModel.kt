package com.example.ui

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = Repository(db)

    // Language / Localization State
    private val _currentLanguage = MutableStateFlow("ar")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun changeLanguage(langCode: String) {
        _currentLanguage.value = langCode
    }

    // Active Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Database Streams
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMovies: StateFlow<List<MovieEntity>> = repository.allMovies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCodes: StateFlow<List<SubscriptionCodeEntity>> = repository.allCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Navigation / Selected View
    private val _selectedTab = MutableStateFlow("home") // "home", "kids", "foot", "community", "settings", "admin"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    fun navigateTo(tab: String) {
        _selectedTab.value = tab
        // Handle trigger background music inside FOOT section
        if (tab == "foot") {
            playFootMusic()
        } else {
            pauseFootMusic()
        }
    }

    // Comments State for selected video
    private val _activeEntityComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val activeEntityComments: StateFlow<List<CommentEntity>> = _activeEntityComments.asStateFlow()

    // Friends State
    private val _friendsList = MutableStateFlow<List<FriendEntity>>(emptyList())
    val friendsList: StateFlow<List<FriendEntity>> = _friendsList.asStateFlow()

    // Background Football Music Controller
    private val _footMusicUrl = MutableStateFlow("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3")
    val footMusicUrl: StateFlow<String> = _footMusicUrl.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _isMusicMuted = MutableStateFlow(false)
    val isMusicMuted: StateFlow<Boolean> = _isMusicMuted.asStateFlow()

    private val _musicProgress = MutableStateFlow(0f)
    val musicProgress: StateFlow<Float> = _musicProgress.asStateFlow()

    init {
        // Seeding database
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Keep active comments and friends in sync with user
        viewModelScope.launch {
            _currentUser.collectLatest { user ->
                if (user != null) {
                    repository.getFriendsFlow(user.id).collect {
                        _friendsList.value = it
                    }
                } else {
                    _friendsList.value = emptyList()
                }
            }
        }
    }

    // Load active video comments
    fun loadComments(entityId: String) {
        viewModelScope.launch {
            repository.getCommentsForEntity(entityId).collect {
                _activeEntityComments.value = it
            }
        }
    }

    fun getEpisodesForMovie(movieId: String): Flow<List<EpisodeEntity>> {
        return repository.getEpisodesForMovie(movieId)
    }

    // AUTH ACTIONS
    fun login(nickname: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByNickname(nickname)
            if (user == null) {
                onResult(false, "اسم اللقب غير موجود!")
                return@launch
            }
            if (user.password != pass) {
                onResult(false, "كلمة المرور خاطئة!")
                return@launch
            }
            if (user.status == "banned") {
                onResult(false, "عذراً، هذا الحساب محظور نهائياً من الموقع.")
                return@launch
            }
            if (user.tempBanExpires > System.currentTimeMillis()) {
                val remMinutes = ((user.tempBanExpires - System.currentTimeMillis()) / 60000) + 1
                onResult(false, "الحساب محظور مؤقتاً. سيفك الحظر بعد $remMinutes دقيقة.")
                return@launch
            }

            // Normal login success
            val updatedUser = user.copy(
                lastLoginTimestamp = System.currentTimeMillis(),
                stayDurationMinutes = user.stayDurationMinutes + 5 // Simulate stays
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            onResult(true, "مرحباً بك مجدداً ${user.name}!")
        }
    }

    fun register(
        name: String,
        nickname: String,
        phone: String,
        email: String,
        pass: String,
        avatarUrl: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || nickname.isBlank() || phone.isBlank() || pass.isBlank()) {
                onResult(false, "يرجى تعبئة جميع الحقول الإلزامية!")
                return@launch
            }
            val existing = repository.getUserByNickname(nickname)
            if (existing != null) {
                onResult(false, "اللقب مستخدم بالفعل، اختر لقباً آخر.")
                return@launch
            }

            // If it's the first user ever, assign admin role automatically
            val isFirstUser = allUsers.value.isEmpty()
            val role = if (isFirstUser) "admin" else "user"

            val newUser = UserEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                nickname = nickname,
                phone = phone,
                password = pass,
                email = email,
                avatarUrl = avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${Uri.encode(name)}&background=00ff87&color=000&bold=true" },
                role = role,
                status = "active",
                tempBanExpires = 0L,
                subscriptionPlan = null,
                subscriptionExpires = null,
                lastLoginTimestamp = System.currentTimeMillis()
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            onResult(true, "تم إنشاء حسابك بنجاح! مرحباً بك.")
        }
    }

    fun logout() {
        _currentUser.value = null
        pauseFootMusic()
        navigateTo("home")
    }

    // SUBSCRIPTIONS
    fun activateSubscriptionCode(codeText: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "يجب تسجيل الدخول للتفعيل.")
            return
        }
        viewModelScope.launch {
            val allCodesList = db.subscriptionCodeDao().getAllCodesFlow().firstOrNull() ?: emptyList()
            val sCode = allCodesList.find { it.code.trim().equals(codeText.trim(), ignoreCase = true) }
            
            if (sCode == null) {
                onResult(false, "كود الاشتراك غير صحيح أو منتهي.")
                return@launch
            }
            if (sCode.isUsed) {
                onResult(false, "هذا الكود تم استخدامه مسبقاً!")
                return@launch
            }

            val durationMillis = sCode.durationDays.toLong() * 24L * 60L * 60L * 1000L
            val currentExp = user.subscriptionExpires ?: System.currentTimeMillis()
            val newExpires = if (currentExp > System.currentTimeMillis()) {
                currentExp + durationMillis
            } else {
                System.currentTimeMillis() + durationMillis
            }

            val updatedUser = user.copy(
                subscriptionPlan = sCode.planName,
                subscriptionExpires = newExpires
            )
            repository.updateUser(updatedUser)
            repository.useCode(sCode.code, true, user.nickname)
            
            _currentUser.value = updatedUser
            onResult(true, "تم تفعيل باقة (${sCode.planName}) بنجاح!")
        }
    }

    // COMMENTS (WITH PROFANITY FILTER TO "اااااا")
    private val badKeywords = listOf(
        "سيء", "فاشل", "خايب", "بدون فائدة", "كرهت", "زفت", "خرا", "كلب", "حمار",
        "bad", "scam", "trash", "hate", "scoundrel", "stupid", "worst", "suck"
    )

    fun postComment(entityId: String, text: String, onComplete: () -> Unit) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            // Apply bad-word filter
            var filteredText = text
            for (word in badKeywords) {
                val regex = "(?i)$word".toRegex()
                // Replace with "اااااا" (aaaaaa) per user request
                filteredText = filteredText.replace(regex, "اااااا")
            }

            val comment = CommentEntity(
                id = UUID.randomUUID().toString(),
                entityId = entityId,
                username = user.nickname,
                text = filteredText,
                timestamp = System.currentTimeMillis()
            )
            repository.insertComment(comment)
            loadComments(entityId)
            onComplete()
        }
    }

    fun deleteComment(id: String, entityId: String) {
        viewModelScope.launch {
            repository.deleteComment(id)
            loadComments(entityId)
        }
    }

    // FRIENDS SYSTEM
    fun addFriendByUsername(friendNick: String, onResult: (Boolean, String) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "يرجى تسجيل الدخول أولاً.")
            return
        }
        if (user.nickname.equals(friendNick, ignoreCase = true)) {
            onResult(false, "لا يمكنك إضافة نفسك كصديق!")
            return
        }

        viewModelScope.launch {
            val friendUser = repository.getUserByNickname(friendNick)
            if (friendUser == null) {
                onResult(false, "هذا اللقب غير موجود في السيرفر.")
                return@launch
            }

            // Check if already friends
            val currentFriends = friendsList.value
            if (currentFriends.any { it.friendId == friendUser.id }) {
                onResult(false, "هذا العضو موجود بالفعل بقائمتك!")
                return@launch
            }

            val friendEntity = FriendEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                friendId = friendUser.id,
                friendNickname = friendUser.nickname,
                friendName = friendUser.name
            )
            repository.insertFriend(friendEntity)
            onResult(true, "تم إضافة ${friendUser.name} لقائمة أصدقائك بنجاح!")
        }
    }

    fun removeFriend(id: String) {
        viewModelScope.launch {
            repository.deleteFriend(id)
        }
    }

    // ADMIN: MANAGE MATCHES
    fun adminAddMatch(
        team1: String, team2: String, logo1: String, logo2: String,
        streamUrl: String, cover: String, comm: String, desc: String,
        status: String, timeStamp: Long
    ) {
        viewModelScope.launch {
            val match = MatchEntity(
                id = UUID.randomUUID().toString(),
                team1Name = team1,
                team1LogoUrl = logo1.ifBlank { "https://cdn-icons-png.flaticon.com/512/33/33736.png" },
                team2Name = team2,
                team2LogoUrl = logo2.ifBlank { "https://cdn-icons-png.flaticon.com/512/33/33736.png" },
                streamUrl = streamUrl,
                coverUrl = cover.ifBlank { "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=600" },
                commentator = comm,
                description = desc,
                status = status,
                matchTime = timeStamp
            )
            repository.insertMatch(match)
        }
    }

    fun adminDeleteMatch(id: String) {
        viewModelScope.launch {
            repository.deleteMatchById(id)
        }
    }

    // ADMIN: MANAGE MOVIES / SERIES
    fun adminAddMovie(
        title: String, desc: String, cover: String, video: String,
        rating: String, isKids: Boolean, isSeries: Boolean
    ) {
        viewModelScope.launch {
            val movie = MovieEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = desc,
                coverUrl = cover.ifBlank { "https://images.unsplash.com/photo-1626814026160-2237a95fc5a0?w=600" },
                videoUrl = video,
                ageRating = rating,
                isKids = isKids,
                isSeries = isSeries
            )
            repository.insertMovie(movie)
        }
    }

    fun adminAddEpisode(movieId: String, epTitle: String, epNum: Int, videoUrl: String) {
        viewModelScope.launch {
            val ep = EpisodeEntity(
                id = UUID.randomUUID().toString(),
                movieId = movieId,
                episodeTitle = epTitle,
                episodeNumber = epNum,
                videoUrl = videoUrl
            )
            repository.insertEpisode(ep)
        }
    }

    fun adminDeleteMovie(id: String) {
        viewModelScope.launch {
            repository.deleteMovieById(id)
            repository.deleteEpisodesByMovie(id)
        }
    }

    // ADMIN: MANAGE USERS (PROFILE TI)
    fun adminDeleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUserById(userId)
            if (_currentUser.value?.id == userId) {
                logout()
            }
        }
    }

    fun adminWarnUser(userId: String) {
        viewModelScope.launch {
            val u = repository.getUserById(userId) ?: return@launch
            val updated = u.copy(status = "warned")
            repository.updateUser(updated)
            if (_currentUser.value?.id == userId) {
                _currentUser.value = updated
            }
        }
    }

    fun adminBanUserTemp(userId: String, minutes: Int) {
        viewModelScope.launch {
            val u = repository.getUserById(userId) ?: return@launch
            val expiry = System.currentTimeMillis() + (minutes * 60 * 1000L)
            val updated = u.copy(status = "temp_banned", tempBanExpires = expiry)
            repository.updateUser(updated)
            if (_currentUser.value?.id == userId) {
                logout()
            }
        }
    }

    fun adminBanUserPermanent(userId: String) {
        viewModelScope.launch {
            val u = repository.getUserById(userId) ?: return@launch
            val updated = u.copy(status = "banned")
            repository.updateUser(updated)
            if (_currentUser.value?.id == userId) {
                logout()
            }
        }
    }

    fun adminUnbanUser(userId: String) {
        viewModelScope.launch {
            val u = repository.getUserById(userId) ?: return@launch
            val updated = u.copy(status = "active", tempBanExpires = 0L)
            repository.updateUser(updated)
        }
    }

    // ADMIN: MANAGE ACTIVE SUBSCRIPTION CODES
    fun adminGenerateCode(planName: String, days: Int) {
        viewModelScope.launch {
            val codeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val randomString = (1..8)
                .map { codeChars.random() }
                .joinToString("")
            val newCode = SubscriptionCodeEntity(
                code = "BILLK-$randomString",
                planName = planName,
                durationDays = days,
                isUsed = false,
                usedBy = null
            )
            repository.insertCode(newCode)
        }
    }

    fun adminDeleteCode(code: String) {
        viewModelScope.launch {
            repository.deleteCode(code)
        }
    }

    fun adminResetCode(code: String) {
        viewModelScope.launch {
            repository.useCode(code, false, null)
        }
    }

    // GAME SCORE ACTIONS
    fun awardGamePoints(scoredCount: Int) {
        val user = _currentUser.value ?: return
        val earned = scoredCount * 10
        if (earned <= 0) return
        viewModelScope.launch {
            val updated = user.copy(
                points = user.points + earned,
                gamesPlayed = user.gamesPlayed + 1
            )
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    // BACKGROUND FOOTBALL MUSIC PLAYER CONTROLS
    private fun playFootMusic() {
        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    // Public royalty-free sport audio loop URL to play in streaming environment
                    setDataSource(
                        getApplication(),
                        Uri.parse("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                    )
                    isLooping = true
                    prepareAsync()
                    setOnPreparedListener {
                        _isMusicPlaying.value = true
                        if (!_isMusicMuted.value) {
                            setVolume(1.0f, 1.0f)
                        } else {
                            setVolume(0.0f, 0.0f)
                        }
                        start()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            mediaPlayer?.start()
            _isMusicPlaying.value = true
        }
    }

    fun pauseFootMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
        _isMusicPlaying.value = false
    }

    fun toggleFootMusicMute() {
        _isMusicMuted.value = !_isMusicMuted.value
        mediaPlayer?.let {
            if (_isMusicMuted.value) {
                it.setVolume(0f, 0f)
            } else {
                it.setVolume(1f, 1f)
            }
        }
    }

    fun togglePlayPauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isMusicPlaying.value = false
            } else {
                it.start()
                _isMusicPlaying.value = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
    }
}

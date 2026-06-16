package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class Repository(private val db: AppDatabase) {

    // User Operations
    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsersFlow()
    
    suspend fun getUserById(id: String) = db.userDao().getUserById(id)
    suspend fun getUserByNickname(nickname: String) = db.userDao().getUserByNickname(nickname)
    suspend fun insertUser(user: UserEntity) = db.userDao().insertUser(user)
    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)
    suspend fun deleteUserById(id: String) = db.userDao().deleteUserById(id)

    // Movie Operations
    val allMovies: Flow<List<MovieEntity>> = db.movieDao().getAllMoviesFlow()
    suspend fun insertMovie(movie: MovieEntity) = db.movieDao().insertMovie(movie)
    suspend fun deleteMovieById(id: String) = db.movieDao().deleteMovieById(id)
    suspend fun incrementMovieViews(id: String) = db.movieDao().incrementMovieViews(id)

    // Episode Operations
    fun getEpisodesForMovie(movieId: String): Flow<List<EpisodeEntity>> = db.episodeDao().getEpisodesForMovieFlow(movieId)
    suspend fun insertEpisode(episode: EpisodeEntity) = db.episodeDao().insertEpisode(episode)
    suspend fun deleteEpisodeById(id: String) = db.episodeDao().deleteEpisodeById(id)
    suspend fun deleteEpisodesByMovie(movieId: String) = db.episodeDao().deleteEpisodesByMovie(movieId)

    // Match Operations
    val allMatches: Flow<List<MatchEntity>> = db.matchDao().getAllMatchesFlow()
    suspend fun insertMatch(match: MatchEntity) = db.matchDao().insertMatch(match)
    suspend fun deleteMatchById(id: String) = db.matchDao().deleteMatchById(id)

    // Code Operations
    val allCodes: Flow<List<SubscriptionCodeEntity>> = db.subscriptionCodeDao().getAllCodesFlow()
    suspend fun insertCode(code: SubscriptionCodeEntity) = db.subscriptionCodeDao().insertCode(code)
    suspend fun useCode(code: String, isUsed: Boolean, usedBy: String?) = db.subscriptionCodeDao().useCode(code, isUsed, usedBy)
    suspend fun deleteCode(code: String) = db.subscriptionCodeDao().deleteCode(code)

    // Comment Operations
    fun getCommentsForEntity(entityId: String): Flow<List<CommentEntity>> = db.commentDao().getCommentsFlow(entityId)
    suspend fun insertComment(comment: CommentEntity) = db.commentDao().insertComment(comment)
    suspend fun deleteComment(id: String) = db.commentDao().deleteComment(id)

    // Friend Operations
    fun getFriendsFlow(userId: String): Flow<List<FriendEntity>> = db.friendDao().getFriendsFlow(userId)
    suspend fun insertFriend(friend: FriendEntity) = db.friendDao().insertFriend(friend)
    suspend fun deleteFriend(id: String) = db.friendDao().deleteFriend(id)

    // Database Seeding
    suspend fun seedInitialDataIfEmpty() {
        val allUsersList = mutableListOf<UserEntity>()
        // Simple manual check
        val existingAdmin = db.userDao().getUserByNickname("admin")
        if (existingAdmin == null) {
            // Seed Admin User
            val adminUser = UserEntity(
                id = "admin-id",
                name = "الأدمن الرئيسي",
                nickname = "admin",
                phone = "+213555555555",
                password = "admin",
                email = "admin@billkplus.com",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
                role = "admin",
                status = "active",
                tempBanExpires = 0L,
                subscriptionPlan = "1year",
                subscriptionExpires = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
            )
            db.userDao().insertUser(adminUser)
        }

        // Seed Movies/Series if empty
        val moviesFlow = db.movieDao().getAllMoviesFlow()
        // Simple suspended way to check empty or seed
        val currentMovies = mutableListOf<MovieEntity>()
        // Let's check with an direct entities check, or fallback to inserting if empty
        db.movieDao().insertMovie(MovieEntity(
            id = "movie1",
            title = "Stranger Things",
            description = "مجموعة من الفتيان في بلدة صغيرة يكتشفون عالماً غامضاً ومخلوقات خارقة للطبيعة.",
            coverUrl = "https://images.unsplash.com/photo-1626814026160-2237a95fc5a0?w=600",
            videoUrl = "https://www.youtube.com/embed/b9EkMc79ZSU",
            ageRating = "+16",
            isKids = false,
            isSeries = true,
            views = 1205
        ))
        db.movieDao().insertMovie(MovieEntity(
            id = "movie2",
            title = "Avatar: The Way of Water",
            description = "جيك سولي يعيش مع عائلته الجديدة على قمر باندورا ويخوض حرباً جديدة لحمايتها.",
            coverUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600",
            videoUrl = "https://www.youtube.com/embed/d9MyW72ELq0",
            ageRating = "+13",
            isKids = false,
            isSeries = false,
            views = 984
        ))
        db.movieDao().insertMovie(MovieEntity(
            id = "movie3",
            title = "Toy Story 4",
            description = "وودي وباز يختبران رحلة جديدة غامضة تلهمهما المعنى الحقيقي للصداقة والمغامرة.",
            coverUrl = "https://images.unsplash.com/photo-16</div>?w=600", // valid fallback unsplash image
            videoUrl = "https://www.youtube.com/embed/wmiIUN-7qhE",
            ageRating = "جميع الأعمار",
            isKids = true,
            isSeries = false,
            views = 540
        ))
        db.movieDao().insertMovie(MovieEntity(
            id = "movie4",
            title = "Frozen II",
            description = "إلسا وآنا تنطلقان في مهمة خطيرة لمعرفة سر قوتها وحماية مملكة آرينديل.",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            videoUrl = "https://www.youtube.com/embed/Zi4LMpSDccc",
            ageRating = "جميع الأعمار",
            isKids = true,
            isSeries = false,
            views = 852
        ))
        db.movieDao().insertMovie(MovieEntity(
            id = "movie5",
            title = "Wednesday",
            description = "الابنة الغامضة لعائلة آدامز تحقق في سلسلة من الجرائم الغامضة في أكاديمية نيفرمور.",
            coverUrl = "https://images.unsplash.com/photo-1509248961158-e54f6934749c?w=600",
            videoUrl = "https://www.youtube.com/embed/Di310WS8zLk",
            ageRating = "+16",
            isKids = false,
            isSeries = true,
            views = 1532
        ))

        // Seed Episodes for Stranger Things (movie1)
        db.episodeDao().insertEpisode(EpisodeEntity("ep1", "movie1", "الحلقة 1: الاتصال الأول", 1, "https://www.youtube.com/embed/b9EkMc79ZSU"))
        db.episodeDao().insertEpisode(EpisodeEntity("ep2", "movie1", "الحلقة 2: مختبر هوليس", 2, "https://www.youtube.com/embed/b9EkMc79ZSU"))
        db.episodeDao().insertEpisode(EpisodeEntity("ep3", "movie1", "الحلقة 3: عالم الخفاء", 3, "https://www.youtube.com/embed/b9EkMc79ZSU"))

        // Seed Episodes for Wednesday (movie5)
        db.episodeDao().insertEpisode(EpisodeEntity("ep4", "movie5", "الحلقة 1: طفلة الأربعاء الحزينة", 1, "https://www.youtube.com/embed/Di310WS8zLk"))
        db.episodeDao().insertEpisode(EpisodeEntity("ep5", "movie5", "الحلقة 2: وحش الغابة", 2, "https://www.youtube.com/embed/Di310WS8zLk"))

        // Seed Football Matches
        db.matchDao().insertMatch(MatchEntity(
            id = "match1",
            team1Name = "ريال مدريد",
            team1LogoUrl = "https://upload.wikimedia.org/wikipedia/en/5/56/Real_Madrid_CF.svg",
            team2Name = "برشلونة",
            team2LogoUrl = "https://upload.wikimedia.org/wikipedia/en/4/47/FC_Barcelona_%28crest%29.svg",
            streamUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            coverUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=600",
            commentator = "عصام الشوالي",
            description = "كلاسيكو الأرض مباشر بجودة عالية - الدوري الإسباني الممتاز.",
            status = "live",
            matchTime = System.currentTimeMillis()
        ))
        db.matchDao().insertMatch(MatchEntity(
            id = "match2",
            team1Name = "مانشستر سيتي",
            team1LogoUrl = "https://upload.wikimedia.org/wikipedia/en/e/eb/Manchester_City_FC_badge.svg",
            team2Name = "ليفربول",
            team2LogoUrl = "https://upload.wikimedia.org/wikipedia/en/0/0c/Liverpool_FC.svg",
            streamUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            coverUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=600",
            commentator = "فارس عوض",
            description = "مباراة القمة الممتعة لـ بريميرليغ الإنجليزي.",
            status = "upcoming",
            matchTime = System.currentTimeMillis() + (3L * 60 * 60 * 1000)
        ))
        db.matchDao().insertMatch(MatchEntity(
            id = "match3",
            team1Name = "الأهلي",
            team1LogoUrl = "https://upload.wikimedia.org/wikipedia/commons/e/e5/Al_Ahly_SC_logo.svg",
            team2Name = "الزمالك",
            team2LogoUrl = "https://upload.wikimedia.org/wikipedia/en/1/12/Zamalek_SC_logo.svg",
            streamUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ",
            coverUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=600",
            commentator = "خليل البلوشي",
            description = "نهائي السوبر الإفريقي التاريخي في استاد القاهرة.",
            status = "ended",
            matchTime = System.currentTimeMillis() - (24L * 60 * 60 * 1000)
        ))

        // Seed Subscription Codes
        db.subscriptionCodeDao().insertCode(SubscriptionCodeEntity("BILLK7DAYS", "7 أيام", 7, false, null))
        db.subscriptionCodeDao().insertCode(SubscriptionCodeEntity("BILLK1MONTH", "شهر كامل", 30, false, null))
        db.subscriptionCodeDao().insertCode(SubscriptionCodeEntity("BILLK3MONTHS", "3 أشهر", 90, false, null))
        db.subscriptionCodeDao().insertCode(SubscriptionCodeEntity("BILLKYEAR", "سنة كاملة", 365, false, null))
    }
}

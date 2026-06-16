package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE nickname = :nickname LIMIT 1")
    suspend fun getUserByNickname(nickname: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)
}

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies")
    fun getAllMoviesFlow(): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: String)

    @Query("UPDATE movies SET views = views + 1 WHERE id = :id")
    suspend fun incrementMovieViews(id: String)
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE movieId = :movieId ORDER BY episodeNumber ASC")
    fun getEpisodesForMovieFlow(movieId: String): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE id = :id")
    suspend fun deleteEpisodeById(id: String)

    @Query("DELETE FROM episodes WHERE movieId = :movieId")
    suspend fun deleteEpisodesByMovie(movieId: String)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY matchTime DESC")
    fun getAllMatchesFlow(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE id = :id")
    suspend fun deleteMatchById(id: String)
}

@Dao
interface SubscriptionCodeDao {
    @Query("SELECT * FROM subscription_codes")
    fun getAllCodesFlow(): Flow<List<SubscriptionCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: SubscriptionCodeEntity)

    @Query("UPDATE subscription_codes SET isUsed = :isUsed, usedBy = :usedBy WHERE code = :code")
    suspend fun useCode(code: String, isUsed: Boolean, usedBy: String?)

    @Query("DELETE FROM subscription_codes WHERE code = :code")
    suspend fun deleteCode(code: String)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE entityId = :entityId ORDER BY timestamp DESC")
    fun getCommentsFlow(entityId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteComment(id: String)
}

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends WHERE userId = :userId")
    fun getFriendsFlow(userId: String): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteFriend(id: String)
}

@Database(
    entities = [
        UserEntity::class,
        MovieEntity::class,
        EpisodeEntity::class,
        MatchEntity::class,
        SubscriptionCodeEntity::class,
        CommentEntity::class,
        FriendEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun movieDao(): MovieDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun matchDao(): MatchDao
    abstract fun subscriptionCodeDao(): SubscriptionCodeDao
    abstract fun commentDao(): CommentDao
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "billk_plus_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nickname: String,
    val phone: String,
    val password: String,
    val email: String,
    val avatarUrl: String,
    val role: String, // "admin" or "user"
    val status: String, // "active", "banned", "warned"
    val tempBanExpires: Long, // timestamp, 0 if not temporarily banned
    val subscriptionPlan: String?,
    val subscriptionExpires: Long?,
    val points: Int = 0,
    val gamesPlayed: Int = 0,
    val stayDurationMinutes: Int = 0,
    val lastLoginTimestamp: Long = 0
)

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val videoUrl: String,
    val ageRating: String,
    val isKids: Boolean, // true for BILLK KIDS, false for BILLK FILM
    val isSeries: Boolean,
    val views: Int = 0
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val id: String,
    val movieId: String,
    val episodeTitle: String,
    val episodeNumber: Int,
    val videoUrl: String
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val team1Name: String,
    val team1LogoUrl: String,
    val team2Name: String,
    val team2LogoUrl: String,
    val streamUrl: String,
    val coverUrl: String,
    val commentator: String,
    val description: String,
    val status: String, // "live", "upcoming", "ended"
    val matchTime: Long
)

@Entity(tableName = "subscription_codes")
data class SubscriptionCodeEntity(
    @PrimaryKey val code: String,
    val planName: String,
    val durationDays: Int,
    val isUsed: Boolean,
    val usedBy: String?
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val entityId: String, // Movie ID or "community"
    val username: String,
    val text: String,
    val timestamp: Long
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val friendId: String,
    val friendNickname: String,
    val friendName: String
)

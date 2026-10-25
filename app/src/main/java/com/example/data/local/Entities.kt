package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val emailOrPhone: String,
    val dob: String?,
    val gender: String?,
    val profilePic: String?,
    val coverPic: String?,
    val isVerified: Boolean,
    val bio: String?,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val joinedDate: String,
    val authToken: String? = null,
    val isLoggedIn: Boolean = true
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val pageId: String? = null,
    val authorName: String,
    val authorAvatar: String,
    val isVerified: Boolean,
    val timestamp: String,
    val content: String,
    val backgroundStyle: String?,
    val imageUrl: String?,
    val videoUrl: String? = null,
    val isVideo: Boolean = false,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean,
    val userReaction: String? = null,
    val privacy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val senderId: String = "",
    val senderName: String,
    val senderAvatar: String,
    val isVerified: Boolean,
    val lastMessage: String,
    val timestamp: String,
    val isVoiceMessage: Boolean,
    val isMissedCall: Boolean,
    val unreadCount: Int
)

@Entity(tableName = "view_history")
data class ViewHistoryEntity(
    @PrimaryKey val id: String,
    val itemType: String, // "IMAGE", "TEXT", "VIDEO"
    val titleOrContent: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val isVerified: Boolean = false,
    val mediaUrl: String? = null,
    val timestampText: String = "Just now",
    val viewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey val id: String,
    val itemType: String, // "IMAGE", "VIDEO", "PRODUCT"
    val title: String,
    val subtitleOrPrice: String,
    val mediaUrl: String? = null,
    val authorOrSellerName: String,
    val authorOrSellerAvatar: String? = null,
    val isVerified: Boolean = false,
    val originalId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val bio: String,
    val logoUrl: String? = null,
    val bannerUrl: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val followersCount: Int = 0,
    val likesCount: Int = 0,
    val isVerified: Boolean = false,
    val createdAt: String = "Just now"
)

fun PageEntity.toModel(): com.example.data.model.FlikataPage = com.example.data.model.FlikataPage(
    id = id,
    userId = userId,
    name = name,
    category = category,
    bio = bio,
    logoUrl = logoUrl,
    bannerUrl = bannerUrl,
    email = email,
    phone = phone,
    website = website,
    followersCount = followersCount,
    likesCount = likesCount,
    isVerified = isVerified,
    createdAt = createdAt
)

fun com.example.data.model.FlikataPage.toEntity(): PageEntity = PageEntity(
    id = id,
    userId = userId,
    name = name,
    category = category,
    bio = bio,
    logoUrl = logoUrl,
    bannerUrl = bannerUrl,
    email = email,
    phone = phone,
    website = website,
    followersCount = followersCount,
    likesCount = likesCount,
    isVerified = isVerified,
    createdAt = createdAt
)

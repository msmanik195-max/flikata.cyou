package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "email_or_phone") val emailOrPhone: String,
    val dob: String? = null,
    val gender: String? = null,
    @Json(name = "profile_pic") val profilePic: String? = null,
    @Json(name = "cover_pic") val coverPic: String? = null,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    val bio: String? = "Drop a thought...",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val joinedDate: String = "Joined September 2026",
    @Json(name = "auth_token") val authToken: String? = null,
    @Json(name = "is_online") val isOnline: Boolean = true
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

@JsonClass(generateAdapter = true)
data class Reaction(
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val emoji: String // "👍", "❤️", "😆", "😮", "😢", "😡"
)

@JsonClass(generateAdapter = true)
data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val isVerified: Boolean = false,
    val text: String = "",
    val timestamp: String = "",
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<Comment> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CommentsListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<Comment>? = null
)

@JsonClass(generateAdapter = true)
data class Post(
    val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "page_id") val pageId: String? = null,
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "author_avatar") val authorAvatar: String = "",
    @Json(name = "is_verified") val isVerified: Boolean = false,
    val timestamp: String = "",
    val content: String = "",
    @Json(name = "background_style") val backgroundStyle: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "video_url") val videoUrl: String? = null,
    @Json(name = "is_video") val isVideo: Boolean = false,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "comments_count") val commentsCount: Int = 0,
    @Json(name = "shares_count") val sharesCount: Int = 0,
    @Json(name = "views_count") val viewsCount: Int = 0,
    @Json(name = "is_liked") val isLiked: Boolean = false,
    val userReaction: String? = null,
    val reactions: List<Reaction> = emptyList(),
    val commentsList: List<Comment> = emptyList(),
    val privacy: String = "Public"
)

@JsonClass(generateAdapter = true)
data class Story(
    val id: String = "",
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "author_avatar") val authorAvatar: String = "",
    @Json(name = "story_image") val storyImage: String = "",
    @Json(name = "is_user_story") val isUserStory: Boolean = false,
    @Json(name = "text_content") val textContent: String? = null,
    @Json(name = "background_color") val backgroundColor: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String,
    @Json(name = "threadId") val threadIdRaw: String? = null,
    @Json(name = "thread_id") val threadIdAlt: String? = null,
    @Json(name = "senderId") val senderIdRaw: String? = null,
    @Json(name = "sender_id") val senderIdAlt: String? = null,
    @Json(name = "senderName") val senderNameRaw: String? = null,
    @Json(name = "sender_name") val senderNameAlt: String? = null,
    @Json(name = "senderAvatar") val senderAvatarRaw: String? = null,
    @Json(name = "sender_avatar") val senderAvatarAlt: String? = null,
    @Json(name = "is_verified") val isVerifiedAlt: Boolean = false,
    @Json(name = "isVerified") val isVerifiedRaw: Boolean = false,
    val message: String = "",
    val timestamp: String = "",
    @Json(name = "is_voice_message") val isVoiceMessage: Boolean = false,
    @Json(name = "is_missed_call") val isMissedCall: Boolean = false,
    @Json(name = "unread_count") val unreadCountAlt: Int = 0,
    @Json(name = "unreadCount") val unreadCountRaw: Int = 0,
    @Json(name = "is_online") val isOnlineAlt: Boolean? = null,
    @Json(name = "isOnline") val isOnlineRaw: Boolean? = null
) {
    val threadId: String get() = threadIdRaw ?: threadIdAlt ?: id
    val senderId: String get() = senderIdRaw ?: senderIdAlt ?: id
    val senderName: String get() = senderNameRaw ?: senderNameAlt ?: "User"
    val senderAvatar: String get() = senderAvatarRaw ?: senderAvatarAlt ?: ""
    val isVerified: Boolean get() = isVerifiedRaw || isVerifiedAlt
    val unreadCount: Int get() = if (unreadCountRaw > 0) unreadCountRaw else unreadCountAlt
    val isOnline: Boolean get() = isOnlineRaw == true || isOnlineAlt == true
}

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val status: String, // "success" or "error"
    val message: String? = null,
    val data: T? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val status: String,
    val message: String? = null,
    val user: User? = null,
    val token: String? = null
)

@JsonClass(generateAdapter = true)
data class FlikataNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general", // "post", "like", "friend", "system", "general", "announcement", "alert"
    val timestamp: String = "Just now",
    val isRead: Boolean = false,
    val avatarUrl: String? = null,
    val postThumbnail: String? = null
)

@JsonClass(generateAdapter = true)
data class NotificationListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<FlikataNotification>? = null
)

data class PostUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0 (0% to 100%)
    val postSnippet: String = "",
    val postImage: String? = null,
    val postVideo: String? = null,
    val isVideo: Boolean = false,
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ReelCommentItem(
    val id: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val text: String = "",
    val timestamp: String = "",
    var isLiked: Boolean = false,
    val reactionEmoji: String? = null,
    val isVerified: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ReelModel(
    val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val isVerified: Boolean = false,
    val mediaUrl: String = "",
    val caption: String = "",
    val audioTitle: String = "Original Audio",
    var likesCount: Int = 0,
    var commentsCount: Int = 0,
    var sharesCount: Int = 0,
    var viewsCount: String = "0",
    var isLiked: Boolean = false,
    var userReaction: String? = null,
    var isFollowing: Boolean = false,
    val comments: MutableList<ReelCommentItem> = mutableListOf()
)

@JsonClass(generateAdapter = true)
data class ReelSettingsData(
    @Json(name = "max_duration_seconds") val maxDurationSeconds: Int = 60
)

@JsonClass(generateAdapter = true)
data class UploadData(
    val url: String = "",
    @Json(name = "bucket_id") val bucketId: Int? = null,
    @Json(name = "bucket_name") val bucketName: String? = null,
    val size: Long? = null,
    val mime: String? = null
)

@JsonClass(generateAdapter = true)
data class UploadResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UploadData? = null
)

@JsonClass(generateAdapter = true)
data class GenericResponse(
    val success: Boolean = false,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class RecordViewData(
    @Json(name = "views_count") val viewsCount: Int = 0,
    val counted: Boolean = false,
    val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class RecordViewResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: RecordViewData? = null
)

@JsonClass(generateAdapter = true)
data class CommentResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: Comment? = null
)

@JsonClass(generateAdapter = true)
data class StoriesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<Story>? = null
)

@JsonClass(generateAdapter = true)
data class ReelsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ReelModel>? = null
)

@JsonClass(generateAdapter = true)
data class ReelSettingsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ReelSettingsData? = null
)

@JsonClass(generateAdapter = true)
data class PresignedUploadData(
    @Json(name = "upload_url") val uploadUrl: String = "",
    @Json(name = "file_url") val fileUrl: String = "",
    @Json(name = "bucket_id") val bucketId: Int? = null,
    val key: String? = null
)

@JsonClass(generateAdapter = true)
data class PresignedUploadResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: PresignedUploadData? = null
)

@JsonClass(generateAdapter = true)
data class ServerMessageItem(
    val id: String = "",
    val text: String = "",
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "videoUrl") val videoUrl: String? = null,
    val isMe: Boolean = false,
    val time: String = "Just now",
    val isEdited: Boolean = false,
    val isCallNotice: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ServerMessagesData(
    @Json(name = "conversation_id") val conversationId: Int? = null,
    val messages: List<ServerMessageItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ServerMessagesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ServerMessagesData? = null
)

@JsonClass(generateAdapter = true)
data class UserProfileData(
    val id: Int? = null,
    @Json(name = "full_name") val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "profile_pic") val profilePic: String? = null,
    @Json(name = "cover_pic") val coverPic: String? = null,
    val bio: String? = null,
    val occupation: String? = null,
    val location: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    @Json(name = "is_verified") val isVerified: Int? = null,
    @Json(name = "is_online") val isOnline: Int? = null,
    @Json(name = "wallet_balance") val walletBalance: Double? = 0.0,
    @Json(name = "verified_until") val verifiedUntil: String? = null,
    @Json(name = "posts_count") val postsCount: Int? = 0,
    @Json(name = "photos_count") val photosCount: Int? = 0,
    @Json(name = "reels_count") val reelsCount: Int? = 0,
    @Json(name = "followers_count") val followersCount: Int? = 0,
    @Json(name = "following_count") val followingCount: Int? = 0,
    @Json(name = "is_following") val isFollowing: Boolean? = false,
    @Json(name = "about_settings") val aboutSettings: Map<String, Boolean>? = null,
    val joinedDate: String? = null
) {
    val idString: String get() = id?.toString() ?: ""
    val isVerifiedBool: Boolean get() = isVerified == 1
    val isOnlineBool: Boolean get() = isOnline == 1
    val walletBalanceDouble: Double get() = walletBalance ?: 0.0
    val totalFollowers: Int get() = followersCount ?: 0
    val totalFollowing: Int get() = followingCount ?: 0
    val isFollowedByMe: Boolean get() = isFollowing == true
}

@JsonClass(generateAdapter = true)
data class UserProfileResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UserProfileData? = null
)

@JsonClass(generateAdapter = true)
data class ToggleFollowData(
    @Json(name = "is_following") val isFollowing: Boolean = false,
    @Json(name = "followers_count") val followersCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class ToggleFollowResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ToggleFollowData? = null
)

@JsonClass(generateAdapter = true)
data class InitiateCallResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: InitiateCallData? = null
)

@JsonClass(generateAdapter = true)
data class InitiateCallData(
    @Json(name = "call_id") val callId: Int = 0,
    @Json(name = "channel_name") val channelName: String = "",
    @Json(name = "caller_id") val callerId: Int = 0,
    @Json(name = "receiver_id") val receiverId: Int = 0,
    @Json(name = "caller_name") val callerName: String = "",
    @Json(name = "caller_avatar") val callerAvatar: String = "",
    @Json(name = "caller_verified") val callerVerified: Boolean = false,
    val status: String = "ringing"
)

@JsonClass(generateAdapter = true)
data class CheckIncomingCallResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: IncomingCallData? = null
)

@JsonClass(generateAdapter = true)
data class IncomingCallData(
    @Json(name = "has_call") val hasCall: Boolean = false,
    @Json(name = "call_id") val callId: Int? = null,
    @Json(name = "channel_name") val channelName: String? = null,
    @Json(name = "caller_id") val callerId: Int? = null,
    @Json(name = "receiver_id") val receiverId: Int? = null,
    @Json(name = "caller_name") val callerName: String? = null,
    @Json(name = "caller_avatar") val callerAvatar: String? = null,
    @Json(name = "caller_verified") val callerVerified: Boolean? = null,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class CallStatusResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: CallStatusData? = null
)

@JsonClass(generateAdapter = true)
data class CallStatusData(
    @Json(name = "call_id") val callId: Int = 0,
    val status: String = "ringing",
    @Json(name = "channel_name") val channelName: String = "",
    @Json(name = "duration_seconds") val durationSeconds: Int = 0
)

// --- Verification Badge Models ---
@JsonClass(generateAdapter = true)
data class VerificationPackage(
    val id: Int = 0,
    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    @Json(name = "duration_type") val durationType: String = "month",
    @Json(name = "duration_value") val durationValue: Int = 1,
    @Json(name = "is_active") val isActive: Int = 1
) {
    val durationText: String get() {
        return when (durationType.lowercase()) {
            "lifetime" -> "Lifetime"
            "day" -> if (durationValue == 1) "1 Day" else "$durationValue Days"
            "month" -> if (durationValue == 1) "1 Month" else "$durationValue Months"
            "year" -> if (durationValue == 1) "1 Year" else "$durationValue Years"
            else -> "$durationValue $durationType"
        }
    }
}

@JsonClass(generateAdapter = true)
data class VerificationSettings(
    @Json(name = "id_card_required") val idCardRequired: Boolean = false,
    @Json(name = "selfie_required") val selfieRequired: Boolean = false,
    @Json(name = "grace_period_days") val gracePeriodDays: Int = 3
)

@JsonClass(generateAdapter = true)
data class VerificationUserInfo(
    val id: Int = 0,
    @Json(name = "full_name") val fullName: String = "",
    @Json(name = "profile_pic") val profilePic: String? = null,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    @Json(name = "verified_until") val verifiedUntil: String? = null,
    @Json(name = "is_lifetime") val isLifetime: Boolean = false,
    @Json(name = "remaining_seconds") val remainingSeconds: Long = 0,
    @Json(name = "is_grace_active") val isGraceActive: Boolean = false,
    @Json(name = "wallet_balance") val walletBalance: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class VerificationRequestItem(
    val id: Int = 0,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "user_avatar") val userAvatar: String? = null,
    @Json(name = "user_email") val userEmail: String? = null,
    @Json(name = "user_phone") val userPhone: String? = null,
    @Json(name = "package_id") val packageId: Int? = null,
    @Json(name = "package_title") val packageTitle: String? = null,
    val amount: Double? = 0.0,
    @Json(name = "id_front_url") val idFrontUrl: String? = null,
    @Json(name = "id_back_url") val idBackUrl: String? = null,
    @Json(name = "selfie_url") val selfieUrl: String? = null,
    val status: String = "pending",
    @Json(name = "rejection_reason") val rejectionReason: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "duration_type") val durationType: String? = null,
    @Json(name = "duration_value") val durationValue: Int? = null
)

@JsonClass(generateAdapter = true)
data class VerificationPackagesData(
    val settings: VerificationSettings? = null,
    val packages: List<VerificationPackage> = emptyList(),
    val user: VerificationUserInfo? = null,
    @Json(name = "pending_request") val pendingRequest: VerificationRequestItem? = null
)

@JsonClass(generateAdapter = true)
data class VerificationPackagesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: VerificationPackagesData? = null
)

@JsonClass(generateAdapter = true)
data class AdminPackagesData(
    val settings: VerificationSettings? = null,
    val packages: List<VerificationPackage> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AdminPackagesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminPackagesData? = null
)

@JsonClass(generateAdapter = true)
data class RequestCounts(
    val pending: Int = 0,
    val approved: Int = 0,
    val rejected: Int = 0
)

@JsonClass(generateAdapter = true)
data class AdminVerificationRequestsData(
    val counts: RequestCounts? = null,
    val requests: List<VerificationRequestItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AdminVerificationRequestsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminVerificationRequestsData? = null
)

// --- Wallet & Deposit Models ---
@JsonClass(generateAdapter = true)
data class DepositMethod(
    val id: Int = 0,
    val name: String = "",
    val number: String = "",
    @Json(name = "image_url") val imageUrl: String? = null,
    val description: String? = null,
    @Json(name = "is_active") val isActive: Int = 1
)

@JsonClass(generateAdapter = true)
data class WalletTransactionItem(
    val id: Int = 0,
    @Json(name = "user_id") val userId: Int = 0,
    val type: String = "deposit",
    val amount: Double = 0.0,
    val description: String = "",
    val status: String = "completed",
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class DepositRequestItem(
    val id: Int = 0,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "user_avatar") val userAvatar: String? = null,
    @Json(name = "user_email") val userEmail: String? = null,
    @Json(name = "user_phone") val userPhone: String? = null,
    @Json(name = "method_id") val methodId: Int? = null,
    @Json(name = "method_name") val methodName: String = "",
    val amount: Double = 0.0,
    @Json(name = "transaction_id") val transactionId: String = "",
    val status: String = "pending",
    @Json(name = "rejection_reason") val rejectionReason: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class WalletData(
    @Json(name = "wallet_balance") val walletBalance: Double = 0.0,
    @Json(name = "account_id") val accountId: String = "",
    @Json(name = "user_name") val userName: String = "",
    val methods: List<DepositMethod> = emptyList(),
    @Json(name = "withdraw_methods") val withdrawMethods: List<WithdrawMethod> = emptyList(),
    val transactions: List<WalletTransactionItem> = emptyList(),
    @Json(name = "deposit_requests") val depositRequests: List<DepositRequestItem> = emptyList(),
    @Json(name = "withdraw_requests") val withdrawRequests: List<WithdrawRequestItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WalletResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: WalletData? = null
)

@JsonClass(generateAdapter = true)
data class DepositMethodsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<DepositMethod>? = null
)

@JsonClass(generateAdapter = true)
data class AdminDepositRequestsData(
    val counts: RequestCounts? = null,
    val requests: List<DepositRequestItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AdminDepositRequestsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminDepositRequestsData? = null
)

@JsonClass(generateAdapter = true)
data class WithdrawMethod(
    val id: Int = 0,
    val name: String = "",
    val number: String = "",
    @Json(name = "image_url") val imageUrl: String? = null,
    val description: String? = null,
    @Json(name = "min_amount") val minAmount: Double = 50.0,
    @Json(name = "max_amount") val maxAmount: Double = 25000.0,
    @Json(name = "is_active") val isActive: Int = 1
)

@JsonClass(generateAdapter = true)
data class WithdrawRequestItem(
    val id: Int = 0,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "user_name") val userName: String? = null,
    @Json(name = "user_avatar") val userAvatar: String? = null,
    @Json(name = "user_email") val userEmail: String? = null,
    @Json(name = "user_phone") val userPhone: String? = null,
    @Json(name = "method_id") val methodId: Int? = null,
    @Json(name = "method_name") val methodName: String = "",
    val amount: Double = 0.0,
    @Json(name = "account_name") val accountName: String = "",
    @Json(name = "account_number") val accountNumber: String = "",
    val status: String = "pending",
    @Json(name = "rejection_reason") val rejectionReason: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class WithdrawMethodsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<WithdrawMethod>? = null
)

@JsonClass(generateAdapter = true)
data class AdminWithdrawRequestsData(
    val counts: RequestCounts? = null,
    val requests: List<WithdrawRequestItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AdminWithdrawRequestsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AdminWithdrawRequestsData? = null
)

@JsonClass(generateAdapter = true)
data class DashboardPerformance(
    @Json(name = "totalViews") val totalViews: Int = 0,
    @Json(name = "engagement") val engagement: Int = 0,
    @Json(name = "newFollowers") val newFollowers: Int = 0,
    @Json(name = "totalPosts") val totalPosts: Int = 0,
    @Json(name = "totalReels") val totalReels: Int = 0
)

@JsonClass(generateAdapter = true)
data class CriterionDetail(
    val current: Int = 0,
    val required: Int = 0,
    val isMet: Boolean = false,
    val title: String = ""
)

@JsonClass(generateAdapter = true)
data class MonetizationCriteria(
    val followers: CriterionDetail = CriterionDetail(),
    val posts: CriterionDetail = CriterionDetail(),
    val reels: CriterionDetail = CriterionDetail(),
    @Json(name = "accountAge") val accountAge: CriterionDetail = CriterionDetail()
)

@JsonClass(generateAdapter = true)
data class LastMonetizationApplication(
    val id: String = "",
    val status: String = "pending",
    @Json(name = "rejectionReason") val rejectionReason: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class DashboardMonetization(
    @Json(name = "revenueShareText") val revenueShareText: String = "Earn 55% revenue share from ad views on your content",
    @Json(name = "revenueSharePercent") val revenueSharePercent: Int = 55,
    @Json(name = "isMonetized") val isMonetized: Boolean = false,
    @Json(name = "applicationStatus") val applicationStatus: String = "none",
    @Json(name = "allCriteriaMet") val allCriteriaMet: Boolean = false,
    @Json(name = "fulfilledCount") val fulfilledCount: Int = 0,
    @Json(name = "totalCriteria") val totalCriteria: Int = 4,
    val criteria: MonetizationCriteria = MonetizationCriteria(),
    val lastApplication: LastMonetizationApplication? = null
)

@JsonClass(generateAdapter = true)
data class DashboardContentItem(
    val id: String = "",
    val type: String = "post",
    val content: String = "",
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null,
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class DashboardData(
    val period: String = "7d",
    val performance: DashboardPerformance = DashboardPerformance(),
    val monetization: DashboardMonetization = DashboardMonetization(),
    val recentContent: List<DashboardContentItem> = emptyList(),
    val allContent: List<DashboardContentItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DashboardResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: DashboardData? = null
)

@JsonClass(generateAdapter = true)
data class SubmitMonetizationData(
    @Json(name = "application_id") val applicationId: String = "",
    @Json(name = "user_name") val userName: String = "",
    @Json(name = "account_age_days") val accountAgeDays: Int = 0,
    @Json(name = "followers_count") val followersCount: Int = 0,
    @Json(name = "posts_count") val postsCount: Int = 0,
    @Json(name = "reels_count") val reelsCount: Int = 0,
    @Json(name = "submission_time") val submissionTime: String = "",
    val status: String = "pending"
)

@JsonClass(generateAdapter = true)
data class SubmitMonetizationResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: SubmitMonetizationData? = null
)

@JsonClass(generateAdapter = true)
data class MarketplaceProduct(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val formattedPrice: String = "৳ 0",
    val currency: String = "BDT",
    val category: String = "General",
    val condition: String = "New",
    val location: String = "Dhaka, Bangladesh",
    val description: String = "",
    val imageUrl: String? = null,
    val isSold: Boolean = false,
    val viewsCount: Int = 0,
    val createdAt: String = "",
    val sellerName: String = "Seller",
    val sellerAvatar: String? = null,
    val sellerVerified: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MarketplaceListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<MarketplaceProduct> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MarketplaceCreateResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: MarketplaceProduct? = null
)

@JsonClass(generateAdapter = true)
data class FlikataPage(
    val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    val name: String = "",
    val category: String = "Creator",
    val bio: String = "",
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "banner_url") val bannerUrl: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val website: String? = null,
    @Json(name = "followers_count") val followersCount: Int = 0,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    @Json(name = "created_at") val createdAt: String = "Just now"
)

@JsonClass(generateAdapter = true)
data class PagesListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<FlikataPage> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PageCreateResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: FlikataPage? = null
)

@JsonClass(generateAdapter = true)
data class PageSettingsResponse(
    val success: Boolean = false,
    @Json(name = "max_pages_per_user") val maxPagesPerUser: Int = 5,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class LeaderboardUser(
    val id: String = "",
    val name: String = "",
    val username: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    val rank: Int = 1,
    @Json(name = "total_likes") val totalLikes: Int = 0,
    @Json(name = "total_comments") val totalComments: Int = 0,
    @Json(name = "total_followers") val totalFollowers: Int = 0,
    @Json(name = "engagement_score") val engagementScore: Int = 0
)

@JsonClass(generateAdapter = true)
data class LeaderboardResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<LeaderboardUser> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LeaderboardSettingsResponse(
    val success: Boolean = false,
    @Json(name = "user_limit") val userLimit: Int = 20,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class AppUpdateResponse(
    val success: Boolean = false,
    @Json(name = "update_available") val updateAvailable: Boolean = false,
    @Json(name = "is_force") val isForce: Boolean = false,
    @Json(name = "latest_version_code") val latestVersionCode: Int = 0,
    @Json(name = "latest_version_name") val latestVersionName: String = "",
    @Json(name = "apk_url") val apkUrl: String = "",
    @Json(name = "apk_size_mb") val apkSizeMb: String = "",
    @Json(name = "release_notes") val releaseNotes: String = "",
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class FeatureSettingsResponse(
    val success: Boolean = true,
    @Json(name = "allow_page_creation") val allowPageCreation: Boolean = true,
    @Json(name = "allow_login") val allowLogin: Boolean = true,
    @Json(name = "allow_registration") val allowRegistration: Boolean = true,
    @Json(name = "allow_chat") val allowChat: Boolean = true,
    @Json(name = "allow_reels") val allowReels: Boolean = true,
    @Json(name = "feed_show_text_posts") val feedShowTextPosts: Boolean = true,
    @Json(name = "feed_show_image_posts") val feedShowImagePosts: Boolean = true,
    @Json(name = "feed_show_video_posts") val feedShowVideoPosts: Boolean = true,
    @Json(name = "daily_text_limit") val dailyTextLimit: Int = 30,
    @Json(name = "daily_image_limit") val dailyImageLimit: Int = 20,
    @Json(name = "daily_video_limit") val dailyVideoLimit: Int = 10,
    @Json(name = "daily_story_limit") val dailyStoryLimit: Int = 15,
    @Json(name = "default_page_create_limit") val defaultPageCreateLimit: Int = 5
)





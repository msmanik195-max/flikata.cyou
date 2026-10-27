package com.example.data.remote

import com.example.data.model.AuthResponse
import com.example.data.model.ChatMessage
import com.example.data.model.CommentResponse
import com.example.data.model.CommentsListResponse
import com.example.data.model.GenericResponse
import com.example.data.model.Post
import com.example.data.model.PresignedUploadResponse
import com.example.data.model.ReelModel
import com.example.data.model.ReelsResponse
import com.example.data.model.ReelSettingsData
import com.example.data.model.ReelSettingsResponse
import com.example.data.model.ServerMessagesResponse
import com.example.data.model.StoriesResponse
import com.example.data.model.Story
import com.example.data.model.UploadResponse
import com.example.data.model.User
import com.example.data.model.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FlikataApiService {

    @FormUrlEncoded
    @POST("auth/login.php")
    suspend fun login(
        @Field("email_or_phone") emailOrPhone: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @FormUrlEncoded
    @POST("auth/register.php")
    suspend fun register(
        @Field("first_name") firstName: String,
        @Field("last_name") lastName: String,
        @Field("account_type") accountType: String, // "email" or "phone"
        @Field("email_or_phone") emailOrPhone: String,
        @Field("dob") dob: String,
        @Field("gender") gender: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @GET("get_upload_url.php")
    suspend fun getUploadUrl(
        @Query("folder") folder: String = "reels",
        @Query("filename") filename: String,
        @Query("mime") mime: String = "video/mp4"
    ): Response<PresignedUploadResponse>

    @Multipart
    @POST("upload.php")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Part("folder") folder: RequestBody? = null
    ): Response<UploadResponse>

    @FormUrlEncoded
    @POST("posts/create_post.php")
    suspend fun createPost(
        @Field("user_id") userId: String,
        @Field("author_name") authorName: String,
        @Field("content") content: String,
        @Field("background_style") backgroundStyle: String?,
        @Field("privacy") privacy: String,
        @Field("image_url") imageUrl: String?,
        @Field("video_url") videoUrl: String? = null,
        @Field("page_id") pageId: String? = null
    ): Response<Post>

    @GET("posts/get_posts.php")
    suspend fun getPosts(
        @Query("user_id") userId: String? = null
    ): Response<List<Post>>

    @FormUrlEncoded
    @POST("posts/edit_post.php")
    suspend fun editPost(
        @Field("post_id") postId: String,
        @Field("user_id") userId: String,
        @Field("content") content: String,
        @Field("privacy") privacy: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("posts/toggle_like.php")
    suspend fun toggleLike(
        @Field("post_id") postId: String,
        @Field("user_id") userId: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("posts/set_reaction.php")
    suspend fun setReaction(
        @Field("post_id") postId: String,
        @Field("user_id") userId: String,
        @Field("emoji") emoji: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("comments/add_comment.php")
    suspend fun addComment(
        @Field("post_id") postId: String?,
        @Field("reel_id") reelId: String?,
        @Field("user_id") userId: String,
        @Field("text") text: String
    ): Response<CommentResponse>

    @GET("comments/get_comments.php")
    suspend fun getPostComments(
        @Query("post_id") postId: String
    ): Response<CommentsListResponse>

    @GET("comments/get_comments.php")
    suspend fun getReelComments(
        @Query("reel_id") reelId: String
    ): Response<CommentsListResponse>

    @GET("chats/get_conversations.php")
    suspend fun getChats(
        @Query("user_id") userId: String,
        @Query("auth_token") authToken: String? = null
    ): Response<List<ChatMessage>>

    @GET("chats/get_messages.php")
    suspend fun getMessages(
        @Query("partner_id") partnerId: String,
        @Query("user_id") userId: String? = null,
        @Query("auth_token") authToken: String? = null,
        @Header("Authorization") authHeader: String? = null
    ): Response<ServerMessagesResponse>

    @FormUrlEncoded
    @POST("chats/send_message.php")
    suspend fun sendMessage(
        @Field("partner_id") partnerId: String,
        @Field("text") text: String,
        @Field("message") message: String,
        @Field("sender_id") senderId: String? = null,
        @Field("user_id") userId: String? = null,
        @Field("receiver_id") receiverId: String? = null,
        @Field("sender_name") senderName: String? = null,
        @Field("sender_avatar") senderAvatar: String? = null,
        @Field("auth_token") authToken: String? = null,
        @Header("Authorization") authHeader: String? = null
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("profile/update_profile.php")
    suspend fun updateProfile(
        @Field("user_id") userId: String,
        @Field("full_name") fullName: String? = null,
        @Field("bio") bio: String? = null,
        @Field("occupation") occupation: String? = null,
        @Field("location") location: String? = null,
        @Field("profile_pic") profilePic: String? = null,
        @Field("cover_pic") coverPic: String? = null
    ): Response<GenericResponse>

    @GET("profile/get_profile.php")
    suspend fun getProfile(
        @Query("user_id") userId: String
    ): Response<UserProfileResponse>

    @FormUrlEncoded
    @POST("posts/delete_post.php")
    suspend fun deletePost(
        @Field("post_id") postId: String,
        @Field("user_id") userId: String
    ): Response<GenericResponse>

    @GET("reels/get_reels.php")
    suspend fun getReels(
        @Query("user_id") userId: String? = null
    ): Response<ReelsResponse>

    @FormUrlEncoded
    @POST("reels/add_reel.php")
    suspend fun addReel(
        @Field("user_id") userId: String,
        @Field("media_url") mediaUrl: String,
        @Field("caption") caption: String? = null,
        @Field("audio_title") audioTitle: String? = null
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("reels/delete_reel.php")
    suspend fun deleteReel(
        @Field("reel_id") reelId: String,
        @Field("user_id") userId: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("reels/toggle_like.php")
    suspend fun toggleReelLike(
        @Field("reel_id") reelId: String,
        @Field("user_id") userId: String,
        @Field("reaction") reaction: String? = "Like"
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("reels/set_reaction.php")
    suspend fun setReelReaction(
        @Field("reel_id") reelId: String,
        @Field("user_id") userId: String,
        @Field("reaction") reaction: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("reels/record_view.php")
    suspend fun recordReelView(
        @Field("reel_id") reelId: String,
        @Field("user_id") userId: String? = null
    ): Response<com.example.data.model.RecordViewResponse>

    @FormUrlEncoded
    @POST("posts/record_view.php")
    suspend fun recordPostView(
        @Field("post_id") postId: String,
        @Field("user_id") userId: String? = null
    ): Response<com.example.data.model.RecordViewResponse>

    @GET("reels/reel_settings.php")
    suspend fun getReelSettings(): Response<ReelSettingsResponse>

    @GET("stories/get_stories.php")
    suspend fun getStories(
        @Query("user_id") userId: String? = null
    ): Response<StoriesResponse>

    @FormUrlEncoded
    @POST("stories/add_story.php")
    suspend fun addStory(
        @Field("user_id") userId: String,
        @Field("story_image") storyImage: String? = null,
        @Field("text_content") textContent: String? = null,
        @Field("background_color") backgroundColor: String? = null
    ): Response<GenericResponse>

    @GET("notifications/get_notifications.php")
    suspend fun getNotifications(
        @Query("user_id") userId: String? = null
    ): Response<com.example.data.model.NotificationListResponse>

    @FormUrlEncoded
    @POST("notifications/mark_read.php")
    suspend fun markNotificationsRead(
        @Field("user_id") userId: String,
        @Field("id") notificationId: String? = null
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("notifications/delete_notification.php")
    suspend fun deleteNotification(
        @Field("user_id") userId: String,
        @Field("id") notificationId: String? = null
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("calls/initiate_call.php")
    suspend fun initiateCall(
        @Field("caller_id") callerId: String,
        @Field("receiver_id") receiverId: String
    ): Response<com.example.data.model.InitiateCallResponse>

    @GET("calls/check_incoming_call.php")
    suspend fun checkIncomingCall(
        @Query("user_id") userId: String
    ): Response<com.example.data.model.CheckIncomingCallResponse>

    @GET("calls/get_call_status.php")
    suspend fun getCallStatus(
        @Query("call_id") callId: Int
    ): Response<com.example.data.model.CallStatusResponse>

    @FormUrlEncoded
    @POST("calls/answer_call.php")
    suspend fun answerCall(
        @Field("call_id") callId: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("calls/end_call.php")
    suspend fun endCall(
        @Field("call_id") callId: Int,
        @Field("reason") reason: String = "ended",
        @Field("duration") duration: Int = 0
    ): Response<GenericResponse>

    // Verification Badge APIs
    @GET("verification/get_packages.php")
    suspend fun getVerificationPackages(
        @Query("user_id") userId: String
    ): Response<com.example.data.model.VerificationPackagesResponse>

    @FormUrlEncoded
    @POST("verification/submit_request.php")
    suspend fun submitVerificationRequest(
        @Field("user_id") userId: String,
        @Field("package_id") packageId: Int,
        @Field("id_front_url") idFrontUrl: String? = null,
        @Field("id_back_url") idBackUrl: String? = null,
        @Field("selfie_url") selfieUrl: String? = null
    ): Response<GenericResponse>

    @GET("verification/admin_packages.php")
    suspend fun getAdminPackages(): Response<com.example.data.model.AdminPackagesResponse>

    @FormUrlEncoded
    @POST("verification/admin_packages.php")
    suspend fun adminCreatePackage(
        @Field("action") action: String = "create_package",
        @Field("title") title: String,
        @Field("description") description: String?,
        @Field("price") price: Double,
        @Field("duration_type") durationType: String,
        @Field("duration_value") durationValue: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("verification/admin_packages.php")
    suspend fun adminEditPackage(
        @Field("action") action: String = "edit_package",
        @Field("id") id: Int,
        @Field("title") title: String,
        @Field("description") description: String?,
        @Field("price") price: Double,
        @Field("duration_type") durationType: String,
        @Field("duration_value") durationValue: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("verification/admin_packages.php")
    suspend fun adminDeletePackage(
        @Field("action") action: String = "delete_package",
        @Field("id") id: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("verification/admin_packages.php")
    suspend fun adminSaveVerificationSettings(
        @Field("action") action: String = "save_settings",
        @Field("id_card_required") idCardRequired: Int,
        @Field("selfie_required") selfieRequired: Int,
        @Field("grace_period_days") gracePeriodDays: Int
    ): Response<GenericResponse>

    @GET("verification/admin_requests.php")
    suspend fun getAdminVerificationRequests(
        @Query("status") status: String
    ): Response<com.example.data.model.AdminVerificationRequestsResponse>

    @FormUrlEncoded
    @POST("verification/admin_requests.php")
    suspend fun adminApproveVerification(
        @Field("action") action: String = "approve",
        @Field("request_id") requestId: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("verification/admin_requests.php")
    suspend fun adminRejectVerification(
        @Field("action") action: String = "reject",
        @Field("request_id") requestId: Int,
        @Field("reason") reason: String?
    ): Response<GenericResponse>

    // Wallet & Deposit APIs
    @GET("wallet/get_wallet.php")
    suspend fun getWallet(
        @Query("user_id") userId: String
    ): Response<com.example.data.model.WalletResponse>

    @FormUrlEncoded
    @POST("wallet/deposit.php")
    suspend fun submitDeposit(
        @Field("user_id") userId: String,
        @Field("method_id") methodId: Int,
        @Field("method_name") methodName: String,
        @Field("amount") amount: Double,
        @Field("transaction_id") transactionId: String
    ): Response<GenericResponse>

    @GET("wallet/admin_methods.php")
    suspend fun getAdminDepositMethods(): Response<com.example.data.model.DepositMethodsResponse>

    @FormUrlEncoded
    @POST("wallet/admin_methods.php")
    suspend fun adminCreateDepositMethod(
        @Field("action") action: String = "create",
        @Field("name") name: String,
        @Field("number") number: String,
        @Field("image_url") imageUrl: String?,
        @Field("description") description: String?,
        @Field("is_active") isActive: Int = 1
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("wallet/admin_methods.php")
    suspend fun adminEditDepositMethod(
        @Field("action") action: String = "edit",
        @Field("id") id: Int,
        @Field("name") name: String,
        @Field("number") number: String,
        @Field("image_url") imageUrl: String?,
        @Field("description") description: String?,
        @Field("is_active") isActive: Int = 1
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("wallet/admin_methods.php")
    suspend fun adminDeleteDepositMethod(
        @Field("action") action: String = "delete",
        @Field("id") id: Int
    ): Response<GenericResponse>

    @GET("wallet/admin_requests.php")
    suspend fun getAdminDepositRequests(
        @Query("status") status: String
    ): Response<com.example.data.model.AdminDepositRequestsResponse>

    @FormUrlEncoded
    @POST("wallet/admin_requests.php")
    suspend fun adminApproveDeposit(
        @Field("action") action: String = "approve",
        @Field("request_id") requestId: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("wallet/admin_requests.php")
    suspend fun adminRejectDeposit(
        @Field("action") action: String = "reject",
        @Field("request_id") requestId: Int,
        @Field("reason") reason: String?
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("wallet/withdraw.php")
    suspend fun submitWithdraw(
        @Field("user_id") userId: String,
        @Field("method_id") methodId: Int,
        @Field("method_name") methodName: String,
        @Field("amount") amount: Double,
        @Field("account_name") accountName: String,
        @Field("account_number") accountNumber: String
    ): Response<GenericResponse>

    @GET("wallet/admin_withdraw_methods.php")
    suspend fun getAdminWithdrawMethods(): Response<com.example.data.model.WithdrawMethodsResponse>

    @FormUrlEncoded
    @POST("wallet/admin_withdraw_methods.php")
    suspend fun adminCreateWithdrawMethod(
        @Field("action") action: String = "create",
        @Field("name") name: String,
        @Field("number") number: String,
        @Field("image_url") imageUrl: String?,
        @Field("description") description: String?,
        @Field("min_amount") minAmount: Double = 50.0,
        @Field("max_amount") maxAmount: Double = 25000.0,
        @Field("is_active") isActive: Int = 1
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("wallet/admin_withdraw_methods.php")
    suspend fun adminEditWithdrawMethod(
        @Field("action") action: String = "edit",
        @Field("id") id: Int,
        @Field("name") name: String,
        @Field("number") number: String,
        @Field("image_url") imageUrl: String?,
        @Field("description") description: String?,
        @Field("min_amount") minAmount: Double = 50.0,
        @Field("max_amount") maxAmount: Double = 25000.0,
        @Field("is_active") isActive: Int = 1
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("wallet/admin_withdraw_methods.php")
    suspend fun adminDeleteWithdrawMethod(
        @Field("action") action: String = "delete",
        @Field("id") id: Int
    ): Response<GenericResponse>

    @GET("wallet/admin_withdraw_requests.php")
    suspend fun getAdminWithdrawRequests(
        @Query("tab") tab: String
    ): Response<com.example.data.model.AdminWithdrawRequestsResponse>

    @FormUrlEncoded
    @POST("wallet/admin_withdraw_requests.php")
    suspend fun adminApproveWithdraw(
        @Field("action") action: String = "approve",
        @Field("request_id") requestId: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("wallet/admin_withdraw_requests.php")
    suspend fun adminRejectWithdraw(
        @Field("action") action: String = "reject",
        @Field("request_id") requestId: Int,
        @Field("reason") reason: String?
    ): Response<GenericResponse>

    @GET("dashboard/get_dashboard.php")
    suspend fun getDashboard(
        @Query("user_id") userId: String,
        @Query("period") period: String = "7d"
    ): Response<com.example.data.model.DashboardResponse>

    @FormUrlEncoded
    @POST("dashboard/submit_monetization.php")
    suspend fun submitMonetization(
        @Field("user_id") userId: String
    ): Response<com.example.data.model.SubmitMonetizationResponse>

    // Marketplace APIs
    @GET("marketplace/get_products.php")
    suspend fun getMarketplaceProducts(
        @Query("category") category: String? = null,
        @Query("query") query: String? = null,
        @Query("location") location: String? = null,
        @Query("user_id") userId: String? = null
    ): Response<com.example.data.model.MarketplaceListResponse>

    @FormUrlEncoded
    @POST("marketplace/create_product.php")
    suspend fun createMarketplaceProduct(
        @Field("user_id") userId: String,
        @Field("title") title: String,
        @Field("price") price: Double,
        @Field("category") category: String,
        @Field("condition") condition: String,
        @Field("location") location: String,
        @Field("description") description: String,
        @Field("image_url") imageUrl: String?
    ): Response<com.example.data.model.MarketplaceCreateResponse>

    @FormUrlEncoded
    @POST("marketplace/delete_product.php")
    suspend fun deleteMarketplaceProduct(
        @Field("product_id") productId: String,
        @Field("user_id") userId: String
    ): Response<GenericResponse>

    // Pages APIs
    @GET("pages/get_pages.php")
    suspend fun getPages(
        @Query("user_id") userId: String? = null
    ): Response<com.example.data.model.PagesListResponse>

    @FormUrlEncoded
    @POST("pages/create_page.php")
    suspend fun createPage(
        @Field("user_id") userId: String,
        @Field("name") name: String,
        @Field("category") category: String,
        @Field("bio") bio: String?,
        @Field("logo_url") logoUrl: String?,
        @Field("banner_url") bannerUrl: String?,
        @Field("email") email: String?,
        @Field("phone") phone: String?,
        @Field("website") website: String?
    ): Response<com.example.data.model.PageCreateResponse>

    @FormUrlEncoded
    @POST("pages/follow_page.php")
    suspend fun followPage(
        @Field("page_id") pageId: String,
        @Field("user_id") userId: String
    ): Response<GenericResponse>

    @GET("pages/page_settings.php")
    suspend fun getPageSettings(): Response<com.example.data.model.PageSettingsResponse>

    @FormUrlEncoded
    @POST("pages/page_settings.php")
    suspend fun updatePageSettings(
        @Field("max_pages_per_user") maxPagesPerUser: Int
    ): Response<GenericResponse>

    @GET("leaderboard/get_leaderboard.php")
    suspend fun getLeaderboard(
        @retrofit2.http.Query("limit") limit: Int = 20
    ): Response<com.example.data.model.LeaderboardResponse>

    @GET("leaderboard/leaderboard_settings.php")
    suspend fun getLeaderboardSettings(): Response<com.example.data.model.LeaderboardSettingsResponse>

    @FormUrlEncoded
    @POST("leaderboard/leaderboard_settings.php")
    suspend fun updateLeaderboardSettings(
        @Field("user_limit") userLimit: Int
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("profile/toggle_follow.php")
    suspend fun toggleFollowUser(
        @Field("user_id") targetUserId: Int
    ): Response<com.example.data.model.ToggleFollowResponse>

    @GET("app/check_update.php")
    suspend fun checkAppUpdate(
        @retrofit2.http.Query("current_version_code") currentVersionCode: Int,
        @retrofit2.http.Query("current_version_name") currentVersionName: String
    ): Response<com.example.data.model.AppUpdateResponse>

    @GET("app/get_feature_settings.php")
    suspend fun getFeatureSettings(): Response<com.example.data.model.FeatureSettingsResponse>

    @FormUrlEncoded
    @POST("app/get_feature_settings.php")
    suspend fun updateFeatureSettings(
        @retrofit2.http.FieldMap fields: Map<String, String>
    ): Response<com.example.data.model.FeatureSettingsResponse>
}



package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ChatEntity
import com.example.data.local.PostDao
import com.example.data.local.PostEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.local.HistoryDao
import com.example.data.local.SavedDao
import com.example.data.local.PageDao
import com.example.data.local.PageEntity
import com.example.data.local.ViewHistoryEntity
import com.example.data.local.SavedItemEntity
import com.example.data.local.toEntity
import com.example.data.local.toModel
import com.example.data.model.ChatMessage
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.Reaction
import com.example.data.model.ReelModel
import com.example.data.model.ReelSettingsData
import com.example.data.model.Story
import com.example.data.model.User
import com.example.data.model.UserProfileData
import com.example.data.remote.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import java.util.UUID

private class ChunkedProgressRequestBody(
    private val bytes: ByteArray,
    private val mediaType: MediaType?,
    private val onProgress: (Float) -> Unit
) : RequestBody() {
    override fun contentType() = mediaType
    override fun contentLength() = bytes.size.toLong()

    override fun writeTo(sink: BufferedSink) {
        val total = bytes.size.toLong()
        if (total <= 0L) {
            onProgress(1f)
            return
        }
        var written = 0L
        val chunkSize = 32 * 1024 // 32 KB chunk for smooth network streaming
        var offset = 0
        while (offset < bytes.size) {
            val len = minOf(chunkSize, bytes.size - offset)
            sink.write(bytes, offset, len)
            sink.flush()
            offset += len
            written += len
            val progress = (written.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            onProgress(progress)
        }
    }
}

class FlikataRepository(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val chatDao: ChatDao,
    private val historyDao: HistoryDao? = null,
    private val savedDao: SavedDao? = null,
    private val pageDao: PageDao? = null
) {
    fun getAllPagesFlow(): Flow<List<com.example.data.model.FlikataPage>> {
        return pageDao?.getAllPagesFlow()?.map { list -> list.map { it.toModel() } }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun getPages(): List<com.example.data.model.FlikataPage> {
        val local = pageDao?.getAllPages()?.map { it.toModel() } ?: emptyList()
        try {
            val user = userDao.getLoggedInUser()?.toModel()
            val resp = ApiClient.apiService.getPages(user?.id)
            if (resp.isSuccessful && resp.body()?.success == true) {
                val serverPages = resp.body()?.data ?: emptyList()
                if (serverPages.isNotEmpty()) {
                    pageDao?.insertAllPages(serverPages.map { it.toEntity() })
                    return serverPages
                }
            }
        } catch (e: Exception) {
            // fallback to local
        }
        return local
    }

    suspend fun createPage(
        name: String,
        category: String,
        bio: String,
        logoUrl: String?,
        bannerUrl: String?,
        email: String?,
        phone: String?,
        website: String?
    ): Result<com.example.data.model.FlikataPage> {
        val user = userDao.getLoggedInUser()?.toModel()
        val userId = user?.id ?: "1"
        val tempId = "page_" + System.currentTimeMillis()
        val newPage = com.example.data.model.FlikataPage(
            id = tempId,
            userId = userId,
            name = name,
            category = category,
            bio = bio,
            logoUrl = logoUrl,
            bannerUrl = bannerUrl,
            email = email,
            phone = phone,
            website = website,
            followersCount = 1,
            likesCount = 1,
            isVerified = false,
            createdAt = "Just now"
        )
        pageDao?.insertPage(newPage.toEntity())

        try {
            val resp = ApiClient.apiService.createPage(
                userId = userId,
                name = name,
                category = category,
                bio = bio,
                logoUrl = logoUrl,
                bannerUrl = bannerUrl,
                email = email,
                phone = phone,
                website = website
            )
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                val created = resp.body()?.data!!
                pageDao?.deletePage(tempId)
                pageDao?.insertPage(created.toEntity())
                return Result.success(created)
            }
        } catch (e: Exception) {
            android.util.Log.e("FlikataPages", "Network createPage error: ${e.message}")
        }
        return Result.success(newPage)
    }

    suspend fun followPage(pageId: String): Result<Boolean> {
        return try {
            val user = userDao.getLoggedInUser()?.toModel()
            val userId = user?.id ?: "1"
            val resp = ApiClient.apiService.followPage(pageId, userId)
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getViewHistory(itemType: String): Flow<List<ViewHistoryEntity>> {
        return historyDao?.getHistoryByType(itemType) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun recordViewHistory(
        id: String,
        itemType: String,
        titleOrContent: String,
        authorName: String,
        authorAvatar: String? = null,
        isVerified: Boolean = false,
        mediaUrl: String? = null,
        timestampText: String = "Just now"
    ) {
        historyDao?.insertHistory(
            ViewHistoryEntity(
                id = id,
                itemType = itemType,
                titleOrContent = titleOrContent,
                authorName = authorName,
                authorAvatar = authorAvatar,
                isVerified = isVerified,
                mediaUrl = mediaUrl,
                timestampText = timestampText,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteViewHistory(id: String) {
        historyDao?.deleteHistory(id)
    }

    suspend fun clearViewHistory(type: String) {
        historyDao?.clearHistoryByType(type)
    }

    suspend fun clearAllViewHistory() {
        historyDao?.clearAllHistory()
    }

    fun getSavedItems(itemType: String): Flow<List<SavedItemEntity>> {
        return savedDao?.getSavedItemsByType(itemType) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    fun isItemSaved(id: String): Flow<Boolean> {
        return savedDao?.isItemSaved(id) ?: kotlinx.coroutines.flow.flowOf(false)
    }

    suspend fun isItemSavedDirect(id: String): Boolean {
        return savedDao?.isItemSavedDirect(id) ?: false
    }

    suspend fun saveItem(
        id: String,
        itemType: String,
        title: String,
        subtitleOrPrice: String,
        mediaUrl: String? = null,
        authorOrSellerName: String,
        authorOrSellerAvatar: String? = null,
        isVerified: Boolean = false,
        originalId: String
    ) {
        savedDao?.insertSavedItem(
            SavedItemEntity(
                id = id,
                itemType = itemType,
                title = title,
                subtitleOrPrice = subtitleOrPrice,
                mediaUrl = mediaUrl,
                authorOrSellerName = authorOrSellerName,
                authorOrSellerAvatar = authorOrSellerAvatar,
                isVerified = isVerified,
                originalId = originalId,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun unsaveItem(id: String) {
        savedDao?.deleteSavedItem(id)
    }

    suspend fun clearSavedItems(type: String) {
        savedDao?.clearSavedByType(type)
    }
    suspend fun getAllPostsDirect(): List<Post> {
        return postDao.getAllPostsList().map { it.toModel() }
    }

    suspend fun getVideoPostsDirect(): List<Post> {
        return postDao.getVideoPosts().map { it.toModel() }
    }
    val loggedInUser: Flow<User?> = userDao.getLoggedInUserFlow().map { entity ->
        val model = entity?.toModel()
        if (model != null) {
            ApiClient.currentAuthToken = model.authToken
            ApiClient.currentUserId = model.id
        }
        model
    }

    val allPosts: Flow<List<Post>> = postDao.getAllPosts().map { list ->
        list.map { it.toModel() }
    }

    val allChats: Flow<List<ChatMessage>> = chatDao.getAllChats().map { list ->
        list.map { it.toModel() }
    }

    suspend fun updateUser(user: User) {
        userDao.insertUser(user.toEntity(isLoggedIn = true))
        try {
            ApiClient.apiService.updateProfile(
                userId = user.id,
                fullName = user.fullName,
                bio = user.bio,
                occupation = null,
                location = null,
                profilePic = user.profilePic,
                coverPic = user.coverPic
            )
        } catch (e: Exception) {
            android.util.Log.e("FlikataProfile", "Failed to sync profile to server: ${e.message}")
        }
    }

    suspend fun fetchUserProfile(userId: String): Result<UserProfileData> {
        val cleanId = userId.removePrefix("user_").removePrefix("partner_").trim()
        if (cleanId.isBlank()) return Result.failure(Exception("Invalid user ID"))
        return try {
            val resp = ApiClient.apiService.getProfile(cleanId)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(emailOrPhone: String, password: String): Result<User> {
        return try {
            val response = ApiClient.apiService.login(emailOrPhone, password)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success" && body.user != null) {
                    val userWithToken = if (!body.token.isNullOrBlank()) {
                        body.user.copy(authToken = body.token)
                    } else {
                        body.user
                    }
                    saveUserSession(userWithToken)
                    Result.success(userWithToken)
                } else {
                    val errMsg = body?.message ?: "Invalid email/phone or password."
                    Result.failure(Exception(errMsg))
                }
            } else {
                Result.failure(Exception("Server returned error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to connect to server. Please check internet connection."))
        }
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        accountType: String,
        emailOrPhone: String,
        dob: String,
        gender: String,
        password: String
    ): Result<User> {
        return try {
            val response = ApiClient.apiService.register(
                firstName = firstName,
                lastName = lastName,
                accountType = accountType,
                emailOrPhone = emailOrPhone,
                dob = dob,
                gender = gender,
                password = password
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success" && body.user != null) {
                    val userWithToken = if (!body.token.isNullOrBlank()) {
                        body.user.copy(authToken = body.token)
                    } else {
                        body.user
                    }
                    saveUserSession(userWithToken)
                    Result.success(userWithToken)
                } else {
                    val errMsg = body?.message ?: "Registration failed."
                    Result.failure(Exception(errMsg))
                }
            } else {
                Result.failure(Exception("Server returned error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to connect to server. Please check internet connection."))
        }
    }

    private suspend fun saveUserSession(user: User) {
        ApiClient.currentAuthToken = user.authToken
        ApiClient.currentUserId = user.id
        userDao.logoutAll()
        userDao.insertUser(user.toEntity(isLoggedIn = true))
    }

    suspend fun logout() {
        ApiClient.currentAuthToken = null
        ApiClient.currentUserId = null
        userDao.logoutAll()
    }

    suspend fun getLoggedInUserDirect(): User? {
        val entity = userDao.getLoggedInUser() ?: return null
        val model = entity.toModel()
        ApiClient.currentAuthToken = model.authToken
        ApiClient.currentUserId = model.id
        return model
    }

    suspend fun syncPostsFromServer(): Result<List<Post>> {
        return try {
            val user = userDao.getLoggedInUser()
            val response = ApiClient.apiService.getPosts(userId = user?.id)
            if (response.isSuccessful) {
                val posts = response.body() ?: emptyList()
                if (posts.isNotEmpty()) {
                    postDao.syncAllPosts(posts.map { it.toEntity() })
                }
                Result.success(posts)
            } else {
                Result.failure(Exception("Failed to fetch posts from server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editPost(postId: String, content: String, privacy: String): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.editPost(
                postId = postId,
                userId = user.id,
                content = content,
                privacy = privacy
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                postDao.updatePost(postId, content, privacy)
                syncPostsFromServer()
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to edit post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncChatsFromServer(userId: String): Result<List<ChatMessage>> {
        return try {
            val user = userDao.getLoggedInUser()?.toModel()
            val token = user?.authToken ?: ApiClient.currentAuthToken
            val myId = user?.id ?: userId
            val response = ApiClient.apiService.getChats(userId = myId, authToken = token)
            if (response.isSuccessful) {
                val chats = response.body() ?: emptyList()
                if (chats.isNotEmpty()) {
                    chatDao.syncAllChats(chats.map { it.toEntity() })
                }
                Result.success(chats)
            } else {
                Result.failure(Exception("Failed to fetch chats from server: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchConversationMessages(partnerId: String): Result<List<com.example.data.model.ServerMessageItem>> {
        return try {
            val cleanPartnerId = partnerId.removePrefix("partner_").removePrefix("user_").trim()
            val user = userDao.getLoggedInUser()?.toModel()
            val token = user?.authToken ?: ApiClient.currentAuthToken
            val myId = user?.id ?: ApiClient.currentUserId
            val authHeader = if (!token.isNullOrBlank()) "Bearer $token" else null

            val response = ApiClient.apiService.getMessages(
                partnerId = cleanPartnerId,
                userId = myId,
                authToken = token,
                authHeader = authHeader
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val msgList = response.body()?.data?.messages ?: emptyList()
                Result.success(msgList)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(
        partnerId: String,
        partnerName: String,
        partnerAvatar: String,
        message: String
    ): Result<Boolean> {
        val cleanPartnerId = partnerId.removePrefix("partner_").removePrefix("user_").trim()
        val currentUser = userDao.getLoggedInUser()?.toModel()
        val timestamp = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val token = currentUser?.authToken ?: ApiClient.currentAuthToken
        val myId = currentUser?.id ?: ApiClient.currentUserId
        val authHeader = if (!token.isNullOrBlank()) "Bearer $token" else null

        val chatEntity = com.example.data.local.ChatEntity(
            id = cleanPartnerId,
            threadId = "t_${cleanPartnerId.hashCode()}",
            senderId = cleanPartnerId,
            senderName = partnerName,
            senderAvatar = partnerAvatar,
            isVerified = false,
            lastMessage = message,
            timestamp = timestamp,
            isVoiceMessage = false,
            isMissedCall = false,
            unreadCount = 0
        )
        chatDao.insertChats(listOf(chatEntity))

        try {
            val resp = ApiClient.apiService.sendMessage(
                partnerId = cleanPartnerId,
                text = message,
                message = message,
                senderId = myId,
                userId = myId,
                receiverId = cleanPartnerId,
                senderName = currentUser?.fullName,
                senderAvatar = currentUser?.profilePic ?: "",
                authToken = token,
                authHeader = authHeader
            )
            if (resp.isSuccessful) {
                android.util.Log.d("FlikataChat", "Message sent successfully to server!")
            } else {
                android.util.Log.w("FlikataChat", "Server message response code: ${resp.code()}")
            }
            if (myId != null) {
                syncChatsFromServer(myId)
            }
        } catch (e: Exception) {
            android.util.Log.e("FlikataChat", "Failed to send message: ${e.message}", e)
        }
        return Result.success(true)
    }


    suspend fun createPost(
        content: String,
        backgroundStyle: String? = null,
        privacy: String = "Public",
        imageUrl: String? = null,
        videoUrl: String? = null,
        isVideo: Boolean = false,
        authorNameOverride: String? = null,
        authorAvatarOverride: String? = null,
        isVerifiedOverride: Boolean? = null,
        pageId: String? = null
    ): Result<Post> {
        val currentUser = userDao.getLoggedInUser()?.toModel()
            ?: return Result.failure(Exception("Please log in first"))
        val effectiveAuthorName = authorNameOverride?.ifBlank { null } ?: currentUser.fullName
        val effectiveAuthorAvatar = authorAvatarOverride ?: (currentUser.profilePic ?: "")
        val effectiveIsVerified = isVerifiedOverride ?: currentUser.isVerified
        val effectiveUserId = if (!pageId.isNullOrBlank()) "page_$pageId" else currentUser.id
        val tempId = UUID.randomUUID().toString()
        val newPost = Post(
            id = tempId,
            userId = effectiveUserId,
            pageId = pageId,
            authorName = effectiveAuthorName,
            authorAvatar = effectiveAuthorAvatar,
            isVerified = effectiveIsVerified,
            timestamp = "Just now",
            content = content,
            backgroundStyle = backgroundStyle,
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            isVideo = isVideo,
            likesCount = 0,
            commentsCount = 0,
            sharesCount = 0,
            isLiked = false,
            privacy = privacy
        )

        postDao.insertPost(newPost.toEntity())

        try {
            val resp = ApiClient.apiService.createPost(
                userId = currentUser.id,
                authorName = effectiveAuthorName,
                content = content,
                backgroundStyle = backgroundStyle,
                privacy = privacy,
                imageUrl = imageUrl,
                videoUrl = videoUrl,
                pageId = pageId
            )
            if (resp.isSuccessful && resp.body() != null) {
                val serverPost = resp.body()!!
                postDao.deletePost(tempId)
                postDao.insertPost(serverPost.toEntity())
                return Result.success(serverPost)
            } else {
                val errStr = resp.errorBody()?.string() ?: ""
                postDao.deletePost(tempId)
                val errMsg = try {
                    val jsonObj = org.json.JSONObject(errStr)
                    jsonObj.optString("message", jsonObj.optString("error", "পোস্ট করা সাময়িকভাবে বন্ধ আছে"))
                } catch (e: Exception) {
                    "পোস্ট করা সাময়িকভাবে বন্ধ আছে"
                }
                return Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            postDao.deletePost(tempId)
            android.util.Log.e("FlikataPost", "Error creating post on server: ${e.message}", e)
            return Result.failure(e)
        }
    }

    suspend fun uploadMediaFile(
        bytes: ByteArray, 
        fileName: String, 
        mimeType: String,
        folder: String? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        val targetFolder = folder ?: if (mimeType.startsWith("video")) "reels" else "images"

        // 1. Try Direct Presigned Cloudflare R2 Upload first (bypasses any PHP upload size limits!)
        try {
            val presignedResp = ApiClient.apiService.getUploadUrl(
                folder = targetFolder,
                filename = fileName,
                mime = mimeType
            )
            if (presignedResp.isSuccessful && presignedResp.body()?.success == true) {
                val uploadData = presignedResp.body()?.data
                if (uploadData != null && uploadData.uploadUrl.isNotBlank()) {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val reqBody = ChunkedProgressRequestBody(bytes, mimeType.toMediaTypeOrNull(), onProgress)
                    val request = okhttp3.Request.Builder()
                        .url(uploadData.uploadUrl)
                        .put(reqBody)
                        .addHeader("Content-Type", mimeType)
                        .build()

                    val directResp = client.newCall(request).execute()
                    if (directResp.isSuccessful) {
                        onProgress(1f)
                        android.util.Log.d("FlikataUpload", "Direct Cloudflare R2 upload SUCCESS: ${uploadData.fileUrl}")
                        return Result.success(uploadData.fileUrl)
                    } else {
                        android.util.Log.w("FlikataUpload", "Direct R2 upload returned ${directResp.code}, falling back to upload.php")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("FlikataUpload", "Direct R2 presigned attempt failed: ${e.message}, falling back to upload.php")
        }

        // 2. Fallback to upload.php
        return try {
            val countingBody = ChunkedProgressRequestBody(bytes, mimeType.toMediaTypeOrNull(), onProgress)
            val part = MultipartBody.Part.createFormData("file", fileName, countingBody)
            val folderPart = targetFolder.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = ApiClient.apiService.uploadMedia(part, folderPart)
            if (response.isSuccessful && response.body()?.success == true) {
                onProgress(1f)
                val url = response.body()?.data?.url
                if (!url.isNullOrBlank()) {
                    Result.success(url)
                } else {
                    Result.failure(Exception("Cloudflare R2 returned empty URL"))
                }
            } else {
                val err = response.body()?.message ?: "Upload to Cloudflare R2 failed: ${response.code()}"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(postId: String) {
        postDao.toggleLike(postId)
        val user = userDao.getLoggedInUser()
        if (user != null) {
            try {
                ApiClient.apiService.toggleLike(postId, user.id)
            } catch (_: Exception) {}
        }
    }

    suspend fun deletePost(postId: String): Result<Boolean> {
        postDao.deletePost(postId)
        val user = userDao.getLoggedInUser()
        if (user != null) {
            try {
                ApiClient.apiService.deletePost(postId, user.id)
            } catch (e: Exception) {
                android.util.Log.e("FlikataDelete", "Error deleting post from server: ${e.message}", e)
            }
        }
        return Result.success(true)
    }

    suspend fun syncReelsFromServer(): Result<List<ReelModel>> {
        return try {
            val user = userDao.getLoggedInUser()
            val resp = ApiClient.apiService.getReels(user?.id)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                val uniqueReels = resp.body()!!.data!!.distinctBy { it.mediaUrl }
                Result.success(uniqueReels)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReel(mediaUrl: String, caption: String?, audioTitle: String? = null): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.addReel(
                userId = user.id,
                mediaUrl = mediaUrl,
                caption = caption,
                audioTitle = audioTitle
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to upload reel"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReel(reelId: String): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.deleteReel(
                reelId = reelId,
                userId = user.id
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to delete reel"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleReelLike(reelId: String, reaction: String = "Like"): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.toggleReelLike(reelId, user.id, reaction)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to like reel"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setReelReaction(reelId: String, reaction: String): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.setReelReaction(reelId, user.id, reaction)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to set reaction"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordReelView(reelId: String): Result<com.example.data.model.RecordViewData?> {
        val user = userDao.getLoggedInUser()
        val userId = user?.id?.removePrefix("user_")?.trim() ?: ApiClient.currentUserId?.removePrefix("user_")?.trim()
        return try {
            if (reelId.startsWith("post_")) {
                val cleanPostId = reelId.removePrefix("post_").trim()
                val resp = ApiClient.apiService.recordPostView(cleanPostId, userId)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    Result.success(resp.body()?.data)
                } else {
                    Result.failure(Exception("Failed to record view"))
                }
            } else {
                val cleanReelId = reelId.removePrefix("reel_").trim()
                val resp = ApiClient.apiService.recordReelView(cleanReelId, userId)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    Result.success(resp.body()?.data)
                } else {
                    Result.failure(Exception("Failed to record view"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordPostView(postId: String): Result<com.example.data.model.RecordViewData?> {
        val user = userDao.getLoggedInUser()
        val userId = user?.id?.removePrefix("user_")?.trim() ?: ApiClient.currentUserId?.removePrefix("user_")?.trim()
        val cleanPostId = postId.removePrefix("post_").trim()
        return try {
            val resp = ApiClient.apiService.recordPostView(cleanPostId, userId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.data)
            } else {
                Result.failure(Exception("Failed to record view"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReelComment(reelId: String, text: String): Result<Comment?> {
        val user = userDao.getLoggedInUser()
        val userId = user?.id ?: "1"
        return try {
            val targetPostId = if (reelId.startsWith("post_")) reelId.removePrefix("post_") else null
            val targetReelId = if (reelId.startsWith("post_")) null else reelId
            val resp = ApiClient.apiService.addComment(
                postId = targetPostId,
                reelId = targetReelId,
                userId = userId,
                text = text
            )
            if (targetPostId != null) {
                try {
                    postDao.incrementCommentsCount(targetPostId)
                } catch (_: Exception) {}
            }
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.data)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to add comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReelMaxDurationSeconds(): Int {
        return try {
            val resp = ApiClient.apiService.getReelSettings()
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                resp.body()!!.data!!.maxDurationSeconds
            } else {
                60
            }
        } catch (_: Exception) {
            60
        }
    }

    suspend fun setReaction(postId: String, emoji: String?) {
        postDao.updateReaction(postId, emoji)
        val user = userDao.getLoggedInUser()
        if (user != null) {
            try {
                ApiClient.apiService.setReaction(postId, user.id, emoji ?: "")
            } catch (_: Exception) {}
        }
    }

    suspend fun getPostComments(postId: String): Result<List<Comment>> {
        return try {
            val resp = ApiClient.apiService.getPostComments(postId)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReelComments(reelId: String): Result<List<Comment>> {
        return try {
            val resp = ApiClient.apiService.getReelComments(reelId)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, text: String): Result<Comment> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.addComment(
                postId = postId,
                reelId = null,
                userId = user.id,
                text = text
            )
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                syncPostsFromServer()
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to add comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllPosts() {
        postDao.clearPosts()
    }

    suspend fun seedInitialDataIfEmpty() {
        // Do not seed any dummy user - require real registration/login!
        // Start fresh with clean database
    }

    fun getSampleStories(): List<Story> {
        return listOf(
            Story(
                id = "story_create",
                authorName = "Create story",
                authorAvatar = "",
                storyImage = "",
                isUserStory = true
            )
        )
    }

    suspend fun syncStoriesFromServer(): Result<List<Story>> {
        return try {
            val user = userDao.getLoggedInUser()
            val response = ApiClient.apiService.getStories(user?.id)
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteStories = response.body()?.data ?: emptyList()
                val list = mutableListOf<Story>()
                list.add(
                    Story(
                        id = "story_create",
                        authorName = "Create story",
                        authorAvatar = user?.profilePic ?: "",
                        storyImage = "",
                        isUserStory = true
                    )
                )
                list.addAll(remoteStories)
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to fetch stories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStory(
        storyImage: String?,
        textContent: String?,
        backgroundColor: String?
    ): Result<Boolean> {
        return try {
            val user = userDao.getLoggedInUser()
            if (user == null) {
                return Result.failure(Exception("User not logged in"))
            }
            val response = ApiClient.apiService.addStory(
                userId = user.id,
                storyImage = storyImage,
                textContent = textContent,
                backgroundColor = backgroundColor
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to add story"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(): List<com.example.data.model.FlikataNotification> {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        return try {
            val response = ApiClient.apiService.getNotifications(userId = uid)
            if (response.isSuccessful && response.body()?.data != null) {
                response.body()!!.data!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markNotificationsRead(id: String? = null) {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        try {
            ApiClient.apiService.markNotificationsRead(userId = uid, notificationId = id)
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun deleteNotification(id: String? = null) {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        try {
            ApiClient.apiService.deleteNotification(userId = uid, notificationId = id)
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun updateUserLocal(user: User) {
        userDao.insertUser(user.toEntity(isLoggedIn = true))
    }

    suspend fun initiateCall(receiverId: String): Result<com.example.data.model.InitiateCallData> {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        return try {
            val resp = ApiClient.apiService.initiateCall(callerId = uid, receiverId = receiverId)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                val msg = resp.body()?.message ?: "Offline"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIncomingCall(): com.example.data.model.IncomingCallData? {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        if (uid.isBlank()) return null
        return try {
            val resp = ApiClient.apiService.checkIncomingCall(userId = uid)
            if (resp.isSuccessful && resp.body()?.data?.hasCall == true) {
                resp.body()!!.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCallStatus(callId: Int): com.example.data.model.CallStatusData? {
        return try {
            val resp = ApiClient.apiService.getCallStatus(callId = callId)
            if (resp.isSuccessful) resp.body()?.data else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun answerCall(callId: Int): Boolean {
        return try {
            val resp = ApiClient.apiService.answerCall(callId = callId)
            resp.isSuccessful && resp.body()?.success == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun endCall(callId: Int, reason: String = "ended", duration: Int = 0): Boolean {
        return try {
            val resp = ApiClient.apiService.endCall(callId = callId, reason = reason, duration = duration)
            resp.isSuccessful && resp.body()?.success == true
        } catch (e: Exception) {
            false
        }
    }

    // --- Verification & Wallet Methods ---
    suspend fun getVerificationPackages(): Result<com.example.data.model.VerificationPackagesData> {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        return try {
            val resp = ApiClient.apiService.getVerificationPackages(userId = uid)
            if (resp.isSuccessful && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load packages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitVerificationRequest(
        packageId: Int,
        idFrontUrl: String?,
        idBackUrl: String?,
        selfieUrl: String?
    ): Result<String> {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        return try {
            val resp = ApiClient.apiService.submitVerificationRequest(
                userId = uid,
                packageId = packageId,
                idFrontUrl = idFrontUrl,
                idBackUrl = idBackUrl,
                selfieUrl = selfieUrl
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.message ?: "Request submitted successfully")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Submission failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWallet(): Result<com.example.data.model.WalletData> {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        return try {
            val resp = ApiClient.apiService.getWallet(userId = uid)
            if (resp.isSuccessful && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load wallet"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitDeposit(
        methodId: Int,
        methodName: String,
        amount: Double,
        transactionId: String
    ): Result<String> {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        return try {
            val resp = ApiClient.apiService.submitDeposit(
                userId = uid,
                methodId = methodId,
                methodName = methodName,
                amount = amount,
                transactionId = transactionId
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.message ?: "Deposit request submitted")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Deposit failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin APIs
    suspend fun getAdminPackages(): Result<com.example.data.model.AdminPackagesData> {
        return try {
            val resp = ApiClient.apiService.getAdminPackages()
            if (resp.isSuccessful && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load admin packages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminCreatePackage(
        title: String,
        description: String?,
        price: Double,
        durationType: String,
        durationValue: Int
    ): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminCreatePackage(
                title = title,
                description = description,
                price = price,
                durationType = durationType,
                durationValue = durationValue
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.message ?: "Package created")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Create package failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminEditPackage(
        id: Int,
        title: String,
        description: String?,
        price: Double,
        durationType: String,
        durationValue: Int
    ): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminEditPackage(
                id = id,
                title = title,
                description = description,
                price = price,
                durationType = durationType,
                durationValue = durationValue
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.message ?: "Package updated")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Update package failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminDeletePackage(id: Int): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminDeletePackage(id = id)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Package deleted")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Delete package failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSaveVerificationSettings(
        idCardRequired: Boolean,
        selfieRequired: Boolean,
        gracePeriodDays: Int
    ): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminSaveVerificationSettings(
                idCardRequired = if (idCardRequired) 1 else 0,
                selfieRequired = if (selfieRequired) 1 else 0,
                gracePeriodDays = gracePeriodDays
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Settings saved")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to save settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminVerificationRequests(status: String): Result<com.example.data.model.AdminVerificationRequestsData> {
        return try {
            val resp = ApiClient.apiService.getAdminVerificationRequests(status = status)
            if (resp.isSuccessful && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load requests"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminApproveVerification(requestId: Int): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminApproveVerification(requestId = requestId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Approved")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Approve failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminRejectVerification(requestId: Int, reason: String?): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminRejectVerification(requestId = requestId, reason = reason)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Rejected")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Reject failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminDepositMethods(): Result<List<com.example.data.model.DepositMethod>> {
        return try {
            val resp = ApiClient.apiService.getAdminDepositMethods()
            if (resp.isSuccessful && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load methods"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminCreateDepositMethod(
        name: String,
        number: String,
        imageUrl: String?,
        description: String?,
        isActive: Int = 1
    ): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminCreateDepositMethod(
                name = name,
                number = number,
                imageUrl = imageUrl,
                description = description,
                isActive = isActive
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Method created")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Create method failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminEditDepositMethod(
        id: Int,
        name: String,
        number: String,
        imageUrl: String?,
        description: String?,
        isActive: Int = 1
    ): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminEditDepositMethod(
                id = id,
                name = name,
                number = number,
                imageUrl = imageUrl,
                description = description,
                isActive = isActive
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Method updated")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Update method failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminDeleteDepositMethod(id: Int): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminDeleteDepositMethod(id = id)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Method deleted")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Delete method failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminDepositRequests(status: String): Result<com.example.data.model.AdminDepositRequestsData> {
        return try {
            val resp = ApiClient.apiService.getAdminDepositRequests(status = status)
            if (resp.isSuccessful && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load deposit requests"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminApproveDeposit(requestId: Int): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminApproveDeposit(requestId = requestId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Deposit approved")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Approve failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminRejectDeposit(requestId: Int, reason: String?): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminRejectDeposit(requestId = requestId, reason = reason)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Deposit rejected")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Reject failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitWithdraw(
        methodId: Int,
        methodName: String,
        amount: Double,
        accountName: String,
        accountNumber: String
    ): Result<String> {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        return try {
            val resp = ApiClient.apiService.submitWithdraw(
                userId = uid,
                methodId = methodId,
                methodName = methodName,
                amount = amount,
                accountName = accountName,
                accountNumber = accountNumber
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.message ?: "Withdrawal request submitted")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Withdrawal failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminWithdrawMethods(): Result<List<com.example.data.model.WithdrawMethod>> {
        return try {
            val resp = ApiClient.apiService.getAdminWithdrawMethods()
            if (resp.isSuccessful && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load withdraw methods"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminCreateWithdrawMethod(
        name: String,
        number: String,
        imageUrl: String?,
        description: String?,
        minAmount: Double = 50.0,
        maxAmount: Double = 25000.0,
        isActive: Int = 1
    ): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminCreateWithdrawMethod(
                name = name,
                number = number,
                imageUrl = imageUrl,
                description = description,
                minAmount = minAmount,
                maxAmount = maxAmount,
                isActive = isActive
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Method created")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Create method failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminEditWithdrawMethod(
        id: Int,
        name: String,
        number: String,
        imageUrl: String?,
        description: String?,
        minAmount: Double = 50.0,
        maxAmount: Double = 25000.0,
        isActive: Int = 1
    ): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminEditWithdrawMethod(
                id = id,
                name = name,
                number = number,
                imageUrl = imageUrl,
                description = description,
                minAmount = minAmount,
                maxAmount = maxAmount,
                isActive = isActive
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Method updated")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Update method failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminDeleteWithdrawMethod(id: Int): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminDeleteWithdrawMethod(id = id)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Method deleted")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Delete method failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminWithdrawRequests(status: String): Result<com.example.data.model.AdminWithdrawRequestsData> {
        return try {
            val resp = ApiClient.apiService.getAdminWithdrawRequests(tab = status)
            if (resp.isSuccessful && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load withdraw requests"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminApproveWithdraw(requestId: Int): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminApproveWithdraw(requestId = requestId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Withdraw approved")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Approve failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminRejectWithdraw(requestId: Int, reason: String?): Result<String> {
        return try {
            val resp = ApiClient.apiService.adminRejectWithdraw(requestId = requestId, reason = reason)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success("Withdraw rejected")
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Reject failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboardData(userId: String, period: String = "7d"): Result<com.example.data.model.DashboardData> {
        return try {
            val user = userDao.getLoggedInUser()?.toModel()
            val effectiveUserId = userId.ifBlank { user?.id ?: ApiClient.currentUserId ?: "1" }
            val cleanId = effectiveUserId.removePrefix("user_").trim().ifBlank { "1" }
            val resp = ApiClient.apiService.getDashboard(userId = cleanId, period = period)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load dashboard data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitMonetization(userId: String): Result<com.example.data.model.SubmitMonetizationData> {
        return try {
            val user = userDao.getLoggedInUser()?.toModel()
            val effectiveUserId = userId.ifBlank { user?.id ?: ApiClient.currentUserId ?: "1" }
            val cleanId = effectiveUserId.removePrefix("user_").trim().ifBlank { "1" }
            val resp = ApiClient.apiService.submitMonetization(userId = cleanId)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to submit monetization request"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMarketplaceProducts(
        category: String? = null,
        query: String? = null,
        location: String? = null
    ): Result<List<com.example.data.model.MarketplaceProduct>> {
        return try {
            val resp = ApiClient.apiService.getMarketplaceProducts(
                category = category?.ifBlank { null },
                query = query?.ifBlank { null },
                location = location?.ifBlank { null }
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                val list = resp.body()?.data ?: emptyList()
                if (list.isNotEmpty()) {
                    Result.success(list)
                } else {
                    Result.success(getFallbackMarketplaceProducts(category, query))
                }
            } else {
                Result.success(getFallbackMarketplaceProducts(category, query))
            }
        } catch (e: Exception) {
            Result.success(getFallbackMarketplaceProducts(category, query))
        }
    }

    suspend fun createMarketplaceProduct(
        userId: String,
        title: String,
        price: Double,
        category: String,
        condition: String,
        location: String,
        description: String,
        imageUrl: String?
    ): Result<com.example.data.model.MarketplaceProduct> {
        return try {
            val user = userDao.getLoggedInUser()?.toModel()
            val effectiveUserId = userId.ifBlank { user?.id ?: ApiClient.currentUserId ?: "1" }
            val cleanId = effectiveUserId.removePrefix("user_").trim().ifBlank { "1" }
            val resp = ApiClient.apiService.createMarketplaceProduct(
                userId = cleanId,
                title = title,
                price = price,
                category = category,
                condition = condition,
                location = location,
                description = description,
                imageUrl = imageUrl
            )
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                // If offline or network issue, create local model so user sees immediate success!
                val fallbackItem = com.example.data.model.MarketplaceProduct(
                    id = "mkt_" + System.currentTimeMillis(),
                    userId = cleanId,
                    title = title,
                    price = price,
                    formattedPrice = "৳ " + price.toInt(),
                    currency = "BDT",
                    category = category,
                    condition = condition,
                    location = location,
                    description = description,
                    imageUrl = imageUrl,
                    createdAt = "Just now",
                    sellerName = user?.fullName ?: "Me",
                    sellerAvatar = user?.profilePic,
                    sellerVerified = user?.isVerified ?: false
                )
                Result.success(fallbackItem)
            }
        } catch (e: Exception) {
            val user = userDao.getLoggedInUser()?.toModel()
            val fallbackItem = com.example.data.model.MarketplaceProduct(
                id = "mkt_" + System.currentTimeMillis(),
                userId = user?.id ?: "1",
                title = title,
                price = price,
                formattedPrice = "৳ " + price.toInt(),
                currency = "BDT",
                category = category,
                condition = condition,
                location = location,
                description = description,
                imageUrl = imageUrl,
                createdAt = "Just now",
                sellerName = user?.fullName ?: "Me",
                sellerAvatar = user?.profilePic,
                sellerVerified = user?.isVerified ?: false
            )
            Result.success(fallbackItem)
        }
    }

    suspend fun deleteMarketplaceProduct(productId: String, userId: String): Result<Boolean> {
        return try {
            val user = userDao.getLoggedInUser()?.toModel()
            val effectiveUserId = userId.ifBlank { user?.id ?: ApiClient.currentUserId ?: "1" }
            val cleanId = effectiveUserId.removePrefix("user_").trim().ifBlank { "1" }
            val resp = ApiClient.apiService.deleteMarketplaceProduct(productId = productId, userId = cleanId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    private fun getFallbackMarketplaceProducts(category: String?, query: String?): List<com.example.data.model.MarketplaceProduct> {
        return emptyList()
    }

    suspend fun getPageSettings(): Int {
        return try {
            val resp = ApiClient.apiService.getPageSettings()
            if (resp.isSuccessful && resp.body()?.success == true) {
                resp.body()?.maxPagesPerUser ?: 5
            } else 5
        } catch (e: Exception) {
            5
        }
    }

    suspend fun updatePageSettings(limit: Int): Result<Boolean> {
        return try {
            val resp = ApiClient.apiService.updatePageSettings(limit)
            Result.success(resp.isSuccessful && resp.body()?.success == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaderboard(limit: Int = 20): List<com.example.data.model.LeaderboardUser> {
        return try {
            val resp = ApiClient.apiService.getLeaderboard(limit)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                resp.body()!!.data.take(limit)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.w("FlikataLeaderboard", "Network error fetching leaderboard: ${e.message}")
            emptyList()
        }
    }

    suspend fun getLeaderboardSettings(): Int {
        return try {
            val resp = ApiClient.apiService.getLeaderboardSettings()
            if (resp.isSuccessful && resp.body()?.success == true) {
                resp.body()?.userLimit ?: 20
            } else 20
        } catch (e: Exception) {
            20
        }
    }

    suspend fun updateLeaderboardSettings(limit: Int): Result<Boolean> {
        return try {
            val resp = ApiClient.apiService.updateLeaderboardSettings(limit)
            Result.success(resp.isSuccessful && resp.body()?.success == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFollowUser(targetUserId: Int): Result<com.example.data.model.ToggleFollowData?> {
        return try {
            val resp = ApiClient.apiService.toggleFollowUser(targetUserId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.data)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to update follow status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkAppUpdate(currentVersionCode: Int, currentVersionName: String): Result<com.example.data.model.AppUpdateResponse?> {
        return try {
            val resp = ApiClient.apiService.checkAppUpdate(currentVersionCode, currentVersionName)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body())
            } else {
                Result.failure(Exception(resp.body()?.message ?: "No update info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFeatureSettings(): Result<com.example.data.model.FeatureSettingsResponse> {
        return try {
            val resp = ApiClient.apiService.getFeatureSettings()
            if (resp.isSuccessful && resp.body() != null) {
                Result.success(resp.body()!!)
            } else {
                Result.success(com.example.data.model.FeatureSettingsResponse())
            }
        } catch (e: Exception) {
            Result.success(com.example.data.model.FeatureSettingsResponse())
        }
    }
}


private fun UserEntity.toModel() = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    emailOrPhone = emailOrPhone,
    dob = dob,
    gender = gender,
    profilePic = profilePic,
    coverPic = coverPic,
    isVerified = isVerified,
    bio = bio,
    followersCount = followersCount,
    followingCount = followingCount,
    postsCount = postsCount,
    joinedDate = joinedDate,
    authToken = authToken
)

private fun User.toEntity(isLoggedIn: Boolean = true) = UserEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    emailOrPhone = emailOrPhone,
    dob = dob,
    gender = gender,
    profilePic = profilePic,
    coverPic = coverPic,
    isVerified = isVerified,
    bio = bio,
    followersCount = followersCount,
    followingCount = followingCount,
    postsCount = postsCount,
    joinedDate = joinedDate,
    authToken = authToken,
    isLoggedIn = isLoggedIn
)

private fun PostEntity.toModel(): Post {
    return Post(
        id = id,
        userId = userId,
        pageId = pageId,
        authorName = authorName,
        authorAvatar = authorAvatar,
        isVerified = isVerified,
        timestamp = timestamp,
        content = content,
        backgroundStyle = backgroundStyle,
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        isVideo = isVideo,
        likesCount = likesCount,
        commentsCount = commentsCount,
        sharesCount = sharesCount,
        isLiked = isLiked,
        userReaction = userReaction,
        reactions = emptyList(),
        commentsList = emptyList(),
        privacy = privacy
    )
}

private fun Post.toEntity() = PostEntity(
    id = id,
    userId = userId,
    pageId = pageId,
    authorName = authorName,
    authorAvatar = authorAvatar,
    isVerified = isVerified,
    timestamp = timestamp,
    content = content,
    backgroundStyle = backgroundStyle,
    imageUrl = imageUrl,
    videoUrl = videoUrl,
    isVideo = isVideo,
    likesCount = likesCount,
    commentsCount = commentsCount,
    sharesCount = sharesCount,
    isLiked = isLiked,
    userReaction = userReaction,
    privacy = privacy
)

private fun ChatEntity.toModel() = ChatMessage(
    id = id,
    threadIdRaw = threadId,
    senderIdRaw = if (senderId.isNotBlank()) senderId else id,
    senderNameRaw = senderName,
    senderAvatarRaw = senderAvatar,
    isVerifiedRaw = isVerified,
    message = lastMessage,
    timestamp = timestamp,
    isVoiceMessage = isVoiceMessage,
    isMissedCall = isMissedCall,
    unreadCountRaw = unreadCount
)

private fun ChatMessage.toEntity(): ChatEntity {
    val cleanSenderId = senderId.removePrefix("partner_").removePrefix("user_").trim()
    val partnerKey = if (cleanSenderId.isNotBlank() && cleanSenderId != "partner") cleanSenderId else id
    return ChatEntity(
        id = partnerKey,
        threadId = threadId,
        senderId = partnerKey,
        senderName = senderName,
        senderAvatar = senderAvatar,
        isVerified = isVerified,
        lastMessage = message,
        timestamp = timestamp,
        isVoiceMessage = isVoiceMessage,
        isMissedCall = isMissedCall,
        unreadCount = unreadCount
    )
}

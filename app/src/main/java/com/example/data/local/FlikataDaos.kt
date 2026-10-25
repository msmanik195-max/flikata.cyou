package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY CASE WHEN id LIKE 'post_%' THEN CAST(SUBSTR(id, 6) AS INTEGER) WHEN id GLOB '[0-9]*' THEN CAST(id AS INTEGER) ELSE 0 END DESC, createdAt DESC, id DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY CASE WHEN id LIKE 'post_%' THEN CAST(SUBSTR(id, 6) AS INTEGER) WHEN id GLOB '[0-9]*' THEN CAST(id AS INTEGER) ELSE 0 END DESC, createdAt DESC, id DESC")
    suspend fun getAllPostsList(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE videoUrl IS NOT NULL AND videoUrl != '' ORDER BY CASE WHEN id LIKE 'post_%' THEN CAST(SUBSTR(id, 6) AS INTEGER) WHEN id GLOB '[0-9]*' THEN CAST(id AS INTEGER) ELSE 0 END DESC, createdAt DESC, id DESC")
    suspend fun getVideoPosts(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("""
        UPDATE posts 
        SET userReaction = :emoji, 
            likesCount = CASE 
                WHEN :emoji IS NULL AND isLiked = 1 THEN MAX(0, likesCount - 1)
                WHEN :emoji IS NOT NULL AND isLiked = 0 THEN likesCount + 1
                ELSE likesCount
            END,
            isLiked = CASE WHEN :emoji IS NULL THEN 0 ELSE 1 END 
        WHERE id = :postId OR id = 'post_' || :postId OR ('post_' || id) = :postId
    """)
    suspend fun updateReaction(postId: String, emoji: String?)

    @Query("UPDATE posts SET likesCount = CASE WHEN isLiked = 1 THEN likesCount - 1 ELSE likesCount + 1 END, isLiked = CASE WHEN isLiked = 1 THEN 0 ELSE 1 END WHERE id = :postId OR id = 'post_' || :postId OR ('post_' || id) = :postId")
    suspend fun toggleLike(postId: String)

    @Query("UPDATE posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentsCount(postId: String)

    @Query("UPDATE posts SET content = :content, privacy = :privacy WHERE id = :postId")
    suspend fun updatePost(postId: String, content: String, privacy: String)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("DELETE FROM posts")
    suspend fun clearPosts()

    @androidx.room.Transaction
    suspend fun syncAllPosts(posts: List<PostEntity>) {
        if (posts.isEmpty()) return
        val newIds = posts.map { it.id }.toSet()
        val current = getAllPostsList()
        for (item in current) {
            if (item.id !in newIds) {
                deletePost(item.id)
            }
        }
        insertPosts(posts)
    }
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY timestamp DESC, id DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("DELETE FROM chats")
    suspend fun clearChats()

    @androidx.room.Transaction
    suspend fun syncAllChats(chats: List<ChatEntity>) {
        clearChats()
        if (chats.isNotEmpty()) {
            insertChats(chats)
        }
    }
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM view_history WHERE itemType = :type ORDER BY viewedAt DESC")
    fun getHistoryByType(type: String): Flow<List<ViewHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ViewHistoryEntity)

    @Query("DELETE FROM view_history WHERE id = :id")
    suspend fun deleteHistory(id: String)

    @Query("DELETE FROM view_history WHERE itemType = :type")
    suspend fun clearHistoryByType(type: String)

    @Query("DELETE FROM view_history")
    suspend fun clearAllHistory()
}

@Dao
interface SavedDao {
    @Query("SELECT * FROM saved_items WHERE itemType = :type ORDER BY savedAt DESC")
    fun getSavedItemsByType(type: String): Flow<List<SavedItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_items WHERE id = :id OR originalId = :id)")
    fun isItemSaved(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_items WHERE id = :id OR originalId = :id)")
    suspend fun isItemSavedDirect(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedItem(item: SavedItemEntity)

    @Query("DELETE FROM saved_items WHERE id = :id OR originalId = :id")
    suspend fun deleteSavedItem(id: String)

    @Query("DELETE FROM saved_items WHERE itemType = :type")
    suspend fun clearSavedByType(type: String)
}

@Dao
interface PageDao {
    @Query("SELECT * FROM pages ORDER BY createdAt DESC")
    fun getAllPagesFlow(): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages ORDER BY createdAt DESC")
    suspend fun getAllPages(): List<PageEntity>

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    suspend fun getPageById(id: String): PageEntity?

    @Query("SELECT * FROM pages WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPagesByUser(userId: String): Flow<List<PageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPages(pages: List<PageEntity>)

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun deletePage(id: String)
}


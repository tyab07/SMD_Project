package com.example.fastconnect.firebase

import com.example.fastconnect.models.Bookmark
import com.example.fastconnect.models.BookmarkFolder
import com.example.fastconnect.models.Society
import com.example.fastconnect.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FirebaseHelper — Central data access layer for Firebase Realtime Database (F2).
 *
 * Replaces FastConnectDbHelper (SQLite) with cloud-based real-time operations.
 * All operations are asynchronous via Firebase callbacks.
 *
 * Database Structure:
 *   /users/{uid}/name, email, role
 *   /users/{uid}/folders/{folderId}/name, createdAt
 *   /users/{uid}/bookmarks/{bookmarkId}/title, url, note, folderId, createdAt
 *   /users/{uid}/followedSocieties/{societyId}: true
 *   /users/{uid}/savedEvents/{eventId}: true
 *   /societies/{pushId}/name, description
 *   /announcements/{pushId}/title, description, category, type, date, societyId
 */
object FirebaseHelper {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // ==================== References ====================

    private fun usersRef(): DatabaseReference = database.reference.child("users")
    private fun societiesRef(): DatabaseReference = database.reference.child("societies")
    private fun announcementsRef(): DatabaseReference = database.reference.child("announcements")

    private fun currentUserRef(): DatabaseReference? {
        val uid = auth.currentUser?.uid ?: return null
        return usersRef().child(uid)
    }

    private fun foldersRef(): DatabaseReference? = currentUserRef()?.child("folders")
    private fun bookmarksRef(): DatabaseReference? = currentUserRef()?.child("bookmarks")
    private fun followedSocietiesRef(): DatabaseReference? = currentUserRef()?.child("followedSocieties")
    private fun savedEventsRef(): DatabaseReference? = currentUserRef()?.child("savedEvents")

    // ==================== USER OPERATIONS ====================

    /**
     * Saves user profile to /users/{uid} after registration or Google Sign-In.
     */
    fun saveUserProfile(uid: String, name: String, email: String, role: String = "user", onComplete: (Boolean) -> Unit) {
        val userData = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "role" to role
        )
        usersRef().child(uid).setValue(userData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Retrieves user profile from /users/{uid}.
     */
    fun getUserProfile(uid: String, onResult: (User?) -> Unit) {
        usersRef().child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                onResult(user)
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })
    }

    /**
     * Checks if a user exists by email (used during sign-up validation).
     */
    fun checkUserExistsByEmail(email: String, onResult: (Boolean) -> Unit) {
        usersRef().orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.exists())
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(false)
                }
            })
    }

    // ==================== SOCIETY OPERATIONS ====================

    /**
     * Adds a new society to /societies/{pushId}.
     */
    fun addSociety(name: String, description: String, onComplete: (Boolean) -> Unit) {
        val ref = societiesRef().push()
        val society = mapOf(
            "id" to ref.key,
            "name" to name,
            "description" to description
        )
        ref.setValue(society)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Listens for real-time updates on /societies (F2 — real-time sync).
     */
    fun observeSocieties(onUpdate: (List<Society>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val societies = mutableListOf<Society>()
                for (child in snapshot.children) {
                    val society = child.getValue(Society::class.java)
                    if (society != null) {
                        societies.add(society.copy(id = child.key ?: ""))
                    }
                }
                onUpdate(societies)
            }
            override fun onCancelled(error: DatabaseError) {
                onUpdate(emptyList())
            }
        }
        societiesRef().addValueEventListener(listener)
        return listener
    }

    /**
     * Removes the society listener when the UI is destroyed.
     */
    fun removeSocietiesListener(listener: ValueEventListener) {
        societiesRef().removeEventListener(listener)
    }

    /**
     * Fetches all societies once (for spinners, etc.).
     */
    fun getAllSocieties(onResult: (List<Society>) -> Unit) {
        societiesRef().addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val societies = mutableListOf<Society>()
                for (child in snapshot.children) {
                    val society = child.getValue(Society::class.java)
                    if (society != null) {
                        societies.add(society.copy(id = child.key ?: ""))
                    }
                }
                onResult(societies)
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    // ==================== ANNOUNCEMENT OPERATIONS ====================

    /**
     * Adds a new announcement to /announcements/{pushId}.
     */
    fun addAnnouncement(
        title: String, description: String, category: String,
        type: String, date: String, societyId: String?,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = announcementsRef().push()
        val announcement = mutableMapOf<String, Any?>(
            "id" to ref.key,
            "title" to title,
            "description" to description,
            "category" to category,
            "type" to type,
            "date" to date
        )
        if (societyId != null) {
            announcement["societyId"] = societyId
        }
        ref.setValue(announcement)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Listens for real-time updates on /announcements (F2 — real-time sync).
     * Optionally filter by type ("event" or "announcement").
     */
    fun observeAnnouncements(
        typeFilter: String? = null,
        onUpdate: (List<Map<String, Any?>>) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements = mutableListOf<Map<String, Any?>>()
                for (child in snapshot.children) {
                    val map = child.value as? Map<String, Any?> ?: continue
                    val mutableMap = map.toMutableMap()
                    mutableMap["id"] = child.key
                    if (typeFilter == null || mutableMap["type"] == typeFilter) {
                        announcements.add(mutableMap)
                    }
                }
                onUpdate(announcements)
            }
            override fun onCancelled(error: DatabaseError) {
                onUpdate(emptyList())
            }
        }
        announcementsRef().addValueEventListener(listener)
        return listener
    }

    /**
     * Removes the announcements listener.
     */
    fun removeAnnouncementsListener(listener: ValueEventListener) {
        announcementsRef().removeEventListener(listener)
    }

    // ==================== FOLDER OPERATIONS ====================

    /**
     * Creates a new bookmark folder at /users/{uid}/folders/{pushId}.
     */
    fun insertFolder(name: String, onComplete: (Boolean, String?) -> Unit) {
        val ref = foldersRef() ?: run { onComplete(false, null); return }
        val pushRef = ref.push()
        val folder = mapOf(
            "id" to pushRef.key,
            "name" to name,
            "createdAt" to getCurrentTimestamp()
        )
        pushRef.setValue(folder)
            .addOnSuccessListener { onComplete(true, pushRef.key) }
            .addOnFailureListener { onComplete(false, null) }
    }

    /**
     * Retrieves all folders for the current user.
     */
    fun getAllFolders(onResult: (List<BookmarkFolder>) -> Unit) {
        val ref = foldersRef() ?: run { onResult(emptyList()); return }
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val folders = mutableListOf<BookmarkFolder>()
                for (child in snapshot.children) {
                    val folder = child.getValue(BookmarkFolder::class.java)
                    if (folder != null) {
                        folders.add(folder.copy(id = child.key ?: ""))
                    }
                }
                // Sort alphabetically
                folders.sortBy { it.name.lowercase() }
                onResult(folders)
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    /**
     * Deletes a folder and all its bookmarks.
     */
    fun deleteFolder(folderId: String, onComplete: (Boolean) -> Unit) {
        val ref = foldersRef() ?: run { onComplete(false); return }
        ref.child(folderId).removeValue()
            .addOnSuccessListener {
                // Also delete bookmarks in this folder
                deleteBookmarksByFolder(folderId) { onComplete(true) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Updates a folder's name.
     */
    fun updateFolder(folderId: String, newName: String, onComplete: (Boolean) -> Unit) {
        val ref = foldersRef() ?: run { onComplete(false); return }
        ref.child(folderId).child("name").setValue(newName)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Creates default folders for a new user.
     */
    fun createDefaultFolders(uid: String) {
        val ref = usersRef().child(uid).child("folders")
        val now = getCurrentTimestamp()
        val defaults = listOf("General", "University", "Technology", "Saved News")
        for (name in defaults) {
            val pushRef = ref.push()
            pushRef.setValue(mapOf(
                "id" to pushRef.key,
                "name" to name,
                "createdAt" to now
            ))
        }
    }

    // ==================== BOOKMARK OPERATIONS ====================

    /**
     * Inserts a new bookmark at /users/{uid}/bookmarks/{pushId}.
     */
    fun insertBookmark(
        title: String, url: String, note: String, folderId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = bookmarksRef() ?: run { onComplete(false); return }
        val pushRef = ref.push()
        val bookmark = mapOf(
            "id" to pushRef.key,
            "title" to title,
            "url" to url,
            "note" to note,
            "folderId" to folderId,
            "createdAt" to getCurrentTimestamp()
        )
        pushRef.setValue(bookmark)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Retrieves all bookmarks for the current user with folder names resolved.
     */
    fun getAllBookmarks(onResult: (List<Bookmark>) -> Unit) {
        val bRef = bookmarksRef() ?: run { onResult(emptyList()); return }
        val fRef = foldersRef() ?: run { onResult(emptyList()); return }

        // First get folders to resolve names
        fRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(foldersSnapshot: DataSnapshot) {
                val folderMap = mutableMapOf<String, String>()
                for (child in foldersSnapshot.children) {
                    val name = child.child("name").getValue(String::class.java) ?: "Unknown"
                    folderMap[child.key ?: ""] = name
                }

                // Then get bookmarks
                bRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(bookmarksSnapshot: DataSnapshot) {
                        val bookmarks = mutableListOf<Bookmark>()
                        for (child in bookmarksSnapshot.children) {
                            val bookmark = child.getValue(Bookmark::class.java)
                            if (bookmark != null) {
                                val folderName = folderMap[bookmark.folderId] ?: "Unknown"
                                bookmarks.add(bookmark.copy(
                                    id = child.key ?: "",
                                    folderName = folderName
                                ))
                            }
                        }
                        // Sort by createdAt descending (newest first)
                        bookmarks.sortByDescending { it.createdAt }
                        onResult(bookmarks)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        onResult(emptyList())
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }

    /**
     * Retrieves a single bookmark by ID.
     */
    fun getBookmarkById(bookmarkId: String, onResult: (Bookmark?) -> Unit) {
        val ref = bookmarksRef() ?: run { onResult(null); return }
        ref.child(bookmarkId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onResult(snapshot.getValue(Bookmark::class.java)?.copy(id = snapshot.key ?: ""))
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })
    }

    /**
     * Updates an existing bookmark.
     */
    fun updateBookmark(
        id: String, title: String, url: String, note: String, folderId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = bookmarksRef() ?: run { onComplete(false); return }
        val updates = mapOf(
            "title" to title,
            "url" to url,
            "note" to note,
            "folderId" to folderId
        )
        ref.child(id).updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Deletes a bookmark by ID.
     */
    fun deleteBookmark(id: String, onComplete: (Boolean) -> Unit) {
        val ref = bookmarksRef() ?: run { onComplete(false); return }
        ref.child(id).removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Deletes all bookmarks in a given folder (used when deleting a folder).
     */
    private fun deleteBookmarksByFolder(folderId: String, onComplete: () -> Unit) {
        val ref = bookmarksRef() ?: run { onComplete(); return }
        ref.orderByChild("folderId").equalTo(folderId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                    onComplete()
                }
                override fun onCancelled(error: DatabaseError) {
                    onComplete()
                }
            })
    }

    /**
     * Searches bookmarks by title, url, or note containing the query.
     */
    fun searchBookmarks(query: String, onResult: (List<Bookmark>) -> Unit) {
        getAllBookmarks { bookmarks ->
            if (query.isEmpty()) {
                onResult(bookmarks)
            } else {
                val lowerQuery = query.lowercase()
                onResult(bookmarks.filter {
                    it.title.lowercase().contains(lowerQuery) ||
                    it.url.lowercase().contains(lowerQuery) ||
                    it.note.lowercase().contains(lowerQuery)
                })
            }
        }
    }

    /**
     * Retrieves bookmarks sorted by a specific field.
     */
    fun getBookmarksSorted(sortField: String, ascending: Boolean, onResult: (List<Bookmark>) -> Unit) {
        getAllBookmarks { bookmarks ->
            val sorted = when (sortField) {
                "title" -> if (ascending) bookmarks.sortedBy { it.title.lowercase() }
                           else bookmarks.sortedByDescending { it.title.lowercase() }
                "createdAt" -> if (ascending) bookmarks.sortedBy { it.createdAt }
                               else bookmarks.sortedByDescending { it.createdAt }
                else -> bookmarks
            }
            onResult(sorted)
        }
    }

    /**
     * Retrieves bookmarks filtered by folder.
     */
    fun getBookmarksByFolder(folderId: String, onResult: (List<Bookmark>) -> Unit) {
        getAllBookmarks { bookmarks ->
            onResult(bookmarks.filter { it.folderId == folderId })
        }
    }

    /**
     * Gets the count of bookmarks in a folder.
     */
    fun getBookmarkCountByFolder(folderId: String, onResult: (Int) -> Unit) {
        val ref = bookmarksRef() ?: run { onResult(0); return }
        ref.orderByChild("folderId").equalTo(folderId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.childrenCount.toInt())
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(0)
                }
            })
    }

    // ==================== FOLLOW / SAVE OPERATIONS ====================

    /**
     * Follows a society — writes to /users/{uid}/followedSocieties/{societyId}.
     */
    fun followSociety(societyId: String, onComplete: (Boolean) -> Unit) {
        val ref = followedSocietiesRef() ?: run { onComplete(false); return }
        ref.child(societyId).setValue(true)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Saves an event — writes to /users/{uid}/savedEvents/{eventId}.
     */
    fun saveEvent(eventId: String, onComplete: (Boolean) -> Unit) {
        val ref = savedEventsRef() ?: run { onComplete(false); return }
        ref.child(eventId).setValue(true)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // ==================== UTILITY ====================

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}

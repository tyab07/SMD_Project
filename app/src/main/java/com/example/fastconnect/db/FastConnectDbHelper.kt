package com.example.fastconnect.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.fastconnect.models.Bookmark
import com.example.fastconnect.models.BookmarkFolder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SQLiteOpenHelper subclass for FastConnect local database (Requirements F2, F3, F5).
 *
 * F2: Two tables (folders, bookmarks) with Foreign Key relationship.
 * F3: Full CRUD operations (Create, Read, Update, Delete).
 * F5: Dynamic SQL queries using LIKE (search) and ORDER BY (sort).
 *
 * All public methods are designed to be called from coroutines (Dispatchers.IO).
 */
class FastConnectDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "fastconnect.db"
        private const val DATABASE_VERSION = 1

        // ----- Folders Table (Parent) -----
        const val TABLE_FOLDERS = "folders"
        const val COL_FOLDER_ID = "_id"
        const val COL_FOLDER_NAME = "name"
        const val COL_FOLDER_CREATED_AT = "created_at"

        // ----- Bookmarks Table (Child — references folders via FK) -----
        const val TABLE_BOOKMARKS = "bookmarks"
        const val COL_BOOKMARK_ID = "_id"
        const val COL_BOOKMARK_TITLE = "title"
        const val COL_BOOKMARK_URL = "url"
        const val COL_BOOKMARK_NOTE = "note"
        const val COL_BOOKMARK_FOLDER_ID = "folder_id"
        const val COL_BOOKMARK_CREATED_AT = "created_at"
    }

    /**
     * F2: Creates two tables with Primary Keys (AUTOINCREMENT) and a Foreign Key
     * linking bookmarks.folder_id → folders._id.
     */
    override fun onCreate(db: SQLiteDatabase) {
        // Enable foreign key support
        db.execSQL("PRAGMA foreign_keys = ON;")

        // Create folders table (parent table)
        val createFoldersTable = """
            CREATE TABLE $TABLE_FOLDERS (
                $COL_FOLDER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FOLDER_NAME TEXT NOT NULL UNIQUE,
                $COL_FOLDER_CREATED_AT TEXT NOT NULL
            );
        """.trimIndent()

        // Create bookmarks table (child table with FK to folders)
        val createBookmarksTable = """
            CREATE TABLE $TABLE_BOOKMARKS (
                $COL_BOOKMARK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BOOKMARK_TITLE TEXT NOT NULL,
                $COL_BOOKMARK_URL TEXT NOT NULL,
                $COL_BOOKMARK_NOTE TEXT DEFAULT '',
                $COL_BOOKMARK_FOLDER_ID INTEGER NOT NULL,
                $COL_BOOKMARK_CREATED_AT TEXT NOT NULL,
                FOREIGN KEY ($COL_BOOKMARK_FOLDER_ID) REFERENCES $TABLE_FOLDERS($COL_FOLDER_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        db.execSQL(createFoldersTable)
        db.execSQL(createBookmarksTable)

        // Insert default folders so the user has something to start with
        val now = getCurrentTimestamp()
        insertDefaultFolder(db, "General", now)
        insertDefaultFolder(db, "University", now)
        insertDefaultFolder(db, "Technology", now)
        insertDefaultFolder(db, "Saved News", now)
    }

    private fun insertDefaultFolder(db: SQLiteDatabase, name: String, timestamp: String) {
        val values = ContentValues().apply {
            put(COL_FOLDER_NAME, name)
            put(COL_FOLDER_CREATED_AT, timestamp)
        }
        db.insert(TABLE_FOLDERS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKMARKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOLDERS")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Ensure foreign keys are enforced every time DB opens
        db.execSQL("PRAGMA foreign_keys = ON;")
    }

    // ==================== FOLDER CRUD (F3) ====================

    /**
     * F3 - Create: Inserts a new folder into the folders table.
     * @return the row ID of the newly inserted folder, or -1 if error
     */
    fun insertFolder(name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FOLDER_NAME, name)
            put(COL_FOLDER_CREATED_AT, getCurrentTimestamp())
        }
        return db.insert(TABLE_FOLDERS, null, values)
    }

    /**
     * F3 - Read: Retrieves all folders from the database.
     * @return List of BookmarkFolder objects
     */
    fun getAllFolders(): List<BookmarkFolder> {
        val folders = mutableListOf<BookmarkFolder>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_FOLDERS ORDER BY $COL_FOLDER_NAME ASC", null
        )
        cursor.use {
            while (it.moveToNext()) {
                folders.add(cursorToFolder(it))
            }
        }
        return folders
    }

    /**
     * F3 - Delete: Deletes a folder and all its bookmarks (ON DELETE CASCADE).
     * @return number of rows affected
     */
    fun deleteFolder(folderId: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_FOLDERS, "$COL_FOLDER_ID = ?", arrayOf(folderId.toString()))
    }

    /**
     * F3 - Update: Renames an existing folder.
     * @return number of rows affected
     */
    fun updateFolder(folderId: Long, newName: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FOLDER_NAME, newName)
        }
        return db.update(TABLE_FOLDERS, values, "$COL_FOLDER_ID = ?", arrayOf(folderId.toString()))
    }

    // ==================== BOOKMARK CRUD (F3) ====================

    /**
     * F3 - Create: Inserts a new bookmark into the bookmarks table.
     * @return the row ID of the newly inserted bookmark, or -1 if error
     */
    fun insertBookmark(title: String, url: String, note: String, folderId: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_BOOKMARK_TITLE, title)
            put(COL_BOOKMARK_URL, url)
            put(COL_BOOKMARK_NOTE, note)
            put(COL_BOOKMARK_FOLDER_ID, folderId)
            put(COL_BOOKMARK_CREATED_AT, getCurrentTimestamp())
        }
        return db.insert(TABLE_BOOKMARKS, null, values)
    }

    /**
     * F3 - Read: Retrieves all bookmarks with their folder names using a JOIN.
     * @return List of Bookmark objects with folderName populated
     */
    fun getAllBookmarks(): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()
        val db = readableDatabase
        val query = """
            SELECT b.*, f.$COL_FOLDER_NAME AS folder_name
            FROM $TABLE_BOOKMARKS b
            LEFT JOIN $TABLE_FOLDERS f ON b.$COL_BOOKMARK_FOLDER_ID = f.$COL_FOLDER_ID
            ORDER BY b.$COL_BOOKMARK_CREATED_AT DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                bookmarks.add(cursorToBookmark(it))
            }
        }
        return bookmarks
    }

    /**
     * F3 - Read: Retrieves a single bookmark by its ID.
     * @return Bookmark object or null
     */
    fun getBookmarkById(bookmarkId: Long): Bookmark? {
        val db = readableDatabase
        val query = """
            SELECT b.*, f.$COL_FOLDER_NAME AS folder_name
            FROM $TABLE_BOOKMARKS b
            LEFT JOIN $TABLE_FOLDERS f ON b.$COL_BOOKMARK_FOLDER_ID = f.$COL_FOLDER_ID
            WHERE b.$COL_BOOKMARK_ID = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(bookmarkId.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToBookmark(it)
            }
        }
        return null
    }

    /**
     * F3 - Update: Updates an existing bookmark's fields.
     * @return number of rows affected
     */
    fun updateBookmark(id: Long, title: String, url: String, note: String, folderId: Long): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_BOOKMARK_TITLE, title)
            put(COL_BOOKMARK_URL, url)
            put(COL_BOOKMARK_NOTE, note)
            put(COL_BOOKMARK_FOLDER_ID, folderId)
        }
        return db.update(TABLE_BOOKMARKS, values, "$COL_BOOKMARK_ID = ?", arrayOf(id.toString()))
    }

    /**
     * F3 - Delete: Removes a bookmark from the database.
     * @return number of rows affected
     */
    fun deleteBookmark(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_BOOKMARKS, "$COL_BOOKMARK_ID = ?", arrayOf(id.toString()))
    }

    // ==================== DYNAMIC SQL QUERIES (F5) ====================

    /**
     * F5 - Search: Finds bookmarks whose title, URL, or note contains the query string.
     * Uses SQL LIKE operator for pattern matching.
     */
    fun searchBookmarks(searchQuery: String): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()
        val db = readableDatabase
        val query = """
            SELECT b.*, f.$COL_FOLDER_NAME AS folder_name
            FROM $TABLE_BOOKMARKS b
            LEFT JOIN $TABLE_FOLDERS f ON b.$COL_BOOKMARK_FOLDER_ID = f.$COL_FOLDER_ID
            WHERE b.$COL_BOOKMARK_TITLE LIKE ?
               OR b.$COL_BOOKMARK_URL LIKE ?
               OR b.$COL_BOOKMARK_NOTE LIKE ?
            ORDER BY b.$COL_BOOKMARK_CREATED_AT DESC
        """.trimIndent()
        val likePattern = "%$searchQuery%"
        val cursor = db.rawQuery(query, arrayOf(likePattern, likePattern, likePattern))
        cursor.use {
            while (it.moveToNext()) {
                bookmarks.add(cursorToBookmark(it))
            }
        }
        return bookmarks
    }

    /**
     * F5 - Sort: Retrieves bookmarks sorted by a specified column and order.
     * Uses SQL ORDER BY for dynamic sorting.
     * @param sortColumn Column to sort by (title, created_at, url)
     * @param ascending Whether to sort ascending (true) or descending (false)
     */
    fun getBookmarksSorted(sortColumn: String, ascending: Boolean): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()
        val db = readableDatabase
        // Validate column name to prevent SQL injection
        val validColumn = when (sortColumn) {
            COL_BOOKMARK_TITLE, COL_BOOKMARK_CREATED_AT, COL_BOOKMARK_URL -> sortColumn
            else -> COL_BOOKMARK_CREATED_AT
        }
        val order = if (ascending) "ASC" else "DESC"
        val query = """
            SELECT b.*, f.$COL_FOLDER_NAME AS folder_name
            FROM $TABLE_BOOKMARKS b
            LEFT JOIN $TABLE_FOLDERS f ON b.$COL_BOOKMARK_FOLDER_ID = f.$COL_FOLDER_ID
            ORDER BY b.$validColumn $order
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                bookmarks.add(cursorToBookmark(it))
            }
        }
        return bookmarks
    }

    /**
     * F5 - Filter: Retrieves bookmarks belonging to a specific folder.
     * Uses SQL WHERE clause for folder-based filtering.
     */
    fun getBookmarksByFolder(folderId: Long): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()
        val db = readableDatabase
        val query = """
            SELECT b.*, f.$COL_FOLDER_NAME AS folder_name
            FROM $TABLE_BOOKMARKS b
            LEFT JOIN $TABLE_FOLDERS f ON b.$COL_BOOKMARK_FOLDER_ID = f.$COL_FOLDER_ID
            WHERE b.$COL_BOOKMARK_FOLDER_ID = ?
            ORDER BY b.$COL_BOOKMARK_CREATED_AT DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(folderId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                bookmarks.add(cursorToBookmark(it))
            }
        }
        return bookmarks
    }

    /**
     * Gets the count of bookmarks in a folder.
     */
    fun getBookmarkCountByFolder(folderId: Long): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_BOOKMARKS WHERE $COL_BOOKMARK_FOLDER_ID = ?",
            arrayOf(folderId.toString())
        )
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0)
        }
        return 0
    }

    // ==================== HELPER METHODS ====================

    private fun cursorToFolder(cursor: Cursor): BookmarkFolder {
        return BookmarkFolder(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FOLDER_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOLDER_NAME)),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOLDER_CREATED_AT))
        )
    }

    private fun cursorToBookmark(cursor: Cursor): Bookmark {
        val folderNameIndex = cursor.getColumnIndex("folder_name")
        return Bookmark(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOKMARK_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOKMARK_TITLE)),
            url = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOKMARK_URL)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOKMARK_NOTE)) ?: "",
            folderId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOKMARK_FOLDER_ID)),
            folderName = if (folderNameIndex >= 0) cursor.getString(folderNameIndex) ?: "Unknown" else "Unknown",
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOKMARK_CREATED_AT))
        )
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}

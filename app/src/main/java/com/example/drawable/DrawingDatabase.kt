package com.example.drawable

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities= [DrawingPath::class], version = 3, exportSchema = false)

abstract class  DrawingDatabase  : RoomDatabase(){
    abstract fun drawingDao(): DrawingDAO
}

@Dao
interface DrawingDAO {

    /**
     * Gets a list of drawing paths from the repo
     */
    @Query("SELECT * FROM drawingpaths ORDER BY modDate DESC")
    fun getAllPaths(): Flow<List<DrawingPath>>

    /**
     * Adds the drawing path to the database.
     * Replaces if there is a duplicate entry making sure the newest one is always the one that's there
     * Marked as suspend so the thread can yield in case the DB update is slow
     * @param path The path to add
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(path: DrawingPath)

    /**
     * Deletes the drawing path from the database
     * Marked as suspend so the thread can yield in case the DB update is slow
     * @param path The path to delete
     */
    @Delete
    suspend fun deleteDrawing(path: DrawingPath)

    /**
     * Gets the count of items in the database
     */
    @Query("SELECT COUNT(*) FROM drawingpaths")
    fun getDrawingCount():  Flow<Int>

    @Query("SELECT EXISTS(SELECT * FROM drawingpaths WHERE name = :name)")
    suspend fun doesDrawingExist(name: String): Boolean

    @Query("DELETE FROM drawingpaths")
    suspend fun deleteAllDrawingPaths()

}
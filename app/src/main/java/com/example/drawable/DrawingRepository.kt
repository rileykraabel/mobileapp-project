package com.example.drawable

import android.graphics.Bitmap
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.Exception
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.BitmapFactory
import android.graphics.DiscretePathEffect
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DrawingRepository(
    private val scope: CoroutineScope,
    private val dao: DrawingDAO,
    private val context: Context
) {

    //updated when the DB is modified
    val paths: Flow<List<DrawingPath>> = dao.getAllPaths()
    val drawings = paths.map {
        it.map { drawingPath ->
            return@map loadDrawing(drawingPath)
        }
    }
    val count: Flow<Int> = dao.getDrawingCount()


    /**
     * Method that handles adding a drawing to the database.
     * @param: drawing: Drawing object to save to the database.
     */
    suspend fun saveDrawing(drawing: Drawing) {
        val date = saveBitmapToFile(drawing.bitmap, drawing.dPath.name)
        val imageEntity = DrawingPath(modDate = date, name = drawing.dPath.name)
        scope.launch {
            dao.insertImage(imageEntity)
        }
    }

    /**
     * Method that handles deleting a drawing from the database.
     * @param path: DrawingPath object representing the unique drawing's attributes.
     */
    suspend fun deleteDrawing(path: DrawingPath) {
        scope.launch {
            dao.deleteDrawing(path)
        }
        context.deleteFile(path.name)
    }

    /**
     * Method that handles loading a drawing from the database, and returning it.
     * @param path: DrawingPath object representing the file and its' unique data.
     * @return Drawing object representing the drawing being returned.
     */
    fun loadDrawing(path: DrawingPath): Drawing {
        val file = File(context.filesDir, path.name)
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        bitmap.recycle()
        return Drawing(mutableBitmap, path)
    }

    /**
     * Method that handles saving a drawing's bitmap to some file location.
     * @param bmp: Bitmap object containing the user's drawing.
     * @param name: String object representing the file's new name.
     * @return Long object representing the file's last modified date.
     */
    private fun saveBitmapToFile(bmp: Bitmap, name: String): Long {
        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput(name, Context.MODE_PRIVATE)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val file = File(context.filesDir, name)
        return file.lastModified()
    }

    suspend fun nameCheck(name: String): Boolean{
        return dao.doesDrawingExist(name)
    }

    suspend fun clearDatabase(){
        dao.deleteAllDrawingPaths()
    }


}
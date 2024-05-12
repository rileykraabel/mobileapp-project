package com.example.drawable

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * This class contains instrumented tests for the Drawing App for CS 4530 - Spring 2024.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var drawingDatabase: DrawingDatabase
    private lateinit var drawingDao: DrawingDAO
    private lateinit var drawingApplication: DrawableApplication

    /**
     * This method is called prior to all other tests, and initializes the database/dao for those.
     */
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        drawingDatabase = Room.inMemoryDatabaseBuilder(context, DrawingDatabase::class.java).build()
        drawingDao = drawingDatabase.drawingDao()
    }

    /**
     * This test verifies that adding an item to the database works as expected.
     */
    @Test
    fun test_add_item() = runBlocking {
        val dPath = DrawingPath(System.currentTimeMillis(), "Riley Test")
        drawingDao.insertImage(dPath)

        val result: Int = drawingDao.getDrawingCount().first()
        assertEquals(1, result)
    }

    /**
     * This test verifies that adding more than one item to the databse works as expected.
     */
    @Test
    fun test_add_few_items() = runBlocking {
        val dPath = DrawingPath(System.currentTimeMillis(), "Riley Test2")
        drawingDao.insertImage(dPath)
        val dPath2 = DrawingPath(System.currentTimeMillis(), "Riley Test3")
        drawingDao.insertImage(dPath2)
        val dPath3 = DrawingPath(System.currentTimeMillis(), "Riley Test4")
        drawingDao.insertImage(dPath3)

        val result: Int = drawingDao.getDrawingCount().first()
        assertEquals(3, result)
    }

    /**
     * This test attempts to overload the insertion operator, and verifies a high number of
     *  drawings can be added successfully.
     */
    @Test
    fun test_add_lots_of_items() = runBlocking {
        for (i in 1..100) {
            val dPath = DrawingPath(System.currentTimeMillis(), "test " + i)
            drawingDao.insertImage(dPath)
        }

        val result: Int = drawingDao.getDrawingCount().first()
        assertEquals(100, result)
    }

//    @Test
//    fun test_add_drawings_with_same_name() = runBlocking{
//        var prev: DrawingPath = DrawingPath(System.currentTimeMillis(), "Drawing " + drawingDao.getDrawingCount())
//        for(i in 1..10){
//            if(i % 3 == 0){
//                drawingDao.deleteDrawing(prev)
//            }
//            val dPath = DrawingPath(System.currentTimeMillis(), "Drawing " + drawingDao.getDrawingCount()+1)
//            prev = dPath
//            if
//            drawingDao.insertImage(dPath)
//        }
//        val result: Int = drawingDao.getDrawingCount().first()
//        assertEquals(10, result)
//    }

//    @Test
//    fun test_add_then_remove() = runBlocking {
//        val dPath = DrawingPath(System.currentTimeMillis(), "Riley Test2")
//        drawingDao.insertImage(dPath)
//        assertEquals(1, drawingDao.getDrawingCount().first())
//
//        drawingDao.deleteDrawing(dPath)
//        assertEquals(0, drawingDao.getDrawingCount().first())
//    }

    /**
     * This test ensures applications are initialized properly, and tests functionalities like color changing.
     */
    @Test
    fun test_more_features() {
        drawingApplication = ApplicationProvider.getApplicationContext()
        val drawingRepository = drawingApplication.drawingRepository
        val vm = DrawableViewModel(drawingRepository)
        assertNotNull(vm)
        assertNotNull(drawingRepository)

        runBlocking {
            val lifecycleOwner = TestLifecycleOwner()
            val before = vm.currColor.value!!
            var callbackFired = false

            lifecycleOwner.run {
                withContext(Dispatchers.Main) {
                    vm.currColor.observe(lifecycleOwner) {
                        callbackFired = true
                    }
                    vm.updateColor(Color.BLUE)
                    assertTrue(callbackFired)

                    assertNotSame(before, vm.currColor.value!!)
                }
            }
        }
    }

    /**
     * This test verifies that adding then deleting from the databse works as expected.
     */
    @Test
    fun test_add_then_delete() = runBlocking {
        for (i in 1..100) {
            val dPath = DrawingPath(System.currentTimeMillis(), "test " + i)
            drawingDao.insertImage(dPath)
        }
        var result = drawingDao.getDrawingCount().first()
        assertEquals(100, result)

        val allPaths = drawingDao.getAllPaths().first()
        allPaths.forEach { path ->
            drawingDao.deleteDrawing(path)
        }

        result = drawingDao.getDrawingCount().first()
        assertEquals(0, result)
    }

    /**
     * This test verifies that adding then deleting a lot from the databse works as expected.
     */
    @Test
    fun test_add_a_lot_then_delete() = runBlocking {
        val dPath = DrawingPath(System.currentTimeMillis(), "Riley Test2")
        drawingDao.insertImage(dPath)
        var result = drawingDao.getDrawingCount().first()
        assertEquals(1, result)

        val allPaths = drawingDao.getAllPaths().first()
        allPaths.forEach { path ->
            drawingDao.deleteDrawing(path)
        }

        result = drawingDao.getDrawingCount().first()
        assertEquals(0, result)
    }

    /**
     * This method is called after each test is run to ensure databases are refreshed and updated for each.
     */
    @After
    fun tearDown() {
        drawingDatabase.close()
    }
}
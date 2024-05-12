package com.example.drawable

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class EspressoTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * This basic test ensures that the main page (of previous drawings) is rendered properly.
     */
    @Test
    fun mainScreen() {
        onView(withId(R.id.add_drawing)).check(matches(isDisplayed()))
        onView(withId(R.id.composeView1)).check(matches(isDisplayed()))
    }

    /**
     * This test ensures that the Drawing Page's back button navigates as expected.
     */
    @Test
    fun testBackButton() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
        onView(withId(R.id.back_button)).perform(click())
        onView(withId(R.id.composeView1)).check(matches(isDisplayed()))
    }

    @Test
    fun testTrianglePenShapeButton() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.trianglePen)).check(matches(isDisplayed()))
        onView(withId(R.id.trianglePen)).perform(click())
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testSquarePenShapeButton() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.squarePen)).check(matches(isDisplayed()))
        onView(withId(R.id.squarePen)).perform(click())
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testRoundPenShapeButton() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.roundPen)).check(matches(isDisplayed()))
        onView(withId(R.id.roundPen)).perform(click())
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testThinPenSizeButton() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.thinPen)).check(matches(isDisplayed()))
        onView(withId(R.id.thinPen)).perform(click())
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testMedPenSizeButton() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.medPen)).check(matches(isDisplayed()))
        onView(withId(R.id.medPen)).perform(click())
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testThickPenSizeButton() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.canvas)).check(matches(isDisplayed()))
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.thickPen)).check(matches(isDisplayed()))
        onView(withId(R.id.thickPen)).perform(click())
        onView(withId(R.id.back_button)).check(matches(isDisplayed()))
    }

    /**
     * This test ensures that the Drawing Page's color palette displays as expected.
     */
    @Test
    fun testColorPicker() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.pallete)).check(matches(isDisplayed()))
        onView(withId(R.id.pallete)).perform(click())
        onView(withId(yuku.ambilwarna.R.id.ambilwarna_dialogView)).check(matches(isDisplayed()))
        onView(withId(yuku.ambilwarna.R.id.ambilwarna_dialogView)).perform(click())
    }

    /**
     * This test ensures that the Drawing Page's paint brush button leads to another dialog
     * popup, and tests that all buttons on it display as expected.
     */
    @Test
    fun testPaintBrushButton() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.medPen)).check(matches(isDisplayed()))
        onView(withId(R.id.thinPen)).perform(click())
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.medPen)).perform(click())
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.thickPen)).perform(click())
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.trianglePen)).perform(click())
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.squarePen)).perform(click())
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.paintBrush)).perform(click())
        onView(withId(R.id.roundPen)).perform(click())
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
    }

    /**
     * This test ensures that the Drawing Page's paint brush works as expected.
     */
    @Test
    fun testPaintBrushFunctionality() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.paintBrush)).check(matches(isDisplayed()))
        onView(withId(R.id.canvas)).perform(longClick(), swipeDown())
        onView(withId(R.id.canvas)).perform(longClick(), swipeLeft())
        onView(withId(R.id.canvas)).perform(longClick(), swipeUp())
        onView(withId(R.id.canvas)).perform(longClick(), swipeRight())
    }

//  tests for the drawings list page //
    @Test
    fun testClickDrawing() {
        onView(withId(R.id.add_drawing)).perform(click())
        onView(withId(R.id.canvas)).perform(longClick(), swipeDown())
        onView(withId(R.id.back_button)).perform(click())
        onView(withId(R.id.composeView1)).check(matches(isDisplayed()))
    }
}
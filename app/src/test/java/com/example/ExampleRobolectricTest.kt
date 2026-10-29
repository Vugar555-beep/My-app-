package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ExerciseType
import com.example.data.repository.CurriculumData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Картули", appName)
    }

    @Test
    fun `verify all 33 letters of Mkhedruli are present`() {
        val letters = CurriculumData.ALL_33_LETTERS
        assertEquals(33, letters.size)
        assertEquals("ა", letters.first().letter)
        assertEquals("ჰ", letters.last().letter)
    }

    @Test
    fun `verify curriculum modules and lessons structure`() {
        val modules = CurriculumData.ALL_MODULES
        assertTrue("Modules must not be empty", modules.isNotEmpty())
        val firstLesson = modules.first().lessons.first()
        assertEquals("les_1_1", firstLesson.id)
        assertTrue("First lesson must have exercises", firstLesson.exercises.isNotEmpty())
    }

    @Test
    fun `verify voice practice exercises and dialogue scenarios are present`() {
        val allExercises = CurriculumData.ALL_MODULES.flatMap { it.lessons }.flatMap { it.exercises }
        val speakExercises = allExercises.filter { it.type == ExerciseType.SPEAK_PRACTICE }
        assertTrue("Must have speaking practice exercises", speakExercises.isNotEmpty())

        val scenarios = CurriculumData.ALL_DIALOGUE_SCENARIOS
        assertTrue("Must have dialogue scenarios", scenarios.isNotEmpty())
        assertTrue("First scenario must have turns", scenarios.first().turns.isNotEmpty())
    }

    @Test
    fun `verify vocabulary database richness and accuracy`() {
        val vocab = CurriculumData.ALL_VOCABULARY
        assertTrue("Vocabulary should have at least 50 core words", vocab.size >= 50)
        assertTrue("Must include key greetings", vocab.any { it.ge == "გამარჯობა" })
        assertTrue("Must include gratitude", vocab.any { it.ge == "მადლობა" })
        assertTrue("Must include culinary heritage", vocab.any { it.ge == "ხაჭაპური" || it.ge == "ხინკალი" })
    }
}

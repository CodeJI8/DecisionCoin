package com.example.decisioncoin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionLogicTest {

    private val logic = DecisionLogic()

    @Test
    fun pickWinner_returnsValidIndex() {
        // Run it a bunch of times to ensure we don't get out of bounds
        for (i in 0..100) {
            val winner2 = logic.pickWinner(2)
            assertTrue(winner2 in 0..1)
            
            val winner3 = logic.pickWinner(3)
            assertTrue(winner3 in 0..2)
            
            val winner4 = logic.pickWinner(4)
            assertTrue(winner4 in 0..3)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun pickWinner_throwsIfLessThan2Options() {
        logic.pickWinner(1)
    }

    @Test
    fun getRelievedMessage_usesOptionName() {
        val options = listOf("Pizza", "Burger")
        val message = logic.getRelievedMessage(1, options)
        assertEquals("Your gut backs it. Go with Burger.", message)
    }

    @Test
    fun getRelievedMessage_usesFallbackNameIfBlank() {
        val options = listOf("Pizza", "   ")
        val message = logic.getRelievedMessage(1, options)
        assertEquals("Your gut backs it. Go with Option 2.", message)
    }

    @Test
    fun getDisappointed2OptionsMessage_usesOptionName() {
        val options = listOf("Pizza", "Burger")
        val message = logic.getDisappointed2OptionsMessage(0, options)
        assertEquals("That disappointment is information. You may have been hoping for Pizza. Consider choosing it.", message)
    }

    @Test
    fun getDisappointedMoreOptionsMessage_usesOptionName() {
        val options = listOf("Pizza", "Burger", "Sushi")
        val message = logic.getDisappointedMoreOptionsMessage(2, options)
        assertEquals("Noted. Your gut was pulling toward Sushi. Consider choosing it.", message)
    }
}

package com.wishINC.decisioncoin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionViewModelTest {

    @Test
    fun addOption_increasesListUpTo4_andForcesWheel() {
        val viewModel = DecisionViewModel()
        
        // Starts with 2 options
        assertEquals(2, viewModel.state.value.options.size)
        assertEquals(DecisionMode.COIN, viewModel.state.value.mode)

        // Add 3rd
        viewModel.addOption()
        assertEquals(3, viewModel.state.value.options.size)
        assertEquals(DecisionMode.WHEEL, viewModel.state.value.mode)

        // Add 4th
        viewModel.addOption()
        assertEquals(4, viewModel.state.value.options.size)
        assertEquals(DecisionMode.WHEEL, viewModel.state.value.mode)

        // Add 5th (should be ignored)
        viewModel.addOption()
        assertEquals(4, viewModel.state.value.options.size)
    }

    @Test
    fun removeOption_decreasesListDownTo2() {
        val viewModel = DecisionViewModel()
        viewModel.addOption() // 3 options
        viewModel.addOption() // 4 options
        
        viewModel.removeOption(3)
        assertEquals(3, viewModel.state.value.options.size)
        assertEquals(DecisionMode.WHEEL, viewModel.state.value.mode)

        viewModel.removeOption(2)
        assertEquals(2, viewModel.state.value.options.size)
        
        // Mode stays WHEEL if it was WHEEL, user can manually change to COIN
        // Actually, logic preserves previous mode if size == 2, so it stays WHEEL
        assertEquals(DecisionMode.WHEEL, viewModel.state.value.mode)

        // Try removing below 2
        viewModel.removeOption(1)
        assertEquals(2, viewModel.state.value.options.size)
    }

    @Test
    fun setMode_forcedWheelWhenMoreThan2() {
        val viewModel = DecisionViewModel()
        viewModel.addOption() // 3 options
        
        viewModel.setMode(DecisionMode.COIN)
        // Should ignore and keep WHEEL
        assertEquals(DecisionMode.WHEEL, viewModel.state.value.mode)

        viewModel.removeOption(2) // back to 2 options
        viewModel.setMode(DecisionMode.COIN)
        assertEquals(DecisionMode.COIN, viewModel.state.value.mode)
    }

    @Test
    fun decide_setsWinnerIndexInRange() {
        val viewModel = DecisionViewModel()
        viewModel.addOption() // 3 options
        
        viewModel.decide()
        val phase = viewModel.state.value.phase
        assertTrue(phase is DecisionPhase.Result)
        
        val winnerIndex = viewModel.state.value.winnerIndex
        assertTrue(winnerIndex != null && winnerIndex in 0..2)
    }

    @Test
    fun feeling_relieved_goesToFeedback() {
        val viewModel = DecisionViewModel()
        viewModel.decide()
        viewModel.onFeeling(Feeling.RELIEVED)
        
        assertTrue(viewModel.state.value.phase is DecisionPhase.Feedback)
        assertEquals(Feeling.RELIEVED, viewModel.state.value.feeling)
    }

    @Test
    fun feeling_disappointed2Options_goesToFeedbackWithHopedFor() {
        val viewModel = DecisionViewModel()
        viewModel.decide() // 2 options
        viewModel.onFeeling(Feeling.DISAPPOINTED)
        
        assertTrue(viewModel.state.value.phase is DecisionPhase.Feedback)
        assertEquals(Feeling.DISAPPOINTED, viewModel.state.value.feeling)
        assertTrue(viewModel.state.value.hopedForIndex != null)
    }

    @Test
    fun feeling_disappointedMoreOptions_waitsForHopedForSelection() {
        val viewModel = DecisionViewModel()
        viewModel.addOption() // 3 options
        viewModel.decide()
        viewModel.onFeeling(Feeling.DISAPPOINTED)
        
        // Stays in Result phase, waits for user selection
        assertTrue(viewModel.state.value.phase is DecisionPhase.Result)
        assertEquals(Feeling.DISAPPOINTED, viewModel.state.value.feeling)
        assertEquals(null, viewModel.state.value.hopedForIndex)

        // User selects hoped for
        viewModel.setHopedFor(1)
        assertTrue(viewModel.state.value.phase is DecisionPhase.Feedback)
        assertEquals(1, viewModel.state.value.hopedForIndex)
    }
}

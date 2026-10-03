package com.wishINC.decisioncoin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

enum class DecisionMode { COIN, WHEEL }
enum class Feeling { RELIEVED, DISAPPOINTED }

sealed interface DecisionPhase {
    object Editing : DecisionPhase
    object Deciding : DecisionPhase
    object Result : DecisionPhase
    object Feedback : DecisionPhase
}

data class DecisionUiState(
    val options: List<String> = listOf("", ""),
    val mode: DecisionMode = DecisionMode.COIN,
    val phase: DecisionPhase = DecisionPhase.Editing,
    val winnerIndex: Int? = null,
    val feeling: Feeling? = null,
    val hopedForIndex: Int? = null
)

class DecisionLogic(private val random: Random = Random) {
    fun pickWinner(optionsSize: Int): Int {
        if (optionsSize < 2) throw IllegalArgumentException("Must have at least 2 options")
        return random.nextInt(optionsSize)
    }

    fun getRelievedMessage(winnerIndex: Int, options: List<String>): String {
        val winner = options.getOrNull(winnerIndex)?.takeIf { it.isNotBlank() } ?: "Option ${winnerIndex + 1}"
        return "Your gut backs it. Go with $winner."
    }

    fun getDisappointed2OptionsMessage(hopedForIndex: Int, options: List<String>): String {
        val hopedFor = options.getOrNull(hopedForIndex)?.takeIf { it.isNotBlank() } ?: "Option ${hopedForIndex + 1}"
        return "That disappointment is information. You may have been hoping for $hopedFor. Consider choosing it."
    }

    fun getDisappointedMoreOptionsMessage(hopedForIndex: Int, options: List<String>): String {
        val hopedFor = options.getOrNull(hopedForIndex)?.takeIf { it.isNotBlank() } ?: "Option ${hopedForIndex + 1}"
        return "Noted. Your gut was pulling toward $hopedFor. Consider choosing it."
    }
}

class DecisionViewModel(private val savedStateHandle: SavedStateHandle = SavedStateHandle()) : ViewModel() {
    private val _state = MutableStateFlow(
        DecisionUiState(
            options = savedStateHandle.get<List<String>>("options") ?: listOf("", ""),
            mode = savedStateHandle.get<String>("mode")?.let { enumValueOf<DecisionMode>(it) } ?: DecisionMode.COIN
        )
    )
    val state: StateFlow<DecisionUiState> = _state.asStateFlow()
    
    private val logic = DecisionLogic()

    private fun updateState(update: (DecisionUiState) -> DecisionUiState) {
        _state.update { currentState ->
            val nextState = update(currentState)
            savedStateHandle["options"] = nextState.options
            savedStateHandle["mode"] = nextState.mode.name
            nextState
        }
    }

    fun addOption() {
        updateState { currentState ->
            if (currentState.options.size < 4) {
                val newOptions = currentState.options + ""
                currentState.copy(
                    options = newOptions,
                    mode = DecisionMode.WHEEL // force WHEEL when > 2 options
                )
            } else {
                currentState
            }
        }
    }

    fun removeOption(index: Int) {
        updateState { currentState ->
            if (currentState.options.size > 2 && index in currentState.options.indices) {
                val newOptions = currentState.options.toMutableList().apply { removeAt(index) }
                currentState.copy(
                    options = newOptions,
                    mode = if (newOptions.size == 2) currentState.mode else DecisionMode.WHEEL
                )
            } else {
                currentState
            }
        }
    }

    fun updateOption(index: Int, text: String) {
        updateState { currentState ->
            if (index in currentState.options.indices) {
                val newOptions = currentState.options.toMutableList()
                newOptions[index] = text.take(24)
                currentState.copy(options = newOptions)
            } else {
                currentState
            }
        }
    }

    fun setMode(mode: DecisionMode) {
        updateState { currentState ->
            if (currentState.options.size > 2) {
                currentState.copy(mode = DecisionMode.WHEEL)
            } else {
                currentState.copy(mode = mode)
            }
        }
    }

    fun decide() {
        updateState { it.copy(phase = DecisionPhase.Deciding) }
        val winnerIndex = logic.pickWinner(_state.value.options.size)
        updateState { it.copy(phase = DecisionPhase.Result, winnerIndex = winnerIndex) }
    }

    fun onFeeling(feeling: Feeling) {
        updateState { currentState ->
            if (currentState.phase is DecisionPhase.Result) {
                if (feeling == Feeling.RELIEVED) {
                    currentState.copy(feeling = feeling, phase = DecisionPhase.Feedback)
                } else if (feeling == Feeling.DISAPPOINTED && currentState.options.size == 2) {
                    val winnerIndex = currentState.winnerIndex ?: 0
                    val hopedForIndex = if (winnerIndex == 0) 1 else 0
                    currentState.copy(feeling = feeling, hopedForIndex = hopedForIndex, phase = DecisionPhase.Feedback)
                } else {
                    // For 3 or 4 options, wait for user to select hopedForIndex
                    currentState.copy(feeling = feeling)
                }
            } else {
                currentState
            }
        }
    }

    fun setHopedFor(index: Int) {
        updateState { it.copy(hopedForIndex = index, phase = DecisionPhase.Feedback) }
    }

    fun decideAgain() {
        updateState { it.copy(phase = DecisionPhase.Editing, winnerIndex = null, feeling = null, hopedForIndex = null) }
    }

    fun editOptions() {
        updateState { it.copy(phase = DecisionPhase.Editing, winnerIndex = null, feeling = null, hopedForIndex = null) }
    }
}

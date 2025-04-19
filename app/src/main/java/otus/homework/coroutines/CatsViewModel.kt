package otus.homework.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatsViewModel(
    private val catsService: CatsService,
    private val catsImageService: CatsImageService
) : ViewModel() {
    val handler = CoroutineExceptionHandler { _, exception ->
        _factState.value = Result.Error(exception)
        CrashMonitor.trackWarning()
    }
    private val _factState = MutableStateFlow<Result>(Result.Loading)
    val factState: StateFlow<Result> = _factState

    fun onInitComplete() {
        _factState.value = Result.Loading
        viewModelScope.launch(handler) {
            val factDeferred = async { catsService.getCatFact() }
            val imageDeferred = async { catsImageService.getCatImage() }
            val factResponse = factDeferred.await()
            val imageResponse = imageDeferred.await()

            if (factResponse.isSuccessful && factResponse.body() != null
                && imageResponse.isSuccessful && imageResponse.body() != null
            ) {
                _factState.value = Result.Success(
                    PresentationFact(
                        factResponse.body()?.fact,
                        imageResponse.body()?.get(0)?.imageUrl
                    )
                )
            }
        }
    }
}

sealed class Result() {
    data class Success(val data: PresentationFact) : Result()
    data class Error(val exception: Throwable) : Result()
    object Loading : Result()
}
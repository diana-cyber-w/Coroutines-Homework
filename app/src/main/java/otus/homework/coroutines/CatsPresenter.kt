package otus.homework.coroutines

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService,
    private val catsImageService: CatsImageService,
    val context: Context
) {
    private var _catsView: ICatsView? = null
    private val presenterScope = PresenterScope()

    fun onInitComplete() {
        presenterScope.coroutineContext.cancelChildren()
        presenterScope.launch {
            try {
                supervisorScope {
                    val factDeferred = async { catsService.getCatFact() }
                    val imageDeferred = async { catsImageService.getCatImage() }
                    val factResponse = factDeferred.await()
                    val imageResponse = imageDeferred.await()

                    if (factResponse.isSuccessful && factResponse.body() != null
                        && imageResponse.isSuccessful && imageResponse.body() != null
                    ) {
                        _catsView?.populate(
                            PresentationFact(
                                factResponse.body()?.fact,
                                imageResponse.body()?.get(0)?.imageUrl
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is SocketTimeoutException) {
                    _catsView?.showToast(context.getString(R.string.server_error))
                } else {
                    _catsView?.showToast(e.message ?: context.getString(R.string.error))
                    CrashMonitor.trackWarning()
                }
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
    }

    fun cancel() {
        presenterScope.cancel()
    }
}
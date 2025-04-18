package otus.homework.coroutines

import android.content.Context
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService,
    val context: Context
) {

    private var _catsView: ICatsView? = null
    private val presenterScope = PresenterScope()

    fun onInitComplete() {
        presenterScope.launch {
            try {
                val response = catsService.getCatFact()

                if (response.isSuccessful && response.body() != null) {
                    _catsView?.populate(response.body()!!)
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
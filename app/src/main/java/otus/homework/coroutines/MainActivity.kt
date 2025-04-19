package otus.homework.coroutines

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class MainActivity : AppCompatActivity() {

//    lateinit var catsPresenter: CatsPresenter

    private val diContainer = DiContainer()

    private val viewModel: CatsViewModel by viewModels {
        CatsViewModelFactory(
            catsService = diContainer.catsService,
            catImagesService = diContainer.catsImageservice
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = layoutInflater.inflate(R.layout.activity_main, null) as CatsView
        setContentView(view)

//        catsPresenter = CatsPresenter(diContainer.catsService, diContainer.catsImageservice, this)
        view.viewModel = viewModel
//        catsPresenter.attachView(view)
        viewModel.onInitComplete()

        lifecycleScope.launch {
            viewModel.factState.collect { result ->
                when (result) {
                    is Result.Success -> view.populate(result.data)

                    is Result.Error -> {
                        if (result.exception is SocketTimeoutException) {
                            view.showToast(getString(R.string.server_error))
                        } else {
                            view.showToast(result.exception.message ?: getString(R.string.error))
                        }
                    }

                    is Result.Loading -> {}
                }
            }
        }
    }

//    override fun onStop() {
//        if (isFinishing) {
//            catsPresenter.detachView()
//            catsPresenter.cancel()
//        }
//        super.onStop()
//    }
}
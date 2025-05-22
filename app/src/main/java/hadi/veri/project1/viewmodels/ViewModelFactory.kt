package hadi.veri.project1.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hadi.veri.project1.api.SessionManager
import hadi.veri.project1.repository.AuthRepository
import hadi.veri.project1.repository.OrderRepository
import hadi.veri.project1.repository.ProductRepository
import hadi.veri.project1.repository.StockCardRepository
import hadi.veri.project1.repository.StockMutationRepository

class ViewModelFactory private constructor(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
    
    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null
        
        fun getInstance(context: Context): ViewModelFactory {
            return instance ?: synchronized(this) {
                instance ?: ViewModelFactory(context).also { instance = it }
            }
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val sessionManager = SessionManager(context)
        
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                val repository = AuthRepository(sessionManager)
                AuthViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProductViewModel::class.java) -> {
                val repository = ProductRepository(sessionManager)
                ProductViewModel(repository) as T
            }
            modelClass.isAssignableFrom(StockCardViewModel::class.java) -> {
                val repository = StockCardRepository(sessionManager)
                StockCardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(StockMutationViewModel::class.java) -> {
                val repository = StockMutationRepository(sessionManager)
                StockMutationViewModel(repository) as T
            }
            modelClass.isAssignableFrom(OrderViewModel::class.java) -> {
                val repository = OrderRepository(sessionManager)
                OrderViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
} 
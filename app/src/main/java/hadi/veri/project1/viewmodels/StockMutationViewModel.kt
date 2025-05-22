package hadi.veri.project1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.api.ProductWithStockMutationsResponse
import hadi.veri.project1.models.StockMutation
import hadi.veri.project1.repository.StockMutationRepository
import kotlinx.coroutines.launch

class StockMutationViewModel(private val repository: StockMutationRepository) : ViewModel() {
    
    private val _stockMutations = MutableLiveData<Result<ApiResponse<List<StockMutation>>>>()
    val stockMutations: LiveData<Result<ApiResponse<List<StockMutation>>>> = _stockMutations
    
    private val _stockMutation = MutableLiveData<Result<ApiResponse<StockMutation>>>()
    val stockMutation: LiveData<Result<ApiResponse<StockMutation>>> = _stockMutation
    
    private val _createStockMutationResult = MutableLiveData<Result<ApiResponse<StockMutation>>>()
    val createStockMutationResult: LiveData<Result<ApiResponse<StockMutation>>> = _createStockMutationResult
    
    private val _stockMutationsByProduct = MutableLiveData<Result<ApiResponse<ProductWithStockMutationsResponse>>>()
    val stockMutationsByProduct: LiveData<Result<ApiResponse<ProductWithStockMutationsResponse>>> = _stockMutationsByProduct
    
    fun getAllStockMutations(
        productId: Int? = null,
        type: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            _stockMutations.value = repository.getAllStockMutations(productId, type, startDate, endDate)
        }
    }
    
    fun getStockMutation(id: Int) {
        viewModelScope.launch {
            _stockMutation.value = repository.getStockMutation(id)
        }
    }
    
    fun createStockMutation(
        productId: Int,
        type: String,
        quantity: Int,
        date: String,
        description: String? = null
    ) {
        viewModelScope.launch {
            _createStockMutationResult.value = repository.createStockMutation(
                productId, type, quantity, date, description
            )
        }
    }
    
    fun getStockMutationsByProduct(productId: Int) {
        viewModelScope.launch {
            _stockMutationsByProduct.value = repository.getStockMutationsByProduct(productId)
        }
    }
} 
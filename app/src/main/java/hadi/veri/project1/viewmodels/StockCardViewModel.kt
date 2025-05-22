package hadi.veri.project1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.api.ProductWithStockCardsResponse
import hadi.veri.project1.models.StockCard
import hadi.veri.project1.repository.StockCardRepository
import kotlinx.coroutines.launch

class StockCardViewModel(private val repository: StockCardRepository) : ViewModel() {
    
    private val _stockCards = MutableLiveData<Result<ApiResponse<List<StockCard>>>>()
    val stockCards: LiveData<Result<ApiResponse<List<StockCard>>>> = _stockCards
    
    private val _stockCard = MutableLiveData<Result<ApiResponse<StockCard>>>()
    val stockCard: LiveData<Result<ApiResponse<StockCard>>> = _stockCard
    
    private val _createStockCardResult = MutableLiveData<Result<ApiResponse<StockCard>>>()
    val createStockCardResult: LiveData<Result<ApiResponse<StockCard>>> = _createStockCardResult
    
    private val _updateStockCardResult = MutableLiveData<Result<ApiResponse<StockCard>>>()
    val updateStockCardResult: LiveData<Result<ApiResponse<StockCard>>> = _updateStockCardResult
    
    private val _deleteStockCardResult = MutableLiveData<Result<ApiResponse<Nothing>>>()
    val deleteStockCardResult: LiveData<Result<ApiResponse<Nothing>>> = _deleteStockCardResult
    
    private val _stockCardsByProduct = MutableLiveData<Result<ApiResponse<ProductWithStockCardsResponse>>>()
    val stockCardsByProduct: LiveData<Result<ApiResponse<ProductWithStockCardsResponse>>> = _stockCardsByProduct
    
    fun getAllStockCards(productId: Int? = null, startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            _stockCards.value = repository.getAllStockCards(productId, startDate, endDate)
        }
    }
    
    fun getStockCard(id: Int) {
        viewModelScope.launch {
            _stockCard.value = repository.getStockCard(id)
        }
    }
    
    fun createStockCard(
        productId: Int,
        initialStock: Int,
        inStock: Int,
        outStock: Int,
        finalStock: Int,
        date: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _createStockCardResult.value = repository.createStockCard(
                productId, initialStock, inStock, outStock, finalStock, date, notes
            )
        }
    }
    
    fun updateStockCard(
        id: Int,
        initialStock: Int,
        inStock: Int,
        outStock: Int,
        finalStock: Int,
        date: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _updateStockCardResult.value = repository.updateStockCard(
                id, initialStock, inStock, outStock, finalStock, date, notes
            )
        }
    }
    
    fun deleteStockCard(id: Int) {
        viewModelScope.launch {
            _deleteStockCardResult.value = repository.deleteStockCard(id)
        }
    }
    
    fun getStockCardsByProduct(productId: Int) {
        viewModelScope.launch {
            _stockCardsByProduct.value = repository.getStockCardsByProduct(productId)
        }
    }
} 
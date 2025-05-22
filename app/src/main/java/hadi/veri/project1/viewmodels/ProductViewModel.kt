package hadi.veri.project1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.models.Product
import hadi.veri.project1.repository.ProductRepository
import kotlinx.coroutines.launch
import java.io.File

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    
    private val _products = MutableLiveData<Result<ApiResponse<List<Product>>>>()
    val products: LiveData<Result<ApiResponse<List<Product>>>> = _products
    
    private val _product = MutableLiveData<Result<ApiResponse<Product>>>()
    val product: LiveData<Result<ApiResponse<Product>>> = _product
    
    private val _createProductResult = MutableLiveData<Result<ApiResponse<Product>>>()
    val createProductResult: LiveData<Result<ApiResponse<Product>>> = _createProductResult
    
    private val _updateProductResult = MutableLiveData<Result<ApiResponse<Product>>>()
    val updateProductResult: LiveData<Result<ApiResponse<Product>>> = _updateProductResult
    
    private val _deleteProductResult = MutableLiveData<Result<ApiResponse<Nothing>>>()
    val deleteProductResult: LiveData<Result<ApiResponse<Nothing>>> = _deleteProductResult
    
    fun getAllProducts() {
        viewModelScope.launch {
            _products.value = repository.getAllProducts()
        }
    }
    
    fun getProduct(id: Int) {
        viewModelScope.launch {
            _product.value = repository.getProduct(id)
        }
    }
    
    fun createProduct(
        code: String,
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        unit: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _createProductResult.value = repository.createProduct(
                code, name, description, price, stock, unit, imageFile
            )
        }
    }
    
    fun updateProduct(
        id: Int,
        code: String,
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        unit: String,
        imageFile: File?
    ) {
        viewModelScope.launch {
            _updateProductResult.value = repository.updateProduct(
                id, code, name, description, price, stock, unit, imageFile
            )
        }
    }
    
    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            _deleteProductResult.value = repository.deleteProduct(id)
        }
    }
} 
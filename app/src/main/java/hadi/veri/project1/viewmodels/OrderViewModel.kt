package hadi.veri.project1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.models.Order
import hadi.veri.project1.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {
    
    private val _myOrders = MutableLiveData<Result<ApiResponse<List<Order>>>>()
    val myOrders: LiveData<Result<ApiResponse<List<Order>>>> = _myOrders
    
    private val _orders = MutableLiveData<Result<ApiResponse<List<Order>>>>()
    val orders: LiveData<Result<ApiResponse<List<Order>>>> = _orders
    
    private val _order = MutableLiveData<Result<ApiResponse<Order>>>()
    val order: LiveData<Result<ApiResponse<Order>>> = _order
    
    private val _createOrderResult = MutableLiveData<Result<ApiResponse<Order>>>()
    val createOrderResult: LiveData<Result<ApiResponse<Order>>> = _createOrderResult
    
    private val _updateOrderStatusResult = MutableLiveData<Result<ApiResponse<Order>>>()
    val updateOrderStatusResult: LiveData<Result<ApiResponse<Order>>> = _updateOrderStatusResult
    
    fun getMyOrders() {
        viewModelScope.launch {
            _myOrders.value = repository.getMyOrders()
        }
    }
    
    fun getAllOrders(
        userId: Int? = null,
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            _orders.value = repository.getAllOrders(userId, status, startDate, endDate)
        }
    }
    
    fun getOrder(id: Int) {
        viewModelScope.launch {
            _order.value = repository.getOrder(id)
        }
    }
    
    fun createOrder(
        items: List<Map<String, Any>>,
        shippingAddress: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _createOrderResult.value = repository.createOrder(items, shippingAddress, notes)
        }
    }
    
    fun updateOrderStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            _updateOrderStatusResult.value = repository.updateOrderStatus(orderId, status)
        }
    }
} 
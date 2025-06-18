package cnc.coop.milkcreamies.domain.repository

import cnc.coop.milkcreamies.domain.models.Customer

interface CustomerRepository {
    suspend fun getCustomerById(customerId: String): Customer?
    suspend fun getAllCustomers(): List<Customer>
    suspend fun addCustomer(customer: Customer): Customer
}

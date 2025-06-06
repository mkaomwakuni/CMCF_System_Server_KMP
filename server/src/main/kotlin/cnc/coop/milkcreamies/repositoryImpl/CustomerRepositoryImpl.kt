package cnc.coop.milkcreamies.repositoryImpl

import cnc.coop.milkcreamies.data.DataAccess
import cnc.coop.milkcreamies.data.DatabaseConfig
import cnc.coop.milkcreamies.domain.models.Customer
import cnc.coop.milkcreamies.domain.repository.CustomerRepository

class CustomerRepositoryImpl : CustomerRepository {
    override suspend fun getCustomerById(customerId: String): Customer? {
        return DataAccess.getCustomerById(customerId)
    }

    override suspend fun getAllCustomers(): List<Customer> {
        return DataAccess.getAllCustomers()
    }

    override suspend fun addCustomer(customer: Customer): Customer {
        // This method is only used internally by the system
        // Customers are normally created automatically through milk sales
        val customerId = DatabaseConfig.generateNextCustomerId()
        val newCustomer = customer.copy(customerId = customerId)
        DataAccess.addCustomer(newCustomer)
        return newCustomer
    }
}


package com.mgt.serviceimpl;

import com.mgt.model.Customer;
import com.mgt.repository.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl {

    @Autowired
    private CustomerRepo customerRepo;


    public Customer saveCustomer(Customer customer) {
        customer.getCustomerProductList().forEach(product -> product.setCustomer(customer));
        return customerRepo.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }


    public Customer getCustomerById(int id) {
        return customerRepo.findById(id).orElse(null);
    }

}

package com.reversidebank.customerservice.service;

import com.reversidebank.customerservice.dto.CustomerRequest;
import com.reversidebank.customerservice.dto.CustomerResponse;
import com.reversidebank.customerservice.dto.CustomerUpdateRequest;
import com.reversidebank.customerservice.entity.Address;
import com.reversidebank.customerservice.entity.Customer;
import com.reversidebank.customerservice.exception.CustomerAlreadyExistsException;
import com.reversidebank.customerservice.exception.CustomerNotFoundException;
import com.reversidebank.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new CustomerAlreadyExistsException(
                    "A customer profile already exists for email: " + request.getEmail());
        }

        Address address = new Address(
                request.getStreet(),
                request.getSuburb(),
                request.getCity(),
                request.getPostalCode(),
                request.getProvince()
        );

        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(address);
        customer.setDateOfBirth(request.getDateOfBirth());

        Customer saved = customerRepository.save(customer);
        return toResponse(saved);
    }

    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomerNotFoundException("No customer found with id: " + id));
        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {
        Address address = customer.getAddress();
        return new CustomerResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber(),
                address.getStreet(),
                address.getSuburb(),
                address.getCity(),
                address.getPostalCode(),
                address.getProvince(),
                customer.getDateOfBirth(),
                customer.getCreatedAt()
        );
    }

    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomerNotFoundException("No customer found with id: " + id));

        Address address = new Address(
                request.getStreet(),
                request.getSuburb(),
                request.getCity(),
                request.getPostalCode(),
                request.getProvince()
        );

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(address);
        customer.setDateOfBirth(request.getDateOfBirth());

        Customer updated = customerRepository.save(customer);
        return toResponse(updated);
    }

    public void deleteCustomer(UUID id) {
        Customer customer = customerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomerNotFoundException("No customer found with id: " + id));
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }
}

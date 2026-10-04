package vn.gasstation.customer.application;
import java.util.List;import vn.gasstation.customer.domain.Customer;
public interface CustomerProvider { List<Customer> findAll(); }

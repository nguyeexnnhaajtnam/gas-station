package vn.gasstation.customer.api;
import java.util.List;import org.springframework.web.bind.annotation.*;import vn.gasstation.customer.application.CustomerService;import vn.gasstation.customer.domain.Customer;
@RestController @RequestMapping("/api/v1/customers") public class CustomerController {private final CustomerService service;public CustomerController(CustomerService service){this.service=service;}@GetMapping public List<Customer> findAll(){return service.findAll();}}

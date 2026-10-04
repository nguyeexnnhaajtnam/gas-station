package vn.gasstation.customer.application;
import java.util.List;import org.slf4j.*;import org.springframework.stereotype.Service;import vn.gasstation.customer.domain.Customer;
@Service public class CustomerService {private static final Logger log=LoggerFactory.getLogger(CustomerService.class);private final CustomerProvider provider;
 public CustomerService(CustomerProvider provider){this.provider=provider;} public List<Customer> findAll(){var result=provider.findAll();log.info("[BUSINESS] event=customer.list.loaded count={}",result.size());return result;}}

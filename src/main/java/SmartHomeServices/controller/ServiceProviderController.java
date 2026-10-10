package SmartHomeServices.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SmartHomeServices.entity.ServiceProvider;
import SmartHomeServices.repository.ServiceProviderRepository;

@RestController
@RequestMapping("/providers")
public class ServiceProviderController {

    private final ServiceProviderRepository providerRepository;

    public ServiceProviderController(
            ServiceProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @PostMapping
    public ServiceProvider addProvider(
            @RequestBody ServiceProvider provider) {
        return providerRepository.save(provider);
    }

    @GetMapping
    public List<ServiceProvider> getAllProviders() {
        return providerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ServiceProvider getProviderById(@PathVariable Long id) {
        return providerRepository.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public String deleteProvider(@PathVariable Long id) {
        providerRepository.deleteById(id);
        return "Provider deleted successfully";
    }
}

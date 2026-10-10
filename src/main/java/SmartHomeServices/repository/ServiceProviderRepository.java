package SmartHomeServices.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import SmartHomeServices.entity.ServiceProvider;

public interface ServiceProviderRepository
        extends JpaRepository<ServiceProvider, Long> {

}
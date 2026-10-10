package SmartHomeServices.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import SmartHomeServices.entity.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

}
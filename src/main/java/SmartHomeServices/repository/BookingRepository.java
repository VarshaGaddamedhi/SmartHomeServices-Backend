package SmartHomeServices.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import SmartHomeServices.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}

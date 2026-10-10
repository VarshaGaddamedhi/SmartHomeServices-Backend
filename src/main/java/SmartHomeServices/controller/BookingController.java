package SmartHomeServices.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import SmartHomeServices.dto.BookingRequest;
import SmartHomeServices.entity.Booking;
import SmartHomeServices.entity.Service;
import SmartHomeServices.entity.ServiceProvider;
import SmartHomeServices.entity.User;
import SmartHomeServices.repository.BookingRepository;
import SmartHomeServices.repository.ServiceProviderRepository;
import SmartHomeServices.repository.ServiceRepository;
import SmartHomeServices.repository.UserRepository;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceProviderRepository providerRepository;

    private static final Set<String> VALID_STATUSES = Set.of(
            "PENDING",
            "ACCEPTED",
            "INPROGRESS",
            "COMPLETED",
            "REJECTED",
            "CANCELLED"
    );

    public BookingController(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            ServiceProviderRepository providerRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.providerRepository = providerRepository;
    }

    @PostMapping
    public Booking addBooking(@RequestBody BookingRequest request) {
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service not found"));

        ServiceProvider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Provider not found"));

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setService(service);
        booking.setProvider(provider);
        booking.setBookingDate(LocalDate.parse(request.getBookingDate()));
        booking.setBookingTime(LocalTime.parse(request.getBookingTime()));
        booking.setAddress(request.getAddress());
        booking.setPrice(service.getBasePrice());
        booking.setStatus("PENDING");

        return bookingRepository.save(booking);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));
    }

    @DeleteMapping("/{id}")
    public String deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Booking not found");
        }

        bookingRepository.deleteById(id);
        return "Booking deleted successfully";
    }

    @PutMapping("/{id}/status")
    public Booking updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"));

        String newStatus = status.trim().toUpperCase();

        if (!VALID_STATUSES.contains(newStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid booking status");
        }

        String currentStatus = booking.getStatus() == null
                ? ""
                : booking.getStatus().trim().toUpperCase();

        boolean allowed =
                ("PENDING".equals(currentStatus)
                        && Set.of("ACCEPTED", "REJECTED", "CANCELLED")
                                .contains(newStatus))
                || ("ACCEPTED".equals(currentStatus)
                        && Set.of("INPROGRESS", "CANCELLED")
                                .contains(newStatus))
                || ("INPROGRESS".equals(currentStatus)
                        && "COMPLETED".equals(newStatus));

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This booking status transition is not allowed");
        }

        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }
}

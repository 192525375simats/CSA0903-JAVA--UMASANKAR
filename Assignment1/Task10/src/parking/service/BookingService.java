package parking.service;

import parking.model.Booking;

/** Service class: business logic, uses only the PUBLIC API of the model. */
public class BookingService {
    public double fee(Booking b) {
        int hours = (b.getDurationMinutes() + 59) / 60;
        return hours * 30.0;
    }
}

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core service class: owns the train schedule and every ticket issued,
 * and implements booking, cancellation and automatic waitlist promotion.
 */
public class ReservationSystem {

    /** Train number -> Train. LinkedHashMap keeps the schedule display order stable. */
    private final Map<String, Train> trains = new LinkedHashMap<>();

    /** Ticket ID -> Ticket, for O(1) lookup during cancellation / enquiry. */
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();

    // ---------------------------------------------------------------- schedule

    public void addTrain(Train train) {
        trains.put(train.getTrainNumber(), train);
    }

    public List<Train> getAllTrains() {
        return new ArrayList<>(trains.values());
    }

    public Train findTrain(String trainNumber) {
        return trainNumber == null ? null : trains.get(trainNumber.trim());
    }

    /** Searches the schedule for trains running from source to destination. */
    public List<Train> searchTrains(String source, String destination) {
        List<Train> results = new ArrayList<>();
        for (Train train : trains.values()) {
            if (train.matchesRoute(source, destination)) {
                results.add(train);
            }
        }
        return results;
    }

    // ----------------------------------------------------------------- booking

    /**
     * Books a ticket. If a seat is free the ticket is CONFIRMED with a seat
     * number, otherwise it is WAITLISTED and queued on the train.
     *
     * @return the created ticket, or null if the train number is unknown.
     */
    public Ticket bookTicket(String trainNumber, Passenger passenger) {
        Train train = findTrain(trainNumber);
        if (train == null) {
            return null;
        }

        Ticket ticket;
        if (train.hasSeatAvailable()) {
            int seat = train.allocateSeat();
            ticket = new Ticket(train, passenger, seat, BookingStatus.CONFIRMED);
            passenger.notifyPassenger("Booking CONFIRMED on " + train.getTrainNumber()
                    + " - seat " + seat + ". Ticket " + ticket.getTicketId() + ".");
        } else {
            ticket = new Ticket(train, passenger, -1, BookingStatus.WAITLISTED);
            train.addToWaitlist(ticket);
            passenger.notifyPassenger("Train is full. You are WAITLISTED at position #"
                    + train.waitlistPositionOf(ticket) + " on " + train.getTrainNumber()
                    + ". Ticket " + ticket.getTicketId() + ".");
        }
        tickets.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    // ------------------------------------------------------------ cancellation

    /**
     * Cancels a ticket. Cancelling a CONFIRMED ticket frees its seat and the
     * next waitlisted passenger is promoted automatically and notified.
     *
     * @return the promoted ticket, or null if nobody was promoted.
     * @throws IllegalArgumentException if the ticket does not exist or is already cancelled.
     */
    public Ticket cancelTicket(String ticketId) {
        Ticket ticket = findTicket(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("No ticket found with ID: " + ticketId);
        }
        if (ticket.isCancelled()) {
            throw new IllegalArgumentException("Ticket " + ticket.getTicketId()
                    + " is already cancelled.");
        }

        Train train = ticket.getTrain();

        if (ticket.isWaitlisted()) {
            train.removeFromWaitlist(ticket);
            ticket.cancel();
            ticket.getPassenger().notifyPassenger("Your waitlisted ticket "
                    + ticket.getTicketId() + " has been cancelled.");
            return null;
        }

        // Confirmed ticket: free the seat, then promote the next in line.
        int freedSeat = ticket.getSeatNumber();
        ticket.cancel();
        train.releaseSeat(freedSeat);
        ticket.getPassenger().notifyPassenger("Your ticket " + ticket.getTicketId()
                + " (seat " + freedSeat + ") has been cancelled.");

        return promoteNextWaitlisted(train);
    }

    /**
     * Moves the head of the train's waiting list into a free seat, if both exist.
     *
     * @return the promoted ticket, or null when there is no waitlist / no free seat.
     */
    private Ticket promoteNextWaitlisted(Train train) {
        if (train.getWaitlistSize() == 0 || !train.hasSeatAvailable()) {
            return null;
        }
        Ticket next = train.pollWaitlist();
        if (next == null) {
            return null;
        }
        int seat = train.allocateSeat();
        next.confirmWith(seat);
        next.getPassenger().notifyPassenger("Good news! A seat opened up. Ticket "
                + next.getTicketId() + " is now CONFIRMED with seat " + seat + ".");
        return next;
    }

    // ------------------------------------------------------------- enquiries

    public Ticket findTicket(String ticketId) {
        return ticketId == null ? null : tickets.get(ticketId.trim().toUpperCase());
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets.values());
    }

    /** All tickets belonging to a train, in booking order. */
    public List<Ticket> getTicketsForTrain(String trainNumber) {
        List<Ticket> result = new ArrayList<>();
        Train train = findTrain(trainNumber);
        if (train == null) {
            return result;
        }
        for (Ticket ticket : tickets.values()) {
            if (ticket.getTrain() == train) {
                result.add(ticket);
            }
        }
        return result;
    }
}

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

/**
 * A train with a fixed seat inventory and a FIFO waiting list.
 * Seat numbers are kept in a sorted set so the lowest free seat
 * is always allocated first (and freed seats can be reused).
 */
public class Train {

    private final String trainNumber;
    private final String trainName;
    private final String source;
    private final String destination;
    private final int totalSeats;

    /** Seats currently free, kept sorted ascending. */
    private final TreeSet<Integer> freeSeats = new TreeSet<>();

    /** FIFO waiting list of tickets that could not be confirmed. */
    private final Queue<Ticket> waitlist = new LinkedList<>();

    public Train(String trainNumber, String trainName, String source,
                 String destination, int totalSeats) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("A train must have at least one seat.");
        }
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.totalSeats = totalSeats;
        for (int seat = 1; seat <= totalSeats; seat++) {
            freeSeats.add(seat);
        }
    }

    public String getTrainNumber()  { return trainNumber; }
    public String getTrainName()    { return trainName; }
    public String getSource()       { return source; }
    public String getDestination()  { return destination; }
    public int getTotalSeats()      { return totalSeats; }
    public int getAvailableSeats()  { return freeSeats.size(); }
    public int getWaitlistSize()    { return waitlist.size(); }

    public boolean hasSeatAvailable() {
        return !freeSeats.isEmpty();
    }

    /** Reserves and returns the lowest free seat number, or -1 if the train is full. */
    public int allocateSeat() {
        if (freeSeats.isEmpty()) {
            return -1;
        }
        return freeSeats.pollFirst();
    }

    /** Returns a seat to the free pool (used on cancellation). */
    public void releaseSeat(int seatNumber) {
        if (seatNumber >= 1 && seatNumber <= totalSeats) {
            freeSeats.add(seatNumber);
        }
    }

    public void addToWaitlist(Ticket ticket) {
        waitlist.offer(ticket);
    }

    /** Removes and returns the next waitlisted ticket, or null if none waiting. */
    public Ticket pollWaitlist() {
        return waitlist.poll();
    }

    /** Removes a specific ticket from the waiting list (waitlisted ticket cancelled). */
    public boolean removeFromWaitlist(Ticket ticket) {
        return waitlist.remove(ticket);
    }

    /** 1-based position of a ticket in the waiting list, or -1 if not present. */
    public int waitlistPositionOf(Ticket ticket) {
        int position = 1;
        for (Ticket t : waitlist) {
            if (t.equals(ticket)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    /** Case-insensitive route match used by the search feature. */
    public boolean matchesRoute(String from, String to) {
        return source.equalsIgnoreCase(from.trim())
                && destination.equalsIgnoreCase(to.trim());
    }

    /** Single-line summary showing live availability. */
    public String summary() {
        return String.format("%-7s %-20s %-12s -> %-12s Seats: %2d/%2d   WL: %d",
                trainNumber, trainName, source, destination,
                getAvailableSeats(), totalSeats, getWaitlistSize());
    }

    @Override
    public String toString() {
        return trainNumber + " (" + trainName + ") " + source + " -> " + destination;
    }
}

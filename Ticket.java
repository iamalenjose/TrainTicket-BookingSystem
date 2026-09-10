/**
 * A booking record linking a passenger to a train, with live status.
 * Seat number is -1 while the ticket is waitlisted or cancelled.
 */
public class Ticket {
    private static int counter = 5000;

    private final String ticketId;
    private final Train train;
    private final Passenger passenger;
    private int seatNumber;
    private BookingStatus status;

    public Ticket(Train train, Passenger passenger, int seatNumber, BookingStatus status) {
        this.ticketId = "TKT" + (++counter);
        this.train = train;
        this.passenger = passenger;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public String getTicketId()        { return ticketId; }
    public Train getTrain()            { return train; }
    public Passenger getPassenger()    { return passenger; }
    public int getSeatNumber()         { return seatNumber; }
    public BookingStatus getStatus()   { return status; }

    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public boolean isConfirmed()  { return status == BookingStatus.CONFIRMED; }
    public boolean isWaitlisted() { return status == BookingStatus.WAITLISTED; }
    public boolean isCancelled()  { return status == BookingStatus.CANCELLED; }

    /** Promotes a waitlisted ticket to confirmed with the given seat. */
    public void confirmWith(int seatNumber) {
        this.seatNumber = seatNumber;
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.seatNumber = -1;
        this.status = BookingStatus.CANCELLED;
    }

    private String seatText() {
        return seatNumber > 0 ? String.valueOf(seatNumber) : "-";
    }

    /** Detailed, printable ticket used for booking confirmation. */
    public String details() {
        StringBuilder sb = new StringBuilder();
        sb.append("+------------------------------------------------------+\n");
        sb.append(String.format("| Ticket ID : %-40s |%n", ticketId));
        sb.append(String.format("| Train     : %-40s |%n",
                train.getTrainNumber() + " - " + train.getTrainName()));
        sb.append(String.format("| Route     : %-40s |%n",
                train.getSource() + " -> " + train.getDestination()));
        sb.append(String.format("| Passenger : %-40s |%n",
                passenger.getName() + " (Age " + passenger.getAge() + ")"));
        sb.append(String.format("| Contact   : %-40s |%n", passenger.getContact()));
        sb.append(String.format("| Seat No.  : %-40s |%n", seatText()));
        String statusText = status.toString();
        if (isWaitlisted()) {
            int pos = train.waitlistPositionOf(this);
            if (pos > 0) {
                statusText += " (WL #" + pos + ")";
            }
        }
        sb.append(String.format("| Status    : %-40s |%n", statusText));
        sb.append("+------------------------------------------------------+");
        return sb.toString();
    }

    /** Single-line form used in listings. */
    public String summary() {
        String statusText = status.toString();
        if (isWaitlisted()) {
            int pos = train.waitlistPositionOf(this);
            if (pos > 0) {
                statusText = "WAITLISTED (WL #" + pos + ")";
            }
        }
        return String.format("%-9s %-7s %-18s Seat: %-4s %s",
                ticketId, train.getTrainNumber(), passenger.getName(),
                seatText(), statusText);
    }

    @Override
    public String toString() {
        return summary();
    }
}

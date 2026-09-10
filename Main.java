import java.util.List;
import java.util.Scanner;

/**
 * Menu-driven console front-end for the Train Ticket Booking System.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ReservationSystem system = new ReservationSystem();

    public static void main(String[] args) {
        seedTrains();
        System.out.println("=========================================");
        System.out.println("   TRAIN TICKET BOOKING SYSTEM");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            System.out.println();
            switch (choice) {
                case 1 -> showAllTrains();
                case 2 -> searchTrains();
                case 3 -> bookTicket();
                case 4 -> cancelTicket();
                case 5 -> viewTicket();
                case 6 -> showAllTickets();
                case 7 -> showTrainChart();
                case 0 -> {
                    System.out.println("Thank you for using the booking system. Safe travels!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please pick a number from the menu.");
            }
            System.out.println();
        }
        scanner.close();
    }

    // ------------------------------------------------------------ sample data

    private static void seedTrains() {
        system.addTrain(new Train("12951", "Rajdhani Express", "Mumbai", "Delhi", 4));
        system.addTrain(new Train("12627", "Karnataka Express", "Bangalore", "Delhi", 3));
        system.addTrain(new Train("12841", "Coromandel Express", "Kolkata", "Chennai", 2));
        system.addTrain(new Train("12009", "Shatabdi Express", "Mumbai", "Delhi", 2));
        system.addTrain(new Train("16526", "Island Express", "Bangalore", "Kanyakumari", 3));
    }

    // ----------------------------------------------------------------- menu

    private static void printMenu() {
        System.out.println("-----------------------------------------");
        System.out.println(" 1. View all trains");
        System.out.println(" 2. Search trains (source -> destination)");
        System.out.println(" 3. Book a ticket");
        System.out.println(" 4. Cancel a ticket");
        System.out.println(" 5. View ticket / booking status");
        System.out.println(" 6. View all bookings");
        System.out.println(" 7. View reservation chart for a train");
        System.out.println(" 0. Exit");
        System.out.println("-----------------------------------------");
    }

    // ------------------------------------------------------------- features

    private static void showAllTrains() {
        System.out.println("AVAILABLE TRAINS");
        printTrainTable(system.getAllTrains());
    }

    private static void searchTrains() {
        String source = readLine("Enter source station: ");
        String destination = readLine("Enter destination station: ");
        List<Train> results = system.searchTrains(source, destination);
        System.out.println();
        if (results.isEmpty()) {
            System.out.println("No trains found from " + source + " to " + destination + ".");
            return;
        }
        System.out.println("TRAINS FROM " + source.toUpperCase() + " TO " + destination.toUpperCase());
        printTrainTable(results);
    }

    private static void bookTicket() {
        String trainNumber = readLine("Enter train number: ");
        Train train = system.findTrain(trainNumber);
        if (train == null) {
            System.out.println("No train found with number " + trainNumber + ".");
            return;
        }
        System.out.println("Selected: " + train);
        System.out.println("Seats available: " + train.getAvailableSeats()
                + " | Current waiting list: " + train.getWaitlistSize());

        String name = readNonEmpty("Passenger name: ");
        int age = readAge();
        String contact = readNonEmpty("Contact number / e-mail: ");

        Passenger passenger = new Passenger(name, age, contact);
        Ticket ticket = system.bookTicket(train.getTrainNumber(), passenger);

        System.out.println();
        if (ticket.isConfirmed()) {
            System.out.println(">>> BOOKING CONFIRMED <<<");
        } else {
            System.out.println(">>> TRAIN FULL - ADDED TO WAITING LIST <<<");
        }
        System.out.println(ticket.details());
    }

    private static void cancelTicket() {
        String ticketId = readNonEmpty("Enter ticket ID to cancel: ");
        try {
            Ticket promoted = system.cancelTicket(ticketId);
            System.out.println();
            System.out.println("Ticket " + ticketId.trim().toUpperCase() + " cancelled successfully.");
            if (promoted != null) {
                System.out.println();
                System.out.println(">>> WAITLIST PROMOTION <<<");
                System.out.println(promoted.getPassenger().getName()
                        + " has been moved from the waiting list to a confirmed seat.");
                System.out.println(promoted.details());
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void viewTicket() {
        String ticketId = readNonEmpty("Enter ticket ID: ");
        Ticket ticket = system.findTicket(ticketId);
        System.out.println();
        if (ticket == null) {
            System.out.println("No ticket found with ID " + ticketId + ".");
            return;
        }
        System.out.println(ticket.details());
    }

    private static void showAllTickets() {
        List<Ticket> all = system.getAllTickets();
        if (all.isEmpty()) {
            System.out.println("No bookings have been made yet.");
            return;
        }
        System.out.println("ALL BOOKINGS");
        System.out.printf("%-9s %-7s %-18s %-10s %s%n",
                "TICKET", "TRAIN", "PASSENGER", "SEAT", "STATUS");
        for (Ticket ticket : all) {
            System.out.println(ticket.summary());
        }
    }

    private static void showTrainChart() {
        String trainNumber = readLine("Enter train number: ");
        Train train = system.findTrain(trainNumber);
        if (train == null) {
            System.out.println("No train found with number " + trainNumber + ".");
            return;
        }
        List<Ticket> ticketsForTrain = system.getTicketsForTrain(train.getTrainNumber());
        System.out.println();
        System.out.println("RESERVATION CHART - " + train);
        System.out.println("Seats available: " + train.getAvailableSeats() + "/"
                + train.getTotalSeats() + " | Waiting list: " + train.getWaitlistSize());
        System.out.println("-----------------------------------------------------------");
        if (ticketsForTrain.isEmpty()) {
            System.out.println("No bookings on this train yet.");
            return;
        }
        for (Ticket ticket : ticketsForTrain) {
            if (!ticket.isCancelled()) {
                System.out.println(ticket.summary());
            }
        }
    }

    private static void printTrainTable(List<Train> trains) {
        System.out.printf("%-7s %-20s %-12s    %-12s %-13s %s%n",
                "NO.", "NAME", "FROM", "TO", "SEATS", "WAITLIST");
        for (Train train : trains) {
            System.out.println(train.summary());
        }
    }

    // ---------------------------------------------------------- input helpers

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be blank. Please try again.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static int readAge() {
        while (true) {
            int age = readInt("Passenger age: ");
            if (age > 0 && age < 120) {
                return age;
            }
            System.out.println("Please enter an age between 1 and 119.");
        }
    }
}

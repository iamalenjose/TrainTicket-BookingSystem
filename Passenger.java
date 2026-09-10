/**
 * Represents a passenger travelling on a train.
 */
public class Passenger {
    private static int counter = 1000;

    private final String passengerId;
    private final String name;
    private final int age;
    private final String contact;

    public Passenger(String name, int age, String contact) {
        this.passengerId = "P" + (++counter);
        this.name = name;
        this.age = age;
        this.contact = contact;
    }

    public String getPassengerId() { return passengerId; }
    public String getName()        { return name; }
    public int getAge()            { return age; }
    public String getContact()     { return contact; }

    /** Simulated notification (SMS / e-mail) sent to the passenger. */
    public void notifyPassenger(String message) {
        System.out.println("  [NOTIFY -> " + name + " (" + contact + ")]: " + message);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | Age: %d | Contact: %s",
                passengerId, name, age, contact);
    }
}

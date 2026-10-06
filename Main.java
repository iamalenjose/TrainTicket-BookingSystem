import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class Main extends JFrame {
private final ReservationSystem system = new ReservationSystem();

// Traijn tab
private final DefaultTableModel trainModel =
makeModel("No.", "Name", "From", "To", "Seats", "Waitlist");
private final JTable trainTable = new JTable(trainModel);
private final JTextField searchFrom = new JTextField(12);
private final JTextField searchTo = new JTextField(12);
private boolean searchActive = false;

// Book tab
private JComboBox<Train> bookTrainCombo;
private final JLabel bookInfo = new JLabel(" ");
private final JTextField nameField = new JTextField(20);
private final JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 119, 1));
private final JTextField contactField = new JTextField(20);

// Manage tab
private final JTextField ticketIdField = new JTextField(12);
private final JTextArea ticketDetails = monoArea(10, 60);

// Bookings tab
private final DefaultTableModel ticketModel =
makeModel("Ticket", "Train", "Passenger", "Seat", "Status");
private final JTable ticketTable = new JTable(ticketModel);

// Chart tab
private JComboBox<Train> chartTrainCombo;
private final JLabel chartInfo = new JLabel(" ");
private final DefaultTableModel chartModel =
makeModel("Ticket", "Passenger", "Seat", "Status");
private final JTable chartTable = new JTable(chartModel);

// Notification log
private final JTextArea notifyLog = new JTextArea(5, 60);

public static void main(String[] args) {
SwingUtilities.invokeLater(() -> {
try {
UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
} catch (Exception ignored) { }
new Main().setVisible(true);
});
}
public Main() {
super("Train Ticket Booking System");
seedTrains();
Passenger.setNotificationHandler(msg -> notifyLog.append(msg + "\n"));
setDefaultCloseOperation(EXIT_ON_CLOSE);
setLayout(new BorderLayout(8, 8));
JTabbedPane tabs = new JTabbedPane();
tabs.addTab("Trains", buildTrainsTab(tabs));
tabs.addTab("Book Ticket", buildBookTab());
tabs.addTab("Manage Ticket", buildManageTab());
tabs.addTab("All Bookings", buildBookingsTab());
tabs.addTab("Reservation Chart", buildChartTab());
add(tabs, BorderLayout.CENTER);
add(buildNotifyPanel(), BorderLayout.SOUTH);

refreshAll();
setSize(860, 640);
setLocationRelativeTo(null);
    }
//sample trains
private void seedTrains() {
system.addTrain(new Train("12951", "Rajdhani Express", "Mumbai", "Delhi", 4));
system.addTrain(new Train("12627", "Karnataka Express", "Bangalore", "Delhi", 3));
system.addTrain(new Train("12841", "Coromandel Express", "Kolkata", "Chennai", 2));
system.addTrain(new Train("12009", "Shatabdi Express", "Mumbai", "Delhi", 2));
system.addTrain(new Train("16526", "Island Express", "Bangalore", "Kanyakumari", 3));
}

//tabs

    private JPanel buildTrainsTab(JTabbedPane tabs) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.add(new JLabel("From:"));
        top.add(searchFrom);
        top.add(new JLabel("To:"));
        top.add(searchTo);
        JButton searchBtn = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        top.add(searchBtn);
        top.add(showAllBtn);
        panel.add(top, BorderLayout.NORTH);

        trainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trainTable.setRowHeight(24);
        panel.add(new JScrollPane(trainTable), BorderLayout.CENTER);

        JButton bookSelected = new JButton("Book Selected Train");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(bookSelected);
        panel.add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> {
            String from = searchFrom.getText().trim();
            String to = searchTo.getText().trim();
            if (from.isEmpty() || to.isEmpty()) {
                warn("Please enter both source and destination stations.");
                return;
            }
            searchActive = true;
            refreshTrains();
            if (trainModel.getRowCount() == 0) {
                info("No trains found from " + from + " to " + to + ".");
            }
        });
        showAllBtn.addActionListener(e -> {
            searchActive = false;
            searchFrom.setText("");
            searchTo.setText("");
            refreshTrains();
        });
        bookSelected.addActionListener(e -> {
            int row = trainTable.getSelectedRow();
            if (row < 0) {
                warn("Select a train from the table first.");
                return;
            }
            Train t = system.findTrain((String) trainModel.getValueAt(row, 0));
            bookTrainCombo.setSelectedItem(t);
            tabs.setSelectedIndex(1);
            nameField.requestFocusInWindow();
        });
        return panel;
    }

    private JPanel buildBookTab() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Passenger details"));

        bookTrainCombo = newTrainCombo();
        bookTrainCombo.addActionListener(e -> updateBookInfo());

        addRow(form, 0, "Train:", bookTrainCombo);
        addRow(form, 1, "Availability:", bookInfo);
        addRow(form, 2, "Passenger name:", nameField);
        addRow(form, 3, "Age:", ageSpinner);
        addRow(form, 4, "Contact (phone / e-mail):", contactField);

        JButton bookBtn = new JButton("Book Ticket");
        JButton clearBtn = new JButton("Clear");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(clearBtn);
        buttons.add(bookBtn);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 6, 6, 6);
        form.add(buttons, gc);

        bookBtn.addActionListener(e -> doBook());
        clearBtn.addActionListener(e -> {
            nameField.setText("");
            contactField.setText("");
            ageSpinner.setValue(25);
        });

        outer.add(form, BorderLayout.NORTH);
        return outer;
    }

    private JPanel buildManageTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.add(new JLabel("Ticket ID:"));
        top.add(ticketIdField);
        JButton viewBtn = new JButton("View Status");
        JButton cancelBtn = new JButton("Cancel Ticket");
        top.add(viewBtn);
        top.add(cancelBtn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(ticketDetails), BorderLayout.CENTER);

        viewBtn.addActionListener(e -> doView());
        ticketIdField.addActionListener(e -> doView());
        cancelBtn.addActionListener(e -> doCancel(ticketIdField.getText().trim()));
        return panel;
    }

    private JPanel buildBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketTable.setRowHeight(24);
        panel.add(new JScrollPane(ticketTable), BorderLayout.CENTER);

        JButton cancelSel = new JButton("Cancel Selected Ticket");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancelSel);
        panel.add(bottom, BorderLayout.SOUTH);

        cancelSel.addActionListener(e -> {
            int row = ticketTable.getSelectedRow();
            if (row < 0) {
                warn("Select a booking from the table first.");
                return;
            }
            doCancel((String) ticketModel.getValueAt(row, 0));
        });
        return panel;
    }

    private JPanel buildChartTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        chartTrainCombo = newTrainCombo();
        chartTrainCombo.addActionListener(e -> refreshChart());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.add(new JLabel("Train:"));
        top.add(chartTrainCombo);
        top.add(chartInfo);
        panel.add(top, BorderLayout.NORTH);

        chartTable.setRowHeight(24);
        panel.add(new JScrollPane(chartTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildNotifyPanel() {
        notifyLog.setEditable(false);
        notifyLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> notifyLog.setText(""));

        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel(" Notifications (simulated SMS / e-mail)"), BorderLayout.WEST);
        header.add(clear, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(notifyLog), BorderLayout.CENTER);
        return panel;
    }

    // --------------------------------------------------------------- actions

    private void doBook() {
        Train train = (Train) bookTrainCombo.getSelectedItem();
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        int age = (Integer) ageSpinner.getValue();

        if (train == null) {
            warn("Please select a train.");
            return;
        }
        if (name.isEmpty() || contact.isEmpty()) {
            warn("Passenger name and contact cannot be blank.");
            return;
        }

        Ticket ticket = system.bookTicket(train.getTrainNumber(), new Passenger(name, age, contact));
        if (ticket == null) {
            warn("Train not found.");
            return;
        }
        refreshAll();

        String heading = ticket.isConfirmed()
                ? ">>> BOOKING CONFIRMED <<<"
                : ">>> TRAIN FULL - ADDED TO WAITING LIST <<<";
        showText(heading, ticket.details(),
                ticket.isConfirmed() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        ticketIdField.setText(ticket.getTicketId());
        nameField.setText("");
        contactField.setText("");
    }

    private void doView() {
        String id = ticketIdField.getText().trim();
        if (id.isEmpty()) {
            warn("Enter a ticket ID.");
            return;
        }
        Ticket t = system.findTicket(id);
        ticketDetails.setText(t == null ? "No ticket found with ID " + id + "." : t.details());
        ticketDetails.setCaretPosition(0);
    }

    private void doCancel(String id) {
        if (id.isEmpty()) {
            warn("Enter a ticket ID to cancel.");
            return;
        }
        if (system.findTicket(id) == null) {
            warn("No ticket found with ID " + id + ".");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Cancel ticket " + id.toUpperCase() + "?", "Confirm cancellation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Ticket promoted = system.cancelTicket(id);
            refreshAll();
            ticketIdField.setText(id.toUpperCase());
            doView();
            StringBuilder msg = new StringBuilder("Ticket " + id.toUpperCase() + " cancelled successfully.");
            if (promoted != null) {
                msg.append("\n\n>>> WAITLIST PROMOTION <<<\n")
                   .append(promoted.getPassenger().getName())
                   .append(" has been moved from the waiting list to a confirmed seat.\n\n")
                   .append(promoted.details());
                showText("Ticket cancelled", msg.toString(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                info(msg.toString());
            }
        } catch (IllegalArgumentException ex) {
            warn(ex.getMessage());
        }
    }

    // --------------------------------------------------------------- refresh

    private void refreshAll() {
        refreshTrains();
        updateBookInfo();
        refreshTickets();
        refreshChart();
    }

    private void refreshTrains() {
        trainModel.setRowCount(0);
        List<Train> trains = searchActive
                ? system.searchTrains(searchFrom.getText(), searchTo.getText())
                : system.getAllTrains();
        for (Train t : trains) {
            trainModel.addRow(new Object[]{
                    t.getTrainNumber(), t.getTrainName(), t.getSource(), t.getDestination(),
                    t.getAvailableSeats() + "/" + t.getTotalSeats(), t.getWaitlistSize()});
        }
    }

    private void updateBookInfo() {
        Train t = (Train) bookTrainCombo.getSelectedItem();
        if (t == null) {
            bookInfo.setText(" ");
            return;
        }
        bookInfo.setText("Seats available: " + t.getAvailableSeats() + "/" + t.getTotalSeats()
                + "  |  Waiting list: " + t.getWaitlistSize());
    }

    private void refreshTickets() {
        ticketModel.setRowCount(0);
        for (Ticket t : system.getAllTickets()) {
            ticketModel.addRow(new Object[]{
                    t.getTicketId(), t.getTrain().getTrainNumber(), t.getPassenger().getName(),
                    seatText(t), statusText(t)});
        }
    }

    private void refreshChart() {
        chartModel.setRowCount(0);
        Train train = (Train) chartTrainCombo.getSelectedItem();
        if (train == null) {
            return;
        }
        chartInfo.setText("   Seats available: " + train.getAvailableSeats() + "/"
                + train.getTotalSeats() + "  |  Waiting list: " + train.getWaitlistSize());
        for (Ticket t : system.getTicketsForTrain(train.getTrainNumber())) {
            if (!t.isCancelled()) {
                chartModel.addRow(new Object[]{
                        t.getTicketId(), t.getPassenger().getName(), seatText(t), statusText(t)});
            }
        }
    }

    // --------------------------------------------------------------- helpers

    private static String seatText(Ticket t) {
        return t.getSeatNumber() > 0 ? String.valueOf(t.getSeatNumber()) : "-";
    }

    private static String statusText(Ticket t) {
        if (t.isWaitlisted()) {
            int pos = t.getTrain().waitlistPositionOf(t);
            if (pos > 0) {
                return "WAITLISTED (WL #" + pos + ")";
            }
        }
        return t.getStatus().toString();
    }

    private JComboBox<Train> newTrainCombo() {
        JComboBox<Train> combo = new JComboBox<>();
        for (Train t : system.getAllTrains()) {
            combo.addItem(t);
        }
        return combo;
    }

    private static DefaultTableModel makeModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JTextArea monoArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        return area;
    }

    private static void addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        form.add(new JLabel(label), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, gc);
    }

    private void showText(String title, String text, int messageType) {
        JTextArea area = monoArea(14, 58);
        area.setText(text);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title, messageType);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}

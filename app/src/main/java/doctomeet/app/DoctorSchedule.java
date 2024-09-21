package doctomeet.app;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorSchedule {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database";
    private static final String USER = "your_username";
    private static final String PASS = "your_password";
    private static final String H2_DB_URL = "jdbc:h2:tcp://localhost:9090/mem:dev";  
    private static final String H2_USER = "your_username";
    private static final String H2_PASS = "your_password";

    public Map<LocalDate, List<LocalTime>> getAvailableSlotsH2(LocalDate startDate) {
        Map<LocalDate, List<LocalTime>> availableSlots = new HashMap<>();
    try {
        Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException e) {
        e.printStackTrace();
    }
        try (Connection conn = DriverManager.getConnection(H2_DB_URL, H2_USER, H2_PASS)) {
            for (int i = 0; i < 7; i++) {
                LocalDate date = startDate.plusDays(i);
                List<LocalTime> slots = getAvailableSlotsForDate(conn, date);
                availableSlots.put(date, slots);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableSlots;
    }
    public Map<LocalDate, List<LocalTime>> getAvailableSlots(LocalDate startDate) {
        Map<LocalDate, List<LocalTime>> availableSlots = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            for (int i = 0; i < 7; i++) {
                LocalDate date = startDate.plusDays(i);
                List<LocalTime> slots = getAvailableSlotsForDate(conn, date);
                availableSlots.put(date, slots);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableSlots;
    }

    public List<LocalTime> getAvailableSlotsForDate(Connection conn, LocalDate date) throws SQLException {
        List<LocalTime> availableSlots = new ArrayList<>();
        List<LocalTime> openings = new ArrayList<>();
        List<LocalTime> appointments = new ArrayList<>();

        String query = "SELECT type, start_time, end_time FROM events WHERE date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("type");
                LocalTime startTime = rs.getTime("start_time").toLocalTime();
                LocalTime endTime = rs.getTime("end_time").toLocalTime();

                if ("opening".equals(type)) {
                    openings.add(startTime);
                    while (startTime.isBefore(endTime)) {
                        availableSlots.add(startTime);
                        startTime = startTime.plusMinutes(30); // Assuming 30-minute slots
                    }
                } else if ("appointment".equals(type)) {
                    while (startTime.isBefore(endTime)) {
                        appointments.add(startTime);
                        startTime = startTime.plusMinutes(30); // Assuming 30-minute slots
                    }
                }
            }
        }

        availableSlots.removeAll(appointments);
        return availableSlots;
    }

    public static void main(String[] args) {
        DoctorSchedule schedule = new DoctorSchedule();
        LocalDate startDate = LocalDate.now();
        Map<LocalDate, List<LocalTime>> slots = schedule.getAvailableSlotsH2(startDate);
        System.out.println("Slot Size: " + slots.size());
        for (Map.Entry<LocalDate, List<LocalTime>> entry : slots.entrySet()) {
            //System.out.println("Date: " + entry.getKey());
            for (LocalTime time : entry.getValue()) {
                System.out.println("  Available slot: " +"Date: " + entry.getKey()+"Time:" + time);
            }
        }
    }
}



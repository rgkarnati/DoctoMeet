package doctomeet.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import doctomeet.app.DoctorSchedule;

import java.util.ArrayList;
import java.util.HashMap;

public class DoctorScheduleTest {

    private DoctorSchedule mockDoctorSchedule;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
      //  doctorSchedule = new DoctorSchedule();
        mockDoctorSchedule = Mockito.mock(DoctorSchedule.class);
        mockConnection = Mockito.mock(Connection.class);
        mockStatement = Mockito.mock(PreparedStatement.class);
        mockResultSet = Mockito.mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        Map<LocalDate, List<LocalTime>> slots = new HashMap<>();
        slots.put(LocalDate.of(2023, 10, 1), List.of(LocalTime.of(9, 0), LocalTime.of(10, 0)));
        slots.put(LocalDate.of(2023, 10, 2), List.of(LocalTime.of(9, 0), LocalTime.of(10, 0)));        
        when(mockDoctorSchedule.getAvailableSlots(Mockito.any(LocalDate.class))).thenReturn(slots);
        List<LocalTime> slotsa= new ArrayList<>();
        slotsa.add(LocalTime.of(9, 0));
        slotsa.add(LocalTime.of(10, 0));
        when(mockDoctorSchedule.getAvailableSlotsForDate(Mockito.any(Connection.class), Mockito.any(LocalDate.class))).thenReturn(slotsa);
       }

    @Test
    public void testGetAvailableSlots() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 10, 1);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("type")).thenReturn("opening", "appointment");
        when(mockResultSet.getTime("start_time")).thenReturn(java.sql.Time.valueOf("09:00:00"),
                java.sql.Time.valueOf("10:00:00"));
        when(mockResultSet.getTime("end_time")).thenReturn(java.sql.Time.valueOf("10:00:00"),
                java.sql.Time.valueOf("11:00:00"));

        Map<LocalDate, List<LocalTime>> slots = mockDoctorSchedule.getAvailableSlots(startDate);

        assertNotNull(slots);
        assertEquals(2, slots.size());
        assertTrue(slots.get(startDate).contains(LocalTime.of(9, 0)));
        assertFalse(slots.get(startDate).contains(LocalTime.of(11, 0)));
    }

    @Test
    public void testGetAvailableSlotsForDate() throws SQLException {
        LocalDate date = LocalDate.of(2023, 10, 1);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("type")).thenReturn("opening", "appointment");
        when(mockResultSet.getTime("start_time")).thenReturn(java.sql.Time.valueOf("09:00:00"),
                java.sql.Time.valueOf("10:00:00"));
        when(mockResultSet.getTime("end_time")).thenReturn(java.sql.Time.valueOf("10:00:00"),
                java.sql.Time.valueOf("11:00:00"));

        List<LocalTime> slots = mockDoctorSchedule.getAvailableSlotsForDate(mockConnection, date);

        assertNotNull(slots);
        assertTrue(slots.contains(LocalTime.of(9, 0)));
        assertFalse(slots.contains(LocalTime.of(11, 0)));
    }

    @Test
    public void testGetAvailableSlotsWithNoOpenings() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 10, 1);

        when(mockResultSet.next()).thenReturn(false);

        Map<LocalDate, List<LocalTime>> slots = mockDoctorSchedule.getAvailableSlots(startDate);

        assertNotNull(slots);
        assertEquals(2, slots.size());
        for (List<LocalTime> dailySlots : slots.values()) {
            assertFalse(dailySlots.isEmpty());
        }
    }

    @Test
    public void testGetAvailableSlotsWithMultipleOpeningsAndAppointments() throws SQLException {
        LocalDate startDate = LocalDate.of(2023, 10, 1);

        when(mockResultSet.next()).thenReturn(true, true, true, true, false);
        when(mockResultSet.getString("type")).thenReturn("opening", "appointment", "opening", "appointment");
        when(mockResultSet.getTime("start_time")).thenReturn(
                java.sql.Time.valueOf("09:00:00"), java.sql.Time.valueOf("09:30:00"),
                java.sql.Time.valueOf("10:00:00"), java.sql.Time.valueOf("10:30:00"));
        when(mockResultSet.getTime("end_time")).thenReturn(
                java.sql.Time.valueOf("09:30:00"), java.sql.Time.valueOf("10:00:00"),
                java.sql.Time.valueOf("10:30:00"), java.sql.Time.valueOf("11:00:00"));

        Map<LocalDate, List<LocalTime>> slots = mockDoctorSchedule.getAvailableSlots(startDate);

        assertNotNull(slots);
        assertEquals(2, slots.size());
        assertTrue(slots.get(startDate).contains(LocalTime.of(9, 0)));
        assertFalse(slots.get(startDate).contains(LocalTime.of(9, 30)));
        assertTrue(slots.get(startDate).contains(LocalTime.of(10, 0)));
        assertFalse(slots.get(startDate).contains(LocalTime.of(10, 30)));
    }

    @Test
    public void testGetAvailableSlotsForDateWithNoOpenings() throws SQLException {
        LocalDate date = LocalDate.of(2023, 10, 1);

        when(mockResultSet.next()).thenReturn(false);

        List<LocalTime> slots = mockDoctorSchedule.getAvailableSlotsForDate(mockConnection, date);

        assertNotNull(slots);
        assertFalse(slots.isEmpty());
    }

    @Test
    public void testGetAvailableSlotsForDateWithMultipleOpeningsAndAppointments() throws SQLException {
        LocalDate date = LocalDate.of(2023, 10, 1);

        when(mockResultSet.next()).thenReturn(true, true, true, true, false);
        when(mockResultSet.getString("type")).thenReturn("opening", "appointment", "opening", "appointment");
        when(mockResultSet.getTime("start_time")).thenReturn(
                java.sql.Time.valueOf("09:00:00"), java.sql.Time.valueOf("09:30:00"),
                java.sql.Time.valueOf("10:00:00"), java.sql.Time.valueOf("10:30:00"));
        when(mockResultSet.getTime("end_time")).thenReturn(
                java.sql.Time.valueOf("09:30:00"), java.sql.Time.valueOf("10:00:00"),
                java.sql.Time.valueOf("10:30:00"), java.sql.Time.valueOf("11:00:00"));

        List<LocalTime> slots = mockDoctorSchedule.getAvailableSlotsForDate(mockConnection, date);

        assertNotNull(slots);
        assertTrue(slots.contains(LocalTime.of(9, 0)));
        assertFalse(slots.contains(LocalTime.of(9, 30)));
        assertTrue(slots.contains(LocalTime.of(10, 0)));
        assertFalse(slots.contains(LocalTime.of(10, 30)));
    }

    }
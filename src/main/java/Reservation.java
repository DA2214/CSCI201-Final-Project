
import java.sql.Timestamp;

public class Reservation {
    private int reservationId;
    private int userId;
    private int machineId;
    private Timestamp createdAt;
    private Timestamp expiresAt;
    private ReservationStatus status;
    private int intendedDuration; // Duration in minutes user wants to work out
    private Timestamp workoutStartTime; // When user actually starts the workout
    private Timestamp workoutEndTime; // When workout is scheduled to end

    public Reservation() {}

    public Reservation(int reservationId, int userId, int machineId, 
                       Timestamp createdAt, Timestamp expiresAt, ReservationStatus status,
                       int intendedDuration, Timestamp workoutStartTime, Timestamp workoutEndTime) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.machineId = machineId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.intendedDuration = intendedDuration;
        this.workoutStartTime = workoutStartTime;
        this.workoutEndTime = workoutEndTime;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public int getIntendedDuration() {
        return intendedDuration;
    }

    public void setIntendedDuration(int intendedDuration) {
        this.intendedDuration = intendedDuration;
    }

    public Timestamp getWorkoutStartTime() {
        return workoutStartTime;
    }

    public void setWorkoutStartTime(Timestamp workoutStartTime) {
        this.workoutStartTime = workoutStartTime;
    }

    public Timestamp getWorkoutEndTime() {
        return workoutEndTime;
    }

    public void setWorkoutEndTime(Timestamp workoutEndTime) {
        this.workoutEndTime = workoutEndTime;
    }
}


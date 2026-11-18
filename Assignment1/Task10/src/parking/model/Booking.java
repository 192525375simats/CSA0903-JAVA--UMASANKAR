package parking.model;

/** Model class: all fields private, validation in setters (encapsulation). */
public class Booking {
    private String regNo;
    private int durationMinutes;
    private int slotNumber;
    int packageVisible = 1;               // default (package-private): visible only inside parking.model
    protected double rate = 30;           // protected: visible to subclasses and the same package

    public Booking(String regNo, int durationMinutes, int slotNumber) {
        setRegNo(regNo);                  // constructor re-uses the validating setters
        setDurationMinutes(durationMinutes);
        setSlotNumber(slotNumber);
    }

    public String getRegNo() { return regNo; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getSlotNumber() { return slotNumber; }

    public void setRegNo(String regNo) {
        if (regNo == null || !regNo.matches("[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{4}"))
            throw new IllegalArgumentException("Invalid registration number: " + regNo);
        this.regNo = regNo;
    }
    public void setDurationMinutes(int m) {
        if (m < 0) throw new IllegalArgumentException("Duration cannot be negative: " + m);
        this.durationMinutes = m;
    }
    public void setSlotNumber(int s) {
        if (s < 0 || s >= 100) throw new IllegalArgumentException("Invalid slot number: " + s);
        this.slotNumber = s;
    }
}

# Task 2 – Appointment Desk Design

## 2.1 Event model

| Event source | Event object | Listener | Action |
|---|---|---|---|
| Department JComboBox | ItemEvent / ActionEvent | ItemListener / ActionListener | Refresh doctor choices |
| Book JButton | ActionEvent | ActionListener | Validate and book |
| Window | WindowEvent | WindowListener / WindowAdapter | Confirm close if booking is active |
| Time slot JComboBox | ActionEvent | ActionListener (optional) | Update selected slot/availability |
| Patient ID JTextField | ActionEvent | ActionListener (optional) | Validate on Enter |

## 2.2 Component hierarchy
```text
AppointmentDesk (JFrame, BorderLayout)
├── Form JPanel (GridLayout 4x2)
│   ├── JLabel + JTextField (patient ID)
│   ├── JLabel + JComboBox (department)
│   ├── JLabel + JComboBox (doctor)
│   └── JLabel + JComboBox (time slot)
└── Bottom JPanel (BorderLayout)
    ├── JButton (Book Appointment)
    └── JLabel (status/confirmation)
```

## 2.3 Handler pseudocode
**Refresh doctors**
1. Read selected department.
2. Remove all entries from doctor combo box.
3. Find the department's doctor list in Map.
4. Add each doctor to the combo box.
5. Select the first doctor if the list is not empty.

**Book appointment**
1. Read patient ID, department, doctor and slot.
2. If any required value is empty, show a clear error and stop.
3. Validate patient ID format.
4. Set bookingInProgress = true and disable duplicate submission.
5. Call appointment service asynchronously.
6. On success, show confirmation; on failure, show error.
7. Set bookingInProgress = false.

## 2.4 Event trace and applet comparison
Main -> SwingUtilities.invokeLater -> AppointmentDesk constructor -> component creation/layout -> register listeners -> refreshDoctors -> setVisible -> user event -> listener -> handler -> confirmation. On close, windowClosing is invoked; the handler asks for confirmation when a booking is in progress, then dispose() closes the frame. An applet uses init(), start(), paint(), stop() and destroy() as container-managed lifecycle callbacks; a JFrame is created and disposed by application code and does not use the applet lifecycle.

## Alternative: fetch doctors on every department change
Database approach: each department change issues a query, approximately O(query latency + number of returned doctors), with low persistent cache memory O(1) beyond the current list. Preloaded Map approach: initial load O(m) time and O(m) memory for m doctor records across d departments; subsequent lookup is O(1) average plus O(r) to populate r doctors for the selected department. Repeated DB access adds latency and database load; a Map gives faster changes at the cost of memory and possible stale data.

# Task 3 – Campus Library Search Screen

## 3.1 Layout manager choices
- JFrame: BorderLayout. The results JScrollPane occupies CENTER and receives extra width/height.
- Search row: FlowLayout(LEFT), preserving natural component sizes.
- Footer: BorderLayout; navigation controls use FlowLayout and status message sits SOUTH.
- GridLayout is not used for the main screen because it forces equal cell sizing. CardLayout is unnecessary because only one screen is shown.

## 3.2 Nested structure
```text
JFrame (BorderLayout)
├── NORTH: title JLabel
├── PAGE_START: search JPanel (FlowLayout)
│   ├── JTextField
│   ├── JComboBox category
│   └── JButton Search
├── CENTER: JScrollPane -> JList results
└── SOUTH: footer JPanel (BorderLayout)
    ├── CENTER: button JPanel (FlowLayout: Prev/Next/Reserve)
    └── SOUTH: status JLabel
```

## 3.3 Implementation
See LibrarySearch.java. The JScrollPane is placed in CENTER so it grows when the frame is resized. The search controls and footer remain preferred-size FlowLayout rows.

## 3.4 Resize test plan
Run the program at 600x400, 850x550 and 1100x700. At each size, verify that the centre results region gains available space while the search row and footer retain their preferred component sizes. Replace the preview evidence with screenshots captured from the running desktop app.

## Alternative concept and complexity
GridBagLayout can express flexible weights and constraints in one container, but has more verbose constraints; layout calculation is typically O(c) for c components. Absolute positioning uses setLayout(null)/setBounds and can be quick to place, but controls do not adapt to resizing, font scaling or platform differences. Nested layout managers create a few additional JPanel containers (O(p) space for p panels), while absolute positioning can reduce containers but increases maintenance risk.

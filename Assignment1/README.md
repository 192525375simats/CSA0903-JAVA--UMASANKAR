# Assignment 1 - Campus Smart-Parking System (Tasks 1-11)

- **Name:** ______________________
- **Register Number:** ______________________
- **Java version:** OpenJDK 21 (`javac 21.0.13`) - Java 16+ is required (pattern-matching `instanceof` is used in `FeeDemo` and `Task11`).

## Folder layout

One folder per task. Each contains the Java source, `output.txt` (captured console output) and `output_screenshot.png` (that output rendered as an image).

## How to compile and run

Run these from inside the task folder.

| Task | Compile | Run |
|------|---------|-----|
| Task01 | `javac Task01.java` | `java Task01` |
| Task02 | `javac Task02.java` | `java Task02` |
| Task03 | `javac ParkingFacility.java Task03Extra.java` | `java ParkingFacility` and `java Task03Extra` |
| Task04 | `javac Task04.java` | `java Task04` |
| Task05 | `javac Task05.java` | `java Task05` |
| Task06 | `javac Task06.java` | `java Task06` |
| Task07 | `javac Task07.java` | `java Task07` |
| Task08 | `javac FeeDemo.java Task08Extra.java` | `java FeeDemo` and `java Task08Extra` |
| Task09 | `javac Task09.java` | `java Task09` |
| Task10 | `javac -d out src/parking/*/*.java` | `java -cp out parking.app.Task10` |
| Task11 | `javac Task11.java` | `java Task11` |

## Result of each task

| Task | Result |
|------|--------|
| 1 | Classes `Vehicle`, `ParkingSlot`, `ParkingTicket`, `Facility`; business rules R1-R4 demonstrated (duplicate entry, full facility, invalid release, ticket numbers never reused). Parallel-array vs object search compared (both O(n)). |
| 2 | Data types chosen; Scanner input; integer-division bug shows Rs 60 instead of Rs 90 for 130 min, fixed with `(m + 59) / 60`. Jagged array vs 1-D array vs `ArrayList<String[]>` compared (235 of 500 flat cells wasted). |
| 3 | `ParkingFacility` completed (TODO 1 and 2); output **matches the expected output exactly**. `pseudocode.txt` has the allocation and fee algorithms. `Task03Extra` compares the linear scan with the free-count + stack allocator. |
| 4 | Default, overloaded, chained and copy constructors; setters and static-factory alternatives compared. |
| 5 | Static counter, static block, static method; the two-facility problem and two fixes; object passing; garbage collection of a released ticket. |
| 6 | Hierarchy `Vehicle` -> `TwoWheeler` / `Car` -> `ElectricCar` with UML (comment at top of file) and the constructor-order trace; composition alternative. |
| 7 | `@Override`, `final`, `equals` / `hashCode` / `toString`; switch-on-type alternative. |
| 8 | `FeeDemo` completed (TODO 1, 2, 3); output **matches the expected output exactly**. The last line prints `true` because `equals()` compares only `regNo`. `Task08Extra` shows overloading, a dynamic-dispatch trace and the `instanceof` alternative. |
| 9 | Abstract class + interfaces with default and static methods; interface-based alternative and what it cannot express. |
| 10 | Packages `parking.model`, `parking.service`, `parking.app`; validating setters; `Integer ==` pitfall; public-field alternative. |
| 11 | Full system with a test table (16 tests, 16 pass) and a hand-calculated day total (430.0) that matches the program output. Array vs `ArrayList` vs `HashMap` lookup measured. |

## Notes

- Timing numbers in the outputs are measurements from one run and will differ slightly on your machine.
- `output_screenshot.png` files are the program's captured console output drawn as an image. Replace them with real terminal screenshots from your own machine if your faculty requires that.
- Task 12 (Day-4 Design Challenge) is not included here.

# B1 – Implementation and Experimental Investigation

PassStore.java completes the supplied TODO using BufferedReader and try-with-resources. It skips lines with the wrong field count, invalid route, invalid amount or negative/non-finite amount. A malformed row therefore cannot stop the search for a later valid row. The assignment's simple comma format is retained; names containing commas/newlines are rejected in add() rather than silently corrupting the record.

## Test table
| Test | Input | Expected |
|---|---|---|
| Normal find | P1 exists | Five fields returned |
| Missing ID | P404 | null |
| Damaged field count | three-field row | skipped |
| Non-numeric amount | amount=abc | skipped |
| Empty line | empty record | skipped |
| Route listing | two valid route-4 rows | count=2 |

## Measurements
Run BufferedLineCounter.java for 1, 10 and 50 MiB. It reports both timings in milliseconds. BufferedLineCounter uses a 64 KiB byte buffer; the unbuffered baseline calls read() one byte at a time. Complexity is O(N) time for either, O(1) extra space for the scalar unbuffered count and O(B) for the buffered version.

## Growth experiment
Use PassStore.find() against files containing 1,000, 10,000 and 100,000 records, recording elapsed time for an ID near the end. A scan is O(n) per lookup; repeated q lookups are O(qn). A HashMap loaded once requires O(n) startup and O(n) memory, then O(1) average lookup, total O(n+q). The file scan uses O(1) working memory.

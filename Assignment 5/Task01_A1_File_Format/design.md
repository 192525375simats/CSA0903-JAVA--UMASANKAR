# A1 – Design a File Format (Bus-Pass Records)

## A1.1 Record layouts
**Delimited text (one pass per line):** `passId|regNo|name|routeNo|boardingStop|validUntil|amountPaid`. Encode backslash as `\\` and pipe as `\|` in text fields; reject embedded newline or encode it as `\n`. Decode using a matching escape-aware parser, not plain split. Example: `P1001|192311001|Arun Kumar|12|Central|2027-06-30|4500.00`.

**Binary DataOutputStream:** fields in the same order. Use writeUTF(passId), writeUTF(regNo), writeUTF(name), writeInt(routeNo), writeUTF(boardingStop), writeUTF(validUntil ISO-8601), writeDouble(amountPaid). Each UTF field is length-prefixed by writeUTF; numeric fields have fixed widths (int=4 bytes, double=8 bytes). A version/magic header should precede records. This avoids delimiter ambiguity.

## A1.2 SIMATS-style pseudocode
**ADD PASS**
1. START
2. RECEIVE pass fields
3. VALIDATE required fields, route and non-negative amount
4. OPEN file using FileWriter(file, append=true), wrap in BufferedWriter
5. ENCODE delimiter characters; WRITE one complete record and newline
6. FLUSH and CLOSE using try-with-resources
7. REPORT success; on IOException report failure
8. STOP

**FIND BY PASS ID**
1. START; RECEIVE target ID
2. OPEN FileReader and wrap in BufferedReader
3. REPEAT readLine until EOF
4. DECODE line; IF malformed, record line number and CONTINUE
5. IF decoded pass ID equals target, DISPLAY record and CLOSE file; STOP
6. CLOSE file; DISPLAY not found
7. STOP

**LIST BY ROUTE**
1. START; RECEIVE route number
2. OPEN BufferedReader over FileReader
3. FOR each line until EOF, decode and validate
4. IF valid record route equals requested route, DISPLAY record
5. SKIP malformed records and log their line numbers
6. CLOSE file; STOP

BufferedReader/BufferedWriter reduce small read/write calls and provide line operations. FileReader/FileWriter are character streams suitable for text. FileInputStream/FileOutputStream are byte streams, suitable for binary data; wrap them in buffered streams for efficient block I/O.

## A1.3 Damaged data and recovery
- Damaged line: verify field count, numeric formats, date and checksum/record version; quarantine malformed line with line number and continue.
- Missing file: distinguish Files.notExists from access errors; create an empty file only for an explicit first-run case, otherwise report missing data.
- Interrupted append: write to a temporary file and atomically replace the original for full rewrites; for append, use a journal/length prefix/checksum and recover by ignoring an incomplete final record. Flush and force storage when durability is required; backups remain necessary.

## Alternative: fixed-length RandomAccessFile
Store fixed-size padded fields and numeric values at known offsets. Record i is read by seek(headerSize + i × recordSize), so locating a known index is O(1). But searching by pass ID without an index remains O(n); maintain a pass-ID-to-record-index index for O(1) average lookup. Padding wastes approximately Σ(maxFieldBytes − actualFieldBytes) bytes per record. Sequential text scan is O(n), uses O(1) working memory; fixed-size seek is O(1) by index but has padding and encoding constraints.

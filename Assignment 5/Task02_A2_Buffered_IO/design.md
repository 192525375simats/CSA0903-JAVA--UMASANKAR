# A2 – Don't Read What You Don't Need

## A2.1 Predict the cost
A 50 MiB file contains 52,428,800 bytes. Calling FileInputStream.read() once per byte makes about 52.4 million Java read invocations. These may be served through runtime/OS buffering, so it is inaccurate to claim exactly one physical disk operation per call; nevertheless the per-call Java/native overhead is large.

## A2.2 Buffered redesign
Use BufferedInputStream with an 8 KiB or 64 KiB buffer and count newline bytes. A 50 MiB file with 8 KiB blocks requires about 6,400 bulk refill operations instead of 52.4 million single-byte calls. Time remains O(N); working memory is O(B), where B is buffer size.

## A2.3 When buffering adds little
If the source is already an in-memory byte array, or the caller already reads large blocks from a buffered stream, adding another buffer may add copying and memory without reducing underlying calls. For tiny files, setup overhead can dominate.

## Alternative: Files.readAllLines
readAllLines loads all lines and their String objects: O(N) time and O(N) memory, potentially much more than 50 MiB due to object/character overhead. A buffered streaming counter uses O(B) memory and is preferable for large logs or constrained memory. readAllLines is convenient when the entire modest file must be processed repeatedly and fits comfortably in memory.

## Pseudocode
START; OPEN BufferedInputStream; SET count=0; REPEAT read byte blocks; FOR each byte increment count when byte is newline; CLOSE stream; DISPLAY count; STOP.

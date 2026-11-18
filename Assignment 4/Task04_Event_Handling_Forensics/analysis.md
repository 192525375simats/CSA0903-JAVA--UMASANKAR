# Task 4 – Event-Handling Forensics

## 4.1 Defect
The method is written as mouseclicked, but the MouseListener callback is mouseClicked with a capital C. Therefore it does not override the callback and is never invoked. The label is never updated because the code does not call status.setText(...).

## 4.2 Failing interaction
Click several times to draw dots, then minimise and restore the window or cover it with another window. Direct drawing through getGraphics() is temporary; AWT may repaint the window and erase those pixels because the points were not stored.

## 4.3 Repair
DrawPad.java stores each click as a Point, updates the label and calls repaint(). paint(Graphics) redraws every stored point. A 6x6 circle is centred at (x,y) by drawing at (x-3,y-3).

## 4.4 @Override
Adding @Override above the incorrectly named mouseclicked method makes the compiler report that the method does not override or implement a method from a supertype. This catches the spelling/capitalisation error before execution.

## Alternative: BufferedImage
Create an off-screen BufferedImage matching the drawing surface, draw each new dot into its Graphics2D, and draw the image in paint(). Repainting copies/draws one image (approximately O(width × height) pixels, often accelerated) rather than iterating O(n) points. A point list uses O(n) point storage; the image uses O(width × height) pixels. The image is fast for many redraws but consumes more fixed memory and must be recreated when the canvas size changes.

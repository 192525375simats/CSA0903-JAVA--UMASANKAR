# Task 5 – Four Event-Handling Styles

All four programs convert INR to USD (rate 0.012) or EUR (rate 0.011), respond to the Convert button and Enter in the amount field, show a dialog, and report invalid amounts.

| Style | Readability | Access to frame fields | Reuse | Change effort |
|---|---|---|---|---|
| Frame implements ActionListener | Simple for tiny forms; frame can become crowded | Direct | One frame handler, can branch on source | Medium; handler grows with controls |
| Named inner class | Clear named responsibility | Direct access to enclosing frame | Easy to reuse for several controls | Low/medium; update one class |
| Anonymous inner class | Local but can become lengthy | Direct | Usually local to registration point | Medium; repeated code can be awkward |
| Lambda | Concise for short action | Captures enclosing fields | Same lambda variable can be registered on button and text field | Low for a simple shared action |

For a large application, prefer a named Action/handler or a shared Swing Action. It centralises behaviour and can be reused by buttons, menu items and keyboard shortcuts. A method reference such as this::convert is concise when the method signature matches the listener.

## Task 5.2 change record
The shared conversion method is used by both the button and text field in all four implementations, so adding Enter support requires one extra listener registration. Replacing only the result label with a JOptionPane call is one change in each handler. Frame-listener style may need an event-source branch if different controls later need different behaviour.

## Alternative concept and complexity
A method reference creates a compact listener adapter around the existing method. A shared AbstractAction can be attached to a button, menu item and key binding. Reusing one Action reduces duplicate handler logic and listener instances; adding a trigger is one registration. Separate anonymous/lambda listener instances per trigger use O(t) listener objects for t triggers; a shared Action uses O(1) action object plus O(t) component references. Event dispatch itself is O(1) for a registered listener.

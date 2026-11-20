# Boundary: in-process events

CDI events (`Event<T>.fire`, `@Observes`) and other calls inside the application carry data the application
produced itself. That data is **trusted**: it was a domain value already, or it was built from one.

- Pass the domain value itself, not its primitives. Then the observer cannot receive an unchecked value.
- When an observer has to build a value, use the **throwing constructor**, not `parse()`. A violation there is a
  bug, not bad input, and should fail loudly.
- There is no invalid branch, no `<OP>_INVALID` and no 400.

The ArchUnit rule does not apply here: observers live in `core` or in `outbound-*`, not in `inbound-*`. If an
`inbound-*` module observes events whose data came from outside (e.g. a webhook relayed internally), treat it as
the boundary it came from and use `parse()`.

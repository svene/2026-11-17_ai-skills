# Boundary: HTML/htmx (`inbound-http-json-vm`)

This module belongs to the htmx skill, which defines the `{route, vm}` envelope. This reference only describes
what the invalid branch returns. If the module does not exist, don't create it here: point the user to the
htmx skill.

## The invalid branch

The form posts its fields as text. The Receiver passes them to the text overload, `Xxx.parse(String, …)`, so an
empty or non-numeric field is a violation, not a JAX-RS 400 before the method runs. (An `int` `@FormParam` fails
before the method runs.)

- **Valid:** call the Handler and answer the way the htmx skill's mutation resources do (no body, `HX-Trigger`).
- **Invalid:** **400** with the form's own envelope:
  ```json
  {"route": "<FormRoute>", "vm": {"name": " ", "count": "abc", "errors": {"name": "must not be blank", "count": "must be a number"}}}
  ```
  `vm` holds the **submitted** values, unchanged, so the user's input survives. `errors` maps each field (the last
  node of the property path) to its message. When a field has two violations, keep the first after sorting by
  message, so the output is stable. The form's template renders both states, and htmx re-renders it into the same
  target. The form VM always carries `errors`, empty on GET.

400 (not 422) is the status, the same as on every other boundary. Log `<OP>_RECEIVED` before `parse()` and
`<OP>_INVALID` (WARN) in the invalid branch.

## Test

A `@QuarkusTest` in `app-server`: an invalid POST returns 400, and the envelope's `vm.errors` names the field and
its message. Another POST checks that the submitted values come back unchanged. No browser test is needed.

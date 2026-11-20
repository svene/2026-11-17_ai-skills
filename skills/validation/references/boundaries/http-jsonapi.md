# Boundary: HTTP JSON API (`inbound-http-jsonapi`)

The caller is synchronous, so the invalid branch answers it: **400** with every violation, as
`[{"field": "<component>", "message": "<message>"}]`, sorted by field, then message. The template is
`${CLAUDE_SKILL_DIR}/templates/inbound-http-jsonapi/ExampleReceiver.java`.

## The shape

- **The DTO stays the wire format**: a plain record (`ExampleJson`), with no constraint annotations and no `@Valid`.
  Numbers the user types are `String` on the wire, so `"abc"` reaches `parse(String, String)` and becomes a
  violation with a field name, not a Jackson error without one. If the API must accept JSON numbers only, keep the
  number type and accept Jackson's 400 for the wrong type.
- **A null body** (an empty request) is answered before `parse()`: `[{"field": "body", "message": "must not be empty"}]`.
  A missing field arrives as `null` and is reported by the domain's `@NotBlank` (on the text constructor).
- **The method** logs `<OP>_RECEIVED`, then switches:
  ```java
  return switch (Example.parse(request.name(), request.count())) {
      case Example example -> Response.status(CREATED).entity(new AcceptedJson(handler.accept(example))).build();
      case ParsedExample.Invalid invalid -> badRequest(toJson(invalid.violations()));
  };
  ```
- **The field name** is the last node of the violation's property path. For a constructor parameter Hibernate
  Validator reports `Example.count`, so the field is `count`. This works when the DTO's field names match the
  record's components. When they differ, map the names in `toJson`.
- `badRequest` logs `<OP>_INVALID` (WARN) with the violations.

No exception mapper is involved: the 400 is built in the method from the switch.

## Test

A `@QuarkusTest` in `app-server` (template: `templates/app-server/ExampleReceiverTest.java`):
- valid input gives 201, and the Handler saw the value
- one test per constraint: the 400 names the field and the message
- `{}` gives 400 naming every field
- an invalid request never reaches the Handler

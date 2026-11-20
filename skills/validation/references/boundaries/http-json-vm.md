# Boundary: HTML/htmx (`inbound-http-json-vm`)

This module belongs to the htmx-hono skill, which defines the `{route, vm}` envelope and the shape of a form
(htmx-hono's `references/add-page.md`, steps 4 and 6). This reference only covers what `parse()` changes in it. If
the module does not exist, don't create it here: point the user to the htmx-hono skill.

## The form action

A form posts to a method of the feature's `<X>ActionResource` (literal `@Path`, returns `Response`). Its fields
arrive as text: `@FormParam String`, passed to the text overload `Xxx.parse(String, …)`, so an empty or
non-numeric field is a violation. (An `int` `@FormParam` fails with a bare 400 before the method runs.)

- **Valid:** call the Handler, then `Response.noContent()` with `HtmxHeaders.HX_TRIGGER` set to the feature's
  `UiEventName` (or `HtmxHeaders.HX_REDIRECT`).
- **Invalid:** 400 with the form's envelope:
  ```java
  return Response.status(Response.Status.BAD_REQUEST)
      .entity(new UiResponse(UiRouteName.<X>Form, new <X>FormVM(name, count, errors)))
      .build();
  ```
  which serializes to
  ```json
  {"route": "<X>Form", "vm": {"name": " ", "count": "abc", "errors": {"name": "must not be blank", "count": "must be a number"}}}
  ```
  The `<X>FormVM` holds the **submitted** values, unchanged, so the user's input survives. `errors` maps each field
  (the last node of the property path) to its message. When a field has two violations, keep the first after
  sorting by message, so the output is stable. The form VM always carries `errors`, empty from its `empty()` on GET;
  its template renders both states, and htmx re-renders it into the same target.

400 (not 422) is the status, the same as on every other boundary. Log `<OP>_RECEIVED` before `parse()` and
`<OP>_INVALID` (WARN) in the invalid branch.

## Test

Extend the feature's `<X>UiTest` in `app-server`: an invalid POST answers 400 with `route` `<X>Form`, and
`vm.errors` names the field and its message. Another POST checks that the submitted values come back unchanged.
Browser tests belong to the Playwright skill.

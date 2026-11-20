# Existing project

For a Triptych project with real feature slices and without `ConstructorValidation`. The work runs in **stages**.
Each stage is confirmed before it starts (unless called `from: setup-guide`) and ends on a green `./mvnw verify`,
so the user can commit between stages. Never commit yourself.

## Stage 1: the infrastructure

**Confirm** with one line: `Validation infrastructure in <dir>: ConstructorValidation, quarkus-hibernate-validator in core, ValidationArchitectureTest, README link, package <p>`.

Run `${CLAUDE_SKILL_DIR}/scripts/add-infrastructure.sh <dir> [--package <p>]`, then `./mvnw verify`.

The rule may already fail, if an inbound class constructs a type that implements an interface whose name starts
with `Parsed`. That can only be a type the project made itself; show the violations and ask whether to fix them
first (stage 2) or rename the interface. Don't disable the rule.

Done when the build is green and `ValidationArchitectureTest` ran.

## Stage 2..n: one endpoint or consumer per stage

1. **Choose.** The user names the endpoint or consumer. To help, list the candidates:
   - every `@Valid` in `inbound-*` modules
   - every Receiver method that passes request fields straight to a Handler

   Show each with a file reference and let the user pick.
2. **Confirm** the stage with one line: `<Receiver>.<method>: <Value> + Parsed<Value> in core feature.<x>, <boundary>, @Valid removed`.
3. **The domain value.** Reuse a domain value the Handler already takes, if there is one. Otherwise create
   `<Value>` and `Parsed<Value>` in `core`'s `feature.<x>`, by copying `templates/core/Example.java` and
   `ParsedExample.java`. Move the DTO's constraints onto the record's components. Add the text constructor and
   `parse(String, …)` only if a component arrives as text that has to become a number or another type.
4. **The Handler** takes the domain value instead of primitives or the DTO. Its other callers (tests, other
   Receivers) are trusted, or they go through `parse()` too.
5. **The Receiver.**
   - The DTO stays the wire format. Remove its constraint annotations and the `@Valid` on the parameter.
   - Log `<OP>_RECEIVED`.
   - Switch on `parse()`. The invalid branch follows the boundary's reference in `boundaries/`.
   - If an exception mapper for `ConstraintViolationException` now has no other user, tell the user. Don't
     remove it unasked.
6. **Tests.**
   - A unit test of `parse` in `core`, modelled on `templates/core/ExampleTest.java`: a valid value, each
     violation, and non-numeric text if there is a text overload.
   - The existing endpoint tests must stay green. If they asserted the old 400 body, update them to the new
     shape, and tell the user that the response body changed for clients.
   - Add a test for each violation the old tests did not cover.

Done when `./mvnw verify` is green. Report the stage, then offer the next candidate.

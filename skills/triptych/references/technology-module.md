# Add a Technology module

A Technology module holds the adapters for one direction and one technology: `inbound-<tech>` or `outbound-<tech>`. If the project already has a module for that direction and technology, add the adapter there instead.

The module references below give the extensions, a minimal adapter and the config for each known technology:

| Module | Reference |
|---|---|
| `inbound-http-jsonapi` | [modules/inbound-http-jsonapi.md](modules/inbound-http-jsonapi.md) |
| `inbound-http-html` | [modules/inbound-http-html.md](modules/inbound-http-html.md) |
| `inbound-kafka` | [modules/inbound-kafka.md](modules/inbound-kafka.md) |
| `inbound-event` | [modules/inbound-event.md](modules/inbound-event.md) (CDI events, scheduler) |
| `outbound-postgres` | [modules/outbound-postgres.md](modules/outbound-postgres.md) |
| `outbound-mongodb` | [modules/outbound-mongodb.md](modules/outbound-mongodb.md) |
| `outbound-webservice` | [modules/outbound-webservice.md](modules/outbound-webservice.md) (SOAP) |
| `outbound-httpclient` | [modules/outbound-httpclient.md](modules/outbound-httpclient.md) |
| `outbound-kafka` | [modules/outbound-kafka.md](modules/outbound-kafka.md) |

`inbound-http-json-vm` belongs to the htmx skill, which creates it. For any other technology, follow the same steps and choose the Quarkus extension yourself.

## Steps

1. **Confirm** (skip `from: setup-guide`). Show `<module> in <dir>: extensions <…>, example <Name>Receiver|Service in feature.<x>` and wait for a yes. Pick the feature it serves with the user. On a fresh project that is `feature.example`.
2. **Module pom.** Create `<module>/pom.xml` like `core/pom.xml`: the same parent, `<artifactId><module></artifactId>`, a dependency on `core` (`${project.groupId}`, version from dependency management), and the extensions from the module reference.
3. **Wire it.** Add these entries in the parent pom, each placed next to its siblings:
   - `<module><module></module>` before `app-server`
   - a dependency-management entry with `${project.groupId}` / `${project.version}`
   - any version property the reference names
   
   Then add the module as a dependency of `app-server`.
4. **Adapter.** Write the reference's minimal adapter under `<root>.feature.<x>` (or `cross.<y>`):
   - inbound: a `*Receiver` that injects the feature's Handler.
   - outbound: first the `*SPI` in `core`, then a `*Service` implementing it, then a Handler method that uses the SPI.
5. **Config.** Add the reference's properties to `app-server/src/main/resources/application.properties`. Where Dev Services provides the backing service, leave its URL unset and say so in a comment.
6. **Test.** Add a test in `app-server` that exercises the adapter the way the reference describes. Technologies backed by Dev Services need Docker; say so.
7. **Verify.** Run `./mvnw verify`. Done when it is green, including `ArchitectureTest`.
8. **Report** the module, the wiring, the adapter, the config keys, and any Docker requirement.

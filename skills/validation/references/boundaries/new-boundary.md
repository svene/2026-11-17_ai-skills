# Boundary: a new kind

For an inbound technology without a reference here (gRPC, WebSocket, a scheduler reading files, SQS, …).

1. **Is the input untrusted?** If it comes from outside the process, yes. If the application produced it, follow
   [in-process-events.md](in-process-events.md) instead.
2. **Keep the wire type.** The technology's own message or DTO type stays in the `inbound-<tech>` module and has
   no constraints. Text stays text where users type it, and goes through `parse(String, …)`.
3. **Separate "unprocessable" from "invalid".** Unprocessable means the Receiver cannot read the values at all
   (no payload, a broken structure). Invalid means it can read them, but `parse()` returns `Invalid`. They often
   need different answers.
4. **Pick the invalid branch by asking: is someone waiting for the answer?**
   - Yes (request/response, like HTTP or gRPC): answer with the technology's "bad request" status, naming each
     field and its message.
   - No (fire-and-forget, like Kafka): log `<OP>_INVALID` at WARN and skip, or dead-letter it if it must not be
     lost. Never retry it.
5. **Logging**: `<OP>_RECEIVED` before `parse()`, `<OP>_INVALID` at WARN in the invalid branch, and
   `<OP>_PROCESSING` in the Handler.
6. **Test it** like the closest existing boundary: one valid case, one per kind of violation, and one
   unprocessable case if the technology has them.

Once the boundary is done and green, tell the user that a reference for it could be added to this skill
(`references/boundaries/<tech>.md`).

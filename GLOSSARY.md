# Glossary

**Technique**: A reusable way of building software (an architecture, a validation approach, a frontend approach) that a project may adopt. Independent of any business domain.

**Technique skill**: A skill that teaches one Technique and applies it to an existing project; it never creates a project. A freshly created project is just the simplest existing one: the skill recognises it and also adds a small example. Every Technique skill other than the Triptych architecture one requires the project to use the Triptych architecture already; beyond that, Technique skills are independent of each other.
_Avoid_: scaffold (as a separate mode)

**Bare project skill**: A skill that creates a new Quarkus project with no Techniques applied. Only the Setup guide hands off to it; it knows nothing about Techniques.
_Avoid_: setup skill (confusable with Setup guide)

**Setup guide**: A guiding skill that walks the user through starting a new project: it hands off to the Bare project skill, always applies the Triptych architecture, then asks which technologies and further Techniques to adopt and hands off to the matching skills. Declining one there never prevents adding it later.

**Source repo**: An example repository from which a Technique is extracted. Its business domain (e.g. the supermarket) is illustrative only and never part of a skill.

**Provenance note**: The record, kept with a skill, of which Source repo(s) and commit(s) it was derived from.

**Triptych architecture**: The author's variant of hexagonal architecture. Defined in detail by its Technique skill.

**Technology module**: A module of a Triptych architecture project that holds the adapters for one direction (inbound or outbound) and one technology, e.g. Kafka consumers or a Postgres store. A module is added when a technology is added, never when a feature is added.

**Technique Conformance Check**: Applying a Technique skill to a project that already uses its Technique, to find where the project deviates from it.
_Avoid_: audit (reserved for audit logging)

**Skill test**: A test, kept in this repo next to the skills, that checks a Technique skill works the way a unit test checks code: the skill is run against a throwaway project and the resulting project is verified by its own tests (e.g. browser tests). There are two kinds per Technique skill: applying the skill to a freshly created throwaway project, and applying it to a Fixture project standing in for an existing one.
_Avoid_: eval (reserved for whatever tooling ends up running skill tests)

**Fixture project**: The throwaway project an apply Skill test starts from, standing in for an existing project (e.g. a plain Quarkus project, or one already using the Triptych architecture). Generated fresh for each run, never kept.

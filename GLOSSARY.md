# Glossary

**Technique**: A reusable way of building software (an architecture, a validation approach, a frontend approach) that a project may adopt. Independent of any business domain.

**Technique skill**: A skill that teaches one Technique and can both scaffold it into a new project and apply it to an existing one. It scaffolds by handing off to the Bare project skill, then applying the Technique. Usable on its own; never requires another Technique skill.

**Bare project skill**: A skill that creates a new Quarkus project with no Techniques applied. Technique skills (when scaffolding) and the Setup guide hand off to it; it knows nothing about Techniques.
_Avoid_: setup skill (confusable with Setup guide)

**Setup guide**: A guiding skill that walks the user through starting a new project, asking which Techniques to adopt and handing off to the matching Technique skills. Declining a Technique there never prevents applying it later.

**Source repo**: An example repository from which a Technique is extracted. Its business domain (e.g. the supermarket) is illustrative only and never part of a skill.

**Provenance note**: The record, kept with a skill, of which Source repo(s) and commit(s) it was derived from.

**Triptych architecture**: The author's variant of hexagonal architecture. Defined in detail by its Technique skill.

**Skill test**: A test, kept in this repo next to the skills, that checks a Technique skill works the way a unit test checks code: the skill is run against a throwaway project and the resulting project is verified by its own tests (e.g. browser tests). There are two kinds per Technique skill: scaffolding a new throwaway project, and applying the skill to an existing throwaway project.
_Avoid_: eval (reserved for whatever tooling ends up running skill tests)

**Fixture project**: The throwaway project an apply Skill test starts from, standing in for an existing project (e.g. a plain Quarkus project, or one already using the Triptych architecture). Generated fresh for each run, never kept.

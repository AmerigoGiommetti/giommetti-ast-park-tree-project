# Park Tree Project

[![Java CI with Maven, Docker, SonarCloud and Coveralls](https://github.com/AmerigoGiommetti/giommetti-ast-park-tree-project/actions/workflows/maven.yml/badge.svg)](https://github.com/AmerigoGiommetti/giommetti-ast-park-tree-project/actions/workflows/maven.yml)
[![Coverage Status](https://coveralls.io/repos/github/AmerigoGiommetti/giommetti-ast-park-tree-project/badge.svg)](https://coveralls.io/github/AmerigoGiommetti/giommetti-ast-park-tree-project)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AmerigoGiommetti_giommetti-ast-park-tree-project&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AmerigoGiommetti_giommetti-ast-park-tree-project)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AmerigoGiommetti_giommetti-ast-park-tree-project&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AmerigoGiommetti_giommetti-ast-park-tree-project)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=AmerigoGiommetti_giommetti-ast-park-tree-project&metric=bugs)](https://sonarcloud.io/summary/new_code?id=AmerigoGiommetti_giommetti-ast-park-tree-project)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=AmerigoGiommetti_giommetti-ast-park-tree-project&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=AmerigoGiommetti_giommetti-ast-park-tree-project)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=AmerigoGiommetti_giommetti-ast-park-tree-project&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=AmerigoGiommetti_giommetti-ast-park-tree-project)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=AmerigoGiommetti_giommetti-ast-park-tree-project&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=AmerigoGiommetti_giommetti-ast-park-tree-project)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=AmerigoGiommetti_giommetti-ast-park-tree-project&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=AmerigoGiommetti_giommetti-ast-park-tree-project)

A desktop application to manage parks, trees, and the associations between them (which trees grow in which park, and in what percentage), built as the exam project for the *Automated software testing* course, following the practices described in

## Overview

The application lets a user:

- add and remove **trees** (id, name, lifespan, evergreen flag);
- add and remove **parks** (id, name, region, area, free-access flag);
- associate one or more trees to a park with a percentage of coverage (the percentages of a park's associations must add up to exactly 100%);
- browse the current list of trees and parks, with the tree composition of each park.

It is built test-first (TDD) following the Model-View-Presenter pattern: a Swing `View`, a `Controller` acting as the presenter, and a `Repository` (`Park`, `Tree`, `ParkTreeAssociation`), each available in two interchangeable implementations — JPA/MySQL and MongoDB — selected at startup.

## Tech stack

- **UI**: Java Swing
- **Dependency Injection**: Google Guice (constructor injection, `AssistedInject` for the View→Controller factory, an AOP `MethodInterceptor` wrapping JPA repository calls in a transaction)
- **CLI bootstrap**: Picocli, choosing the persistence technology (JPA/MySQL or MongoDB) from command-line arguments
- **Persistence**: JPA/Hibernate over MySQL, or the MongoDB Java driver, behind a common `Repository` interface
- **Build**: Maven

## Testing

The test suite follows the testing pyramid described in the book:

| Level | Location | What it exercises |

| Unit tests | `src/test/java` | Controller and both Repository implementations in isolation (mocked collaborators); the Swing View, with AssertJ Swing |

| Integration tests (`*IT`) | `src/it/java` | Controller + real Repository, against a real MySQL/MongoDB instance started on the fly with Testcontainers, with the third component (the View) mocked |

| End-to-end tests (`*E2E`) | `src/e2e/java` | The real, fully wired application (View + Controller + Repository, nothing mocked), launched and driven through its actual Swing GUI |

Additional quality gates:

- **Code coverage**: JaCoCo, enforced with a minimum threshold and reported to both Coveralls and SonarCloud
- **Mutation testing**: PIT, targeting the Controller and Repository classes, run through the `mutation-testing` Maven profile
- **Static analysis**: SonarCloud, with justified exclusions documented directly in the `pom.xml`

## Building and running

Run the full build (unit + integration + end-to-end tests, coverage report):

```bash
mvn verify
```

Only the fast unit tests:

```bash
mvn test
```

Mutation testing:

```bash
mvn verify -Pmutation-testing
```

Launch the application (defaults to JPA/MySQL on `localhost`; pass `--mongo` to use MongoDB instead — see `--help` for every option):

```bash
mvn compile exec:java
```

## Continuous Integration

Every push and pull request is built on GitHub Actions: it runs the whole test suite (unit, integration and end-to-end, the latter two against Docker containers started automatically by Testcontainers) under Xvfb, then publishes coverage to Coveralls and a full analysis to SonarCloud.

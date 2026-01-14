# Monster-Competition
A turn-based monster battle engine implemented in Java.

This is a **turn-based monster battle engine** implemented in Java.  
Players interact via the command line, using monsters and actions defined in an external configuration file to run “competitions”. Each monster has stats such as HP and speed. Actions can deal damage, heal, apply status conditions (e.g. poison, slow), or modify stats. The project includes a complete **battle flow controller, random number system, and command handling layer**, so new monsters and actions can be added by editing configuration files rather than changing core logic.

## Features
- Config-driven monsters, actions, and effects (damage, heal, status, stat changes, protect, repeat).
- CLI commands for loading configs, creating competitions, performing actions, showing state, passing, quitting.
- Turn order by speed; deterministic runs via seed or `debug` mode.
- Clear layering: `model` (monsters/competition/effects), `command` (input handling), `util` (parsers, RNG, round handler).

## Project Layout
- `src/` – Java sources (entry: `edu.kit.kastel.monstercompetition.Main`).
- `input/` – sample config (e.g., `study.txt`).
- `pom.xml` – Maven build config.
- (Not tracked) `target/`, `bin/`, IDE files (`*.iml`).

## Build
```bash
mvn clean package
```

## Run
Use your built jar/classpath; supply config plus optional seed or `debug`:
```bash
java -cp target/<jar>.jar edu.kit.kastel.monstercompetition.Main input/study.txt
java -cp target/<jar>.jar edu.kit.kastel.monstercompetition.Main input/study.txt 42
java -cp target/<jar>.jar edu.kit.kastel.monstercompetition.Main input/study.txt debug
```

Pyproject Tools Linter/Formatter Connector
----

Python external linter/formatter connector plugin for PyCharm and IntelliJ IDEA,
This plugin runs according to the `pyrproject.toml` file.

## Description

This plugin is python external linter and formatter connector.

- Connect external linters' output to IDE Inspections.
- Replace python source code to external formatters' output on save.
- You need to write linter/formatter configuration in `pyrproject.toml`

Support for linters:

- pyproject-flake8
- mypy
- Pylint
- ruff

Support for formatters:

- black
- isort
- ruff

Limitations:

- run on Unix-based systems only. (not support windows)
- run on local interpreter only. (not support remote interpreter)

## Installation

1. Install a compatible JetBrains IDE, such as IntelliJ IDEA or PyCharm.
2. Launch the IDE and open plugin settings.
3. Search for `Pyproject Tools Linter/Formatter Connector` and click install

## Settings

1. Open setting, follow path:  
   Settings -> Editor -> Inspections -> ExternalLinters -> Python
2. Check or remove the following options.
   - pyproject-flake8
   - mypy
   - Pylint
   - ruff

## Build

Build plugin distribution.

```sh
./gradlew buildPlugin
```

Install plugin.

1. Open setting, follow path:  
   Settings -> Plugins
2. Select 'Install Plugin from Disk'
4. Choice 'build/distributions/pyprojecttools_linterconnector-VERSION.zip'

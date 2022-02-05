intellij-plugin-external-linters
----

External Linter To IDE Inspection Connector Plugin for IntelliJ Platform-based IDEs.

Support Tools:

- flake8
- mypy

Limitations:

- run on unix-bases systems only. (not support windows)
- run on local interpreter only. (not support remote interpreter)

## Install

Build plugin distribution.

```sh
./gradlew bludPlugin
```

Install plugin.

- Start IntelliJ IDEA
- Open Plugins Preferences
- Select 'Install Plugin from Disk'
- Choice 'build/distributions/intellij-plubin-external-linters-VERSION.zip'

Enable/Disable inspection.

- Open Editor/Inspections Preferences
- Check or uncheck tools' check box under ExternalLinters

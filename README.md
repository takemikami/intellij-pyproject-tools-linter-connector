intellij-plugin-external-linters
----

External Linter To IDE Inspection Connector Plugin for IntelliJ Platform-based IDEs.

Support Tools:

- flake8
- pylint
- mypy
- tflint
- SQLFluff

Limitations:

- run on Unix-based systems only. (not support windows)
- run on local interpreter only. (not support remote interpreter)

## Install

Build plugin distribution.

```sh
./gradlew buildPlugin
```

Install plugin.

- Start IntelliJ IDEA
- Open Plugins Preferences
- Select 'Install Plugin from Disk'
- Choice 'build/distributions/intellij-plubin-external-linters-VERSION.zip'

Enable/Disable inspection.

- Open Editor/Inspections Preferences
- Check or uncheck tools' check box under ExternalLinters

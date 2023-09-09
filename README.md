# Spoofax Analysis Benchmark

This tool allows perform a runtime performance benchmark of the full static analysis stage resulting from a Spoofax language definition. Part of the [supplementary material of my master thesis](https://github.com/jessetilro/thesis).

Run analysis on example program
```shell
mvn clean verify exec:java@run -Dargs="PATH_TO_LANGUAGE_PROJECT PATH_TO_EXAMPLE_PROGRAM"
```

Benchmark analysis on example program
```shell
mvn clean verify exec:exec@benchmark -Dbenchmark.args="-p languagePath=PATH_TO_LANGUAGE_PROJECT -p programPath=PATH_TO_EXAMPLE_PROGRAM"
```

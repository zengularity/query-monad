# Query Monad

A Query Monad implementation for Anorm.

(Initiated for the article **Functional database programming with Play Anorm through the `Reader Monad`**)

## Build

The project is using [SBT](http://www.scala-sbt.org/), so to build it from sources the following command can be used.

```shell
sbt +publishLocal
```

[![Build Status](https://travis-ci.org/zengularity/query-monad.svg?branch=master)](https://travis-ci.org/zengularity/query-monad)
[![Zen Entrepot](http://zen-entrepot.nestincloud.io/entrepot/shields/snapshots/com/zengularity/query-monad-core_2.12.svg)](https://zen-entrepot.nestincloud.io/entrepot/pom/snapshots/com/zengularity/query-monad-core_2.12)
[![Zen Entrepot](http://zen-entrepot.nestincloud.io/entrepot/shields/releases/com/zengularity/query-monad-core_2.12.svg)](https://zen-entrepot.nestincloud.io/entrepot/pom/releases/com/zengularity/query-monad-core_2.12)

## Contributing

Please take a look at the [Contribution guide](.github/CONTRIBUTING.md)

## Publishing

To publish a snapshot or a release on [Zengularity Entrepot](https://github.com/zengularity/entrepot):

- set the environment variable `REPO_PATH`;
- run the SBT command `+publish`.

For example:
```shell
export REPO_PATH=/path/to/entrepot/snapshots/
sbt +publish
```

Then in Entrepot, the changes must be commited and pushed.

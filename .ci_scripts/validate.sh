#! /bin/bash

set -e

echo "[INFO] Check the source format"

sbt ++$TRAVIS_SCALA_VERSION scalafmt test:scalafmt > /dev/null
git diff --exit-code || (cat >> /dev/stdout <<EOF
[ERROR] Scalafmt check failed, see differences above.
To fix, format your sources using sbt scalafmt test:scalafmt before submitting a pull request.
Additionally, please squash your commits (eg, use git commit --amend) if you're going to update this pull request.
EOF
false
)

echo "[INFO] Running tests" >> /dev/stdout

sbt ++$TRAVIS_SCALA_VERSION test

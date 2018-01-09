
This repo follows a standard fork and pull model for contributions via GitHub pull requests. Thus, the contributing process looks as follows:

0. [Pick an issue](#pick-an-issue)
1. [Write code](#write-code)
2. [Write tests](#write-tests)
3. [Write docs](#write-docs)
4. [Submit a PR](#submit-a-pr)

# Pick an issue

* Set yourself as assignee
* On Github, leave a comment on the issue you picked to notify others that the issues is taken

# Write Code

We use [WartRemover](http://www.wartremover.org/doc/warts.html) for linting and [Scalafmt](http://scalameta.org/scalafmt/).
TODO: add style guide

# Write Tests

TODO: Add tests + define test conventions

# Submit a PR

- PR should be submitted from a separate branch (use `git checkout -b task/adding-awesome-feature`)
- Use the following nomenclature for branch name:
  - for a fix: `fix/quick-description`
  - for a task: `task/quick-description`
- PR's commit message should use present tense
- PR worflow with github labels:
  - `WIP`: more work need to be done on the PR (can be used to collaborate)
  - `requires-review`: to ask for review
  - `requires-changes`: some changes need to be done on the PR
  - `merge-ready`: PR can be merged (at least one approve review + CI OK + no conflicts + up to date with master)
- PR should generally contain only one commit (use git commit --amend and git --force push or squash existing commits into one)

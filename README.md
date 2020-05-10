# CLI Journal

This is a CLI REPL that allows you to treat a directory of files with a particular naming scheme as timestamped entries in a digital journal. For example, a journal directory containing the following file:

```
my-test-entry~2020-03-22_18-56-00~tag1,tag2.md
```

will show up in the CLI as:

```
2020-03-22 18:56:00       my-test-entry.md     tag1 tag2
```

As of 2020-05-10, the tool provides functionality to write new entries with tags, open existing entries in Vim, search entries by name or tag, and list tags & entries.

## Installation
TODO

## Usage
Start the REPL by running `cli-journal`. NOTE: for convenience, you can pass in any REPL arguments from the CLI - e.g. `cli-journal ls`

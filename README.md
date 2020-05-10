# CLI Journal

This is a CLI REPL that allows you to treat a directory of files with a particular naming scheme as timestamped entries in a digital journal, complete with tags. For example, a journal directory containing the following file:

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

## Basic Usage
Start the REPL by running `cli-journal`, then type `help` to show the available commands. All commands can take a `--help` flag to display their options.

_NOTE: for convenience, you can pass in any REPL arguments from the CLI - e.g. `cli-journal ls`_

Some example usages below:

### Show all entries, reverse-sorted by name 
```
>> ls -rn
0     2020-03-06 09:00:00    entry2.md    tag2
1     2020-03-05 12:00:00    entry1.md    tag1
```

### Add an entry
`new entry3.md tag1 tag2` (takes you to Vim for editing)

### Find entries by tag
```
>> find -t tag1
0     2020-03-05 12:00:00    entry1.md    tag1
```

## Referencing previous results
To make the journal easier to work with, the output of listing commands are numbered and can be referenced in later commands by prefixing `@` to a comma-separated list. For example:

```
>> ls
0     2020-03-05 12:00:00    entry1.md    tag1
1     2020-03-06 09:00:00    entry2.md    tag2

>> vim @0,1
```

will open `entry1.md` and `entry2.md` simultaneously, in Vim vertical splits. This also works for tags, e.g.:

```
>> tags
0     tag1
1     tag2

>> new new-entry.md @0
```

## Future Development
* [ ] Easy user onboarding:
    * [ ] Nice onboarding flow to set up `.clijournal` when not already done
    * [ ] Homebrew installation
* [ ] Split references into `@entry` and `#tag`, so entry-listing commands don't invalidate tag references & vice versa
* [ ] Editing:
    * [ ] Renaming entries
    * [ ] Adding & removing tags
    * [ ] Changing timestamps????
* [ ] Selection buffer, for easier tagging & editing
* [ ] Prefs file:
    * [ ] Recolor tags
    * [ ] Default sort types????

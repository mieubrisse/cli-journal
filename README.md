# CLI Journal

This is a CLI REPL that allows you to treat a directory of files with a particular naming scheme as timestamped entries in a digital journal, complete with tags. For example, a journal directory containing the following file:

```
my-test-entry~2020-03-22_18-56-00~tag1,tag2.md
```

will show up in the CLI as:

```
2020-03-22 18:56:00       my-test-entry.md     tag1 tag2
```

## Installation
1. Run `gradle build` from the root
1. A `.jar` file will be produced in `build/libs`; run this with `java -jar *.jar`


## Basic Usage
Start the REPL by running `cli-journal`, then type `help` to show the available commands:

```
>> help

  find           Finds entries matching the given parameters
  help           Prints help data for all commands
  ls             Lists all entries in the journal
  new            Adds a new journal entry with the given parameters
  quit           Quits the CLI
  tags           Lists the tags currently in use in the journal
  vim            Opens the referenced result in vim, using vertical splits if more than one result
```

All commands can take a `--help` flag to display their options.

_NOTE: for convenience, you can pass in any REPL arguments from the command line - e.g. `cli-journal ls`_

Some example usages below:

### Get help
```
>> ls --help
usage: ls [-h] [-r] [-n]

Lists all entries in the journal

named arguments:
  -h, --help             show this help message and exit
  -r                     Reverse sort direction (default: false)
  -n                     Sort the results by name (default: TIME)
```

### Show all entries, reverse-sorted by name 
```
>> ls -rn
0     2020-03-06 09:00:00    entry2.md    tag2
1     2020-03-05 12:00:00    entry1.md    tag1
```


### Add an entry
`>> new entry3.md tag1 tag2` (takes you to Vim for editing)

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
* [ ] Allow use of several journals at once (e.g. work vs personal)????
* [ ] Easy user onboarding:
    * [x] Nice onboarding flow to set up `.clijournal` when not already done
    * [ ] Homebrew installation
* [ ] Split references into `@entry` and `#tag`, so entry-listing commands don't invalidate tag references & vice versa
* [ ] Editing:
    * [ ] Renaming entries
    * [ ] Adding & removing tags
    * [ ] Changing timestamps????
* [ ] Selection buffer, for easier tagging & editing
* [ ] Add ability to import external files, adding creation timestamp & tags
* [ ] Prefs file:
    * [ ] Recolor tags
    * [ ] Default sort types????
* [ ] Caching metadata-parsing, so startup is super fast

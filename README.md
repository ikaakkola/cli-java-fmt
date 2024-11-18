# cli-java-fmt

A command-line java formatter using google-java-format

# build requirements

* Oracle GraalVM 23.0.1+11.1 or newer
* maven

# building

    mvn clean compile -Pnative package
    
This produces `target/javafmt` which is a standalone native executable. Copy it to somewhere in $PATH to use globally.


# usage

```
Usage: javafmt [-hrsV] files...
Format Java sources with 'google-java-fmt'
      files...      one or more files or directories to format
  -h, --help        Show this help message and exit.
  -r, --recursive   Recurse into directories (default: true)
  -s, --split       Split long strings into multiple lines (default: true)
  -V, --version     Print version information and exit.
```

Format all .java files under /code/my-project:

    javafmt -r -s /code/my-project
    
Format from stdin and write resulting formatted sources to stdout:

    javafmt - < /path/to/file.java

## use as a git pre-commit hook

The formatter utility can be used to format all changed files of a git commit. Create a 
pre-commit file (eg. `.git/hooks/pre-commit`) with the following contents:

```
#!/bin/sh

# find all changed java files
CHANGED_FILES=$(git diff --cached --name-only --diff-filter=ACM -- '*.java')

if [ -n "$CHANGED_FILES" ]; then
    javafmt -v $CHANGED_FILES;
    git add $CHANGED_FILES;
fi
```

Make the hook executable (eg. `chmod +x .git/hooks/pre-commit`) and it files in a commit will be 
automatically formatted and staged.

Note that having a pre-commit hook (re)stage the changed files is not best practice as it breaks
partially changed files (`git add -p`). Consider using something like editor save actions instead.


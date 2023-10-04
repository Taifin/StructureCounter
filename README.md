# Structure Counter IDEA Plugin

This is a simple IDEA plugin that adds new tool panel to the interface called "Structure Counter"
that shows information about Java files, classes and methods
in the current project. For every module, package and source file it provides amount of classes and
functions inside. For classes themselves, all inner methods are shown.

The plugin supports real-time updates of the file structure: if you move, copy, create or delete a
file/directory/class/method, the changes will be reflected in the interface. However, if something
is missing or lost, you can click the "Refresh" button and force update of the structure tree from scratch.

## Implementation details

Current implementation uses inefficient and bug-prone algorithm: PSI elements, such as files, classes and functions, are
identified only by their name. This fails when there are many elements with the same name in the project. A possible fix
is quite simple: use paths instead of names to identify elements.

Nodes are located using plain DFS algorithm, which may be not the most efficient when it comes to larger projects. It
may be more time-effective to create a custom `Node`-like class to store its children not in a list, but in a hashtable.

The project was initially written in Java, but in the middle of the development I decided to switch to Kotlin. Thus,
some parts of the source code and the overall structure are not very Kotlin-ish.

## Running

To run the plugin inside a test IDE instance, execute

```bash
./gradlew runIde
```

Inside a newly created IDE instance, choose any project that uses Java as its main language. When the project is opened,
you will see a new tab on the left side of the window called "Structure Counter". 
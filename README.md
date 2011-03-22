Green UML Plugin for Eclipse
============================
cloned by fmjrey
----------------

This project is a clone of the original project at [http://green.sourceforge.net/](http://green.sourceforge.net/).
Its purpose is not to fork development of the original project but simply to make it easier to share some changes that hopefully will be merged back into the original.

*REBASE WARNING*
The only branches that will not be rebased are the main branches listed below. If you are cloning this repository you are strongly advised to create your own branches based on these. Other branches like topic branches may be rebased over time in order to keep track with upstream changes.

Main Branches
-------------

* [master](/fmjrey/green-uml/tree/master) -- master branch that tracks the original cvs repository trunk
* [fmjrey](/fmjrey/green-uml/tree/fmjrey) -- own master branch, which merges the other branches below and may contain other commits

Topic Branches
--------------

* [fix-create-diagram](/fmjrey/green-uml/tree/fix-create-diagram) -- fix bug when creating new diagram from package
* [version-qualifier](/fmjrey/green-uml/tree/version-qualifier) -- added version qualifier on each plugin and feature version (e.g. 3.5.0.qualifier)
* [ccvisu-3.0](/fmjrey/green-uml/tree/ccvisu-3.0) -- upgrade ccvisu used internally from v2.1 to 3.0


Structure
---------
Each directory is an eclipse plugin project that correspond to a module imported from the [original cvs repository on sourceforge](https://sourceforge.net/projects/green/develop) (see cvsimport.sh).
The import was done by running cvsimport.sh. It created a git repository for each module (git cvs-import cannot import all modules at once in one git project).
In other words each subdirectory had its own .git folder created during the import.
A few changes have been made in separate branches in each subproject.
However working with one repository per plugin/feature proved to be tedious.
Therefore this repository which contains all subprojects was created subsequently.
In this repository each subproject is simple directory, not a git submodule.
The changes that were committed in each subproject have been replicated in this repository.
The .git folder of each subproject has been compressed and checked into the branch fmjrey as a regular file for historical purposes.

Contributing
------------
If you want to provide a contribution it's best to do so by forking this project and make your changes inside your own new branch in your fork.
Having each change/feature in its own unique branch makes it easier for the original project to pull each branch independently, thus allowing cherry-picking branches before the need for cherry-picking commits.
Such branches are usually called "topic branches", as described [here](https://github.com/dchelimsky/rspec/wiki/Topic-Branches) and [here](http://stackoverflow.com/questions/284514/what-is-a-git-topic-branch).
You may also want to create your own "master" branch in which you can merge all your branches and other commits from elsewhere you may need for yourself.
To signal your changes you can create issues or pull requests in the original project and link to the corresponding branch in your fork.
When the original projects commits new changes, you need to bring these changes into your fork by rebasing your topic branches. Other branches should be merging with the changes upstream.
Once the topic branch has been merged into the original project, you probably want to delete it.

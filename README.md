bakka
=====

akka + bams = bakka

Playing with combining akka with parsing bam files, implementing a map/reduce functionality.

Quick run instructions
----------------------
Basic setup of bakka requires two steps:

* Setup Scala development environment

The easiest way to do this is to go [here](http://typesafe.com/resources/typesafe-stack/downloading-installing.html) and follow the instructions for installing the Typesafe stack.

* Install sbt

Right now the project is build (and run) using the sbt (Simple Build Tool), you'll find the setup instructions here: http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html

When the above dependencies have been fullfilled, checkout bakka from github, and go into the bakka folder. Type `sbt` to initiate sbt in interactive mode. Some basic sbt commands to be aware of are:

`eclipse`

Sets up a eclipse project with correct class path, etc, from the project. You should then be able to import the project straight in to eclipse without any problems. ("Should" is a important word in this context)

`compile`

Compiles the project

`run`

Runs the bakka App.

`one-jar`

Produces jar file, complete with all dependencies.


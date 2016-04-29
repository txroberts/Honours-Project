# Honours-Project
This is my Honours Project for the 4th year of my undergraduate degree.

## Description
Solving problems often involves making choices. **Constraint Programming** is a branch of **Artificial Intelligence** that aims to solve problems that involve choices. **Constraint solvers** are pieces of software that can find solutions to these kinds of problems. The series of choices that a constraint solver makes can be presented in a tree structure known as a **search tree**.

The surge of Web 2.0 and the NoSQL database movement has given rise to a multitude of new database technologies that Constraint Programming could use to analyse the data it generates. **Graph databases** are of particular interest to Constraint Programming as they provide a way to store and query graph-like structures.

This project is **original research** that investigates whether graph databases can be used to explore constraint problem search trees. This would give insights into the solving techniques used in Constraint Programming.

[Savile Row](http://savilerow.cs.st-andrews.ac.uk/), a constraint modelling assistant, was used to model a real world problem. The constraint solver [Minion](http://constraintmodelling.org/minion/) was used to solve the problem using different solving techniques. The search trees produced were then loaded into [Neo4j](http://neo4j.com/) to examine their shape and structure, and to discover interesting patterns in the graphs.

## Contents
### 1. Parser
A Python parser that parses a Minion search tree into the format required to load it into a Neo4j database
### 2. Auto_Measure
A Java program that runs the set of Cypher queries against a directory of databases
### 3. Evaluation
  1. The Essence' models, three instances of BIBD problem (Minion parameter files), and the Minion file for each BIBD problem
  2. The data generated from the evaluation
### 4. Batch files
Automation scripts to speed up the parsing and loading process
# Gitlet Design Document
author: Michael Wu

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

### Main

#### Instance Variables


### Commit

#### Instance Variables
* Message - contains the message of the commit.
* Timestamp - time at which the commit was created. Assigned by the constructor
* Parent - the SHA-1 code of a commit object.
* Tree - an ArrayList containing the SHA-1 codes of all blobs tracked by a commit and the name of each blob

### Tree
* Directory structures mapping names to references to blobs and other trees (subdirectories)
* Each commit object then stores a tree ID. Git uses that to find the tree object. The tree object contains (along with other stuff) a name, like 1.txt paired with a blob hash ID. Git uses the blob hash ID to find the blob object, and the blob object stores the complete contents of the file.
### Repository

#### Instance Variables

## 2. Algorithms

### Main

#### Class Methods
* Main - 

### Commit

#### Class Methods

#### Instance Methods
Commit(String message, String parent)
* Creates a commit object

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

## 3. Persistence

### init 
* Calls Repository constructor, which creates a new ./gitlet directory and all necessary subdirectories
* Calls Commit constructor to create initial commit, then serialize commit in /objects
* Creates branch master (file containing SHA-1 hashcode of initial commit in /index)
* Creates branch HEAD (file containing SHA-1 hashcode of initial commit in /index)
* Before creating the directory, checks if a ./gitlet already exists in the CWD - if so error and exit
### add [file name]
* 
### commit [message]

### rm [file name]

### log

### global-log

### find [commit message]

### status

### checkout -- [file name]

### checkout [commit id] -- [file name]

### checkout [branch name]

### branch [branch name]

### rm-branch [branch name]

### reset [commit id]

### merge [branch name]


Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.


package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Michael Wu
 * credit Sean Dooher for exitWithError and validateNumArgs from capers lab
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main add hello.txt
     *  */

    public static void main(String... args) {
        File gitlet = new File(".gitlet");
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        } else if (args[0].equals("init")) {
            if (!gitlet.exists()) {
                new Repository();
            } else {
                exitWithError("A Gitlet version-control system "
                        +
                        "already exists in the current directory.");
            }
        } else if (args[0].equals("add")) {
            initializedRep();
            File fileToAdd = new File(args[1]);
            if (fileToAdd.exists()) {
                Repository.add(fileToAdd);
            } else {
                exitWithError("File does not exist.");
            }
        } else if (args[0].equals("commit")) {
            initializedRep();
            if (args.length < 2) {
                exitWithError("Please enter a commit message.");
            } else {
                Repository.commit(args[1]);
            }
        } else if (args[0].equals("rm")) {
            initializedRep();
            Repository.rm(args[1]);
        } else if (args[0].equals("log")) {
            initializedRep();
            Repository.log();
        } else if (args[0].equals("global-log")) {
            initializedRep();
            Repository.globallog();
        } else if (args[0].equals("find")) {
            initializedRep();
            Repository.find(args[1]);
        } else if (args[0].equals("status")) {
            initializedRep();
            Repository.status();
        } else {
            main2(args);
        }
    }

    public static void main2(String... args) {
        if (args[0].equals("checkout")) {
            initializedRep();
            if (args.length == 2) {
                Repository.checkoutBranch(args[1]);
            } else if (args.length == 3) {
                if (!args[1].equals("--")) {
                    exitWithError("Incorrect operands.");
                }
                Repository.checkoutFile(args[2]);
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    exitWithError("Incorrect operands.");
                }
                Repository.checkoutCommitFile(args[1], args[3]);
            } else {
                exitWithError("Improper number of arguments for checkout.");
            }
        } else if (args[0].equals("branch")) {
            initializedRep();
            Repository.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            initializedRep();
            Repository.rmbranch(args[1]);
        } else if (args[0].equals("reset")) {
            initializedRep();
            Repository.reset(args[1]);
        } else if (args[0].equals("merge")) {
            initializedRep();
            Repository.merge(args[1]);
        } else {
            exitWithError("No command with that name exists.");
        }
    }

    public static void initializedRep() {
        File gitlet = new File(".gitlet");
        if (!gitlet.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
    }
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(0);
    }
}

package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Repository {

    /** cWD Folder. */
    private static File cWD = new File(".");
    /** cWD Folder. */
    private static File gitlet = new File(".gitlet");
    /** cWD Folder. */
    private static File objects = new File(".gitlet/objects");
    /** cWD Folder. */
    private static File commits = new File(".gitlet/objects/commits");
    /** cWD Folder. */
    private static File blobs = new File(".gitlet/objects/blobs");
    /** cWD Folder. */
    private static File index = new File(".gitlet/index");
    /** cWD Folder. */
    private static File staging = new File(".gitlet/staging");
    /** cWD Folder. */
    private static File removal = new File(".gitlet/removal");
    /** cWD Folder. */
    private static File hEAD = new File(".gitlet/index/HEAD");
    /** cWD Folder. */
    private static File master = new File(".gitlet/index/master");
    /** cWD Folder. */
    private static File curBranch = new File(
            ".gitlet/index/curBranch");
    /** cWD Folder. */
    private static int commitLength = 40;


    public Repository() {
        Commit initial = new Commit(
                "initial commit", "", new TreeMap<>());
        gitlet.mkdir();
        objects.mkdir();
        commits.mkdir();
        blobs.mkdir();
        index.mkdir();
        staging.mkdir();
        removal.mkdir();
        String initialSHA = initial.getHash();
        Utils.writeObject(Utils.join(commits, initialSHA), initial);
        try {
            hEAD.createNewFile();
            Utils.writeContents(hEAD, initialSHA);
        } catch (java.io.IOException error) {
            Main.exitWithError("Could not create HEAD branch");
        }
        try {
            master.createNewFile();
            Utils.writeContents(master, initialSHA);
        } catch (java.io.IOException error) {
            Main.exitWithError("Could not create master branch");
        }
        try {
            curBranch.createNewFile();
            Utils.writeContents(curBranch, master.getName());
        } catch (java.io.IOException error) {
            Main.exitWithError("Could not create curBranch branch");
        }
    }

    public static void add(File fileToAdd) {
        String filehash = Utils.sha1(Utils.readContentsAsString(fileToAdd));
        File stagedFile = Utils.join(staging, fileToAdd.getName());
        String commitHash = Utils.readContentsAsString(hEAD);
        Commit current =
                Utils.readObject(Utils.join(commits, commitHash), Commit.class);
        if (current.getTree().get(fileToAdd.getName()) != null
                &&
                current.getTree().get(fileToAdd.getName()).equals(filehash)) {
            if (stagedFile.exists()) {
                stagedFile.delete();
            }
            File stagedRem = Utils.join(removal, fileToAdd.getName());
            if (stagedRem.exists()) {
                stagedRem.delete();
            }
        } else {
            try {
                stagedFile.createNewFile();
                Utils.writeContents(stagedFile, filehash);
            } catch (java.io.IOException error) {
                Main.exitWithError("Could not create pointer to staged file");
            }
            String copyOfBlob = Utils.readContentsAsString(fileToAdd);
            File blob = new File(String.valueOf(Utils.join(blobs, filehash)));
            try {
                blob.createNewFile();
                Utils.writeContents(blob, copyOfBlob);
            } catch (java.io.IOException error) {
                Main.exitWithError("Could not create blob.");
            }
        }
    }

    public static void branch(String branchName) {
        File branch = Utils.join(index, branchName);
        if (branch.exists()) {
            Main.exitWithError("A branch with that name already exists.");
        } else {
            try {
                branch.createNewFile();
                Utils.writeContents(branch, Utils.readContentsAsString(hEAD));
            } catch (java.io.IOException error) {
                Main.exitWithError("Could not create new branch.");
            }
        }
    }

    public static void rmbranch(String branchName) {
        File branch = Utils.join(index, branchName);
        if (!branch.exists()) {
            Main.exitWithError("A branch with that name does not exist.");
        }
        if (Utils.readContentsAsString(branch).
                equals(Utils.readContentsAsString(hEAD))) {
            Main.exitWithError("Cannot remove the current branch.");
        }
        branch.delete();
    }

    public static void commit(String message) {
        if (message.isEmpty()) {
            Main.exitWithError("Please enter a commit message.");
        }
        File[] staged = staging.listFiles();
        File[] removed = removal.listFiles();
        if (staged.length == 0 && removed.length == 0) {
            Main.exitWithError("No changes added to the commit.");
        }
        Commit parent = Utils.readObject(
                Utils.join(commits,
                        Utils.readContentsAsString(hEAD)), Commit.class);
        Commit newCommit = new Commit(message,
                Utils.readContentsAsString(hEAD), parent.getTree());
        for (File file : staged) {
            newCommit.changeTree(file.getName(),
                    Utils.readContentsAsString(file));
        }
        for (File file : removed) {
            newCommit.untrack(file.getName());
        }
        File branch = Utils.join(index, Utils.readContentsAsString(curBranch));
        Utils.writeContents(hEAD, newCommit.getHash());
        Utils.writeContents(branch, newCommit.getHash());
        Utils.writeObject(Utils.join(commits, newCommit.getHash()), newCommit);
        for (File file : staging.listFiles()) {
            file.delete();
        }
        for (File file : removal.listFiles()) {
            file.delete();
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        String currentHash = Utils.readContentsAsString(hEAD);
        List<File> branches = Utils.allFiles(index);
        for (File file : branches) {
            if (!file.getName().equals("HEAD")
                    &&
                    !file.getName().equals("curBranch")) {
                if (Utils.readContentsAsString(file).equals(currentHash)) {
                    System.out.print("*");
                }
                System.out.println(file.getName());
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> stagedFiles = Utils.plainFilenamesIn(staging);
        for (String file : stagedFiles) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removedFiles = Utils.plainFilenamesIn(removal);
        for (String file : removedFiles) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        status2(stagedFiles, removedFiles);
    }

    public static void status2(List<String> stagedFiles,
                               List<String> removedFiles) {
        Commit current = Utils.readObject(Utils.join(commits,
                Utils.readContentsAsString(hEAD)), Commit.class);
        List<String> modifiedFiles = new ArrayList<>();
        List<String> untrackedFiles = new ArrayList<>();
        List<String> cWDFiles = Utils.plainFilenamesIn(cWD);
        for (String file : cWDFiles) {
            String fileHash = Utils.sha1(
                    Utils.readContentsAsString(Utils.join(cWD, file)));
            if (current.getTree().containsKey(file)
                    &&
                    !current.getTree().get(file).equals(fileHash)
                    &&
                    !stagedFiles.contains(file)) {
                modifiedFiles.add(file);
            }
            if (stagedFiles.contains(file)
                    &&
                    !Utils.readContentsAsString(
                            Utils.join(staging, file)).equals(fileHash)) {
                modifiedFiles.add(file);
            }
            if (!current.getTree().containsKey(file)
                    &&
                    !stagedFiles.contains(file)) {
                untrackedFiles.add(file);
            }
        }
        for (String file : stagedFiles) {
            if (!Utils.join(cWD, file).exists()) {
                modifiedFiles.add(file);
            }
        }
        for (String file : current.getTree().keySet()) {
            if (!removedFiles.contains(file)
                    &&
                    !Utils.join(cWD, file).exists()) {
                modifiedFiles.add(file);
            }
        }
        for (String file : modifiedFiles) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String file : untrackedFiles) {
            System.out.println(file);
        }
    }

    public static void rm(String fileName) {
        File deleteFile = Utils.join(staging, fileName);
        Commit head = Utils.readObject(Utils.join(commits,
                Utils.readContentsAsString(hEAD)), Commit.class);
        boolean tracked = head.getTree().containsKey(fileName);
        if (!deleteFile.exists() && !tracked) {
            Main.exitWithError("No reason to remove the file.");
        }
        if (tracked) {
            File stagedFile = Utils.join(removal, fileName);
            try {
                stagedFile.createNewFile();
            } catch (java.io.IOException error) {
                Main.exitWithError("Could not stage file for removal.");
            }
            File cWDFile = new File(fileName);
            if (cWDFile.exists()) {
                cWDFile.delete();
            }
        }
        deleteFile.delete();
    }

    public static void log() {
        String parent;
        Commit initial = Utils.readObject(
                Utils.join(commits,
                        Utils.readContentsAsString(hEAD)), Commit.class);
        if (!initial.getMergeParent().isEmpty()) {
            parent = mergeCommit(Utils.readContentsAsString(hEAD));
        } else {
            parent = logCommit(Utils.readContentsAsString(hEAD));
        }
        while (parent != "") {
            Commit test = Utils.readObject(
                    Utils.join(commits, parent), Commit.class);
            if (!test.getMergeParent().isEmpty()) {
                parent = mergeCommit(parent);
            } else {
                parent = logCommit(parent);
            }
        }
    }

    private static String logCommit(String code) {
        Commit current = Utils.readObject(
                Utils.join(commits, code), Commit.class);
        System.out.println("===");
        System.out.println(String.format("commit %s", current.getHash()));
        System.out.println(String.format("Date: %s", current.getTimeStamp()));
        System.out.println(current.getMessage());
        System.out.println();
        return current.getParent();
    }

    private static String mergeCommit(String code) {
        Commit current = Utils.readObject(
                Utils.join(commits, code), Commit.class);
        System.out.println("===");
        System.out.println(String.format("commit %s", current.getHash()));
        System.out.println(String.format("Date: %s", current.getTimeStamp()));
        System.out.println(current.getMessage());
        System.out.println();
        return current.getParent();
    }

    public static void globallog() {
        List<String> listofcommits = Utils.plainFilenamesIn(commits);
        for (String commit : listofcommits) {
            logCommit(commit);
        }
    }

    public static void find(String message) {
        List<String> listOfCommits = new ArrayList<>();
        List<String> allCommits = Utils.plainFilenamesIn(commits);
        for (String commit : allCommits) {
            Commit current = Utils.readObject(
                    Utils.join(commits, commit), Commit.class);
            if (current.getMessage().equals(message)) {
                listOfCommits.add(commit);
            }
        }
        if (listOfCommits.size() == 0) {
            Main.exitWithError("Found no commit with that message.");
        }
        for (String commit : listOfCommits) {
            System.out.println(commit);
        }
    }

    public static void reset(String commitID) {
        checkoutCommit(commitID);
        File branchHead = Utils.join(index,
                Utils.readContentsAsString(curBranch));
        Utils.writeContents(branchHead, commitID);
        Utils.writeContents(hEAD, commitID);

    }

    public static void checkoutFile(String fileName) {
        checkoutCommitFile(Utils.readContentsAsString(hEAD), fileName);
    }

    public static void checkoutBranch(String branchName) {
        File branch = Utils.join(index, branchName);
        if (!branch.exists()) {
            Main.exitWithError("No such branch exists.");
        }
        if (Utils.readContentsAsString(branch).equals(
                Utils.readContentsAsString(hEAD))
                &&
                Utils.readContentsAsString(curBranch).equals(branchName)) {
            Main.exitWithError("No need to checkout the current branch.");
        }
        checkoutCommit(Utils.readContentsAsString(
                Utils.join(index, branchName)));
        Utils.writeContents(hEAD, Utils.readContentsAsString(branch));
        Utils.writeContents(curBranch, branchName);
    }

    private static void checkoutCommit(String commitID) {
        File commit = Utils.join(commits, commitID);
        if (commitID.length() < commitLength) {
            for (File file : commits.listFiles()) {
                if (file.getName().contains(commitID)) {
                    commit = file;
                }
            }
        } else {
            if (!commit.exists()) {
                Main.exitWithError("No commit with that id exists.");
            }
        }
        Commit currentCommit = Utils.readObject(
                Utils.join(commits,
                        Utils.readContentsAsString(hEAD)), Commit.class);
        Commit resetCommit = Utils.readObject(commit, Commit.class);
        List<String> cWDFiles = Utils.plainFilenamesIn(cWD);
        for (String file : cWDFiles) {
            if (!currentCommit.getTree().containsKey(file)
                    &&
                    resetCommit.getTree().containsKey(file)) {
                Main.exitWithError(
                        "There is an untracked file in the way;"
                                +
                                " delete it, or add and commit it first.");
            }
            if (currentCommit.getTree().containsKey(file)
                    &&
                    !resetCommit.getTree().containsKey(file)) {
                Utils.join(cWD, file).delete();
            }
        }
        for (String fileName : resetCommit.getTree().keySet()) {
            File newFile = Utils.join(cWD, fileName);
            if (!Utils.join(cWD, fileName).exists()) {
                try {
                    newFile.createNewFile();
                } catch (java.io.IOException error) {
                    Main.exitWithError("Could not create new file from reset.");
                }
            }
            String blobHash = resetCommit.getTree().get(fileName);
            File blobFile = Utils.join(blobs, blobHash);
            String blobValue = Utils.readContentsAsString(blobFile);
            Utils.writeContents(newFile, blobValue);
        }
        for (File file : staging.listFiles()) {
            file.delete();
        }
    }

    public static void checkoutCommitFile(String commitID, String fileName) {
        File commit = Utils.join(commits, commitID);
        if (commitID.length() < commitLength) {
            for (File file : commits.listFiles()) {
                if (file.getName().contains(commitID)) {
                    commit = file;
                }
            }
        } else {
            if (!commit.exists()) {
                Main.exitWithError("No commit with that id exists.");
            }
        }
        Commit current = Utils.readObject(commit, Commit.class);
        String blobhash = current.getTree().get(fileName);
        if (blobhash == null) {
            Main.exitWithError("File does not exist in that commit.");
        } else {
            File ofInterest = Utils.join(blobs, blobhash);
            File file = new File(fileName);
            if (file.exists()) {
                Utils.writeContents(file,
                        Utils.readContentsAsString(ofInterest));
            } else {
                try {
                    file.createNewFile();
                    Utils.writeContents(file,
                            Utils.readContentsAsString(ofInterest));
                } catch (java.io.IOException error) {
                    Main.exitWithError("Could not create checkoutFile");
                }
            }
        }
    }

    public static Commit getSplit(String branchName) {
        Set<String> visitedNodes = new TreeSet<>();
        List<String> commitQueue = new ArrayList<>();
        visitedNodes.add(Utils.readContentsAsString(
                Utils.join(index, branchName)));
        commitQueue.add(Utils.readContentsAsString(
                Utils.join(index, branchName)));
        while (!commitQueue.isEmpty()) {
            Commit current = Utils.readObject(
                    Utils.join(commits, commitQueue.remove(0)),
                    Commit.class);
            if (!visitedNodes.contains(current.getParent())) {
                visitedNodes.add(current.getParent());
                if (!current.getParent().isEmpty()) {
                    commitQueue.add(current.getParent());
                }
            }
            if (!visitedNodes.contains(current.getMergeParent())) {
                visitedNodes.add(current.getMergeParent());
                if (!current.getMergeParent().isEmpty()) {
                    commitQueue.add(current.getMergeParent());
                }
            }
        }
        Set<String> visitedNodes2 = new TreeSet<>();
        List<String> commitQueue2 = new ArrayList<>();
        visitedNodes2.add(Utils.readContentsAsString(hEAD));
        commitQueue2.add(Utils.readContentsAsString(hEAD));
        while (!commitQueue2.isEmpty()) {
            String hashCurrent = commitQueue2.remove(0);
            Commit current = Utils.readObject(
                    Utils.join(commits, hashCurrent), Commit.class);
            if (visitedNodes.contains(hashCurrent)) {
                return current;
            }
            if (!visitedNodes2.contains(current.getMergeParent())) {
                visitedNodes2.add(current.getMergeParent());
                if (!current.getMergeParent().isEmpty()) {
                    commitQueue2.add(current.getMergeParent());
                }
            }
            if (!visitedNodes2.contains(current.getParent())) {
                visitedNodes2.add(current.getParent());
                if (!current.getParent().isEmpty()) {
                    commitQueue2.add(current.getParent());
                }
            }
        }
        return null;
    }

    public static Commit[] merge1(String branchName) {
        List<String> stagedFiles = Utils.plainFilenamesIn(staging);
        List<String> removedFiles = Utils.plainFilenamesIn(removal);
        if (stagedFiles.size() != 0 || removedFiles.size() != 0) {
            Main.exitWithError("You have uncommitted changes.");
        }
        File branch = Utils.join(index, branchName);
        if (!branch.exists()) {
            Main.exitWithError("A branch with that name does not exist.");
        }
        if (Utils.readContentsAsString(curBranch).equals(branchName)) {
            Main.exitWithError("Cannot merge a branch with itself.");
        }
        Commit currentCommit = Utils.readObject(Utils.join(
                commits, Utils.readContentsAsString(hEAD)), Commit.class);
        File commit = Utils.join(commits,
                Utils.readContentsAsString(Utils.join(index, branchName)));
        Commit branchCommit = Utils.readObject(commit, Commit.class);
        for (String file : Utils.plainFilenamesIn(cWD)) {
            if (!currentCommit.getTree().containsKey(file)) {
                Main.exitWithError(
                        "There is an untracked file in the way; "
                                +
                                "delete it, or add and commit it first.");
            }
        }
        Commit splitCommit = getSplit(branchName);
        if (splitCommit.getHash().equals(branchCommit.getHash())) {
            Main.exitWithError(
                    "Given branch is an ancestor of the current branch.");
        }
        if (splitCommit.getHash().equals(currentCommit.getHash())) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return null;
        }
        Commit[] foo = {currentCommit, splitCommit, branchCommit};
        return foo;
    }

    public static void merge(String branchName) {
        Commit[] foo = merge1(branchName);
        if (foo == null) {
            return;
        }
        Commit currentCommit = foo[0];
        Commit splitCommit = foo[1];
        Commit branchCommit = foo[2];
        merge1(branchName);
        File currentBranch = Utils.join(index,
                Utils.readContentsAsString(curBranch));
        Commit newCommit = new Commit(String.format("Merged %s into %s.",
                        branchName, currentBranch.getName()), Utils.
                readContentsAsString(hEAD),
                currentCommit.getTree(),
                Utils.readContentsAsString(Utils.join(index, branchName)));
        boolean mergeConflict = false;
        for (String key : splitCommit.getTree().keySet()) {
            File newFile = new File(key);
            if (splitCommit.getTree().get(key).
                    equals(currentCommit.getTree().get(key))) {
                if (branchCommit.getTree().get(key) == null) {
                    newCommit.untrack(key);
                    newFile.delete();
                } else if (!branchCommit.getTree().get(key).
                        equals(splitCommit.getTree().get(key))) {
                    newCommit.changeTree(key, branchCommit.getTree().get(key));
                    makeMergeFile(newFile);
                    Utils.writeContents(newFile,
                            Utils.readContents(Utils.join(
                                    blobs, branchCommit.getTree().get(key))));
                }
            } else if (splitCommit.getTree().get(key).
                    equals(branchCommit.getTree().get(key))) {
                newCommit = idkWhatImDoing(currentCommit, key, newFile,
                        branchCommit, splitCommit, newCommit);
            } else if (currentCommit.getTree().get(key) == null && branchCommit.
                    getTree().get(key) == null) {
                newFile.delete();
            } else if (currentCommit.getTree().get(key) == null) {
                makeMergeFile(newFile);
                writeConflict1(newFile, branchCommit, key);
                mergeConflict = makeSpiderMonkey(newFile, newCommit, key);
            } else if (branchCommit.getTree().get(key) == null) {
                makeMergeFile(newFile);
                makeSpiderman(newFile, currentCommit, key);
                mergeConflict = makeSpiderMonkey(newFile, newCommit, key);

            } else if (!branchCommit.getTree().get(key).
                    equals(currentCommit.getTree().get(key))) {
                makeMergeFile(newFile);
                writeConflict2(newFile, branchCommit, key, currentCommit);
                mergeConflict = makeSpiderMonkey(newFile, newCommit, key);
            }
        }
        makeMystery(branchCommit, splitCommit,
                currentCommit, newCommit, mergeConflict);
    }

    public static Commit idkWhatImDoing(Commit currentCommit, String key,
                                        File newFile, Commit branchCommit,
                                        Commit splitCommit, Commit newCommit) {
        if (currentCommit.getTree().get(key) == null) {
            newFile.delete();
        } else if (!branchCommit.getTree().get(key).
                equals(splitCommit.getTree().get(key))) {
            newCommit.changeTree(key, currentCommit.getTree().get(key));
            makeMergeFile(newFile);
            Utils.writeContents(newFile,
                    Utils.readContents(Utils.join(
                            blobs, currentCommit.getTree().get(key))));
        }
        return newCommit;
    }
    public static boolean makeSpiderMonkey(File newFile,
                                           Commit newCommit, String key) {
        String filehash = Utils.sha1(
                Utils.readContentsAsString(newFile));
        String copyOfBlob = Utils.readContentsAsString(newFile);
        File blob = new File(String.valueOf(
                Utils.join(blobs, filehash)));
        makeBlob(blob, copyOfBlob);
        newCommit.changeTree(key, filehash);
        return true;
    }
    public static void makeSpiderman(File newFile,
                                     Commit currentCommit, String key) {
        Utils.writeContents(newFile, "<<<<<<< HEAD", "\n");
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                Utils.readContentsAsString(Utils.join(blobs,
                        currentCommit.getTree().get(key))));
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                "=======", "\n");
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                ">>>>>>>", "\n");
    }

    public static void makeMystery(Commit branchCommit, Commit splitCommit,
                                   Commit currentCommit, Commit newCommit,
                                   boolean mergeConflict) {
        for (String key : branchCommit.getTree().keySet()) {
            File newFile = new File(key);
            if (splitCommit.getTree().get(key) == null && currentCommit.
                    getTree().get(key) == null) {
                newCommit.changeTree(key, branchCommit.getTree().get(key));
                makeMergeFile(newFile);
                Utils.writeContents(newFile,
                        Utils.readContents(Utils.join(blobs,
                                branchCommit.getTree().get(key))));
            }
        }
        newCommit = merge2(currentCommit, splitCommit,
                branchCommit, newCommit, mergeConflict);
        File branch = Utils.join(index, Utils.readContentsAsString(curBranch));
        Utils.writeContents(hEAD, newCommit.getHash());
        Utils.writeContents(branch, newCommit.getHash());
        File newCommitFile = Utils.join(commits, newCommit.getHash());
        if (!newCommitFile.exists()) {
            try {
                newCommitFile.createNewFile();
            } catch (java.io.IOException error) {
                Main.exitWithError("Could not create blob.");
            }
        }
        Utils.writeObject(newCommitFile, newCommit);
    }

    public static void makeBlob(File blob, String copyOfBlob) {
        try {
            blob.createNewFile();
            Utils.writeContents(blob, copyOfBlob);
        } catch (java.io.IOException error) {
            Main.exitWithError("Could not create blob.");
        }
    }

    public static void writeConflict1(File newFile,
                                      Commit branchCommit, String key) {
        Utils.writeContents(newFile,
                "<<<<<<< HEAD", "\n");
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                "=======", "\n");
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                Utils.readContentsAsString(Utils.join(
                        blobs, branchCommit.getTree().get(key))));
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                ">>>>>>>", "\n");
    }

    public static void writeConflict2(File newFile, Commit branchCommit,
                                      String key, Commit currentCommit) {
        Utils.writeContents(newFile, "<<<<<<< HEAD", "\n");
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                Utils.readContentsAsString(Utils.join(blobs,
                        currentCommit.getTree().get(key))));
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                "=======", "\n");
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                Utils.readContentsAsString(Utils.join(blobs,
                        branchCommit.getTree().get(key))));
        Utils.writeContents(newFile,
                Utils.readContentsAsString(newFile),
                ">>>>>>>", "\n");
    }

    public static void makeMergeFile(File newFile) {
        if (!newFile.exists()) {
            try {
                newFile.createNewFile();
            } catch (java.io.IOException error) {
                Main.exitWithError(
                        "Could not make new file from merge.");
            }
        }
    }

    public static Commit merge2(Commit currentCommit, Commit splitCommit,
                                Commit branchCommit, Commit newCommit,
                                boolean mergeConflict) {
        for (String key : currentCommit.getTree().keySet()) {
            File newFile = new File(key);
            if (splitCommit.getTree().get(key) == null
                    &&
                    branchCommit.getTree().get(key) != null
                    &&
                    !currentCommit.getTree().get(key).
                            equals(branchCommit.getTree().get(key))) {
                if (!newFile.exists()) {
                    try {
                        newFile.createNewFile();
                    } catch (java.io.IOException error) {
                        Main.exitWithError(
                                "Could not make new file from merge.");
                    }
                }
                Utils.writeContents(newFile, "<<<<<<< HEAD", "\n");
                Utils.writeContents(newFile,
                        Utils.readContentsAsString(newFile),
                        Utils.readContentsAsString(Utils.join(blobs,
                                currentCommit.getTree().get(key))));
                Utils.writeContents(newFile,
                        Utils.readContentsAsString(newFile),
                        "=======", "\n");
                Utils.writeContents(newFile,
                        Utils.readContentsAsString(newFile),
                        Utils.readContentsAsString(Utils.join(blobs,
                                branchCommit.getTree().get(key))));
                Utils.writeContents(newFile,
                        Utils.readContentsAsString(newFile), ">>>>>>>", "\n");
                String filehash = Utils.sha1(
                        Utils.readContentsAsString(newFile));
                String copyOfBlob = Utils.readContentsAsString(newFile);
                File blob = new File(String.valueOf(
                        Utils.join(blobs, filehash)));
                try {
                    blob.createNewFile();
                    Utils.writeContents(blob, copyOfBlob);
                } catch (java.io.IOException error) {
                    Main.exitWithError("Could not create blob.");
                }
                newCommit.changeTree(key, filehash);
                mergeConflict = true;
            }
        }
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        return newCommit;
    }
}

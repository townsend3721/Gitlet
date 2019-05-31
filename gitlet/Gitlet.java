package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @author Townsend Saunders III
 * Gitlet. An emulation of the great github.
 */
@SuppressWarnings("unchecked")
public class Gitlet {

    /**
     *Set up Gitlet, make a new file, first commit, etc.
     * @param args - A list string.
     */
    public Gitlet(String[] args) {
        if (new File(".gitlet").exists()) {
            System.out.println("A gitlet version control system already"
                    + " exists in the current directory.");
        } else {
            File file = new File(".gitlet");
            file.mkdir();
            Commit firstCommit = new Commit("initial commit", null, "master");
            HashMap<String, Commit> commits = new HashMap<String, Commit>();
            commits.put("master", firstCommit);
            Helper firstHelper = new Helper("master");
            Gitlet.serialize(firstHelper, ".gitlet/staging");
            Gitlet.serialize(commits, ".gitlet/hash");
            ArrayList<Commit> commitList = new ArrayList<Commit>();
            commitList.add(firstCommit);
            Gitlet.serialize(commitList, ".gitlet/list");
        }
    }
    /**
     *add file and stage it for a commit.
     * @param args - the file to commit.
     */
    public static void add(String[] args) {
        if (args.length == 1) {
            System.out.println("Did not enter enough arguments.");
        } else {
            File file = new File(args[1]);
            Object helpObject = Gitlet.deserialize(".gitlet/staging");
            Object hashObject = Gitlet.deserialize(".gitlet/hash");
            Helper help = (Helper) helpObject;
            HashMap<String, Commit> commits =
                    (HashMap<String, Commit>) hashObject;
            if (!file.isFile()) {
                System.out.println("File does not exist.");
            } else {
                if (help.getGiven() != null) {
                    if (help.getGiven().contains(args[1])) {
                        Commit committ = commits.get(help.getNewChild());
                        while (!committ.getFiles().contains
                                (args[1]) && committ != null) {
                            committ = committ.getParent();
                        }
                        if (Gitlet.compareFiles(Paths.get(args[1]),
                                Paths.get(".gitlet/"
                                        + committ.getIdentification()
                                        + "/" + args[1]))) {
                            return;
                        }
                    }
                }
                help.getFiles().add(args[1]);
                if (help.getDeleted() != null) {
                    if (help.getDeleted().contains(args[1])) {
                        help.getDeleted().remove(args[1]);
                        help.getFiles().remove(args[1]);
                    }
                }
                Gitlet.serialize(help, ".gitlet/staging");
            }
        }
    }

    /**
     *Saves a snapshot of certain files in the
     * current commit and staging area so they
     * can be restored at a later time, creating a new commit.
     * @param args - the files to commit.
     */
    public static void commit(String[] args) {
        if (commitError(args)) {
            return;
        }
        Object helpObject = Gitlet.deserialize(".gitlet/staging");
        Object hashObject = Gitlet.deserialize(".gitlet/hash");
        Helper help = (Helper) helpObject;
        HashMap<String, Commit> commits = (HashMap<String, Commit>) hashObject;
        if (help.getFiles().size() == 0) {
            if (help.getDeleted().size() != 0) {
                help.setDeleted(new HashSet<String>());
                Gitlet.serialize(help, ".gitlet/staging");
            } else {
                System.out.println("No changes added to the commit.");
                return;
            }
        }
        Commit committ = new Commit(args[1],
                (Commit) commits.get(help.getNewChild()),
                help.getNewChild());
        committ.commitAll(help);
        ArrayList<Commit> commitList =
                (ArrayList<Commit>) Gitlet.deserialize(".gitlet/list");
        commitList.add(committ);
        Gitlet.serialize(commitList, ".gitlet/list");
        commits.put(help.getNewChild(), committ);
        String stringer = ".gitlet/" + committ.getIdentification();
        File dir = new File(stringer);
        dir.mkdir();

        for (String file : help.getFiles()) {
            help.getGiven().add(file);
            String newaddr = stringer + "/" + file;
            Path path = Paths.get(file);
            Path newpath = Paths.get(newaddr);
            File newfile = new File(newaddr);
            newfile.getParentFile().mkdirs();
            copy(path, newpath);
        }
        Gitlet.serialize(commits, ".gitlet/hash");
        help.clear();
        Gitlet.serialize(help, ".gitlet/staging");
    }

    /**
     *
     * @param args - check to see if args is a commitError.
     * @return a boolean true or false.
     */
    private static boolean commitError(String[] args) {
        if (args.length == 1 || args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            return true;
        }
        return false;
    }

    /**
     * Copies the path into files.
     * @param path - path1.
     * @param newpath - path2
     */
    private static void copy(Path path, Path newpath) {
        try {
            Files.copy(path, newpath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("IOException while copying.");
        }
    }

    /**
     * Unstage the file if it is currently staged.
     * deletes the specified files from this child.
     * @param args - file to be deleted.
     */
    public static void delete(String[] args) {
        if (commitError(args)) {
            return;
        } else {
            Helper help = (Helper) Gitlet.deserialize(".gitlet/staging");
            Object hashObject = Gitlet.deserialize(".gitlet/hash");
            @SuppressWarnings("unchecked")
            HashMap<String, Commit> commits =
                    (HashMap<String, Commit>) hashObject;
            Commit com = commits.get(help.getNewChild());
            if (!help.getFiles().contains(args[1])
                    && !com.getFiles().contains(args[1])) {
                System.out.println("No reason to remove the file.");
            }
            if (help.getFiles().contains(args[1])) {
                help.getFiles().remove(args[1]);
            }
            if (com.getFiles().contains(args[1])) {
                File file = new File(args[1]);
                if (file.isFile()) {
                    Utils.restrictedDelete(file);
                }
                com.getFiles().remove(args[1]);
                help.getDeleted().add(args[1]);
            }
            if (help.getGiven().contains(args[1])) {
                help.getGiven().remove(args[1]);
            }
            Gitlet.serialize(help, ".gitlet/staging");
        }

    }
    /**
     * arting at the current head commit,
     * display information about each commit
     * backwards along the commit tree until
     * the initial commit, following the first
     * parent commit links, ignoring any second
     * parents found in merge commits.
    * log the commits and print them out.
    */
    public static void log() {
        Helper help = (Helper)
                Gitlet.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits =
                (HashMap<String, Commit>) Gitlet.deserialize(".gitlet/hash");
        Commit committ = commits.get(help.getNewChild());
        while (committ != null) {
            committ.print();
            committ = committ.getParent();
        }
    }

    /**
     * Like log, except displays information about all
     * commits ever made. The order of the commits
     * does not matter.
     * print everything.
     */
    public static void globaLog() {
        ArrayList<Commit> commitList =
                (ArrayList<Commit>) Gitlet.deserialize(".gitlet/list");
        for (Commit committ : commitList) {
            committ.print();
        }
    }

    /**
     * Prints out the ids of all commits that
     * have the given commit message, one per line.
     * @param args - the file being searched for.
     */
    public static void find(String[] args) {
        if (args.length == 1) {
            System.out.println("Did not enter enough arguments");
        } else {
            boolean bool = false;
            String stringy = args[1];
            Object commitee = Gitlet.deserialize(".gitlet/list");
            ArrayList<Commit> commitmylist = (ArrayList<Commit>) commitee;
            for (Commit committ : commitmylist) {
                if (committ.getm().equals(stringy)) {
                    System.out.println(committ.getIdentification());
                    bool = true;
                }
            }
            if (!bool) {
                System.out.println("Found no commit with that message");
            }
        }
    }

    /**
     *Print the current contents of gitlet.
     */
    public static void status() {
        System.out.println("=== Branches ===");
        Object gitletstaging = Gitlet.deserialize(".gitlet/staging");
        Helper help = (Helper) gitletstaging;
        Object hashc = Gitlet.deserialize(".gitlet/hash");
        HashMap<String, Commit> commits = (HashMap<String, Commit>) hashc;
        ArrayList<String> mylist = new ArrayList<String>();
        for (String stringy : commits.keySet()) {
            mylist.add(stringy);
        }
        java.util.Collections.sort(mylist);
        for (String stringer : mylist) {
            if (stringer.equals(help.getNewChild())) {
                System.out.println("*" + stringer);
            } else {
                System.out.println(stringer);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String file : help.getFiles()) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String file : help.getDeleted()) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /**
     * takes the given files and puts it in the working directory.
     * @param args - checkout the specified files from args.
     */
    public static void checkout(String[] args) {
        if (args.length == 1) {
            System.out.println("Did not enter enough arguments.");
            return;
        }
        String stringy = args[1];
        Helper help = (Helper) Gitlet.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits =
                (HashMap<String, Commit>) Gitlet.deserialize(".gitlet/hash");
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            }
            String stringer = stringy;
            stringy = args[3];
            for (String branch : commits.keySet()) {
                Commit committ = commits.get(branch);
                String currentBranch = committ.getChild();
                while (committ != null
                        && committ.getChild().equals(currentBranch)) {
                    if (committ.getIdentification().equals(stringer)
                            || committ.getIdentification().
                            startsWith(stringer, 0)) {
                        stringer = committ.getIdentification();
                        if (copyHelp(stringy, stringer, committ)) {
                            return;
                        }
                        while (!committ.getFiles().contains(stringy)) {
                            committ = committ.getParent();
                        }
                        String newStringer = ".gitlet/"
                                + committ.getIdentification() + "/" + stringy;
                        Path nextPath = Paths.get(stringy);
                        Path path = Paths.get(newStringer);
                        copy(path, nextPath);
                    }
                    committ = committ.getParent();
                }
            }
            System.out.println("No commit with that id exists.");
        } else if (stringy.equals(help.getNewChild())) {
            System.out.println("No need to checkout the current branch.");
            return;
        } else {
            if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                } else {
                    stringy = args[2];
                }
            }
            for (String branch : commits.keySet()) {
                if (branch.equals(stringy)) {
                    checkoutMyFiles(stringy, help, commits);
                    return;
                }
            }
            checkoutMyFile(stringy, help, commits);
        }
    }

    /**
     * Helper function for checkout.
     * @param stringy - a string.
     * @param help - A helper class that has relevant
     *            information about children, parents
     *             etc.
     * @param commits - Hashmap of all the commits.
     */
    private static void checkoutMyFile(String stringy,
                                     Helper help,
                                       HashMap<String, Commit> commits) {
        Commit committ = commits.get(help.getNewChild());
        if (help.getGiven().contains(stringy)) {
            while (!committ.getFiles().contains(stringy)) {
                committ = committ.getParent();
            }
            String newstringy = ".gitlet/"
                    + committ.getIdentification() + "/" + stringy;
            Path newpath = Paths.get(stringy);
            Path path = Paths.get(newstringy);
            copy(path, newpath);
        } else {
            System.out.println("No such branch exists.");
        }
    }

    /**
     * Helper function for checkout.
     * @param stringy - a string.
     * @param help - A helper class that has relevent
     *             info about children, parents etc.
     * @param commits - HashMap of all the commits.
     */
    private static void checkoutMyFiles(
            String stringy, Helper help, HashMap<String, Commit> commits) {
        Commit committ = commits.get(help.getNewChild());
        for (String file : Utils.plainFilenamesIn(".")) {
            int length = file.length();
            if (file.substring(length - 4, length).equals(".txt")) {
                if (!committ.getFiles().contains(file)
                        && !committ.getGiven().contains(file)
                        && !help.getFiles().contains(file)) {
                    System.out.println("There is an untracked file "
                            + "in the way; delete it or add it first.");
                    return;
                }
            }
        }
        help.setNewChild(stringy);
        help.superClear();
        help.setGiven(new HashSet<String>());
        Commit commithead = commits.get(stringy);
        newHelp(help, commithead);
        Gitlet.serialize(help, ".gitlet/staging");
        for (String file : Utils.plainFilenamesIn(".")) {
            int length = file.length();
            if (file.substring(length - 4, length).equals(".txt")) {
                if (!commithead.getFiles().contains(file)) {
                    File filer = new File(file);
                    Utils.restrictedDelete(filer);
                }
            }
        }
        for (String file : commithead.getFiles()) {
            String newstringy =
                    ".gitlet/" + commithead.getIdentification() + "/" + file;
            Path newpath = Paths.get(file);
            Path path = Paths.get(newstringy);
            copy(path, newpath);
        }
        for (String file : commithead.getGiven()) {
            Commit variable = commithead;
            while (!variable.getFiles().contains(file)) {
                variable = variable.getParent();
            }
            String newerstringy = ".gitlet/"
                    + variable.getIdentification() + "/" + file;
            Path newpath = Paths.get(file);
            Path path = Paths.get(newerstringy);
            copy(path, newpath);
        }
    }

    /**
     * A helper function for copy.
     * @param stringy - a string.
     * @param identifcation - identification number of the c
     * @param committ - a commit.
     * @return a boolean, true or false.
     */
    private static boolean copyHelp(
            String stringy, String identifcation, Commit committ) {
        if (!committ.getGiven().contains(stringy)
                && !committ.getFiles().contains(stringy)) {
            System.out.println(
                    "File does not exist in that commit.");
            return true;
        }
        if (committ.getFiles().contains(stringy)) {
            String stringer = ".gitlet/" + identifcation + "/" + stringy;
            Path newpath = Paths.get(stringy);
            Path path = Paths.get(stringer);
            copy(path, newpath);
            return true;
        }
        return false;
    }

    /**
     * A helper function for the helper class Helper.
     * @param help - a helper object.
     * @param committ - a commit.
     */
    private static void newHelp(Helper help, Commit committ) {
        for (String stringy : committ.getGiven()) {
            help.getGiven().add(stringy);
        }
        for (String stringy : committ.getDeleted()) {
            help.getDeleted().add(stringy);
        }
        for (String stringy : committ.getFiles()) {
            help.getGiven().add(stringy);
        }
    }

    /**
     * Creates a new child with the given name.
     * @param args - name of the child.
     */
    public static void child(String[] args) {
        if (commitError(args)) {
            return;
        }
        String stringer = args[1];
        Helper help = (Helper) Gitlet.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits =
                (HashMap<String, Commit>) Gitlet.deserialize(".gitlet/hash");
        if (commits.containsKey(stringer)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Commit mycommit = commits.get(help.getNewChild());
        commits.put(stringer, mycommit);
        Gitlet.serialize(commits, ".gitlet/hash");
    }

    /**
     * remove the pointer to the child.
     * @param args - the child to be removed.
     */
    public static void deletebranch(String[] args) {
        commitError(args);
        String stringer = args[1];
        Helper help = (Helper) Gitlet.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits =
                (HashMap<String, Commit>) Gitlet.deserialize(".gitlet/hash");
        if (!commits.containsKey(stringer)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (help.getNewChild().equals(stringer)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        commits.remove(stringer);
        Gitlet.serialize(commits, ".gitlet/hash");
    }

    /**
     *Checks out all the files tracked by
     * the given commit. Removes tracked files
     * that are not present in that commit.
     * Also moves the current branch's head to
     * that commit node.
     * @param args - the files to be modified.
     */
    public static void reset(String[] args) {
        if (commitError(args)) {
            return;
        } else {
            Object gitletdes = Gitlet.deserialize(("gitlet/staging"));
            Helper help = (Helper) gitletdes;
            HashMap<String, Commit> commits =
                    (HashMap<String, Commit>)
                            Gitlet.deserialize(".gitlet/hash");
            String identification = args[1];
            for (String child : commits.keySet()) {
                Commit committ = commits.get(child);
                String childlabel = committ.getChild();
                while (committ != null
                        && committ.getChild().equals(childlabel)) {
                    if (committ.getIdentification().equals(identification)
                            || committ.getIdentification()
                            .startsWith(identification, 0)) {
                        identification = committ.getIdentification();
                        help.superClear();
                        newHelp(help, committ);
                        commits.put(help.getNewChild(), committ);
                        Gitlet.serialize(commits, ".gitlet/hash");
                        Gitlet.serialize(help, ".gitlet/staging");

                        for (String file : committ.getFiles()) {
                            String stringy = ".gitlet/"
                                    + committ.getIdentification() + "/" + file;
                            Path newpath = Paths.get(file);
                            Path path = Paths.get(stringy);
                            copy(path, newpath);
                        }
                        for (String file : committ.getGiven()) {
                            Commit variable = committ;
                            while (!variable.getFiles().contains(file)) {
                                variable = variable.getParent();
                            }
                            String newstringer = ".gitlet/"
                                    + variable.getIdentification() + "/" + file;
                            Path newpath = Paths.get(file);
                            Path path = Paths.get(newstringer);
                            copy(path, newpath);
                        }
                        return;
                    }
                    committ = committ.getParent();
                }
            }
            System.out.println("No commit with that id exists.");
        }
    }

    /**
     * Merges files from the given branch into the current branch.
     * @param args - the files to be merged.
     */
    public static void merge(String[] args) {
        if (commitError(args)) {
            return;
        }
        Helper help = (Helper) Gitlet.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits =
                (HashMap<String, Commit>) Gitlet.deserialize(".gitlet/hash");
        if (childError(args, help, commits)) {
            return;
        } else {
            Commit mymerger = commits.get(args[1]);
            Commit mycommit = commits.get(help.getNewChild());
            HashSet<String> mymergeandcommit = new HashSet<String>();
            Commit variable = mymerger;
            while (variable != null) {
                mymergeandcommit.add(variable.getIdentification());
                variable = variable.getParent();
            }
            Commit merged = mycommit;
            while (!mymergeandcommit.contains(merged.getIdentification())) {
                merged = merged.getParent();
            }
            HashSet<String> newmerged = new HashSet<String>();
            variable = mymerger;
            variable = newMerge(variable, merged, newmerged);
            for (String file : mymerger.getDeleted()) {
                newmerged.remove(file);
            }
            HashSet<String> mynewmerged = new HashSet<String>();
            variable = mycommit;
            variable = newMerge(variable, merged, mynewmerged);
            for (String file : mycommit.getDeleted()) {
                mynewmerged.remove(file);
            }
            for (String file : newmerged) {
                variable = mymerger;
                while (!variable.getFiles().contains(file)) {
                    variable = variable.getParent();
                }
                if (!mynewmerged.contains(file)) {
                    String newaddr = ".gitlet/"
                            + variable.getIdentification() + "/" + file;
                    Path newpath = Paths.get(file);
                    Path path = Paths.get(newaddr);
                    copy(path, newpath);
                } else {
                    String newaddr = ".gitlet/"
                            + variable.getIdentification() + "/" + file;
                    String addr = file + ".conflicted";
                    Path newpath = Paths.get(addr);
                    Path path = Paths.get(newaddr);
                    copy(path, newpath);
                }
            }
        }
    }

    /**
     * Helper function for merge.
     * @param committ - a commit.
     * @param merging - a commit to merge.
     * @param mergin - A hashset.
     * @return
     */
    private static Commit newMerge(Commit committ,
                                 Commit merging, HashSet<String> mergin) {
        while (!committ.getIdentification()
                .equals(merging.getIdentification())) {
            for (String file : committ.getFiles()) {
                mergin.add(file);
            }
            committ = committ.getParent();
        }
        return committ;
    }

    /**
     * Checks to see if files are the same.
     * @param p1 - path to first file.
     * @param p2 - path to second file.
     * @return true if they are, false if not.
     */
    static boolean compareFiles(Path p1, Path p2) {
        try {
            return Arrays.equals(Files.readAllBytes(p1),
                    Files.readAllBytes(p2));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Helper function for merge in order to find errors within the children.
     * @param args - input.
     * @param help - the current stage.
     * @param commits - hashset of the current commits.
     * @return
     */
    private static boolean childError(String[] args, Helper help,
                                       HashMap<String, Commit> commits) {
        if (!commits.containsKey(args[1])) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        if (args[1].equals(help.getNewChild())) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        return false;
    }

    /**
     * Serialize a java object then turn it into a file.
     * @param object - object to serialize.
     * @param path - path to the object.
     */
    static void serialize(Object object, String path) {
        Object obj = object;
        File outFile = new File(path);
        try {
            FileOutputStream fOut = new FileOutputStream(outFile);
            ObjectOutputStream objOut = new ObjectOutputStream(fOut);
            objOut.writeObject(obj);
            objOut.close();
        } catch (IOException excp) {
            System.out.println(excp);
        }
    }

    /**
     * Deserialize a file and turn it into a java object.
     * @param path - path to the object to be deserialized.
     * @return
     */
    static Object deserialize(String path) {
        Object obj;
        File inFile = new File(path);
        try {
            FileInputStream fIn = new FileInputStream(inFile);
            ObjectInputStream inp = new ObjectInputStream(fIn);
            obj = inp.readObject();
            inp.close();
            return obj;
        } catch (IOException | ClassNotFoundException excp) {
            return excp;
        }
    }

}


package gitlet;

import java.io.File;
/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Townsend Saunders III
 *  @collaborators Daniel Stephens, Matt Brennan, Jeff Burr.
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {

        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init") && args.length == 1) {
            Gitlet gitlet = new Gitlet(args);
        } else if (!new File(".gitlet").exists()) {
            System.out.println("You should initialize a gitlet system first.");
        } else if (args[0].equals("add")) {
            Gitlet.add(args);
        } else if (args[0].equals("commit")) {
            Gitlet.commit(args);
        } else if (args[0].equals("rm")) {
            Gitlet.delete(args);
        } else if (args[0].equals("log")) {
            Gitlet.log();
        } else if (args[0].equals("global-log")) {
            Gitlet.globaLog();
        } else if (args[0].equals("find")) {
            Gitlet.find(args);
        } else if (args[0].equals("status")) {
            Gitlet.status();
        } else if (args[0].equals("checkout")) {
            Gitlet.checkout(args);
        } else if (args[0].equals("branch")) {
            Gitlet.child(args);
        } else if (args[0].equals("rm-branch")) {
            Gitlet.deletebranch(args);
        } else if (args[0].equals("reset")) {
            Gitlet.reset(args);
        } else if (args[0].equals("merge")) {
            Gitlet.merge(args);
        } else {
            System.out.println("No command with that name exists.");
        }
    }
}

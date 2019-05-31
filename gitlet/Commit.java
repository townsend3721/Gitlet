package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Date;
/** A gitlet commit.**/
/** @author Townsend Saunders III **/
public class Commit implements Serializable {
    /** Sets up the commit.
     * @param m - the commit message.
     * @param parent - the parent for this commit.
     * @param child - the branch for this commit. */

    public Commit(String m, Commit parent, String child) {
        _m = m;
        setChild(child);
        setParent(parent);
        SimpleDateFormat simpleDate = new
                SimpleDateFormat("EEE MMM dd hh:mm:ss YYYY");
        Date date = new Date();
        _time = simpleDate.format(date) + " -0800";
        _identification = Utils.sha1(_m, _time);
    }
    /** set the value of the parent.
     * @param parent - the parent for this commit **/
    public void setParent(Commit parent) {
        this._parent = parent;
    }
    /** set the value of.
     * @param child -
     * the child of this commit.**/
    public void setChild(String child) {
        this._child = child;
    }
    /** Get the value and @return _parent.**/
    public Commit getParent() {
        return _parent;
    }
    /** Get the value and @return _child. **/
    public String getChild() {
        return _child;
    }
    /** commit everything from the helper using.
     * @param help **/
    public void commitAll(Helper help) {
        commitGiven(help);
        commitFiles(help);
        commitDeleted(help);
    }
    /** commit the given files from the Helper.
     * @param help - Helper class.**/
    public void commitGiven(Helper help) {
        if (help.getDeleted() != null) {
            for (String file : help.getFiles()) {
                if (!_files.contains(file)) {
                    _given.add(file);
                }
            }
        }
    }
    /** commit the regular commit files from Helper.
     * @param help - Helper class.**/
    private void commitFiles(Helper help) {
        if (help.getFiles() != null) {
            _files.addAll(help.getFiles());
        }
    }
    /** commit the deleted files from the helper.
     * @param help - Helper class*/
    private void commitDeleted(Helper help) {
        if (help.getGiven() != null) {
            _deleted.addAll(help.getDeleted());
        }
    }
    /** @return _deleted, the deleted files. **/
    public HashSet<String> getDeleted() {
        return _deleted;
    }
    /** @return _files, the current files. **/
    public HashSet<String> getFiles() {
        return _files;
    }
    /** @return _given, the given files. **/
    public HashSet<String> getGiven() {
        return _given;
    }
    /** @return _identification, the identification code. **/
    public String getIdentification() {
        return _identification;
    }
    /** @return _m, the message for this commit. **/
    public String getm() {
        return _m;
    }
    /** @return _time, the time of this commit. **/
    public String getTime() {
        return _time;
    }

    /** print the commit information in the specified format.
     *
     */
    public void print() {
        System.out.println("===");
        String comm = "commit " + _identification;
        System.out.println(comm);
        System.out.println("Date: " + _time);
        System.out.println(_m);
        System.out.println();
    }

    /** the string identification. **/
    private String _identification;
    /**
     * the files for this commit.
     */
    private HashSet<String> _files = new HashSet<String>();
    /**
     * the deleted files.
     */
    private HashSet<String> _deleted = new HashSet<String>();
    /**
     * the files given upon creation.
     */
    private HashSet<String> _given = new HashSet<String>();
    /**
     * the message of the commit.
     */
    private String _m;
    /**
     * the children of this commit.
     */
    private String _child;
    /**
     * the parents of this commit.
     */
    private Commit _parent;
    /**
     * the time of this commit.
     */
    private String _time;
}

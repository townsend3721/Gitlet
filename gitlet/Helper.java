package gitlet;

import java.io.Serializable;
import java.util.HashSet;

/**
 * @author Townsend Saunders III.
 * Helper Class that stages all the information.
 */
public class Helper implements Serializable {
    /**
     *
     * @param child - set the children of this node.
     */
    public Helper(String child) {
        setNewChild(child);
    }

    /**
     *
     * @return the child of this node
     */
    public String getNewChild() {
        return _newChild;
    }

    /**
     *
     * @param child - set the _newchild = to child.
     */
    public void setNewChild(String child) {
        this._newChild = child;
    }

    /**
     * clear the current stage.
     */
    public void clear() {
        setFiles(new HashSet<String>());
    }

    /**
     * clear the current stage, including deleted and given files.
     */
    public void superClear() {
        setFiles(new HashSet<String>());
        setDeleted(new HashSet<String>());
        setGiven(new HashSet<String>());
    }

    /**
     *
     * @return the deleted files.
     */
    public HashSet<String> getDeleted() {
        return _deleted;
    }

    /**
     *
     * @param delete - set the files that are going to be deleted.
     */
    public void setDeleted(HashSet<String> delete) {
        this._deleted = delete;
    }

    /**
     *
     * @return the files that are staged.
     */
    public HashSet<String> getFiles() {
        return _files;
    }

    /**
     *
     * @param files - set the staged files.
     */
    public void setFiles(HashSet<String> files) {
        this._files = files;
    }

    /**
     *
     * @param given - set the given files.
     */
    public void setGiven(HashSet<String> given) {
        this._given = given;
    }

    /**
     *
     * @return the given files.
     */
    public HashSet<String> getGiven() {
        return _given;
    }

    /**
     * the child of this node.
     */
    private String _newChild;
    /**
     * the files that are staged.
     */
    private HashSet<String> _files = new HashSet<String>();
    /**
     * the files that will be deleted.
     */
    private HashSet<String> _deleted = new HashSet<String>();
    /**
     * the files that are given upon creation.
     */
    private HashSet<String> _given = new HashSet<String>();

}

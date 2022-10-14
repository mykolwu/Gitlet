package gitlet;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

/**
 * Class to keep track of commits.
 * @author Michael Wu
 */
public class Commit implements Serializable, Dumpable {

    /**
     * Commit message.
     */
    private String _message;
    /**
     * Commit time stamp.
     */
    private Date _timeStamp;
    /**
     * Blob tree.
     */
    private TreeMap<String, String> _tree;
    /**
     * First parent.
     */
    private String _parent;
    /**
     * Second parent.
     */
    private String _mergeParent;
    /**
     * Commit hash.
     */
    private String _hash;

    public Commit(String message, String parent, TreeMap<String, String> tree) {
        _message = message;
        _parent = parent;
        _tree = tree;
        if (_parent == null) {
            _timeStamp = new Date(0);
        } else {
            _timeStamp = new Date();
        }
        _hash = makeHash();
        _mergeParent = "";
    }

    public Commit(String message, String parent, TreeMap<String,
            String> tree, String mergeParent) {

        _message = message;
        _parent = parent;
        _tree = tree;
        if (_parent == null) {
            _timeStamp = new Date(0);
        } else {
            _timeStamp = new Date();
        }
        _hash = makeHash();
        _mergeParent = mergeParent;
    }

    private String makeHash() {
        ArrayList<String> vals = new ArrayList<>();
        vals.addAll(_tree.keySet());
        vals.addAll(_tree.values());
        vals.add(_parent);
        vals.add(_mergeParent);
        vals.add(_timeStamp.toString());
        vals.add(_message);
        return Utils.sha1(Utils.serialize(vals));
    }

    public String getMessage() {
        return _message;
    }

    public String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return sdf.format(_timeStamp);
    }

    public String getParent() {
        return _parent;
    }

    public String getMergeParent() {
        return _mergeParent;
    }

    public TreeMap<String, String> getTree() {
        return _tree;
    }

    public void changeTree(String fileName, String fileContents) {
        if (_tree.containsKey(fileName)) {
            _tree.replace(fileName, fileContents);
        } else {
            _tree.put(fileName, fileContents);
        }
    }

    public void untrack(String fileName) {
        _tree.remove(fileName);
    }

    public String getHash() {
        return _hash;
    }

    @Override
    public void dump() {
        System.out.printf("parent1: %s parent2: %s", _parent, _mergeParent);
    }
}

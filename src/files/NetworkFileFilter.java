package files;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Created on 5:12 29.11.2015
 *
 * @author Bersik
 */
public class NetworkFileFilter extends FileFilter {
    public static final String ntwFile = "ntw";

    @Override
    public boolean accept(File f) {
        if (f.isDirectory())
            return true;

        String extension = getExtension(f);
        if (extension != null)
            if (extension.equals(ntwFile))
                return true;
        return false;
    }

    @Override
    public String getDescription() {
        return "Файли мережі ntw";
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}

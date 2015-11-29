package files;

import java.io.*;

/**
 * Робота з файлом
 * Created on 5:19 29.11.2015
 *
 * @author Bersik
 */
public class FileWorks {
    /**
     * Записати в файл
     *
     * @param file    файл
     * @param network мережа
     * @throws IOException
     */
    public static void writeToFile(File file, Network network) throws IOException {
        String ext = NetworkFileFilter.getExtension(file);

        if ((ext == null) || (!ext.equals(NetworkFileFilter.ntwFile))) {
            file = new File(file.getAbsolutePath() + "." + NetworkFileFilter.ntwFile);
        }
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(network);
        oos.flush();
        oos.close();
    }

    /**
     * /**
     * Зчитати з файлу
     *
     * @param file файл
     * @return мережа
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Network loadOfFile(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream oin = new ObjectInputStream(fis);
        return (Network) oin.readObject();
    }
}

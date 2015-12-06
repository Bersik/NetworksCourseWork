package util;

import javax.swing.*;
import java.awt.*;

/**
 * Created on 6:28 06.12.2015
 *
 * @author Bersik
 */

public class ErrorDialog {
    public static void showErrorDialog(Component parentComponent, String message){
            JOptionPane.showMessageDialog(parentComponent,
                    message, "Помилка", JOptionPane.ERROR_MESSAGE);
    }
}

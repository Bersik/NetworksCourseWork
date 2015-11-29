import view.Realization;

import javax.swing.*;

/**
 * Created on 20:27 26.11.2015
 *
 * @author Bersik
 */

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run()
            {
                try {
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                new Realization().launchFrame();
            }

        });
    }
}

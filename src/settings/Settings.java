package settings;

import java.awt.*;

/**
 * Created on 1:56 27.11.2015
 *
 * @author Bersik
 */

public class Settings {
    public static final int RADIUS_INTERSECT = 30;
    public static final int CIRCLE_RADIUS = 12;
    public static final int RADIUS_ACCURACY = 3 + CIRCLE_RADIUS;

    //Кольори для вузлів
    public static final Color NODE_COLOR = Color.decode("#94FAA5");
    public static final Color NODE_LINK1_COLOR = Color.decode("#25FEAB");
    public static final Color NODE_LINK2_COLOR = Color.decode("#009E62");
    public static final Color NODE_SELECTED_COLOR = Color.decode("#FF4040");

    //Кольори для каналів
    public static final Color LINK_DUPLEX = Color.decode("#2AB5F4");
    public static final Color LINK_HALF_DUPLEX = Color.decode("#FFA80A");
    public static final Color LINK_SELECTED_COLOR = Color.decode("#94FAA5");

    //Колір виключеного елементу
    public static final Color ELEMENT_DISABLE_COLOR = Color.decode("#858585");

    //Колір по замовчуванню
    public static final Color DEFAULT_COLOR = Color.black;

    //Значення параметрів по замовчуванню
    public static final int COUNT_COMMUTATION_NODES = 24;
    public static final int COUNT_SATELLITE_LINKS = 2;
    public static final int DEGREE_NETWORKS = 4;

    public static final int MIN_VALUE_WEIGHT_OF_RANGE = 2;
    public static final int MAX_VALUE_WEIGHT_OF_RANGE = 20;


}

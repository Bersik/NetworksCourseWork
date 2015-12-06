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
/*
    //Кількість вузлів
    public static final int COUNT_COMMUTATION_NODES = 3;
    //Кількість супутникових каналів
    public static final int COUNT_SATELLITE_LINKS = 1;
    //Середній ступінь мережі
    public static final int DEGREE_NETWORKS = 2;
*/


    //Кількість вузлів
    public static final int COUNT_COMMUTATION_NODES = 24;
    //Кількість супутникових каналів
    public static final int COUNT_SATELLITE_LINKS = 2;
    //Середній ступінь мережі
    public static final int DEGREE_NETWORKS = 4;



    //Мінімальне значення ваги каналу
    public static final int MIN_VALUE_WEIGHT_OF_RANGE = 2;
    //Максимальне значення ваги каналу
    public static final int MAX_VALUE_WEIGHT_OF_RANGE = 20;

    //Мінімальне значення довжини буферу
    public static final int MIN_VALUE_LENGTH_OF_BUFFER = 1000;
    //Максимальне значення довжини буферу
    public static final int MAX_VALUE_LENGTH_OF_BUFFER = 3000;


    //---------Communication
    //час оновлення сусідів
    public static final int UPDATE_NEIGHBOR = 2000;
    //скільки часу сусід буде рахуватись підключеним, після прийому від нього HELLO пакету
    public static final int NEIGHBOR_LIVE = UPDATE_NEIGHBOR*2;
    //Максимальна кількість пакетів в каналі в одиницю часу
    public static final int MAX_PACKETS_IN_LINK = 20;
    //Після скількох пакетів відбується переключення напівдуплексу
    public static final int COUNT_CHANGE_DIRECTION_HALF_DUPLEX = 20;
    //Як часто будуть розсилатись LSA пакети при змінах
    public static final int UPDATE_LSA = 100;

    //--------Генератор
    public static final double FREQUENCY= 0.1;
    public static final int MESSAGE_SIZE_FROM = 2000;
    public static final int MESSAGE_SIZE_TO = 5000;

    //максимальний розмір пакету
    public static final int DEFAULT_MAX_PACKET_SIZE = 512;
    public static final int DEFAULT_MESSAGE_SIZE = 1024;


    public static final Color PACKET_COLOR = Color.decode("#F6D779");
    public static final Color PACKET_COLOR_HELLO = Color.decode("#F6D779");
    public static final Color PACKET_COLOR_LSA = Color.decode("#7992F6");
    public static final Color PACKET_COLOR_ACCEPT = Color.decode("#79F683");
    public static final Color PACKET_COLOR_UDP = Color.decode("#1894FF");
    public static final Color PACKET_COLOR_TCP = Color.decode("#FF6918");

    public static final int DELAY_MAX = 1000;
    public static final int DELAY_MIN = 10;
}

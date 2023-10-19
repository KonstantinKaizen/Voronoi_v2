import javax.swing.*;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class MainWindow extends JFrame {

    public static int width = 1600;

    public static int height = 900;

    public MainWindow(){
        setTitle("Voronoi");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(width+20,height+39+1);
        setLocationRelativeTo(null);
        add(new GameField());
        setVisible(true);
        setResizable(false);



    }
    public static void main(String[] args) {
        new MainWindow();

    }
}

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class BendingNoisyLine extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int LINE_LENGTH = WIDTH - 100;
    private static final int LINE_START_X = 10;
    private static final int LINE_START_Y =20;
    private static final int AMPLITUDE = 10;
    private static final int FREQUENCY = 1;
    private static final int NOISE_MAGNITUDE = 5;

    private static int before_y= 1;



    private Random random;

    public BendingNoisyLine() {
        random = new Random();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));

        for (int x = LINE_START_X; x <= LINE_START_X + LINE_LENGTH;) {
            // Calculate the y-coordinate for the wave pattern
            int waveY = (int) (LINE_START_Y + AMPLITUDE * Math.sin(Math.toRadians(x) / FREQUENCY));

            // Add noise to the y-coordinate
            int noise = random.nextInt(NOISE_MAGNITUDE + 1) - NOISE_MAGNITUDE / 2;
            int noisyY = waveY + noise;

            // Draw a point at the noisy y-coordinate
            g2d.setColor(Color.BLACK);
            g2d.drawLine(x, before_y, x+5, noisyY);
            before_y = noisyY;
            x = x+5;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bending Noisy Line");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.add(new BendingNoisyLine());
        frame.setVisible(true);
    }
}

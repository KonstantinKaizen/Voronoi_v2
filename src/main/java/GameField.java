import org.rogach.jopenvoronoi.VoronoiDiagram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GameField extends JPanel implements ActionListener {

    public static boolean is_dragging = false;

    public static int zoom = 1;
    public static int sites_count = 1000;//SITES COUNT -----------------------------

    VoronoiDiagram voronoi_diagram = new VoronoiDiagram();
    int loyd_relaxation_count = 0;



    public GameField(){

        int site_count = sites_count;
        Random random = new Random();
        for (int i = 0; i < site_count; i++) {
            voronoi_diagram.insert_point_site(random.nextInt(MainWindow.width), random.nextInt(MainWindow.height)); //x y

        }
        setBackground(Color.LIGHT_GRAY);
        initGame();
        setFocusable(true);
        requestFocus();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if(e.getButton() == 3){
                    is_dragging = true;
                    System.out.println("click "+e.getX()+" "+e.getY());
                    FillCanvas.clickX = e.getX()-FillCanvas.offsetX*zoom;
                    FillCanvas.clickY = e.getY()-FillCanvas.offsetY*zoom;
                    System.out.println("FillCanvas_click "+e.getX()+" "+e.getY());

                }
            }


            @Override
            public void mouseReleased(MouseEvent e) {
                is_dragging = false;
            }
        });



        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

                    if(is_dragging) {

                        FillCanvas.offsetX =-(FillCanvas.clickX - e.getX())/zoom;
                        FillCanvas.offsetY = -(FillCanvas.clickY - e.getY())/zoom;
                    }


            }

            @Override
            public void mouseMoved(MouseEvent e) {


            }
        });


        addMouseWheelListener(e -> { //--------------------------------------------wheel
            System.out.println(e.getX()+" "+e.getY());
            if(e.getWheelRotation() == -1) {
                if (zoom>1) zoom--;
            }
            else if (e.getWheelRotation() == 1) {
                zoom++;

                FillCanvas.offsetX = -(e.getX()-(e.getX()/zoom));
                FillCanvas.offsetY = -(e.getY()-(e.getY()/zoom));
            }

        });


        addKeyListener(new KeyListener() { // --------------------KEY
            @Override
            public void keyTyped(KeyEvent e) {
                //System.out.println("123");
            }
            /**
             * space - 32
             * z     - 90
             * x     - 88
             * */
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyCode());

                switch (e.getKeyCode()){
                    case 32:
                        int site_count = sites_count;
                        Random random = new Random();
                        voronoi_diagram = new VoronoiDiagram();
                        for (int i = 0; i < site_count; i++) {
                            voronoi_diagram.insert_point_site(random.nextInt(MainWindow.width), random.nextInt(MainWindow.height)); //x y

                        }

                        repaint();
                        break;

                    case 90:
                        System.out.println("z");
                        loyd_relaxation_count++;
                        System.out.println("relaxation count : "+loyd_relaxation_count);
                        voronoi_diagram = FillCanvas.voronoi_relax(voronoi_diagram);
                        repaint();
                        break;
                    case 88:
                        System.out.println("88");
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                //System.out.println("1234");

            }
        });


    }




    @Override
    public void paint(Graphics g) {
        super.paint(g);



        FillCanvas.drawVoronoi(g,voronoi_diagram,zoom);


    }

    private void initGame() {
        Timer timer = new Timer(1, this);
        timer.start();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
        //System.out.println("offset   "+FillCanvas.offsetX+" "+FillCanvas.offsetY);

    }
}

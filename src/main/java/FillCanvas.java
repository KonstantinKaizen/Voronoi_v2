import org.rogach.jopenvoronoi.HalfEdgeDiagram;
import org.rogach.jopenvoronoi.VoronoiDiagram;

import java.awt.*;
import java.util.Random;

public class FillCanvas {

    public static int clickX = 0;
    public static int clickY = 0;
    public static int offsetX = 0;
    public static int offsetY = 0;


    public static int skip_first_3 = 0;
    static int count = 0;
    static int x_average = 0;
    static int y_average = 0;

    public static VoronoiDiagram voronoi_relax(VoronoiDiagram voronoi) {

        VoronoiDiagram voronoi_return = new VoronoiDiagram();
        HalfEdgeDiagram diagram = voronoi.getDiagram();

        voronoi.getFaces().forEach(face -> {
            diagram.face_edges(face).forEach(edge -> {

                count++;
                count++;
                x_average = (int) (x_average+Utility.cut_size_x(edge.source.position.x)+Utility.cut_size_x(edge.target.position.x));
                y_average = (int) (y_average+Utility.cut_size_y(edge.source.position.y)+Utility.cut_size_y(edge.target.position.y));
            });

            skip_first_3++;
            if(skip_first_3<4){
                x_average = 0;
                y_average = 0;
                count = 0;
            } else {

                voronoi_return.insert_point_site(x_average / count, y_average / count);
                x_average = 0;
                y_average = 0;
                count = 0;
            }

        });

        skip_first_3 = 0;
        return voronoi_return;
    }

    public static void drawVoronoi(Graphics g,VoronoiDiagram voronoi,int scale) {
        g.setColor(Color.MAGENTA);
        Font f = new Font("serif", Font.PLAIN, 50);
        g.setFont(f);
        g.drawString("offset  x: "+offsetX+" y : "+offsetY,5,36);
        g.setColor(Color.BLUE);
        g.drawString("click   x: "+clickX+" y : "+clickY,5,75);

        Random random = new Random();
        HalfEdgeDiagram diagram = voronoi.getDiagram();

        voronoi.getFaces().forEach(face -> {
            Color color = new Color(0,random.nextInt(255),random.nextInt(255));
            g.setColor(Color.BLACK);
            g.fillRect((int) (face.site.x()+offsetX)*scale, (int) (face.site.y()+offsetY)*scale,2*scale,2*scale);

            diagram.face_edges(face).forEach(edge -> {

                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(1.0f*scale));

                g2.drawLine((int) (edge.source.position.x+offsetX)*scale, (int) (edge.source.position.y+offsetY)*scale, (int) (edge.target.position.x+offsetX)*scale, (int) (edge.target.position.y+offsetY)*scale);

            });

        });

    }








}

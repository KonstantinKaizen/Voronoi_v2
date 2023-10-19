public class Utility {

    public static double cut_size_x(double x){
        if(x>MainWindow.width-1){
            return MainWindow.width;
        } else if (x<0) {
            return 1;
        } else {
            return x;
        }

    }
    public static double cut_size_y(double y){
        if(y>MainWindow.height){
            return MainWindow.height-1;
        } else if (y<0) {
            return 1;
        } else {
            return y;
        }

    }
}

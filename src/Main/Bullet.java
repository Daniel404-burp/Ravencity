package Main;

import java.awt.*;

public class Bullet {

    public int x, y;
    private int dx, dy;
    public boolean alive = true;

    public Bullet(int x, int y, int dx, int dy){
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    public void update(){

        x += dx * 8;
        y += dy * 8;

        if(x<0 || x>1600 || y<0 || y>600)
            alive = false;
    }

    public void draw(Graphics2D g2){

        g2.setColor(Color.YELLOW);
        g2.fillOval(x, y, 8, 8);
    }

    public Rectangle getBounds(){
        return new Rectangle(x, y, 8, 8);
    }
}
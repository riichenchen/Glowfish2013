import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*; 
import java.awt.geom.*;
import java.io.*; 
import javax.imageio.*;
import java.util.ArrayList;

class Enemy{
	//This class is responsible for representing, moving, and timing enemies throughout the game
	public static final int MOVE=0;				//A moving type enemy
	public static final int SHOOT=1;			//A stationary type enemy who shoots Attacks
	private int x,y;
	private double dir;
	private int type;							//Whether the enemy is MOVE or SHOOT
	private BufferedImage sIcon;				//Icon if the player cannot kill the Enemy
	private BufferedImage wIcon;				//Icon if the player can kill the Enemy
	private int strength;
	private int radius;
	private GamePanel panel;
	private int timeCount;
	
	public Enemy(int ex, int ey, int etype, int estrength, GamePanel p){
		x=ex; y=ey; strength=estrength; type=etype; panel=p;
		radius=8*strength; dir=Math.random()*2*Math.PI;
		if (type==SHOOT)
			dir=0;
		try{
			sIcon=ImageIO.read(new File("senemy"+type+".png"));
			wIcon=ImageIO.read(new File("wenemy"+type+".png"));
		}
		catch (IOException e){};
		timeCount=80;
	}
	
	//Access and Information Methods
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public int type(){
		return type;
	}
	public double getDir(){
		return dir;
	}
	public double strength(){
		return strength;
	}
	public int time(){
		return timeCount;
	}
	public boolean onscreen(int offX, int offY){
		return (0<=x+offX && 500>=x+offX && 0<=y+offY && 300>=y+offY);
	}
	public boolean collideFish(Glowfish g, int offX, int offY){
		//Whether or not the enemy is colliding with a Glowfish
		double dist=Math.hypot(Math.abs(x+offX-g.getX()),Math.abs(y+offY-g.getY()));
		return (g.inDefense()&&(dist<(radius+30))||(dist<(radius+16)));
	}
	
	//Field Control methods
	public void resetPos(int nx,int ny, double ndir){
		x=nx;
		y=ny;
		dir=ndir;
	}
	public void tick(){
		//Adjusts the timer so that the GamePanel knows when to add an Attack
		timeCount--;
		if (timeCount==0)
			timeCount=80;
	}
	//********************************************************************************************

	//Moving Methods
	public void moveTowards(Glowfish g, int offX, int offY){
		//Move towards the center of a Glowfish
		int dx=g.getX(); int dy=g.getY();
		dir=Math.atan2(dy-y-offY,dx-x-offX);
		x=(int)(x+1.5*Math.cos(dir));						//1.5 is a speed, can be changed
		y=(int)(y+1.5*Math.sin(dir));
	}
	public void moveAway(Glowfish g, int offX, int offY){
		//Move away from a Glowfish: this means running in the same direction if 
		//the Glowfish is swimming toward the enemy, and in the opposite direction
		//if the Glowfish is swimming away. This is checked by subtracting the
		//Glowfish's angle from the angle between the Enemy and Glowfish and checking
		//whether or not that is greater than PI/2.
		dir=g.getDir();
		if (Math.abs(dir-Math.atan2(y+offY-g.getY(),x+offX-g.getX()))>Math.PI/2)
			dir+=Math.PI;
		x=(int)(x+1.5*Math.cos(dir));
		y=(int)(y+1.5*Math.sin(dir));
	}

	public void drawWithOffset(Graphics g, int offX, int offY, Glowfish glowfish){
		//Draws, rotates, and scales the Enemy. The size of an enemy is based on its strength.
		Graphics2D g2D = (Graphics2D)g;
		AffineTransform prev=g2D.getTransform();
		AffineTransform at=new AffineTransform();
		at.rotate(dir,x+offX,y+offY);
		g2D.transform(at);
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		if (strength>glowfish.strength())
			g2D.drawImage(sIcon,x+offX,y+offY,2*radius,2*radius,panel);
		else
			g2D.drawImage(wIcon,x+offX,y+offY,2*radius,2*radius,panel);
		g2D.setTransform(prev);
	}


}

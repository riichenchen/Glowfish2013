import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*; 
import java.awt.geom.*;
import java.io.*; 
import javax.imageio.*;
import java.util.ArrayList;

class Friend{
	//Class to represent the individual friends that may be wild or affiliated with a FriendGroup.
	//It is responsible for keeping track of the point location, picture, and orientation, as well
	//as various types of movement methods. 
	private int x, y;
	private double dir;
	private Image icon;
	private GamePanel panel;
	
	//Constructors
	public Friend(int px, int py, int type, GamePanel p){		//Friend for display
		x=px;y=py; dir=Math.random()*2*Math.PI;
		try{
			icon=ImageIO.read(new File("friend"+type+".png"));
		}
		catch(IOException e){};
		panel=p;
	}
	public Friend(double px, double py, double pdir){			//Friend for calculations in FriendGroup
		x=(int)px; y=(int)(py); dir=pdir;
	}
	public Friend(Enemy e, GamePanel p){						//Wild friend made from Enemy
		x=e.getX(); y=e.getY(); dir=e.getDir();
		try{
			icon=ImageIO.read(new File("friend"+e.type()+".png"));
		}
		catch (IOException ex){};
		panel=p;
	}
	
	
	//Access and Information Methods
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public double getDir(){
		return dir;
	}
	public boolean onscreen(int offX, int offY){
		//Used for wild friends who have coordinates based on the entire map system
		return (0<=x+offX && 500>=x+offX && 0<=y+offY && 300>=y+offY);
	}
	
	//Field Specifiers
	public void resetPos(int nx,int ny, double ndir){
		x=nx;
		y=ny;
		dir=ndir;
	}
	public void randomPos(int maxX, int maxY){
		x=(int)(maxX*Math.random());
		y=(int)(maxY*Math.random());
		dir=2*Math.PI*Math.random();
	}
	
	//Calculation methods for various distance measurements
	public double dist(Glowfish g){
		//Distance to a Glowfish
		return Math.hypot (Math.abs(g.getX()-x),Math.abs(g.getY()-y));
	}
	public double dist(Friend f){
		//Distance to another Friend
		return Math.hypot (Math.abs(f.x-x),Math.abs(f.y-y));
	}
	public double dist(double x1,double y1,double x2,double y2){
		//Distance to a point
		return Math.hypot (Math.abs(x1-x2),Math.abs(y1-y2));
	}
	public double dist(Glowfish g, int offX, int offY){
		//Distance, for a wild Friend, to a Glowfish
		return Math.hypot (Math.abs(g.getX()-x-offX),Math.abs(g.getY()-offY-y));
	}
	
	//Field control methods
	public void setPosition(Friend other){
		x=other.x; y=other.y; dir=other.dir;
	}
	public void setPosition(int px, int py, double ang){
		x=px; y=py; dir=ang;
	}
	public void setPosition(double px, double py, double ang){
		x=(int)px; y=(int)py; dir=ang;
	}
	public void setDir(double d){
		dir=d;
	}
	
	//Moving methods
	public void move(Friend dest){
		//Move toward a spot 20 pixels behind another Friend
		double dx=dest.x-20*Math.cos(dest.dir);
		double dy=dest.y-20*Math.sin(dest.dir);
		double tempDir=Math.atan2(dy-y,dx-x);
		x=(int)(x+3*Math.cos(tempDir));
		y=(int)(y+3*Math.sin(tempDir));
	}
	public void move(double dx, double dy){
	//Used exclusively with FriendGroup's shiftGroup function to move in defense mode.
		double tempDir=Math.atan2(dy-y,dx-x);
		x=(int)(x+3*Math.cos(tempDir));
		y=(int)(y+3*Math.sin(tempDir));
//		dir=tempDir;
	}
	public void move(Glowfish g){
		//Move toward a spot 30 pixles behind a Glowfish
		double dx=g.getX()-30*Math.cos(g.getDir());
		double dy=g.getY()-30*Math.sin(g.getDir());
		double tempDir=Math.atan2(dy-y,dx-x);
		x=(int)(x+3*Math.cos(tempDir));
		y=(int)(y+3*Math.sin(tempDir));
	}
	public void moveTowards(Glowfish g, double speed, int offX, int offY){
	//For moving wild Friends that are not part of a FriendGroup towards the Glowfish
		double dx=g.getX();
		double dy=g.getY();
		double tempDir=Math.atan2(dy-y-offY,dx-x-offX);
		x=(int)(x+speed*Math.cos(tempDir));
		y=(int)(y+speed*Math.sin(tempDir));
		dir=tempDir;
	}


	public void draw(Graphics g){
		Graphics2D g2D = (Graphics2D)g;
		AffineTransform prev=g2D.getTransform();
		AffineTransform at=new AffineTransform();
		at.rotate(dir,x,y);
		g2D.transform(at);
		g2D.drawImage(icon,x-8,y-8,panel);
		g2D.setTransform(prev);
	}
	public void drawWithOffset(Graphics g, int offX, int offY){
	//Draws a Friend and moves them based on the offset to maintain relative location with the map
		Graphics2D g2D = (Graphics2D)g;
		AffineTransform prev=g2D.getTransform();
		AffineTransform at=new AffineTransform();
		at.rotate(dir,x+offX,y+offY);
		g2D.transform(at);
		g2D.drawImage(icon,x-8+offX,y-8+offY,panel);
		g2D.setTransform(prev);
	}

}


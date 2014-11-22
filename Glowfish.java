import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*; 
import java.awt.geom.*;
import java.io.*; 
import javax.imageio.*;
import java.util.ArrayList;


class Glowfish{
	//This class represents the player and is responsible for moving and drawing anything affiliated
	//with the player, including the icon and the recruited friends.
	private Image fIcon;
	private int x,y;			//Center of the fish
	private double dir;
	private GamePanel panel;
	private FriendGroup friends;	//The group of friends is represented by a FriendGroup, from which we call all methods	
	
	public Glowfish(int nx,int ny, GamePanel p){
		x=nx; y=ny; dir=0; panel=p;
		friends=new FriendGroup(this,p);
		try{
			fIcon=ImageIO.read(new File("fish1.png"));
		}
		catch (IOException e){};
	}
	
	//Accessor Methods
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public double getDir(){
		return dir;
	}
	
	//FriendGroup Methods: Find out specific information about the Glowfish's FriendGroup
	public boolean inDefense(){
		return friends.inDefense();
	}
	public int strength(){
		return friends.strength();
	}
	public int numFriends(){
		return friends.size();
	}
	
	
	//Field Control Methods- Changing various fields
	public void resetPos(int nx,int ny, double ndir){
		x=nx;
		y=ny;
		dir=ndir;
	}
	public void addFriend(Friend f){
		friends.addFriend(f);
	}
	public Friend removeFriend(){
		return friends.removeFriend();
	}
	public void changeMode(){
		friends.changeMode();
	}
	
	//Drawing
	public void draw(Graphics g){
		Graphics2D g2D = (Graphics2D)g;
		AffineTransform prev=g2D.getTransform();
		AffineTransform at=new AffineTransform();
		at.rotate(dir,x,y);
		g2D.transform(at);
		g2D.drawImage(fIcon,x-16,y-16,panel);
		g2D.setTransform(prev);
		friends.draw(g);
	}
	
	//Moving
	public boolean canMove(BufferedImage buff,int cx, int cy){
		//The Glowfish cannont move into the walls; we check the location on a BufferedImage
		//to determine whether or not a position is valid (white) or invalid (green)
		int c,red,green,blue;
		c = buff.getRGB(x-cx,y-cy);
		red = (c >> 16) & 0xFF;
		green = (c >> 8) & 0xFF;
		blue = c & 0xFF;
		return !(green==255 && red==0 && blue==0);
	}
	
	public void moveF(double mx, double my, double mdir){
		x=(int)(x+mx); y=(int)(y+my);
		dir=mdir;
	}
	public void moveFriends(){
		friends.shiftGroup();
	}
	
	//*********************************************************************************************
	//Makes the necessary adjustment to keep the player within movable bounds and returns a double
	//value for the GamePanel to move the background if necessary
	public double fixX(){
		double out=0;
		if (x>300){
			out=x-300;
			x=300;
		}
		if (x<200){
			out=x-200;
			x=200;
		}
		return out;
	}
	public double fixY(){
		double out=0;
		if (y>200){
			out= y-200;
			y=200;
		}
		if (y<100){
			out= y-100;
			y=100;
		}
		return out;
	}
	//**********************************************************************************************
}
class Attack{
	//This class represents and moves the attack bubbles created by the enemies based on
	//the initial position and direction. Anything involving a specific Attack is handled
	//entirely within the class, therefore there are no specifier methods and only a few
	//specific information methods
	private int x,y;
	private double dir;
	private Image icon;
	private GamePanel panel;
	
	public Attack(int sx, int sy, double sdir, int type, GamePanel p){
		x=sx;y=sy;dir=sdir;panel=p;
		try{
			icon=ImageIO.read(new File("attack"+type+".png"));
		}
		catch (IOException e){};
	}
	
	//Information Methods
	public boolean offscreen(){
	//Attacks are removed once they are offscreen, this method checks for said condition
		return (x>=500 || x<=0||y<=0||y>=300);
	}
	public boolean collide(Glowfish g){
	//Glowfish are damaged when hit by Attacks, this method checks for said condition
		return Math.hypot(Math.abs(x-g.getX()),Math.abs(y-g.getY()))<(15+16);	//radius of Attack + radius of Glowfish
	}
	
	
	public void move(){
		x+=10*Math.cos(dir); y+=10*Math.sin(dir);		//10 is an arbitrary speed; can be changed.
	}
	public void draw(Graphics g){
		g.drawImage(icon,x-15,y-15,panel);
	}
}
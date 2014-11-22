import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*; 
import java.awt.geom.*;
import java.io.*; 
import javax.imageio.*;
import java.util.ArrayList;
import java.util.Scanner;

class GamePanel extends JPanel implements MouseMotionListener, MouseListener{
	//This class is the JPanel where all the gameplay occurs. The class is responsible
	//for loading levels, tracking all Enemies, Friends, and Glowfish, moving the
	//background as necessary, recording score information, and providing feedback 
	//about the game state to the Game class
	private Image bg;
	private BufferedImage buff;
	private int x,y;			//x,y offset of the background
	private int mx, my;			//x,y location of the mouse
	private Glowfish myFish;
	private int houseX, houseY, houseCap;
	private int maxScore;
	private int maxEnemies;
	private int numKilled;
	private ArrayList<Friend> wildFriends;
	private ArrayList<Enemy> enemies;
	private ArrayList<Attack> attacks;
	
	public void loadWild(int n) throws IOException{
		//Wild Friend files consist of an integer N followed by N subsequent lines
		//with three numbers each: the x,y location and the type.
		Scanner br=new Scanner(new BufferedReader(new FileReader("wild"+n+".txt")));
		maxScore=br.nextInt();
		for (int i=0;i<maxScore;i++){
			wildFriends.add(new Friend(br.nextInt(),br.nextInt(),br.nextInt(),this));			
		}
	}
	public void loadEnemies(int n) throws IOException{
		//Wild Friend files consist of an integer N followed by N subsequent lines
		//with 4 numbers each: the x,y location, type, and strength.
		Scanner br=new Scanner(new BufferedReader(new FileReader("enemies"+n+".txt")));
		maxEnemies=br.nextInt();
		maxScore+=maxEnemies;
		for (int i=0;i<maxEnemies;i++){
			enemies.add(new Enemy(br.nextInt(),br.nextInt(),br.nextInt(),br.nextInt(),this));
		}
	}
	public void loadLevel(int n) throws IOException{
		//Level files consist of 5 numbers: the starting offset x,y, the x,y location of the
		//destination house, and the number of Friends one must bring to the house to pass the level
		Scanner read=new Scanner(new BufferedReader(new FileReader("level"+n+".txt")));
		x=read.nextInt(); y=read.nextInt();								//Adjust for various levels
		houseX=read.nextInt(); houseY=read.nextInt(); houseCap=read.nextInt();
	}
	
	public GamePanel(int level){
		super();
		wildFriends=new ArrayList<Friend>();
		enemies=new ArrayList<Enemy>();
		attacks=new ArrayList<Attack>();
		myFish=new Glowfish(250,150,this);
		try{
			bg=ImageIO.read(new File("map"+level+".png"));
			buff=ImageIO.read(new File("mapbuff"+level+".png"));
			loadWild(level);
			loadEnemies(level);
			loadLevel(level);
		}
		catch (IOException e){};
		numKilled=0;
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	public void reset (int level){
		//Reset all fields; essentially re-running the constructor
		wildFriends=new ArrayList<Friend>();
		enemies=new ArrayList<Enemy>();
		attacks=new ArrayList<Attack>();
		myFish=new Glowfish(250,150,this);
		try{
			bg=ImageIO.read(new File("map"+level+".png"));
			buff=ImageIO.read(new File("mapbuff"+level+".png"));
			loadWild(level);
			loadEnemies(level);
			loadLevel(level);
		}
		catch (IOException e){};
		numKilled=0;
	}
	
	//MouseListener and MouseMotionListener required methods
    public void mouseDragged(MouseEvent e){
    	mx=e.getX(); my=e.getY();
    }
    public void mouseMoved(MouseEvent e){
    	mx=e.getX(); my=e.getY();
    }
 	public void mouseClicked(MouseEvent e){
 		myFish.changeMode();
    }
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	
 	//Calculation methods involving the Glowfish
    public double mDist(){
    	//Distance between mouse and Glowfish
    	return Math.hypot((mx-myFish.getX()),(my-myFish.getY()));
    }
    public double calcDir(){
    	//Direction the Glowfish must move in
    	if (mDist()<40)
    		return myFish.getDir();
    	return Math.atan2((my-myFish.getY()),(mx-myFish.getX()));
  	}

	//*****************************************************************************************************
	//Moving and Gameplay methods
    public void moveGF(){
    //Move the glowfish towards the mouse, taking care to not let the Glowfish swim through walls
    	int ox=x;int oy=y;int ofx=myFish.getX();int ofy=myFish.getY(); double odir=myFish.getDir();		//Old values
    	double dir=calcDir();
    	if (mDist()>30){
	    	myFish.moveF(5*Math.cos(dir),5*Math.sin(dir),dir);
	    	double changeX=myFish.fixX();
	    	double changeY=myFish.fixY();
	    	moveBG(changeX,changeY);
   		}
   		if (myFish.canMove(buff,x,y)==false){			//If the fish hits a wall
			x=ox; y=oy;									//reset all the old values
			myFish.resetPos(ofx,ofy,odir);
			if (myFish.inDefense() && myFish.strength()>0){
				Friend f=myFish.removeFriend();			//Randomly relocate lost friends
				f.randomPos(buff.getWidth(),buff.getHeight());
    			wildFriends.add(f);
			}
		}
    	myFish.moveFriends();		
    }
    public void moveBG(double changeX, double changeY){
    	//Move the backgroun based on the shift values returned by fixX and fixY without
    	//moving past the boundaries of the Image
    	if (changeX<0)
    		x=(int)Math.min(x-changeX,0);						//Move to the right
    	else
    		x=(int)Math.max(500-bg.getWidth(this),x-changeX);	//Move to the left
    	if (changeY<0)
    		y=(int)Math.min(y-changeY,0);     					//Move down
    	else
    		y=(int)Math.max(300-bg.getHeight(this),y-changeY); 	//Move up
    }
    
    public void moveWildFriends(){
    //Move wild Friends and add them to the Glowfish's FriendGroup if necessary
    	ArrayList<Friend> toRemove=new ArrayList<Friend>();
    	for (Friend f:wildFriends){
    		int ox=f.getX();int oy=f.getY();
    		if (f.onscreen(x,y)){
    			f.moveTowards(myFish,0.1,x,y);			//Slowly move toward the Glowfish if onscreen
    			if (f.getX()<0||f.getX()>buff.getWidth(this)||f.getY()<0||f.getY()>buff.getHeight(this))
    				f.resetPos(ox,oy,f.getDir());		//Reset to a random location if the friend swims offscreen
    			if (f.dist(myFish,x,y)<20){
    				toRemove.add(f);
    				myFish.addFriend(f);
    			}
    		}
    	}
    	for (Friend f: toRemove)
    		wildFriends.remove(f);
    }
    public int moveAttacks(){
    	//Move all onscreen attacks, remove any that move offscreen, and return a value
    	//signalling whether the player is killed by the attack or not
    	ArrayList<Attack> toRemove=new ArrayList<Attack>();
    	for (Attack a: attacks){
    		a.move();
    		if (a.collide(myFish)){
    			if (myFish.strength()<0){
    				return Game.PLAYER_LOSES;
    			}
    			toRemove.add(a);
    			Friend f=myFish.removeFriend();
    			f.randomPos(buff.getWidth(),buff.getHeight());
    			wildFriends.add(f);
    		}
    		if (a.offscreen()){
    			toRemove.add(a);
    		}
    	}
    	for (Attack a:toRemove){
    		attacks.remove(a);
    	}
    	return Game.KEEP_PLAYING;
    }
    public void addAttack(Enemy e){
    	double aDir=Math.atan2(myFish.getY()-e.getY()-y,myFish.getX()-e.getX()-x);	//Move toward Glowfish
    	attacks.add(new Attack(e.getX()+x,e.getY()+y,aDir,0,this));
    }
    public int moveEnemies(){
    	//Move all enemies, add attacks if the situation calls for attacking, check for
    	//collisions between the player and the enemy, and remove enemies or return feedback
    	//to Game as necessary
    	ArrayList<Enemy> toRemove=new ArrayList<Enemy>();
    	for (Enemy e: enemies){
    		e.tick();
    		if (e.time()==80 && e.type()==Enemy.SHOOT)		//Shoot once every 2 seconds
    			addAttack(e);
    		if (e.onscreen(x,y) && e.collideFish(myFish,x,y)){
    			if (myFish.strength()>=e.strength()){		//Enemy dies
    				toRemove.add(e);
    				wildFriends.add(new Friend(e,this));
    				numKilled++;
    			}
    			else if (myFish.strength()<0)				//Player dies
    				return Game.PLAYER_LOSES;
    			else{										//Player loses a friend
    				Friend f=myFish.removeFriend();
    				f.randomPos(buff.getWidth(),buff.getHeight());
   					wildFriends.add(f);
    				}
    		}
    		else if (e.onscreen(x, y) && e.type()==Enemy.MOVE){
				if (e.strength()>myFish.strength())
					e.moveTowards(myFish,x,y);
				else
					e.moveAway(myFish,x,y);
				if (e.getX()<0||e.getX()>buff.getWidth(this)||e.getY()<0||e.getY()>buff.getHeight(this))  //Randomly
    				e.resetPos(100+(int)((buff.getWidth(this)-200)*Math.random()),						  //relocate
    				(int)(100+(buff.getHeight(this)-200)*Math.random()),2*Math.PI*Math.random());		  //if offscreen
			}
    	}
    	for (Enemy e:toRemove)
    		enemies.remove(e);
    	return Game.KEEP_PLAYING;
    }
    public int inHouse(){
    	//Check if a person has reached the destination house and if so, whether or not they have collected
    	//enough friends and gives feedback to Game.
    	if (Math.hypot(Math.abs(myFish.getX()-houseX-x),Math.abs(myFish.getY()-houseY-y))<142
    		&& myFish.numFriends()>=houseCap)
    			return Game.PLAYER_WINS;
    	return Game.KEEP_PLAYING;
    }
    //*******************************************************************************************************

	public String score(){
		//Returns a string representation of the number of friends recovered and enemies killed
		return String.format("Friends recovered: %d/%d\nEnemies Killed: %d/%d\n",
								myFish.numFriends(),maxScore,numKilled,maxEnemies);
	}
 
    public void paintComponent(Graphics g){ 	
		g.drawImage(bg,x,y,this);  
		myFish.draw(g);
		for (Friend f: wildFriends)
    		f.drawWithOffset(g,x,y);
		for (Enemy e: enemies)
    		e.drawWithOffset(g,x,y,myFish);
    	for(Attack a: attacks)
    		a.draw(g);
	}
}
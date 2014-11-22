/**
 * Game.java
 *
 * Chen~
 * ICS4U Simple Game Project: Glowfish
 *
 * The set of classes Game, GamePanel, Glowfish, FriendGroup, Friend, Enemy, and Attack allow the user
 * to play a simple game of Glowfish, where the player is a Glowfish and tries to recruit Friends and 
 * return them to the house at the end of each level. When the player has recruited a number of Friends,
 * he can enter "Defense Mode" where he will be able to kill weaker Enemies by hitting them. Enemies
 * become friends upon death and join the player.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*; 
import java.awt.geom.*;
import java.io.*; 
import javax.imageio.*;
import java.util.ArrayList;
import java.util.Scanner;

//TO DO: FIGURE OUT WHY SOME FRIENDS AREN'T SHOWING UP.

//This class is the main JFrame that triggers and controls the gameplay. Events are triggered
//using a timer at 25ms intervals. It controls level progression and flow, time, and score.
public class Game extends JFrame implements ActionListener{
	public static final int KEEP_PLAYING=0;
	public static final int PLAYER_LOSES=1;
	public static final int PLAYER_WINS=2;
	private int levelNumber;
	private GamePanel game;
	private Timer myTimer;
	private int running;
	private int time;
	public Game(){
		super("Glowfish");
		setSize(500,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myTimer=new Timer(25,this);
		running=KEEP_PLAYING;
		levelNumber=0;
		game=new GamePanel(levelNumber);
		add(game);
		setVisible(true);
		myTimer.start();
		time=0;
	}
	
	//********************************************************************************************************************
	//These two methods check, at each call of actionPerformed, whether or not the game is over
	//(based on a value returned by the GamePanel) and displays the appropriate option panes to
	//determine what will happen to the game from that point on.
	public void checkPlayerWin(){
		if (running==PLAYER_WINS){
    		String message=String.format("%sTime: %d'%d\"\nNext Level?",game.score(),time/60000,(time%60000)/1000);
    		int c=JOptionPane.showConfirmDialog(this,message,"LEVEL CLEARED!",2);
    		levelNumber++;
    		if (c==0 && levelNumber<=1){			//1 is the number of levels for which maps exist.
    			running=KEEP_PLAYING;
    			game.reset(levelNumber);
    			time=0;
    		}
    		else
    			System.exit(0);
    	}
	}
	public boolean checkPlayerLoss(){
		if (running==PLAYER_LOSES){
    		String message=String.format("%sTime: %d'%d\"\nTry Again?",game.score(),time/60000,(time%60000)/1000);
    		int c=JOptionPane.showConfirmDialog(this,message,"LEVEL FAILED!",2);
    		if (c==0){
  				running=KEEP_PLAYING;
    			game.reset(levelNumber);
    			time=0;
    		}
    		else
    			System.exit(0);
    		return true;    		
    	}
    	return false;
	}
	//******************************************************************************************************************
	
	public void actionPerformed(ActionEvent e){
	//This method runs the main game loop; moving the various aspects of the GamePanel and
	//updating the running field based on output from the GamePanel.
		game.moveGF();
		game.moveWildFriends();
		running=game.moveEnemies();
		if (checkPlayerLoss()==false)		//If they have already lost, don't override with a second check
			running=game.moveAttacks();
		if (checkPlayerLoss()==false){		
			running=game.inHouse();
			checkPlayerWin();
		}
		repaint();
		time+=25;
	}
	

    public static void main(String[] args){
    	Game play=new Game();
    }
}






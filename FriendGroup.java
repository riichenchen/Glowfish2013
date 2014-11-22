import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*; 
import java.awt.geom.*;
import java.io.*; 
import javax.imageio.*;
import java.util.ArrayList;

class FriendGroup{
	/*This class is responsible for representing the group of friends partnered with the 
	  player's Glowfish. It handles all movement and display of the group depending on
	  which mode the player is in.
	  
	  Calculation methods: 
	  	In streamline mode, the first Friend in the group swims toward the location 30 pixels 
	  	directly behind the master Glowfish and each subsequent friend swims toward the location 
	  	15 pixels directly behind the Friend before.
	  	
	  	In defense mode, the Friends are spaced out evenly around the Glowfish (at angle intervals
	  	of 2PI/n, where n is the number of Friends in the group) and the entire circle is shifted
	  	by a separate angle measurement that goes from 0 to 2PI as the group moves.
	  
	*/

	public static final int STRMLN=0;	//Streamline: Fish follow behind you, no ability to kill enemies
	public static final int DEF=1;		//Defense: Fish circle around you, provides the ability to kill 
										//enemies, but will lose friends if one collides with a wall.
	
	private double defAng;				//How much the fish in defense mode are turned about the x axis
	private int status;					//Whether the group is in STRMLN or DEF mode
	private ArrayList<Friend> members;
	private Glowfish master;
	private GamePanel panel;
	
	public FriendGroup(Glowfish fish, GamePanel p){
		members=new ArrayList<Friend>();
		status=STRMLN;
		defAng=0;
		master=fish;
		panel=p;
	}
	
	//Information Methods
	public int strength(){
		if (members.size()==0)
			return -1;			//-1 is a state where the player has no friends and will die if hit by an enemy
		if (status==STRMLN){
			return 0;
		}
		return members.size();
	}
	public boolean inDefense(){
		return status==DEF;
	}
	public int size(){
		return members.size();
	}
	
	//**********************************************************************************
	//Methods use to compute the next position in streamline mode
	public Friend calcHead(){
		//Calculate the location of the first fish in the group as detailed in Calculation Methods
		double masterAng=master.getDir();
		return new Friend(master.getX()-30*Math.cos(masterAng),master.getY()-30*Math.sin(masterAng),masterAng);
		
	}
	public Friend calcNew(){
		//Calculate the location of subsequent fish as detailed in Calculation Methods
		if (members.size()==0){
			return calcHead();
		}
		Friend prev=members.get(members.size()-1);
		double ang=prev.getDir();
		return new Friend(prev.getX()-15*Math.cos(ang),prev.getY()-15*Math.sin(ang),ang);
	}
	//**********************************************************************************
	
	//Field control methods
	public void changeMode(){
		//Performs the necessary calculations and adjustments to switch modes
		if (status==STRMLN && members.size()>0){		//No switching to defense without friends
			int mx=master.getX(); int my=master.getY();
			for (int i=0;i<members.size();i++){
				members.get(i).setPosition(mx+30*Math.cos(Math.PI*i*2/members.size()),
				my+30*Math.sin(Math.PI*i*2/members.size()),Math.PI*i*2/members.size()+Math.PI/2);
			}
			status=DEF;
		}
		else if (members.size()>0){
			members.get(0).setPosition(calcHead());
			for (int i=1;i<members.size();i++){
				members.get(i).setPosition(calcNew());
			}
			status=STRMLN;
		}
	}
	public void shiftGroup(){
		//Makes the necessary adjustments to the position of the group members based on
		//an updated location of the master Glowfish
		if (members.size()>0){
			if (status==STRMLN){
				members.get(0).move(master);				//Number 1 moves toward the master
				if (members.get(0).dist(master)>25)
					members.get(0).setDir(master.getDir());		//Change direction if far enough
				for (int i=1;i<members.size();i++){
						members.get(i).move(members.get(i-1));		//Subsequent fish move towards the fish ahead
					if (members.get(i).dist(members.get(i-1))>20)
						members.get(i).setDir(members.get(i-1).getDir());
				}
			}
			else{
				int mx=master.getX(); int my=master.getY();
				for (int i=0;i<members.size();i++){
					members.get(i).setPosition(mx+30*Math.cos(defAng+Math.PI*i*2/members.size()),	//Adjust the x,y location
					my+30*Math.sin(defAng+Math.PI*i*2/members.size()),defAng+Math.PI*i*2/members.size()+Math.PI/2);
					defAng+=Math.PI/50;
					if (defAng>Math.PI*2)
						defAng-=Math.PI*2;
					members.get(i).move(mx+30*Math.cos(defAng+Math.PI*i*2/members.size()),			//Make a rotation to 
					my+30*Math.sin(defAng+Math.PI*i*2/members.size()));								//emulate circling
					members.get(i).setDir(defAng+ Math.PI*i*2/members.size()+Math.PI/2);
				}		
			}
		}
	}
	//These two methods allow for the FriendGroup to change the number of members. No recalcuations
	//are necessary as all recalculation will be done at next call of shiftGroup
	public Friend removeFriend(){
		Friend out=members.get(members.size()-1);
		members.remove(members.size()-1);
		return out;
	}
	public void addFriend(Friend f){
//		f.setPosition(calcNew());
		members.add(f);
	}
	
	
	public void draw(Graphics g){
		for (Friend f:members){
			f.draw(g);
		}
	}
}
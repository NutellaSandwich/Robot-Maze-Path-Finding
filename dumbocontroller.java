/*
* File: DumboController.java
* Created: 17 September 2002, 00:34
* Author: Stephen Jarvis
*/


/* The fair probabilities issue came up simply because the round function would round the
number by adding 1/2 to the number that taking the floor and making it an integer which
would produce some values more than others, so I simply changed the round function to floor 
which produces all values with an equal chance.

Incorporating the 1-8 chance was not difficult as I simply produce a new random number 
before the while loop then added an or statement into the while loop so there was a 1 in 8
chance the while loop would 'trigger' regardless of whether the robot was facing a wall or 
not.  
*/

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class DumboController
{

	public void controlRobot(IRobot robot) {

		int randno;
		int direction;
		
		// Creating variables for the number of walls surrounding the robot and the type of square it is on
		
		int walls;
		String status = "";
		
		
		// Setting Placeholder value for direction
		
		direction = IRobot.AHEAD;
		
		// Working out if a random direction will be generated (1/8 chance)
		randno = (int) Math.floor(Math.random()*8);
			
		// Look in the set direction and if there is a wall, randomly set a new direction until you are not facing a wall
			
		while ((robot.look(direction) == IRobot.WALL) || randno == 7){
			
			// Select a random number

			randno = (int) Math.floor(Math.random()*4);

			// Convert this to a direction
		
			if (randno == 0)
			direction = IRobot.LEFT;
			else if (randno == 1)
			direction = IRobot.RIGHT;
			else if (randno == 2)
			direction = IRobot.BEHIND;
			else 
			direction = IRobot.AHEAD;
		}
		
		
			
		// Initialise walls outside the while loop
		
		walls = 0;	
		
		robot.face(direction); /* Face the robot in this direction */ 
		
		//Look in every direction and tally up how many walls are surrounding the robot in the current position
		
		if (robot.look(IRobot.AHEAD) == IRobot.WALL){
			walls++;
		}
		if (robot.look(IRobot.BEHIND) == IRobot.WALL){
			walls++;
		}
		if (robot.look(IRobot.LEFT) == IRobot.WALL){
			walls++;
		}
		if (robot.look(IRobot.RIGHT) == IRobot.WALL){
			walls++;
		}
		
		//Use the number of walls surrounding the robot to decide what type of square it is on and assign a string describing it to status
		
		if (walls == 0){
			status = " at a crossroads";
		} else if (walls == 1) {
			status = " at a junction";
		} else if (walls == 2) {
			status = " down a corridor";
		} else if (walls == 3) {
			status = " at a deadend";
		}
				
		
		//Report the direction the robot is going by using the integer value stored in direction
		
		switch(direction) {
			case 2000: 
				System.out.println("I'm going forward" + status);
				break;
			case 2001:
				System.out.println("I'm going right" + status);
				break;
			case 2002:
				System.out.println("I'm going backwards" + status);
				break;
			case 2003:
				System.out.println("I'm going left " + status);
				break;
		}

	}

}
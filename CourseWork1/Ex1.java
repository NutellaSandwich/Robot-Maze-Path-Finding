/* I solved the collision problem by simply putting a while loop around the entire process
   that decides which direction the robot is facing. The while loops states that it should
   keep looping an generating a new direction until the robot.look function no longer 
   outputs that it sees a wall. I used a while loop as I believe it is the shortest and 
   most efective way to achieve the desired outcome. */

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex1
{

	public void controlRobot(IRobot robot) {

		int randno;
		int direction;
		
		// Creating variables for the number of walls surrounding the robot and the type of square it is on
		
		int walls;
		String status = "";
		
		
		// Setting Placeholder value for direction
		
		direction = IRobot.LEFT;
		
		// Look in the set direction and if there is a wall, randomly set a new direction until you are not facing a wall
		
		
		do{	
			// Select a random number

			randno = (int) Math.round(Math.random()*3);

			// Convert this to a direction
		
			if (randno == 0)
			direction = IRobot.LEFT;
			else if (randno == 1)
			direction = IRobot.RIGHT;
			else if (randno == 2)
			direction = IRobot.BEHIND;
			else 
			direction = IRobot.AHEAD;
		} while (robot.look(direction) == IRobot.WALL); // Look in the set direction and if there is a wall, randomly set a new direction until you are not facing a wall
				

		 
		
		// Initialise walls outside the while loop
		
		walls = 0;	
		
		robot.face(direction); /* Face the robot in this direction */ 
		
		// Look in every direction and tally up how many walls are surrounding the robot in the current position
		
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
		
		// Use the number of walls surrounding the robot to decide what type of square it is on and assign a string describing it to status
		
		if (walls == 0){
			status = " at a crossroads";
		} else if (walls == 1) {
			status = " at a junction";
		} else if (walls == 2) {
			status = " down a corridor";
		} else if (walls == 3) {
			status = " at a deadend";
		}
				
		
		// Report the direction the robot is going by using the integer value stored in direction
		
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
				System.out.println("I'm going left" + status);
				break;
		}

	}

}
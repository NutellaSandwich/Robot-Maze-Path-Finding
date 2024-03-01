/*
 * File:    Broken	.java
 * Created: 7 September 2001
 * Author:  Stephen Jarvis
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Broken 

{
  private byte isTargetNorth(IRobot robot) {
    byte result;
    if (robot.getLocation().y > robot.getTargetLocation().y){
        result = 1;
    } 
    else if (robot.getLocation().y < robot.getTargetLocation().y){
        result = -1;
    }else{
    result = 0;
    }
    return result;
  }

  private byte isTargetEast(IRobot robot) { 
    byte result; 
    if (robot.getLocation().x > robot.getTargetLocation().x){
        result = -1;
    } 
    else if(robot.getLocation().x < robot.getTargetLocation().x){
    	result = 1;
    }else{
        result = 0;
    }
    return result;
  }
  
  
  
  private int lookHeading(IRobot robot, int heading) {
  	int value;
  	value = heading - robot.getHeading();
  	if (value < 0){
  		value +=4;
  	}

  	return robot.look(IRobot.AHEAD + value);
  	
  }
  
    private int randomgen(IRobot robot) {
      int randno;
      int direction; 
      
      direction = 0;
      
      do{

       randno = (int) Math.floor(Math.random()*4);

       if ( randno == 0)
            direction = IRobot.WEST;
       else if (randno == 1)
            direction = IRobot.EAST;
       else if (randno == 2)
            direction = IRobot.SOUTH;
       else 
            direction = IRobot.NORTH;
    	} while (lookHeading(robot,direction)==IRobot.WALL);
   	return direction; 
  	}
  
  
  private int headingController(IRobot robot) {
  	
  	int direction;
  	int wallstatus;
  	int heading;
  	int randnum;
  	
  	heading = 0;
  	randnum = 0;
	do {
	
  	if ((isTargetNorth(robot) == -1) & (isTargetEast(robot) == -1)){
  		randnum = (int) Math.floor(Math.random()*2);
  		if (lookHeading(robot, IRobot.SOUTH) != IRobot.WALL & randnum == 0  ){
  			heading = IRobot.SOUTH; 
  		}
  		else if (lookHeading(robot, IRobot.WEST) != IRobot.WALL & randnum == 1 ){
  			heading = IRobot.WEST;
  		}
  		else if (lookHeading(robot, IRobot.WEST) == IRobot.WALL & lookHeading(robot, IRobot.SOUTH) == IRobot.WALL){
  		 heading = randomgen(robot);
  		}
  	}

  	else if ((isTargetNorth(robot) == -1) & (isTargetEast(robot) == 1)){
  		randnum = (int) Math.floor(Math.random()*2);
  		if (lookHeading(robot, IRobot.SOUTH) != IRobot.WALL & randnum == 0  ){
  			heading = IRobot.SOUTH; 
  		}
  		else if (lookHeading(robot, IRobot.EAST) != IRobot.WALL & randnum == 1  ){
  			heading = IRobot.EAST;
  		}
  		else if (lookHeading(robot, IRobot.EAST) == IRobot.WALL & lookHeading(robot, IRobot.SOUTH) == IRobot.WALL){
  		 heading = randomgen(robot);
  		}
  	}
  			
  	else if ((isTargetNorth(robot) == 1) & (isTargetEast(robot) == -1)){
  		randnum = (int) Math.floor(Math.random()*2);
  		if (lookHeading(robot, IRobot.NORTH) != IRobot.WALL & randnum == 0  ){
  			heading = IRobot.NORTH; 
  		}
  		else if (lookHeading(robot, IRobot.WEST) != IRobot.WALL & randnum == 1  ){
  			heading = IRobot.WEST;
  		}
  		else if (lookHeading(robot, IRobot.WEST) == IRobot.WALL & lookHeading(robot, IRobot.NORTH) == IRobot.WALL){
  		 heading = randomgen(robot);
  		}
  	}
  		
  	else if ((isTargetNorth(robot) == 1) & (isTargetEast(robot) == 1)){
  		randnum = (int) Math.floor(Math.random()*2);
  		if (lookHeading(robot, IRobot.NORTH) != IRobot.WALL & randnum == 0 ){
  			heading = IRobot.NORTH; 
  		}
  		else if (lookHeading(robot, IRobot.EAST) != IRobot.WALL & randnum == 1  ){
  			heading = IRobot.EAST;
  		}
  		else if (lookHeading(robot, IRobot.EAST) == IRobot.WALL & lookHeading(robot, IRobot.NORTH) == IRobot.WALL){
  		 heading = randomgen(robot);
  		}
  	}
  	
  	else if (isTargetNorth(robot) == 0 | isTargetEast(robot) == 0){
  		if (isTargetNorth(robot) == 0){
  			if (isTargetEast(robot) == -1 & lookHeading(robot, IRobot.WEST) != IRobot.WALL){
  				heading = IRobot.WEST;
  			}
  			else if (isTargetEast(robot) == 1 & lookHeading(robot, IRobot.EAST) != IRobot.WALL){
  				heading = IRobot.EAST;
  			}else{
  			heading = randomgen(robot);
  			}
  		}
  		else if (isTargetEast(robot) == 0){
  			if (isTargetNorth(robot) == -1 & lookHeading(robot, IRobot.SOUTH) != IRobot.WALL){
  				heading = IRobot.SOUTH;
  			}
  			else if (isTargetNorth(robot) == 1 & lookHeading(robot, IRobot.NORTH) != IRobot.WALL){
  				heading = IRobot.NORTH;
  			}else{
  			heading = randomgen(robot);
  			}
  		}
  	 }
  	
  	} while (heading == 0);

	return heading;
  }
  			 
  
  public void controlRobot(IRobot robot) {

    	int heading;	
	
		heading = headingController(robot); 
    	ControlTest.test(heading, robot);
 		robot.setHeading(heading);  /* Face the direction */   
 		
  }
  
  public void reset(){
  	ControlTest.printResults();
  }
}

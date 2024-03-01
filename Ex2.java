import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/* The implementation of this was quite simple as you know that if a junction is revisted it must
be the most recent new array as the only time a junction would be revisited is after the robot
turns around after a dead end. Therefore i simply edited the search junction function so that it 
would essentially store the headings as a stack. I used an ArrayList instead of the stack implementation
as there is very little difference and they work in the same way and it just requires less editing to the 
progream. The program will obtain the value stored in the most recent index and put it through the 
reversal algorithm to figure out the direction it should go. 

This saves space as rather than storing the x,y and heading values for each junction you only need to
store the heading, clearly saving space as you are storing 1/3 of the data. It also saves space as the 
headings are removed from the stack so data is actually freed up.  It is also helpful as the method to 
obtain the heading for the revisted junction is very simple so it is much easier to understand and since
it doesnt have to check every junctions x and y values it is more efficient too. 

This is easy to test as you can just use the exact methods for testing stated in ex1. 
*/
public class Ex2 {
	ArrayList<Integer> juncs; //arraylist for junction recorder
	private int pollRun = 0; //how many times the robot has moved
	private RobotData robotData; //create new store of data
	private int junctionCounter; //counting junctions encountered
	private int explorerMode; //whether explore or backtrack mode should be active
	RobotData obj = new RobotData(); //create new instance of class

	private int nonwallExits (IRobot robot) { //look in every direction and count how many non walls there are
		int nonwalls; //num of nonwalls
		nonwalls = 0;

		for (int x = 0; x<4; x++) {
			if (robot.look(IRobot.AHEAD + x) != IRobot.WALL){
				nonwalls++;
			}
		}
		return nonwalls;
	}

	private int beenbeforeExits (IRobot robot) { //look in every direction and count how many beenbefore exits there are
		int beenBefore; //num of beenbefore squares
		beenBefore = 0;

		for (int x = 0; x<4; x++) {
			if (robot.look(IRobot.AHEAD + x) == IRobot.BEENBEFORE) {
				beenBefore++;
			}
		}
		return beenBefore;
	}

	private int passageExits (IRobot robot) { //look in every direction and count how many passage exits there are
		int passages; //num of passage squares
		passages = 0;

		for (int x = 0; x<4; x++) {
			if (robot.look(IRobot.AHEAD + x) == IRobot.PASSAGE) {
				passages++;
			}
		}
		return passages;
	}

	private int deadEnd (IRobot robot) { //look in every direction to find the non wall exit to leave the deadend 
		int directionDead; //direction of non deadend
		directionDead = 0;
		for (int x = 0; x<4; x++) {
			if (robot.look(IRobot.AHEAD + x) != IRobot.WALL){
				directionDead = IRobot.AHEAD + x;
			}
		}
		return directionDead;
	}

	private int corridors (IRobot robot) { //if there is no wall ahead go ahead, if there is check if left or right has no wall
		int corridorDirection; //direction to go down corridor
		corridorDirection = 0;

		if (robot.look(IRobot.AHEAD) != IRobot.WALL) {
			corridorDirection = IRobot.AHEAD;
		}
		else if (robot.look(IRobot.LEFT) != IRobot.WALL) {
			corridorDirection = IRobot.LEFT;
		} else {
			corridorDirection = IRobot.RIGHT;
		}
		return corridorDirection;
	}

	private int junctionsCrossroads (IRobot robot) { //create array to store state of each wall, if theres passages choose them randomly, if not enter beacktrack
		int[] nonwalls = {0,0,0,0}; //array for square states for each direction around the robot
		int randno; //random num
		boolean flag; //flag to indicate if a passage is in the array
		flag = false;

		for (int x = 0; x<4; x++) {
			if (robot.look(IRobot.AHEAD + x) != IRobot.WALL) {
				nonwalls[x] = IRobot.AHEAD + x;
				if (robot.look(IRobot.AHEAD + x) == IRobot.PASSAGE){
					flag = true;
				}
			}	
		}
		if (flag == true) {
			do {
				randno = (int) Math.floor(Math.random()*4);
			} while (nonwalls [randno] == 0 || robot.look(nonwalls[randno]) != IRobot.PASSAGE);
		} else {
			explorerMode = 0;//if there is no passage enter backtrack mode
			return 0;
		}
		return nonwalls[randno];
	}

	public void reset() { //method to reset junctioncounter, also clears arraylist that stores junctions
		robotData.resetJunctionCounter();
		juncs.clear();
		pollRun = 0;
	}

	private void explorerControl(IRobot robot){ 

		int exits; //number of exits
		int direction; //direction the robot should go

		direction = 0;

		exits = nonwallExits(robot);

		explorerMode = 1; 

		if (exits == 1){
			direction = deadEnd(robot);
			if (pollRun != 0){
				explorerMode = 0;	
			}

		}
		else if (exits == 2) {
			direction = corridors(robot);
		}
		else if (exits >2 ){
			if (junctionsCrossroads(robot) == 0){ //when no passages are found enter backtrack
				backtrackControl(robot);
				return;
			} else{
				direction = junctionsCrossroads(robot);
			}
			if (beenbeforeExits(robot) <= 1){ //when new junction encountered add it to counter and create new junctionrecorder object
				junctionCounter++;
				obj.junctionRecorder(robot);
			}
		}
		robot.face(direction);
	}

	private void backtrackControl(IRobot robot){
		int exits; //num of exits
		int direction; //robot direction
		exits = nonwallExits(robot);
		direction = 0;

		if (exits > 2){
			if (passageExits(robot) > 0){ //if there are passagexits it shouldnt be in backtrack
				explorerMode = 1;
			}else{
					direction = obj.searchJunction(); //call the searchjunction to find where to go
				if (direction == IRobot.WEST | direction == IRobot.SOUTH){ //reverses direction
					direction = direction - 2;
				}else{
					direction = direction + 2;
				}
				robot.setHeading(direction);
			}
		}else{
			explorerControl(robot); //if junction not found go to explorercontrol - should never be needed but is there just in case 
		}
	}

	public void controlRobot(IRobot robot) {

		if ((robot.getRuns() == 0) && (pollRun == 0)){ //make sure at the start the new RobotData is created and it is on explore mode
			robotData = new RobotData();
			explorerMode = 1;
		}

		if (pollRun == 0){ //call function to create the arraylist
			obj.createD();
		}
		if (explorerMode == 1){ //setting the mode of the robot
			explorerControl(robot);
		}
		else{
			backtrackControl(robot);
		}
		pollRun++;
	}	

	class RobotData {
		
		private void createD(){ //creating the new, empty arraylist
			juncs = new ArrayList<Integer>();
		}

		private void junctionRecorder(IRobot robot){
			juncs.add(robot.getHeading()); //adding only the heading as an element of the arraylist
			printJunction(robot);//show junction recording is happening properly
		}

		private void printJunction(IRobot robot) {//method to ensure junctions are recorded properly
			System.out.println("Junction " + junctionCounter + "(x="+robot.getLocation().x + ",y="+robot.getLocation().y + ") integer heading value " + robot.getHeading());
		}

		private void resetJunctionCounter() {
			junctionCounter = 0;
		}

		private int searchJunction(){//essentially popping method for 'stack'
			int index = juncs.size() - 1; //get the last index of the arraylist
			int head = juncs.get(index); //get the heading stored there
			juncs.remove(index); //remove it from the arraylist 
			return head;
		}
	}
}













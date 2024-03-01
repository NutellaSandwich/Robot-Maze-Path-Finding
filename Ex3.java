import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
import java.util.Arrays;

/* The reason that ex1 and 2's design does not work for loopy mazes is that in a prim maze the robot 
never will encounter a beenbefore square while in explore mode as this is how the robot will
know it is stuck in a loop so to deal with this. For ex3 i created an if statement to check 
each time the robot is in explore mode, whether the calculated direction will end up leading it
into a beenbefore square. I dealt with this by turning the robot round if this was about to happen, 
and put it in backtrack mode essentially creating a virtual wall and deadend. Another change was to
deal with when the robot is surrounded by beenbefore squares but has never actually been on the square
it is on. This will never happen in a prim maze so didnt need to be handled but it is dealt with by storing
a new juction record. I did this from ex1 as although a little more data is stored it also increases the 
reliability of the robot as you can ensure the robot does the correct actions at the correct junctions 
whereas in ex2 it relies on the prim maze condition that when a deadEnd is found it will return straight 
to the most previously recorded junction whereas this cannot be ensured in a loopy maze. In order to 
accomplish this you also have to record every time a junction is visited and not just when it is the 
first time you have visitied it as recording it multiple times will ensure you know how to leave it.
However you also then need to remove the previous recording of this junction as if the junction has been
visited again it means that the initial heading the robot took through the junction was incorrect so it no
longer needs to be there and now the new recording can be used. This uses a bit more data storage however 
I made sure that a junction was removed from the storage when it is searched to save a little more space.

Testing is difficult as there are very particular situations where the robot could break however I tested
it many times on a 200 by 200 maze and it reaches the target every time, fitting the desired specification
with no collisions with as many loops as possible. This design also works on hill and especiialy blank mazes
which is essentially a maze where everywhere is a loop so this shows that it works for every case of a 
loopy maze so making sure it worked on blank mazes was a good way of testing it for most cases. 
*/
public class Ex3 {
	ArrayList<int[]> juncs; //arraylist for junction recorder
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
		} else if (robot.look(IRobot.RIGHT) != IRobot.WALL) {
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
			explorerMode = 0; //if there is no passage enter backtrack mode
			return 0;
		}
		return nonwalls[randno];
	}

	public void reset() { //method to reset junctioncounter, also clears arraylist that stores junctions
		robotData.resetJunctionCounter();
		juncs.clear();
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
		else if (exits > 2 ){
			if (junctionsCrossroads(robot) == 0){
				explorerMode = 0; //when no passages are found enter backtrack
				backtrackControl(robot);
				return;
			} else{
				direction = junctionsCrossroads(robot);
			}
			
			junctionCounter++;
			obj.junctionRecorder(robot); //record every junction instead of just new ones to ensure the robot knows how to escape a loop, unused junctions will be removed so there isnt much more space used. 
			
		}
		if ((robot.look(direction) == IRobot.BEENBEFORE) & (exits > 1)){ //Main change for ex3, checks if the caluclated direction is going to go onto a beenbefore square. This will only happen if it is in a loop so it therefore acts as if this is a virtual wall and turns around and enters backtrack. 
			explorerMode = 0;
			direction = IRobot.BEHIND;
		}
		robot.face(direction);
	}

	private void backtrackControl(IRobot robot){
		int exits; //num of exits
		int direction; //robot direction
		exits = nonwallExits(robot);
		direction = 0;

		if (exits == 1){ //deadEnd handling if already in backtrack mode
			direction = deadEnd(robot);
			robot.face(direction);
			return;
		}

		if (exits == 2) { //corridor handling for backtrack
			direction = corridors(robot);
			robot.face(direction);
			return;
		}

		if (exits > 2){
			if (passageExits(robot) > 0){
				explorerControl(robot); //if there are passagexits it shouldnt be in backtrack
				explorerMode = 1;
			}else{
				direction = obj.searchJunction(robot, robot.getLocation().x, robot.getLocation().y); //call the searchjunction to find where to go
				if (direction == IRobot.WEST | direction == IRobot.SOUTH){ //reverses direction
					direction = direction - 2;
				}else{
					direction = direction + 2;
				}

				if (direction == 2){//One of the key differences in handling loopy mazes is that you can be surrounded in beenbefore squares with actually having been at the square you are on. This handles that problem by recording a new junction. 
					obj.junctionRecorder(robot);//Second main change from the original design so it would work on loopy and blank mazes. 
					explorerControl(robot);
					return;
				}else{
					robot.setHeading(direction);
				}
			}
		}else{
			explorerControl(robot); 
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
			juncs = new ArrayList<int[]>();
		}

		private void junctionRecorder(IRobot robot){
			int[] junc = {robot.getLocation().x, robot.getLocation().y, robot.getHeading()}; //creating array of the x,y,heading information
			juncs.add(junc); //adding the array as an element of the arraylist
			printJunction(robot);//show junction recording is happening properly
		}

		private void printJunction(IRobot robot) {//method to ensure junctions are recorded properly
			System.out.println("Junction " + junctionCounter + "(x="+robot.getLocation().x + ",y="+robot.getLocation().y + ") integer heading value " + robot.getHeading());
		}

		private void resetJunctionCounter() {
			junctionCounter = 0;
		}

		private int searchJunction(IRobot robot, int searchX, int searchY) {
			for (int i=0; i<juncs.size(); i++){//search through whole array list
				int[] arrayL = juncs.get(i);//get the array from each element of the arraylist
				if (arrayL[0] == searchX & arrayL[1] == searchY){
					junctionCounter--; //since you are removing the junction subtract from junction counter
					juncs.remove(i);//remove the object from the arrayList as it is no longer needed and doesnt need to be stored
					return (arrayL[2]);//if it is the right junction return the heading information
				}
			}
			return(0);
		}
	}
}













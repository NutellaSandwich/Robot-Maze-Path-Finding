import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
import java.util.Arrays;

/* I used route B that is suggested as I had already thought it would be a good implementation
and it used the basis of my ex2 which used a stack. I decided to essentially do a mix of ex1 and 2
by storing the x,y and heading in an intial array list but then just having an ArrayList to act
as a stack as a second storage for the robot to learn from. So the robot will do the general run 
first just as is in ex1 there is no change there. I also remove the junctions as they are searched 
to save some space and so it will complete loopy mazes. Most handling for loops is copied from my ex3
solution. While the robot runs on the first run it adds headings to a stack implemented as an ArrayList
so every heading it goes at junctions is recorded in this new stack. If a deadend is visited that heading
is removed as it is clearly the wrong direction. So this means that only the headings that take the 
robot to the target are stored. After the first run it will clear the orignal junction recordings to 
save space and instead whenever it reaches a junction it will recall the appropriate heading that
was stored in the second arraylist. However unlike a stack it will not actually remove the term,
this is so that it can work multiple times and the record isnt lost. 

The robot is able to repeat runs of the same maze doing the perfect run each time after the first run. 
The robot deals with new mazes by simply running in the original exploring mode then learning the 
maze and doing the perfect run from then on.  

The robot can solve loopy mazes if they are small and will drastically reduce the number of steps it
takes from the orignal run so is effective however will not give the shortest posible path. It will also 
not work occasionally on larger loopy mazes, this is due to the fact the array being accessed tends to get
to the point where you get out of bounds errors. However it works perfectly as intended on prim mazes. 

Testing for prim mazes was quite easy as you can just run it the first time and then from then on you
can clearly see the robot takes a route that never enters any deadends and goes straight to the target. 
Testing on loopy mazes is more difficult as it does not take the shortest path but you can at least see
by the number of steps that the robot does learn and shorten its path. The loopy mazes works most of the
time and through many runs and testing it is quite efficient. 
*/
public class GrandFinale {
	ArrayList<int[]> juncs; //arraylist for junction recorder
	ArrayList<Integer> storageJuncs;
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
		if ((storageJuncs.isEmpty() == false) & (robot.getRuns() == 0)){
			storageJuncs.remove(storageJuncs.size() - 1);
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
			explorerMode = 0; //if there is no passage enter backtrack mode
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
		else if (exits > 2 ){
			if (robot.getRuns()>0){//Main change for GrandFinale
				robot.face(obj.recallJuncs(robot));//if the robot has completed the maze once, access the remembered junction headings whennever you are at a junction and head that direction instead of exploring. 
				return;
			}
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
		if ((robot.look(direction) == IRobot.BEENBEFORE) & (exits > 1) &(robot.getRuns() == 0)){ //Change made in ex3 for program to work on loopy mazes. 
			explorerMode = 0;
			direction = IRobot.BEHIND;

			storageJuncs.remove(storageJuncs.size() - 1);
		}
		robot.face(direction);

		if (exits > 2){
			obj.storeJunctions(robot);
		}
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

		else if (exits > 2){
			if (passageExits(robot) > 0){
				explorerControl(robot); //if there are passagexits it shouldnt be in backtrack
				explorerMode = 1;
				return;
			}else{
				if (robot.getRuns()==0){
					storageJuncs.remove(storageJuncs.size()-1);
				}
				direction = obj.searchJunction(robot, robot.getLocation().x, robot.getLocation().y);//call the searchjunction to find where to go
				if (direction == IRobot.WEST | direction == IRobot.SOUTH){ //reverses direction
					direction = direction - 2;
				}else{
					direction = direction + 2;
				}

				if (direction == 2){ //Fix for loopy mazes from ex3
					obj.junctionRecorder(robot);
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
		if ((pollRun == 1) & (robot.getRuns() == 0)){ //stores the starting squares heading
			obj.storeJunctions(robot);
		}
		if ((pollRun == 0) & (robot.getRuns() > 0)){ //makes sure the robot starts the correct heading as it could travel down multiple in a loopy maze
			robot.setHeading(storageJuncs.get(0));
		}
		if ((robot.getLocation().x == 1) & (robot.getLocation().y == 1) & (pollRun > 0) & (robot.getRuns() == 0)){ //adds headings for if it returns to the start square
			obj.storeJunctions(robot);
		}
		if (pollRun == 0){ //call function to create the arraylist
			obj.createD(robot);
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
		
		int juncRun = 1; //junction and index counter for new arraylist

		private void createD(IRobot robot){ //creating the new, empty arraylist
			juncs = new ArrayList<int[]>();
			if (robot.getRuns()==0){ //creates arraylist for storage of remembered headings only when it is a new maze 
				storageJuncs = new ArrayList<Integer>();
			}
			if(robot.getRuns()>0){
				juncRun = 1;
			}
		}

		private void junctionRecorder(IRobot robot){
			int[] junc = {robot.getLocation().x, robot.getLocation().y, robot.getHeading(), 0}; //creating array of the x,y,heading information
			juncs.add(junc); //adding the array as an element of the arraylist
			printJunction(robot);//show junction recording is happening properly
		}

		private void storeJunctions(IRobot robot){
			storageJuncs.add(robot.getHeading());
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
					System.out.println("remove");
					junctionCounter--;//since you are removing the junction subtract from junction counter
					juncs.remove(i);//check the indexes to see if it is the right junction compared to what is being searched for
					return (arrayL[2]);//if it is the right junction return the heading information
				}
			}
			return(0);
		}

		private int recallJuncs(IRobot robot){//method to return the remembered directions for the runs afetr the first one 
			int x = juncRun;
			System.out.println(juncRun);
			int value = (storageJuncs.get(x) - robot.getHeading());//conversion of headings to absolute directions
			if (value < 0){
				value += 4;
			}
			juncRun++;//increment so next junction can be accessed
			return(IRobot.AHEAD + value); //return calculated direction
		}
	}
}













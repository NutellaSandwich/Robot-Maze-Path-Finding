import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
import java.util.Arrays;

/* all exit calculation methods were virtually the same in that they simply check
every direction for whatever the method is looking for however I decided to create 
these in 3 separate methods as i believe it created better readability and was more
understandble when it came to actually calling them. For the deadEnd method i again
checked every direction however specifically looking for the only non wall as this
must be the exit to the dead end, I did this rather than simply facing backwards as
this solves any kind of problem that would be cause by the robot starting at a dead
end. For the corridors method, the most common time the robot is it a corridor is 
to move ahead in the direction it is already facing, i then set an if statement to check
if ahead is a wall and if there is, the robot is at a corner so it checks left and right
for a wall to see which direction it should go. The junction method was done by creating 
an array to essentially store the state of each direction. So the method checks each direction
around it and stores the robot.look output in each element of the array corresponding to each
direction. While assigning the values to the array it also sets a flag for if a passage 
is seen. If the flag is set it will randomly generate array indexes until a passage is 
picked, this is so that if there is multiple passages, it is picked randomly. If there 
is no passages it enters the backtrackControl as this is what the specification states. 

For my RobotData class i decided to store the data as an arraylist where each element
is a junctionrecorder object that is in the form of an array, so the x,y and heading
values for each junction is stored in an array in an element of the arraylist. I 
decided to do this as an arraylist can increase in size easily and the array is used
as I know i will only need three indexes for each one. I implement this by calling the 
junctionrecorder object only when the number of beenbefore exits is <= 1 as this means
it has not been visited before. When it comes to backtracking it is quite simple to 
search for the junction as you take the current x and y values as paramters and you can
loop through the arraylist to check each array until you find where the x and y values
match, and since each array is organised in the same way, the heading will always be
in the 3rd index so you can simply return this value. 

I repeated the code for searching in every direction quite a few times, this maybe could
have been improved by creating a separate method to do this however i was not sure how i
would have returned which type of square i was looking for. Combining the junction and 
crossroad methods saved many lines as they were both functionally the same as in terms
of the way the robot works there is very little difference between the two. 

The explorer robot should always reach the target in the regular maze but not necessarily
in the other types due to the fact that it can arrive at places where it will either circle 
round or go back and forth between two squares as they are considered junctions even though
it is an open space. The problem with setting a max number of steps is you dont actually
know when it is stuck or when it is simple exploring every part of the maze however when
it reaches excessive numbers (100000+ can be seen) it is quite obvious it is looping round. 

Testing this was quite easy as you could simply have print statements for when it changes modes 
print out the relevant part of junction recorder array and print the direction the robot has decided 
to go at a junction and see if they match, also you can just generally follow the path of the
path an clearly see it is backtracking effectively. Testing in the blank maze showed an
error appear to do with the fact that every square is considered a junction so it can get stuck
surrounded by beenbefore squares when the square it is on itself has not been recorded but
is a junction, i explain how i dealt with this lower down.
*/
public class Explorer {
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
			explorerMode = 0;
			return 0;
		}
		return nonwalls[randno];
	}

	public void reset() { //method to reset junctioncounter, also clears arraylist that stores junctions
		robotData.resetJunctionCounter();
		juncs.clear();
	}

	public void explorerControl(IRobot robot){ 

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

	public void backtrackControl(IRobot robot){
		int exits; //num of exits
		int direction; //robot direction
		exits = nonwallExits(robot);
		direction = 0;

		if (exits > 2){
			if (passageExits(robot) > 0){ //if there are passagexits it shouldnt be in backtrack
				explorerMode = 1;
			}else{
					direction = obj.searchJunction(robot, robot.getLocation().x, robot.getLocation().y); //call the searchjunction to find where to go
				if (direction == IRobot.WEST | direction == IRobot.SOUTH){ //reverses direction
					direction = direction - 2;
				}else{
					direction = direction + 2;
				}

				if (direction == 2){
					obj.junctionRecorder(robot);
					pollRun--;
					return; //this fixes an obscure error where in a blank maze it gets surrrounded by beenbefore squares but the square it is on has not been recorded yet so it doesnt actually know where to go, so it essentially records the junction but doesnt move so it can know where it came from
				}else{
					robot.setHeading(direction);
				}
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
		
		public void createD(){ //creating the new, empty arraylist
			juncs = new ArrayList<int[]>();
		}

		public void junctionRecorder(IRobot robot){
			int[] junc = {robot.getLocation().x, robot.getLocation().y, robot.getHeading()}; //creating array of the x,y,heading information
			juncs.add(junc); //adding the array as an element of the arraylist
			printJunction(robot);//show junction recording is happening properly
		}

		public void printJunction(IRobot robot) {//method to ensure junctions are recorded properly
			System.out.println("Junction " + junctionCounter + "(x="+robot.getLocation().x + ",y="+robot.getLocation().y + ") integer heading value " + robot.getHeading());
		}

		public void resetJunctionCounter() {
			junctionCounter = 0;
		}

		public int searchJunction(IRobot robot, int searchX, int searchY) {
			for (int i=0; i<juncs.size(); i++){//search through whole array list
				int[] arrayL = juncs.get(i);//get the array from each element of the arraylist
				if (arrayL[0] == searchX & arrayL[1] == searchY){//check the indexes to see if it is the right junction compared to what is being searched for
					return (arrayL[2]);//if it is the right junction return the heading information
				}
			}
			return(0);
		}
	}
}














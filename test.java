import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
import java.util.Arrays;

/* i started off with the nonwallExits method first, i am able to code this method out quickly as i got fimiliar with it after course work 1
 * then i added exit variable and set the value by calling nonwallExits method
 * setted up 4 method which are named after the location of the robot,
 * also added variable direction which will inidicate the direction the robot should face, and then value and direction will b edetermined by the value of "exit" and by calling the four brand new method just made
 * 
 * plan 1:
 * for the case deadend i made it just to return behind
 * for the case corridor or corner, i made the method check whether it has been to all direction except behind or not, and if they are wall or not. behind is not checked as it must the direction that the robot has been to before. 
 * for the case junction, break it down into 3 small case:
 * frist being the case where there are two passage it hasn't been to, in this case randomly choose one of them 
 * second being the case where it has already been to one of the passage, then set the direction into the one it hasn't been to yet
 * third case being the case where it has already been to both passages before, then it should randomly choose a direction from 3 
 * to make the randomly choosing code more convinient, i made an method for randomly choosing direction
 * at first i tried to do it case by case, then i realised it would take a lot of code to make the robot check how many direction it hasn't been to yet as we have to repeatthis process for crossroad as well
 * thus i decide to make another method that check how many direction it hasn't been to yet and then return the value 
 * first two case is easier, for the case where's 2 passage that is valid, i break it now into the combination of choosing 2 from ahead, right and left being the valid direction, as behind is always not valid in this case
 * for the last location crossroad i reused the code from junction and added a final case where all 3 passage has not been visited before, and the robot should choose a random direction
 * 
 * however i then soon realised that the method i am using right now is too inefficient and takes too many lines of code as well, especially for junction and crossroad, so i came up with a new idea by using array
 * 
 * plan 2:
 * i kept some of the variable and then combined method for junction and crossroad into one, as they share similar situation and it would save lines of codes
 * by checking if the direction is valid(notbeento and notwall), if it's valid then add it into the array
 * then by checking if the element inside the array has been changed or not, we spplit it into two cases, one being all the direction at the junction or crossroad has been visited before thus random direction should be choseb
 * if there's any direction that is valid, then i would use a do while statement to make sure a valid direction is chosen randomly.
 * 
 * by switching from plan 1 to 2, i deleted two redundant method and decreased line of code by about 80
 * 
 * 
 * 
 */
public class test{

    ArrayList<int[]> junctions;
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int junctionCounter;
    RobotData robotobject = new RobotData();

    public void reset() {
        robotData.resetJunctionCounter();
    }

    public void controlRobot(IRobot robot){
        //set int exit as method nonwallexits to count the location of the robot and variable direction for later use
        int exit = nonwallExits(robot);
        int direction = 0;

        // On the first move of the first run of a new maze
        if ((robot.getRuns() == 0) && (pollRun == 0))
            robotData = new RobotData(); //reset the data store

        //setting direction's value to the result returned by one of the four method depending on the location robot's at which is the value of exit 
        switch (exit) {
            case 1:
                direction = deadEnd(robot);
                break;
            case 2:
                direction = corridor(robot);
                break;
            case 3:
            case 4:
                if(beenbeforeExits(robot) <= 1){
                    junctionCounter++;
                    robotobject.junctionRecorder(robot); 
                }
                direction = junction_and_crossroad(robot);
        }

        pollRun++; // Increment pollRun so that the data is not reset each time the robot moves
        robot.face(direction);
    }

    //method to determine how many nonwall option does robot has and returning the value
    public int nonwallExits(IRobot robot){
        //use if statement to count the number of non-wall square, and save it to variable nonwall
        int nonwall = 0;
        if (robot.look(IRobot.AHEAD) != IRobot.WALL)
            nonwall++;
        if (robot.look(IRobot.BEHIND) != IRobot.WALL)
            nonwall++;
        if (robot.look(IRobot.RIGHT) != IRobot.WALL)
            nonwall++;
        if (robot.look(IRobot.LEFT) != IRobot.WALL)
            nonwall++;
        //return the value of number of non-wall passage
        return nonwall;
    }

    //method to return the value behind, as the only possible direction to go when the robot's at the deadend is behind
    private int deadEnd(IRobot robot){
        return IRobot.BEHIND; 
    }

    //method for deciding the direction if the robot is at a corridor or corner, behind must have been visited before thus it's the else case
    private int corridor(IRobot robot){
        //setting up variable result, which will be returned to the direction
        int result = 0;
        if (robot.look(IRobot.AHEAD) != IRobot.WALL)
            result = IRobot.AHEAD;
        else if (robot.look(IRobot.RIGHT) != IRobot.WALL)
            result = IRobot.RIGHT; 
        else 
            result = IRobot.LEFT;
        //returning the result
        return result;
    }

    //method for deciding the direction if the robot is at a junction or crossroad
    private int junction_and_crossroad(IRobot robot){
        //setting up variable, notbeento be the value of the method checking number of direction robot hasn't been to and result being the direction to be returned to controller 
        int result = 0;
        int randno = 0;
        int counter = pasasagExit(robot);
        
        //setting up an array to save the valid direction
        int[] validpassage = {0,0,0};
        
        //using if statement to check if the direction is valid, if it is add it to the array for later use 
        if (robot.look(IRobot.AHEAD) == IRobot.PASSAGE)
            validpassage[0] = IRobot.AHEAD;
        if (robot.look(IRobot.RIGHT) == IRobot.PASSAGE)
            validpassage[1] = IRobot.RIGHT;
        if (robot.look(IRobot.LEFT) == IRobot.PASSAGE)
            validpassage[2] = IRobot.LEFT;

        //by checking if the value inside the array has changed or not, to decide what the direction should be 
        if (validpassage[0] == 0 && validpassage[1] == 0 && validpassage[2] ==0)
            result = randgen(robot);
        //second case being any case the there's direction that has not been visited, do while statement will repeated choose untill a valid one is chosen
        else{
            do {
                randno = (int) Math.floor(Math.random()*3);
                result = validpassage[randno];    
            } while (result == 0);
        }
        

        return result;
    } 


    //method for choosing a random direction
    private int randgen(IRobot robot){
        int result;	
        int randno;
    
        result = IRobot.WALL;
    
        do {
    
        randno = (int) Math.floor(Math.random()*4);
    
        if (randno == 0)
                result = IRobot.AHEAD;
        else if (randno == 1)
                result = IRobot.BEHIND;
        else if (randno == 2)
                result = IRobot.LEFT;
        else if (randno == 3)
                result = IRobot.RIGHT ;
        } while (robot.look(result)==IRobot.WALL);
        
        return result;  /* returning the direction */ 
    }

    //method to count number of passage that has not been visited around the robot
    private int pasasagExit(IRobot robot){
        //use if statement to count the number of passage that has not been visited, and save it to variable passage, behind is skipped as it must have been visited
        int passage = 0;
        if (robot.look(IRobot.AHEAD) == IRobot.PASSAGE)
            passage ++;
        if (robot.look(IRobot.RIGHT) == IRobot.PASSAGE)
            passage ++;
        if (robot.look(IRobot.LEFT) == IRobot.PASSAGE)
            passage ++;
        //return the value of number of passage
        return passage;
    }

    //method to count number of beenbefore square around the robot
    private int beenbeforeExits(IRobot robot){
        //use if statement to count the number of sqaure that has been visited, and save it to variable
        //default value is 1 as behind must have been visited
        int beenbefore = 1;
        if (robot.look(IRobot.AHEAD) == IRobot.BEENBEFORE)
           beenbefore ++;
        if (robot.look(IRobot.RIGHT) == IRobot.BEENBEFORE)
            beenbefore ++;
        if (robot.look(IRobot.LEFT) == IRobot.BEENBEFORE)
            beenbefore ++;
        //return the value 
        return beenbefore;
    }
    
    public void reset() {
        robotData.resetJunctionCounter();
    }
    
}




class RobotData{

    public void createList(){
        junctions = new ArrayList<int[]>();
    }

    public int junctionRecorder(IRobot robot){
        int[] junc = {robot.getLocation().x , robot.getLocation().y , robot.getHeading()};
        junctions.add(junc);
    }

    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

}
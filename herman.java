import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
import java.util.Arrays;


/* i started off with the nonwallExits method first, i am able to cod85
e this method out quickly as i got fimiliar with it after course work 1
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
 * for the next part, i used arraylist to the save the coordinate and the direction of the robot as it saves lines of code  
 * first i found out after adding the robotdata class and method into the controller, whenever my robot walk into a junction or a crossroad it stops moving and the terminal starts showing error stating that my array is not acessible 
 * secondly, my robot might face collision in the very start of the run if its spawned in a bad position (i.e deadend facing the right / left wall), as i programmed it to just back track in a deadend 
 * 
 * so for the first problem, i was confused for a while, by making many many adjustment, i found i simply forgot to initialise the size of my arrays thus the code didn't work 
 * 
 * for the second problem, i simply made the deadend to use the same method as corridor as i thought they sharew a similar case, by adding behind as the "else" case in my corridor method it should work
 * however there's still a few collision in the run 
 * turns out it just a minor mistake in the corridor_and_deadend method, after adjusting, the explorer works perfectly fine 
 * after that i added the print method to check if the datas are accurate 
 * before i moved on to using stored data part, also found out that my junction counter doesn't reset even after i reseted the maze, 
 * 
 * for the using stored data part, i decided to set up a new variable "finder", which is used as the index for element in the array we want to find 
 * however i faced a few problem after the adding the backtracking and exploring method 
 * my code no longer print out the data added into the array and if all the passage in at a junction are explored and robot stops moving 
 * i figure it has something to do with my logic, as with the current code, i minus the index by 1 (in search junction)each time we each a old junction that has been completely explored 
 * 
 * thus i changed my search junction method into searching the index of the junction we're at by matching the current X Y coordinate with the one in array 
 * 
 * 
 */
    
    public class herman{
        private int pollRun = 0; // Incremented after each pass
        private RobotData robotData; // Data store for junctions
        private int explorerMode = 1; // 1 = explore, 0 = backtrack
        ArrayList<int[]> junctions; //arraylist for junction recorder
        RobotData robotobject = new RobotData();
        private int junctionCounter;

        //reseting junction counter in every new run
        public void reset() {
            robotData.resetJunctionCounter();
            junctions.clear();
        }

        public void exploreControl(IRobot robot){

            //set int exit as method nonwallexits to count the location of the robot and variable direction for later use
            int exit = nonwallExits(robot);
            int direction = 0;

            explorerMode = 1;

            //setting direction's value to the result returned by one of the four method depending on the location robot's at which is the value of exit 
            switch (exit) {
                case 1:
                    direction = corridor_and_deadend(robot); 
                    if (pollRun != 0) {
                        explorerMode = 0;
                    }//turn into backtrack mode 
                    break;
                case 2:
                    direction = corridor_and_deadend(robot); 
                    break;

                case 3:
                case 4:

                    if(junction_and_crossroad(robot) == 0){
                        backtrackControl(robot);
                        return;
                    }else{
                        direction = junction_and_crossroad(robot);
                    }
                    if(beenbeforeExits(robot) <= 1){ //record the data when the robots reaches a new junction or a crossroad
                        junctionCounter++;
                        robotobject.junctionRecorder(robot); 
                        robotobject.printJunctions(robot);//printing the data saved inside the arrays when the robot is at the junction or crossroad
                    }

                    break;  
            }
            robot.face(direction);
        }

        public void backtrackControl(IRobot robot){

            //setting up varible 
            int nonwall = nonwallExits(robot);
            int passage = pasasagExit(robot);
            int result = 0; 

            if (nonwall > 2){
                if (passage > 0){
                    explorerMode = 1;
                }
                else{

                    switch(robotData.searchJunction(robot)){//by checking the direction when we first arrived this junction, choose the opposite direction to back track
                        case IRobot.NORTH:
                            result = IRobot.SOUTH;
                            break;
                        case IRobot.EAST:
                            result = IRobot.WEST;
                            break;
                        case IRobot.SOUTH:
                            result = IRobot.NORTH;
                            break;
                        case IRobot.WEST:
                            result = IRobot.EAST;
                            break;
                    }
                    robot.setHeading(result);
                }
            }else{
                exploreControl(robot);
            }
        }

        public void controlRobot(IRobot robot){

            // On the first move of the first run of a new maze
            if ((robot.getRuns() == 0) && (pollRun == 0))
                robotData = new RobotData(); //reset the data store
                explorerMode = 1;

            if (pollRun == 0){ //call function to create the arraylist
                    robotobject.arraylistcreated();
            }

            //set int exit as method nonwallexits to count the location of the robot and variable direction for later use

            switch (explorerMode){
                case 0:
                    backtrackControl(robot);
                    break;
                case 1:
                    exploreControl(robot);
                    break;
            }

            pollRun++; // Increment pollRun so that the data is not reset each time the robot moves
            
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

    //method for deciding the direction if the robot is at a corridor or corner, behind is added as it is the case for deadend 
    private int corridor_and_deadend(IRobot robot){

        //setting up variable result, which will be returned to the direction
        int result = 0;

        if (robot.look(IRobot.AHEAD) != IRobot.WALL)
            result = IRobot.AHEAD;
        else if (robot.look(IRobot.RIGHT) != IRobot.WALL)
            result = IRobot.RIGHT; 
        else if (robot.look(IRobot.LEFT) != IRobot.WALL) 
            result = IRobot.LEFT;
        else 
            result = IRobot.BEHIND;

        //returning the result
        return result;
    }

    //method for deciding the direction if the robot is at a junction or crossroad
    private int junction_and_crossroad(IRobot robot) {

        // setting up variable, notbeento be the value of the method checking number of
        // direction robot hasn't been to and result being the direction to be returned
        // to controller
        int result = 0;
        int randno = 0;

        // setting up an array to save the valid direction
        int[] validpassage = { 0, 0, 0 };

        // using if statement to check if the direction is valid, if it is add it to the
        // array for later use
        if (robot.look(IRobot.AHEAD) == IRobot.PASSAGE)
            validpassage[0] = IRobot.AHEAD;
        if (robot.look(IRobot.RIGHT) == IRobot.PASSAGE)
            validpassage[1] = IRobot.RIGHT;
        if (robot.look(IRobot.LEFT) == IRobot.PASSAGE)
            validpassage[2] = IRobot.LEFT;

        // by checking if the value inside the array has changed or not, to decide what
        // the direction should be
        if (validpassage[0] != 0 || validpassage[1] != 0 || validpassage[2] != 0)
            do {
                randno = (int) Math.floor(Math.random() * 3);
                result = validpassage[randno];
            } while (result == 0);
        // second case being any case the there's direction that has not been visited,
        // do while statement will repeated choose untill a valid one is chosen
        else {
            explorerMode = 0;
            return 0;
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
    
    //method that turns absolute direction into relative direction

    //class to save the data of the robot such as it's location and absolute direction
    class RobotData{

        private void arraylistcreated(){ //creating the new, empty arraylist
            junctions = new ArrayList<int[]>();
        }

        private void junctionRecorder(IRobot robot){
            int[] junc = {robot.getLocation().x, robot.getLocation().y, robot.getHeading()}; //creating an array for the  X Y coordinate and absolute direction of the current junction  
            junctions.add(junc); //adding the array we just created for the function when this method is called into the arraylist 
        }
        
        
        //method to reset the junction counter after every run
        public void resetJunctionCounter(){
            junctionCounter = 0;
        }

        //method to print out the information we are adding into the arrays 
        public void printJunctions(IRobot robot){

            String heading = "";

            //convert the integer value of absolute direction into string 
            switch (robot.getHeading()){ //by printing the absolute direction separately, we can find out their int value respectively
                case 1000:
                    heading = "NORTH";
                    break;
                case 1001:
                    heading = "EAST";
                    break;
                case 1002:
                    heading = "SOUTH";
                    break;
                case 1003:
                    heading = "WEST";
                    break;
            }

            System.out.println("Junction " + junctionCounter + "( x = " + robot.getLocation().x + ", y = "+robot.getLocation().y +  " ) heading " + heading);
            junctionCounter ++;//increase the value after printing
        }

        public int searchJunction(IRobot robot){

            for (int j=0; j<junctions.size(); j++){
                System.out.println(junctions.get(j));
            }

            for (int i = 0; i < junctions.size(); i++){//for statement to keep searching untill we find the index of the junction we are currently at

                int[] arrayToSearch = junctions.get(i);//get the array from each element of the arraylist
                if (arrayToSearch[0] == robot.getLocation().x & arrayToSearch[1] == robot.getLocation().y){//compare the current X Y coordinate with the index in the array list so we can find the index of the junction the robot is at 
                    return (arrayToSearch[2]);//if it is the right junction return the heading information
                }
            }

            return 0;//return the absolute direction
        }
    }
}
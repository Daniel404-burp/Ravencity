package Main;

public class ObjectiveManager {

    private static String currentObjective = "Explore the Residential District";

    public static void setObjective(String text){
        currentObjective = text;
    }

    public static String getObjective(){
        return currentObjective;
    }
}
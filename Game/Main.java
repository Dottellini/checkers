package Game;

//javac -cp ./Game/core.jar -d ./Game/out/production/Game/ ./Game/Main.java
//java -classpath "C:\Users\naehr\Desktop\Daten\coding\java\checkers\Game\out\production\Game;C:\Users\naehr\Desktop\Daten\coding\java\checkers\Game\core.jar" Game.Main

import java.util.*;
import java.util.stream.Collectors;

import java.lang.reflect.WildcardType;
import processing.core.PApplet;
import processing.core.PGraphics;


//PROCESSING Stuff
public class Main extends PApplet {
    static int width = 1280;
    static int height = 720;
    
    public static void main(String[] args) {
        String[] appArgs = {"Dame"};
		Main mySketch = new Main();
		PApplet.runSketch(appArgs, mySketch);
        System.out.println("new Game()");
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        background(color(255, 255, 255));
        noStroke();
        
    }

    public void mousePressed() {
        
        
    }

    public void draw() {
        
    }
}

import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import processing.core.PApplet;
import processing.core.PGraphics;

public class MySketch extends PApplet {
    
    
    public static void main(String[] args) {
        String[] appArgs = {"Dame"};
		MySketch mySketch = new MySketch();
		PApplet.runSketch(appArgs, mySketch);
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        background(backgroundColor);
        noStroke();
        
    }

    public void mousePressed() {
        
        };
    }

    public void draw() {
        
    }
}

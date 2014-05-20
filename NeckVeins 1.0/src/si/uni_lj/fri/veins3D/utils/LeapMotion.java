package si.uni_lj.fri.veins3D.utils;

import java.io.IOException;
import java.lang.Math;
import java.util.LinkedList;

import sun.security.provider.certpath.OCSP.RevocationStatus;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;

public class LeapMotion extends Listener {
	float startSwipe;
	boolean lastSwipe; // true = left, false = right
	
	float[] axis;
	double[] rotation;
	private Vector startPosition=new Vector(40, -230, 30);
	private Vector relativePosition=new Vector(0,0,0);
	private LimitedQueue<Vector> positionAverage = new LimitedQueue<Vector>(20);
	
	private boolean openPalm;
	
	private class LimitedQueue<E> extends LinkedList<E> {
        private int limit;
        public LimitedQueue(int limit) {
            this.limit = limit;
        }
 
        @Override
        public boolean add(E o) {
            super.add(o);
            while (size() > limit) { super.remove(); }
            return true;
        }
    }
	
	private Vector average(LimitedQueue<Vector> vectors)
    {
        float x=0f, y=0f, z=0f;
        for(Vector v:vectors){
            x=x+v.getX(); y=y+v.getY(); z=z+v.getZ();
        }
        return new Vector(x/vectors.size(), y/vectors.size(), z/vectors.size());
    }
	
    public void onInit(Controller controller) {
        System.out.println("LeapMotion sensor initialized");
        axis=new float[3];
        rotation=new double[3];
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
    }

    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }

    public void onFrame(Controller controller) {
        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();

        if (!frame.hands().isEmpty()) {
            // Get the first hand
            Hand hand = frame.hands().get(0);

            // Check if the hand has any fingers
//            FingerList fingers = hand.fingers();
//            if (!fingers.isEmpty()) {
//                // Calculate the hand's average finger tip position
//                Vector avgPos = Vector.zero();
//                for (Finger finger : fingers) {
//                    avgPos = avgPos.plus(finger.tipPosition());
//                }
//                avgPos = avgPos.divide(fingers.count());
//                /*System.out.println("Hand has " + fingers.count()
//                                 + " fingers, average finger tip position: " + avgPos);*/
//            }

            // Get the hand's sphere radius and palm position
            /*System.out.println("Hand sphere radius: " + hand.sphereRadius()
                             + " mm, palm position: " + hand.palmPosition());*/

            // Get the hand's normal vector and direction
//            Vector normal = hand.palmNormal();
//            Vector direction = hand.direction();

            // Calculate the hand's pitch, roll, and yaw angles
            /*System.out.println("Hand pitch: " + Math.toDegrees(direction.pitch()) + " degrees, "
                             + "roll: " + Math.toDegrees(normal.roll()) + " degrees, "
                             + "yaw: " + Math.toDegrees(direction.yaw()) + " degrees");*/
            
           /* System.out.println("P: " + Math.toDegrees(direction.pitch())
                             + "R: " + Math.toDegrees(normal.roll())
                             + "Y: " + Math.toDegrees(direction.yaw()));*/
            
            Vector handPostion=hand.palmPosition();
            positionAverage.add(handPostion);
            
            Vector averageHandPostion=average(positionAverage);
            
            axis=(averageHandPostion.plus(startPosition).minus(relativePosition)).toFloatArray();
            
          //  System.out.println(axis[0]+"\t"+axis[1]+"\t"+axis[2]);
           /* System.out.println("Frame "+frame.id()+
            		"Start: "+ startPosition+
            		"Relative: "+ relativePosition+
            		"Actual: "+ hand.palmPosition());*/
            
            //If palm has been open, change start position
            if(!openPalm && hand.fingers().count()>3){
            	openPalm=true;
            	relativePosition=hand.palmPosition();
            }
            	
            if(openPalm && hand.fingers().count()<=3){
            	openPalm=false; 
            	System.out.println("Palm closed at: "+averageHandPostion.toString());
            	startPosition=averageHandPostion;
            }
            
            /*
           rotation[0]=Math.toDegrees(direction.pitch())/45.0;
            rotation[2]=-Math.toDegrees(normal.roll())/45.0;
            rotation[1]=Math.toDegrees(direction.yaw())/45.0;*/


        }

    }
    public float[] getAxis(){
  	   	return axis;
    }
    public double[] getRotations(){
    	return rotation;
    }
    public boolean isPalm(){
    	return openPalm;
    }
    
    public boolean getSwipeState(){
    	return lastSwipe;
    }
}
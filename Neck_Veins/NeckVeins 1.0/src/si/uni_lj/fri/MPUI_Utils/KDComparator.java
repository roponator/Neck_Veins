package si.uni_lj.fri.MPUI_Utils;

import java.util.Comparator;

public class KDComparator implements Comparator<OrientedPoint> {
    @Override
    public int compare(OrientedPoint o1, OrientedPoint o2) {
        return ((Float)o1.getPoint().x).compareTo(o2.getPoint().x);
    }
    
}
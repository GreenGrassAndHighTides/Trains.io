import java.lang.Math;
import java.io.*;
import java.util.*;


public class Point {
  int x;
  int y;
  int type;
  final String[] NAMES = new String[]{"Blank", "Mountain", "Water", "City", "Base"};
  Track top, bottom, left, right;
  Player owner; //for homebase
  int[] exports = new int[5];



  public String(Render) {
    if (type == 4) {
      return NAMES[type] + "/" + owner.name;
    }
    return NAMES[type];
  }

}

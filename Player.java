import java.lang.Math;
import java.io.*;
import java.util.*;
import org.webbitserver.*;


public class Player {
  String name;
  ArrayList<Track> tracks = new ArrayList<Track>();
  WebSocketConnection connection;
  Point homeBase;
  Point focus;
  int[] resources = new int[5];
  final int MONEYPOS = 0;
  final int STEELPOS = 1;
  final int WOODPOS = 2;
  final int GUNPOWDERPOS = 3;
  final int COALPOS = 4;
  int tool;
  final String[] TOOLS = new String[]{"Rails", "Gunpowder", "Bridge", "Upgrade"};
  ArrayList<Point> cities = new ArrayList<Point>();






  public String render() {
    String temp = "";
    for (int p: resources) {
      temp += p + "/";
    }
    return temp;
  }
  )
}

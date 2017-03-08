import java.lang.Math;
import java.io.*;
import java.util.*;

public class Track {
  Point startPoint;
  Point endPoint;
  Player owner;





  public String render() {
    return startPoint.x + "/" + startPoint.y + "/" + endPoint.x + "/" + endPoint.y + "/" + owner.name;
  }
}

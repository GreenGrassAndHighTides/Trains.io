import java.lang.Math;
import java.io.*;
import java.util.*;


public class Board {
  final int gridHeight = 50;
  final int gridWidth = 50;
  Point grid[][] = new Point[gridWidth][gridHeight];
  ArrayList<Track> tracks= new ArrayList<Track>();

  //the board constructor will need to generate a random board


  public void Tick() {
    for (int column = 0; column<gridWidth; column ++) {
      for (int row = 0; row<gridHeight; row ++){
        if (grid[column][row].type == 3) {
          grid[column][row].tickCity();
        }
        else if (grid[column][row].type == 4) {
          grid[column][row].tickBase();
        }
      }
    }
  }

  public String Render() {
    String s = "";
    for (int column = 0; column<gridWidth; column ++) {
      for (int row = 0; row<gridHeight; row ++){
        s += grid[column][row].render() + " ";
      }
    }
    for (Track t: tracks) {
      s += t.render() + " ";
    }
    return s;
  }
}

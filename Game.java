import java.lang.Math;
import java.io.*;
import java.util.*;


public class Game {
  Arraylist<Player> players;
  boolean isGameRunning = true;
  int delay;
  Player whoWon;
  Board board;
  String scene;


  public Game(ArrayList<Player> players) {
    this.players = players;
    speed = 250;
    board = new Board();
  }

  public boolean checkWin() {
    return false;
  }

  public void Tick() {
    for (Player p: players) {
      p.Tick();
    }
    board.Tick();
    isGameRunning = !checkWin();
    scene = board.Render();
    for (Player p: players) {
      p.connection.send(scene + p.render());
    }
  }

  public void Run() {
    while (isGameRunning) {
      Tick();
      thread.sleep(delay);
    }
  }
}

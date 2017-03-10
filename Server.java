import org.webbitserver.*;
import org.webbitserver.handler.*;
import java.util.*;

/* Server
 *
 * This class has the game's main() function, which starts a web server and a
 * web-sockets server. Its show the splash/login screen and other pages and
 * handles all incoming connections from brwosers. It also handles the initial
 * login and assigns players to games, which are created as needed.
 */
public class Server {

	// Temporary stub until Elliot's Player class is ready.
	static class PlayerX {
		int id = -1;
		String name;
		WebSocketConnection connection;
		public PlayerX(String n, WebSocketConnection c) {
			name = n;
			connection = c;
		}
	}

	// Temporary stub until Elliot's Game class is ready.
	static class GameX implements Runnable {
		PlayerX players[];
		int id;
		static int nextId = 1;
		final int gridHeight = 30;
		final int gridWidth = 60;
		Random rand = new Random();
		GameX(PlayerX p[]) {
			id = nextId++;
			players = p;
			// test code
			grids = new String[5];
			for (int i = 0; i < 5; i++) {
				String g = "";
				for (int column = 0; column < gridWidth; column ++) {
					for (int row = 0; row < gridHeight; row ++) {
						switch (rand.nextInt(50)) {
							case 1:
							case 10:
							case 11:
							case 12:
							case 13: g += "Mountain "; break;
							case 2:
							case 20:
							case 21:
							case 22:
							case 23:
							case 24: g += "Water "; break;
							case 3: g += "City "; break;
							case 4: g += "Base/" + rand.nextInt(PLAYERS_PER_GAME) + " "; break;
							default: g += "Blank "; break;
						}
					}
				}
				grids[i] = g;
			}
			tracks = new String[5];
			for (int i = 0; i < 5; i++) {
				String t = "";
				for (int column = 0; column < gridWidth-1; column ++) {
					for (int row = 0; row < gridHeight-1; row ++) {
						if (rand.nextInt(10) == 0) {
							t += column+"/"+row+"/"+(column+1)+"/"+row+"/"+rand.nextInt(PLAYERS_PER_GAME)+" ";
						}
						if (rand.nextInt(10) == 0) {
							t += column+"/"+row+"/"+column+"/"+(row+1)+"/"+rand.nextInt(PLAYERS_PER_GAME)+" ";
						}
					}
				}
				tracks[i] = t;
			}
			stats = new String[2];
			for (int i = 0; i < 2; i++) {
				String s = "";
				for (int j = 0; j < 5; j++)
					s += rand.nextInt(50) +"/";
				stats[i] = s;
			}
		}
		String grids[], tracks[], stats[];
		void welcomePlayers() {
			String names = "";
			for (PlayerX p : players)
				names += String.format(", { \"id\": %d, \"name\": \"%s\" }", p.id, p.name);
			names = "[" + names.substring(2) + "]";
			String g = grids[rand.nextInt(grids.length)];
			String t = tracks[rand.nextInt(tracks.length)];
			String s = stats[rand.nextInt(stats.length)];
			for (PlayerX p : players) {
				if (p.connection != null)
					p.connection.send(String.format(
								"{ \"action\": \"JOINED\","
								+" \"gameId\": %d,"
								+" \"playerId\": %d,"
								+" \"players\": %s,"
								+" \"gridHeight\": %d,"
								+" \"gridWidth\": %d,"
								+" \"grid\": \"%s\","
								+" \"tracks\": \"%s\","
								+" \"stats\": \"%s\" }",
								id, p.id, names, gridHeight, gridWidth, g, t, s));
			}
		}
		synchronized void killPlayerAsync(PlayerX lostPlayer) { // called on web socket thread
			lostPlayer.connection = null;
			for (PlayerX p : players) {
				if (p.connection != null)
					p.connection.send(String.format("{ \"action\": \"SURRENDER\", \"playerId\": %d }", lostPlayer.id));
			}
		}
		synchronized void onPlayerMessageAsync(PlayerX p, String message) { // called on web socket thread
			// todo
		}
		public void run() {
			welcomePlayers();
			// test code...
			for (int i = 0; i < 3; i++) {
				String g = grids[rand.nextInt(grids.length)];
				String t = tracks[rand.nextInt(tracks.length)];
				try { Thread.sleep(1000); }
				catch (Exception e) { }
				for (PlayerX p : players) {
					String s = stats[rand.nextInt(stats.length)];
					synchronized(this) {
						if (p.connection == null)
							continue;
						p.connection.send(String.format(
									"{ \"action\": \"UPDATE\","
									+" \"grid\": \"%s\","
									+" \"tracks\": \"%s\","
									+" \"stats\": \"%s\" }",
									g, t, s));
					}
				}
			}
			synchronized(this) {
				for (PlayerX p : players) {
					if (p.connection != null)
						p.connection.send(String.format("{ \"action\": \"WINNER\", \"id\": %d }", players[0].id));
				}
			}
		}
	}

	// Number of players per game.
	public static final int PLAYERS_PER_GAME = 1;

	// Maximum length of player names.
	public static final int MAX_NAME_LENGTH = 40;

	// Name to use for players without a valid name.
	public static final String DEFAULT_NAME = "anonymous";

	// List of spectators viewing the splash page.
	ArrayList<WebSocketConnection> spectators;

	// List of players waiting to play a game.
	ArrayList<PlayerX> queue;

	// List of all active games.
	ArrayList<GameX> games;

	// Constructor.
	public Server() {
		spectators = new ArrayList<WebSocketConnection>();
		queue = new ArrayList<PlayerX>();
		games = new ArrayList<GameX>();
	}

	// Add game to the list of active games and notify all the spectators.
	public void addGame(GameX game) {
		games.add(game);
		for (WebSocketConnection connection : spectators) {
			connection.send(String.format("{ \"action\": \"ADD\", \"id\": %d }", game.id));
		}
	}

	// Remove game from list of active games and notify all the spectators.
	public void removeGame(GameX game) {
		games.remove(game);
		for (WebSocketConnection connection : spectators) {
			connection.send(String.format("{ \"action\": \"DEL\", \"id\": %d }", game.id));
		}
	}

	// Add a new spectator and give them the list of active games.
	public void addSpectator(WebSocketConnection connection) {
		System.out.println("New spectator connection");
		for (GameX game : games) {
			connection.send(String.format("{ \"action\": \"ADD\", \"id\": %d }", game.id));
		}
		spectators.add(connection);
	}

	// Remove a spectator that disconnected.
	public void removeSpectator(WebSocketConnection connection) {
		System.out.println("Lost spectator connection");
		spectators.remove(connection);
	}

	// Sanitize a player name. For security reasons, it is not safe to allow a
	// player to have an arbitrary name (the name contain a "virus" of sorts --
	// see XSS attacks for example). We will allow only short, plain names.
	public static String sanitize(String s) {
		String name = "";
		for (int i = 0; i < s.length() && name.length() < MAX_NAME_LENGTH ; i++) {
			char c = s.charAt(i);
			if (c == ' ' || ('0' <= c && c <= '9') || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z'))
				name += c;
		}
		return name;
	}

	// Add a player to the queue and check if we can start a new game.
	public void addPlayer(WebSocketConnection connection) {
		String name = connection.httpRequest().queryParam("name");
		if (name == null)
			name = DEFAULT_NAME;
		else
			name = sanitize(name);
		if (name.length() == 0)
			name = DEFAULT_NAME;
		System.out.printf("New player connection, name='%s'\n", name);

		PlayerX p = new PlayerX(name, connection);
		connection.data("player", p);
		queue.add(p);

		System.out.println("   ... there are now " + queue.size() + " players ready");
		if (queue.size() < PLAYERS_PER_GAME)
			return;

		// Collect the players.
		PlayerX players[] = new PlayerX[PLAYERS_PER_GAME];
		for (int i = 0; i < PLAYERS_PER_GAME; i++)
			players[i] = queue.remove(0);

		// Create the game.
		final GameX game = new GameX(players);
		addGame(game);
		for (int i = 0; i < PLAYERS_PER_GAME; i++) {
			players[i].id = i;
			players[i].connection.data("game", game);
		}

		// Run the game.
		Thread t = new Thread(game);
		t.setDaemon(true);
		t.start();
	}

	// Remove a player that disconnected.
	public void removePlayer(WebSocketConnection connection) {
		System.out.println("Lost player connection");
		PlayerX p = (PlayerX)connection.data("player");
		GameX g = (GameX)connection.data("game");
		if (g != null) {
			System.out.println("   ... player was playing game " + g.id);
			g.killPlayerAsync(p);
		} else if (p != null) {
			System.out.println("   ... player was waiting for a game");
			queue.remove(p);
		} else  {
			System.out.println("   ... player was unknown");
		}
	}

	// Process a message from a player.
	public void onPlayerMessage(WebSocketConnection connection, String message) {
		System.out.println("Got message from player");
		PlayerX p = (PlayerX)connection.data("player");
		GameX g = (GameX)connection.data("game");
		if (g != null) {
			System.out.println("   ... player is playing game" + g.id);
			g.onPlayerMessageAsync(p, message);
		} else if (p != null) {
			System.out.println("   ... player is waiting for a game, ignore message");
		} else {
			System.out.println("   ... player is unknown");
		}
	}

	// Spectator handler, adds and removes spectators as needed.
	static class SpectatorHandler extends BaseWebSocketHandler {
		Server server;
		public SpectatorHandler(Server s) { server = s; }
		public void onOpen(WebSocketConnection connection) { server.addSpectator(connection); }
		public void onClose(WebSocketConnection connection) { server.removeSpectator(connection); }
		public void onMessage(WebSocketConnection connection, String message) { }
	}

	// Player handler, adds and removes players as needed.
	static class PlayerHandler extends BaseWebSocketHandler {
		Server server;
		public PlayerHandler(Server s) { server = s; }
		public void onOpen(WebSocketConnection connection) { server.addPlayer(connection); }
		public void onClose(WebSocketConnection connection) { server.removePlayer(connection); }
		public void onMessage(WebSocketConnection connection, String message) { server.onPlayerMessage(connection, message); }
	}

	// Shutdown handler, stops the server and exits the program when invoked.
	static class ShutdownHandler implements HttpHandler {
		WebServer webServer;
		public ShutdownHandler(WebServer w) {
			webServer = w;
		}

		public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
			System.out.println("Shutting down server");
			response.header("Content-type", "text/html")
				.content("<a href=\"/\">Go back home</a>")
				.end();
			webServer.stop();
			System.exit(0);
		}
	}

	// The main function, creates the web server and web-sockets server.
    public static void main(String[] args) {
		Server server = new Server();
        WebServer webServer = WebServers.createWebServer(8080);
		webServer.add("/ws/games", new SpectatorHandler(server));
        webServer.add("/ws/play", new PlayerHandler(server));
		webServer.add("/shutdown", new ShutdownHandler(webServer));
		webServer.add(new StaticFileHandler("./html"));
        webServer.start();
        System.out.println("Trains.io web server running at " + webServer.getUri());
    }
}

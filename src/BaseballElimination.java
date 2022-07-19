import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.In;

public class BaseballElimination {
    private final int n; // the number of teams
    private final List<String> teams; // the name of teams
    private List<String> teamsInCut; // the teams that will be connected to the source in the min cut.
    private final HashMap<String, Integer> map; // map each team to its index
    private final int[] wins; // number of wins for team i.
    private final int[] losses; // number of losses for team i.
    private final int[] rem; // number of remaining games for team i.
    private final int[][] games; // number of games between teams i and j.

    // create a baseball division from given filename in format specified below
    // open the text file, read each line, and store the information in the corresponding fields.
    public BaseballElimination(String filename) {
        if (filename == null)
            throw new IllegalArgumentException();
        In in = new In(filename);
        this.n = in.readInt();
        this.teams = new ArrayList<>();
        this.wins = new int[n];
        this.losses = new int[n];
        this.rem = new int[n];
        this.games = new int[n][n];
        this.map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            String teamName = in.readString();
            teams.add(teamName);
            wins[i] = in.readInt();
            losses[i] = in.readInt();
            rem[i] = in.readInt();
            map.put(teamName, i);
            for (int k = 0; k < n; k++) {
                games[i][k] = in.readInt();
            }
        }
    }
    
    // check if the argument is null or if the team given is not in the table.
    private void checkTeam(String team) {
        if (team == null)
            throw new IllegalArgumentException("Null Argument");
        if (!this.map.containsKey(team))
            throw new IllegalArgumentException("Unavailable Team");
    }

    // number of teams
    public int numberOfTeams() {
        return n;
    }

    // all teams
    public Iterable<String> teams() {
        return teams;
    }

    // number of wins for given team
    public int wins(String team) {
        checkTeam(team);
        return wins[this.map.get(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        checkTeam(team);
        return losses[this.map.get(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        checkTeam(team);
        return rem[this.map.get(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        checkTeam(team1);
        checkTeam(team2);
        return games[this.map.get(team1)][this.map.get(team2)];
    }

    

    // is given team eliminated?
    public boolean isEliminated(String team) {
        checkTeam(team);
        this.teamsInCut = new ArrayList<>();
        int maxWins = this.wins(team) + this.remaining(team); // assume the team to be checked has won all its remaining matches.
        // Trivial Elimination
        for (String t : this.teams()) {
            if (this.wins(t) > maxWins) {
                teamsInCut.add(t); // if any team has more wins than the wins of our team, then it is surely eliminated from winning the league.
            }
        }
        if (!teamsInCut.isEmpty())
            return true;
        // Non Trivial Elimination.
        int teamVertices = n - 1;
        int gameVertices = ((n-1) * (n-2)) / 2;
        int numOfV = 2 + teamVertices + gameVertices; // Total number of vertices.
        HashMap<Integer, String> findTeam = new HashMap<>(); // map each team vertex to the corresponding team. 
        HashMap<Integer, List<Integer>> vToteams = new HashMap<>(); // map each game vertex to the teams that play that game.
        int i = 1;
        // Construct the first map
        for (String t : this.teams()) {
            if (!t.equals(team)) {
                findTeam.put(i, t);
                i++;
            }
        }
        // Construct the second map.
        for (int t1 = 1; t1 <= teamVertices - 1; t1++) {
            for (int t2 = t1 + 1; t2 <= teamVertices; t2++) {
                List<Integer> list = new ArrayList<>();
                list.add(t1);
                list.add(t2);
                vToteams.put(i, list);
                i++;
            }
        }
        // Construct the flow network.
        FlowNetwork f = new FlowNetwork(numOfV);
        for (int k = teamVertices + 1; k < numOfV - 1; k++) {
            int v1 = vToteams.get(k).get(0);
            int v2 = vToteams.get(k).get(1);
            int capacity = this.against(findTeam.get(v1), findTeam.get(v2));
            FlowEdge e1 = new FlowEdge(0, k, capacity);
            f.addEdge(e1);
            FlowEdge e2 = new FlowEdge(k, v1, Integer.MAX_VALUE);
            FlowEdge e3 = new FlowEdge(k, v2, Integer.MAX_VALUE);
            f.addEdge(e2);
            f.addEdge(e3);
        }
        int target = numOfV - 1;
        for (int k = 1; k <= teamVertices; k++) {
            int capacity = maxWins - this.wins(findTeam.get(k));
            FlowEdge e = new FlowEdge(k, target, capacity);
            f.addEdge(e);
        }
        // Compute the max flow.
        FordFulkerson ff = new FordFulkerson(f, 0, target);
        boolean elim = false;
        for (FlowEdge e : f.adj(0)) {
            if (e.flow() != e.capacity()) {
                elim = true;
                break;
            }
        }
        // Add the teams in the minimum cut.
        for (int k = 1; k <= teamVertices; k++) {
            if (ff.inCut(k)) {
                teamsInCut.add(findTeam.get(k));
            }
        }
        return elim;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        checkTeam(team);
        if (this.isEliminated(team))
            return teamsInCut;
        return null;
    }

    public static void main(String[] args) {
      /* BaseballElimination e = new BaseballElimination("teams5.txt");
        System.out.println(e.numberOfTeams());
        System.out.println(e.isEliminated("Detroit"));
        for (String t : e.certificateOfElimination("Detroit"))
            System.out.println(t); */
    }
}

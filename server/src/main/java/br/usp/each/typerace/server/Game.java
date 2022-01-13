package br.usp.each.typerace.server;


import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;

//class responsible for the game logic
//all the interaction in game are done in this class (score, words, vallidation, etc)


public class Game {
    
    Map<String, List<String>> playerWords = new HashMap<String, List<String>>();
    Map<String, Integer> playerScores = new HashMap<String, Integer>();
    Map<String, Integer> playerErrors = new HashMap<String, Integer>();
    

    private final List<String> wordList = new ArrayList<>(List.of("sheet", "incentive", "power", "battery", "appoint", "hen", "volunteer", "rabbit", "anticipation"));
    private final Integer WINNING_SCORE = 4;

    public Game(List<String> Players) {
        for (String player : Players) {
            addPlayer(player);
        }
    }

    public String sendWord(String player) {
        return playerWords.get(player).get(0);
    }


    public void verifyAnswer(String playerId, String word) {
        if(playerWords.get(playerId).contains(word)){
            playerWords.get(playerId).remove(word);
            playerScores.put(playerId, playerScores.get(playerId) + 1);
        }
        else{
            playerErrors.put(playerId, playerErrors.get(playerId) + 1);
        }
    }

    //verify if the game is over looking through the scores
    public String verifyWinner() {
        for (String player : playerScores.keySet()) {
            if(playerScores.get(player) >= WINNING_SCORE){
                return("Player " + player + " won the game!\n");
            }
        }
        return null;
    }


    //initialize the players in all the aspects nedded for the game (errors, scores, words)
    public void addPlayer(String playerId) {
        playerWords.put(playerId, wordList);
        playerScores.put(playerId, 0);
        playerErrors.put(playerId, 0);
    }


    //responsible for generating the leaderboard string to show to the players
    //the most important part here is the sorting of the players by score
    //to show the pontuation in descending order 
    public String leaderBoard() {

        int counter = 1;

        LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();

        playerScores.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) 
                    .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        String listString = "Leaderboard:\n";
        for (String player : reverseSortedMap.keySet()) {
            String indPlayer = "Position " + counter + "| " + player + " -> Total Points: " + reverseSortedMap.get(player) + " -> Total Errors: " + playerErrors.get(player) + "\n";
            listString = listString + indPlayer;
            counter++;
        }
        return listString;
    }

    public List<String> startGame() {
        return wordList;
    }

}

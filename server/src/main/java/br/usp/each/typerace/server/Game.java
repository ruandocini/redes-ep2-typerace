package br.usp.each.typerace.server;


import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class Game {
    
    Map<String, List<String>> playerWords = new HashMap<String, List<String>>();
    Map<String, Integer> playerScores = new HashMap<String, Integer>();

    private final List<String> wordList = new ArrayList<>(List.of("sheet", "incentive", "power", "battery", "appoint", "hen", "volunteer", "rabbit", "anticipation"));
    private final Integer WINNING_SCORE = 2;

    public Game(List<String> Players) {
        for (String player : Players) {
            addPlayer(player);
        }
    }

    public void verifyAnswer(String playerId, String word) {
        if(playerWords.get(playerId).contains(word)){
            playerWords.get(playerId).remove(word);
            playerScores.put(playerId, playerScores.get(playerId) + 1);
        }
    }

    public String verifyWinner() {
        for (String player : playerScores.keySet()) {
            if(playerScores.get(player) >= WINNING_SCORE){
                return("Player " + player + " won the game!");
            }
        }
        return null;
    }

    public void addPlayer(String playerId) {
        playerWords.put(playerId, wordList);
        playerScores.put(playerId, 0);
    }

    public String leaderBoard() {
        String listString = "Leaderboard:\n";
        for (String player : playerScores.keySet()) {
            String indPlayer = player + ": " + playerScores.get(player) + "\n";
            listString = listString + indPlayer;
        }
        return listString;
    }

    public List<String> startGame() {
        return wordList;
    }


}

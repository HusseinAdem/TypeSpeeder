package se.ju23.typespeeder.gameLogic;

import se.ju23.typespeeder.InfoForUsers.NewsLetter;

import java.time.LocalTime;
import java.util.List;

public interface Playable {

    Status checkUser(String username, String password);

    void calculateTotalPointsForGame(String userAnswer);


    String printChallengeResult();

    String printScoreBoardBasedOnThree();

    //NewsLetter printNewsletter();

    String printScoreBoardBasedOnLevel();


    void setCurrentSolution(List<String> currentSolution1);

    String printUserInfo();

    String beforeGameStartsText();



    Status standbyInMainMenu(int input);


    void setGameDifficulty(int gameDifficulty1);

    Status standbyInSettingsMenu(int input);

    void setCurrentTaskId(int currentTaskId);

    String getCurrentUsername(int place);
    void setCurrentUsername(String newCurrentEmail);

    void setNewAlias(String input);


    void setNewUsername(String newEmail);

    boolean checkCurrentUsername(String input);

    boolean checkIfUserNameIsBusy(String input);

    void setNewPassword(String newPassword);

    boolean checkCurrentPassword(String input);


    void setTimeResult(double timeResult);

    void setNumOfWords(int words);



    int getCurrentUserId();

    String getCurrentAlias(int place);

    String getPassword(int place);


    int getCurrentLevel();
}

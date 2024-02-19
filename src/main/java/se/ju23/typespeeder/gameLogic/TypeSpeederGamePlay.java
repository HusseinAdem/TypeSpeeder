package se.ju23.typespeeder.gameLogic;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.ju23.typespeeder.classesFromDB.*;
import se.ju23.typespeeder.userInterfaces.MenuService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
import java.util.*;

@Component
public class TypeSpeederGamePlay implements Playable {
    MenuService menuService;

    public static LocalTime startGame = LocalTime.now();
    public static LocalTime endGame = LocalTime.now();

    int correctAnswers = 0;
    int correctAnswersInRow = 0;
    String currentPointsForPrinting = "";
    String currentUserGuess = "";

    public int currentUserId = 0;
    public int currentTaskId = 0;
    double timeResult = 0;
    public String[] currentEmail = {"", "", ""};
    public String[] currentAlias = {"", ""};
    public int currentXp = 0;
    public int currentLevel = 0;
    public String[] currentPassword = {"", "", ""};
    public static List<String> currentSolution = new ArrayList<>();
    IChallenge challenge;




    public TypeSpeederGamePlay() {

    }

    @Autowired
    UsersRepo usersRepo;
    @Autowired
    AttemptRepo attemptRepo;
    @Autowired
    PointParamRepo pointParamRepo;
    @Autowired
    TasksRepo tasksRepo;

    public Status checkUser(String email, String password) {
        printLeaderBoard();


        Optional<Users> users = usersRepo.findByEmailAndPassword(email, password);
        if (users.isPresent()) {
            Users found = users.get();
            currentUserId = found.getUserId();
            currentEmail[0] = found.getEmail();
            currentAlias[0] = found.getAlias();
            currentXp = found.getXp();
            currentLevel = found.getLevel();
            currentPassword[0] = found.getPassword();
            return Status.VERIFIED;
        } else {
            currentEmail[0] = "1";

            return Status.NO_USER_FOUND;
        }
    }

    @Override
    public void calculateTotalPointsForGame(String userAnswer){

        correctAnswers = 0;
        correctAnswersInRow = 0;
        currentUserGuess = userAnswer;
        List<String> userAnswerList = new ArrayList<>(Arrays.asList(userAnswer.split("\\s+")));

        for (int i = 0; i < userAnswerList.size(); i++) {
            for (int j = 0; j < currentSolution.size(); j++) {
                if (userAnswerList.get(i).equals(currentSolution.get(j))){
                    correctAnswers++;
                }
            }
        }
        int find = 0;
        for (int i = 0; i < userAnswerList.size(); i++) {

            if (userAnswerList.get(i).equals(currentSolution.get(find))){
                correctAnswersInRow++;
            }
            find++;
        }

        saveAttemptToDB();
    }
    public void saveAttemptToDB(){
        Tasks t = null;
        Users u = null;
        Optional<Tasks> findTask = tasksRepo.findById(currentTaskId);
        if (findTask.isPresent()){
            t = findTask.get();
        }
        Optional<Users> findUser = usersRepo.findById(currentUserId);
        if (findUser.isPresent()){
            u = findUser.get();
        }
        Attempt newAttempt = new Attempt(calculatedPoints(),currentSolutionToString(),currentUserGuess,u,t);
        attemptRepo.save(newAttempt);
        PointParam pointParam = new PointParam(getTimeResult(),correctAnswers,correctAnswersInRow,newAttempt);
        pointParamRepo.save(pointParam);

    }
    @Override
    public String printChallengeResult(){

        return "--------\n" +
                "| Correct Answers: " + correctAnswers + " |" +
                "\n| Correct Answers in row: " + correctAnswersInRow + " |" +
                "\n| Your Time: " + getTimeResult() + " Seconds" + "|" + "\n| Points: " + currentPointsForPrinting + "|" +
                "\n--------\n";
    }
    @Override
    public String printLeaderBoard(){
        List<Users> topListOfUsers = new ArrayList<>();
        List<String> leaderboardList = new ArrayList<>();
        List<Users> users = usersRepo.findAll();
        int index = 0;
        for (int i = 0; i < users.size(); i++) {

            double correct = 0;
            double correctInOrder = 0;
            double speedInSec = 0;
            int totalAtt = 0;
            String alias = users.get(i).getAlias();
            int level = users.get(i).getLevel();
            int userId = users.get(i).getUserId();
            int userXp = users.get(i).getXp();
            List<Attempt> attUserId = attemptRepo.findByUserId(userId);
            for (int j = 0; j < attUserId.size(); j++) {
                int attId = attUserId.get(j).getAttemptId();
                totalAtt = attUserId.size();
                List<PointParam> uParam = pointParamRepo.findPointParamByAttemptId(attId);
                for (int k = 0; k < uParam.size(); k++) {
                    correct += uParam.get(k).getCorrect();
                    correctInOrder += uParam.get(k).getCorrectInOrder();
                    speedInSec += uParam.get(k).getSpeedInSec();
                }

            }
            topListOfUsers.add(users.get(i));

            leaderboardList.add(index,"Alias: " + alias +
                    "\nXp: " + String.valueOf(userXp) +
                    "\nCorrect %: " + (correct/(totalAtt*7)*100) +
                    "\nCorrect in order %: " + (correctInOrder/(totalAtt*7)*100) +
                    "\nSpeed average: " + (speedInSec/totalAtt) + "\n-------------\n");
            index++;
            //TODO FORTSÄTT MED XP OCH LEVELUPPGRADERING
            //TODO FORMATERA UTSKRIFTER TILL TVÅ DECIMALER
            //TODO OCH SNYGGARE UTSKRIFT AV LEADERBOARD
            //TODO NÄR MAN LEVLAR SÅ SKA ORDEN BLI 2 FLER



        }
        StringBuilder result = new StringBuilder("    SCOREBOARD\n    player          Level   XP\n");
        int pos = 1;
        topListOfUsers.sort((p1,p2) -> Integer.compare(p2.getLevel(), p1.getLevel()));
        for (Users u : topListOfUsers){
            result.append(String.format("%5d %-9s%7.0f%7.0f%n", pos, (u.getEmail()), (double)u.getLevel(),(double)u.getXp()));
            if (pos++ ==10) break;
        }

        return result.toString();
    }

    @Override
    public double calculatedPoints(){
        double points;
        double userTime = getTimeResult();
        double maxTime = 120;
        if (userTime>maxTime){
            points = 0;

        } else if (correctAnswers == 0 && correctAnswersInRow == 0){
            points = 0;
        } else {
            points = (correctAnswers + correctAnswersInRow + (10 * Math.pow((1 - (userTime / maxTime)), 1)));
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat df = new DecimalFormat("#.##", symbols);
        double a = Double.parseDouble(df.format(points));
        currentPointsForPrinting = String.valueOf(a);
        return a;
    }
    public String currentSolutionToString(){
        StringBuilder sb = new StringBuilder();
        for (String s : currentSolution){
            sb.append(s).append(" ");
        }
        return sb.toString();
    }

    public static List<String> getCurrentSolution() {
        return currentSolution;
    }
    @Override
    public void setCurrentSolution(List<String> currentSolution1) {
        currentSolution = currentSolution1;
    }

    @Override
    public String beforeGameStartsText(){
        return "Time starts when you press ENTER, READY?";
    }


    @Override
    public Status standbyInMainMenu(int input) {
        currentPassword[2] = "";
        currentEmail[2] = "";

        Status status = null;
        switch (input) {
            case 0 -> status = Status.EXIT;
            case 1 -> status = Status.SETTING_LANGUAGE;
            case 2 -> status = Status.ACTIVE_IN_GAME;
            case 3 -> status = Status.IN_STATS;
            case 4 -> status = Status.IN_GAME_SETTINGS;
        }
        return status;
    }


    @Override
    public Status standbyInSettingsMenu(int input) {
        Status status = null;
        switch (input) {
            case 1 -> status = Status.CHANGING_ALIAS;
            case 2 -> status = Status.CHANGING_PASSWORD;
            case 3 -> status = Status.CHANGING_USERNAME;
        }
        return status;
    }
    @Override
    public void setNewAlias(String input) {

        List<Users> aliasFromDB = usersRepo.findByAlias(getCurrentAlias(0));
        for (Users u : aliasFromDB) {
            u.setAlias(input);
            usersRepo.save(u);
            currentAlias[0] = input;
            currentAlias[1] = "1";
        }
    }
    @Override
    public void setNewUsername(String newUsername) {
        Optional<Users> emailFromDB = usersRepo.findByUserId(getCurrentUserId());
        if (emailFromDB.isPresent()) {
            Users found = emailFromDB.get();
            found.setEmail(newUsername);
            usersRepo.save(found);
            currentEmail[1] = "1";
        }
    }
    @Override
    public boolean checkCurrentEmail(String input) {
        if (Objects.equals(input, getCurrentEmail(0))) {
            currentEmail[1] = input;
            return true;
        } else {
            currentEmail[2] = "2";
            return false;
        }

    }
    @Override
    public boolean checkIfUserNameIsBusy(String input) {
        Optional<Users> checkIfBusy = usersRepo.findByEmail(input);
        if (checkIfBusy.isEmpty()) {
            return true;
        } else {
            currentEmail[1] = "";
            currentEmail[2] = "2";
            return false;
        }
    }
    @Override
    public void setNewPassword(String newPassword) {
        Optional<Users> passwordFromDB = usersRepo.findById(getCurrentUserId());
        if (passwordFromDB.isPresent()) {
            Users found = passwordFromDB.get();
            found.setPassword(newPassword);
            usersRepo.save(found);
            currentPassword[1] = "1";
        }
    }
    @Override
    public boolean checkCurrentPassword(String input) {
        if (Objects.equals(input, getPassword(0))) {
            currentPassword[1] = input;
            return true;
        } else {
            currentPassword[2] = "2";
            return false;
        }
    }

    public double getTimeResult() {
        return timeResult;
    }
    @Override
    public void setTimeResult(double timeResult) {
        this.timeResult = timeResult;
    }

    public static LocalTime getStartGame() {
        return startGame;
    }
    @Override
    public void setStartGame(LocalTime startGame1) {
       startGame = startGame1;
    }

    public static LocalTime getEndGame() {
        return endGame;
    }
    @Override
    public void setEndGame(LocalTime endGame1) {
        endGame = endGame1;
    }

@Override
    public void setCurrentTaskId(int currentTaskId1) {
        currentTaskId = currentTaskId1;
    }

    public void setCurrentUserGuess(String currentUserGuess) {
        this.currentUserGuess = currentUserGuess;
    }

    @Override
    public String getCurrentEmail(int place) {
        return currentEmail[place];
    }
    @Override
    public void setCurrentEmail(String input) {
        currentEmail[0] = input;
    }
    @Override
    public int getCurrentUserId() {
        return currentUserId;
    }
    @Override
    public String noUserFoundText() {
        return null;
    }
    @Override
    public String getCurrentAlias(int place) {
        return currentAlias[place];
    }
    @Override
    public String getPassword(int place) {
        return currentPassword[place];
    }
    @Override
    public int getCurrentXp() {
        return currentXp;
    }
    @Override
    public int getCurrentLevel() {
        return currentLevel;
    }
}

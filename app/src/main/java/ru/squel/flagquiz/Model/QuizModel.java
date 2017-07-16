package ru.squel.flagquiz.Model;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Саша on 15.07.2017.
 * Текущая игра
 */
public class QuizModel {

    private static final String LOG_TAG = QuizModel.class.getSimpleName();

    // параметры текущей игры
    private int qestionsCount = 10;
    private int choices;
    private Set<String> regionsInGame;
    private HashMap<String, ArrayList<String>> countriesInGame;

    public QuizModel() {

    }

    /**
     * Устанавливаются параметры текущей игры
     * @param choices
     * @param regionsInGame
     * @param countriesInGame
     */
    public void updateQuizeParameters(int qestionsCount,
                                      int choices,
                                      Set<String> regionsInGame,
                                      HashMap<String, ArrayList<String>> countriesInGame) {
        this.qestionsCount = qestionsCount;
        this.choices = choices;
        this.regionsInGame = regionsInGame;
        this.countriesInGame = countriesInGame;
    }

    public String getCountryToQuestion(@Nullable String region) {
        String randomRegionKey;

        // выбираю страну для вопроса
        Random rand = new Random(System.currentTimeMillis());

        List<String> keys = new ArrayList<String>(countriesInGame.keySet());
        if (region == null)
            randomRegionKey = keys.get(rand.nextInt(keys.size()));
        else
            randomRegionKey = region;
        int sizeOfRegion = countriesInGame.get(randomRegionKey).size();
        int countryNumber = rand.nextInt(sizeOfRegion);
        String countryToQuestion = (countriesInGame.get(randomRegionKey)).get(countryNumber);
        return countryToQuestion;
    }

    public ArrayList<String> getCountryListForAnswers(String countryToQuestion, boolean addFromRandRegion)
    {
        Set<String> set = new HashSet<>();
        set.add(countryToQuestion);

        Random rand = new Random(System.currentTimeMillis() - 1546876);
        //выбор случайного региона
        List<String> regionsInGameList = new ArrayList<>(regionsInGame);
        String randomRegionKey = countryToQuestion.substring(0, countryToQuestion.indexOf('-'));

        for(int i = 0; set.size() < choices; i++ ) {
            String countryToAnswer = getCountryToQuestion(randomRegionKey);
            set.add(countryToAnswer);

            if (addFromRandRegion) {
                List<String> keys = new ArrayList<String>(countriesInGame.keySet());
                randomRegionKey = keys.get(rand.nextInt(keys.size()));
            }
        }

        ArrayList<String> list = new ArrayList<>();
        list.addAll(set);

        return list;
    }

    public int getChoices() {
        return  choices;
    }
}

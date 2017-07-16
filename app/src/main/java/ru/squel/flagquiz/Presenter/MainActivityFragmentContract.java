package ru.squel.flagquiz.Presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by sq on 15.07.2017.
 */
public interface MainActivityFragmentContract {

    interface View extends BaseView<Presenter> {
        //отрисовывает на экране следующий вопрос
        void getNewStepOfQuiz(int questionNumber, int totalQusetionsNumber, String countryName, ArrayList<String> answersList);
        HashMap<String, ArrayList<String>> formCountriesList (Set<String> regionsInQuiz);
        void gameOver(final int rightAnswers, final int countQuestions);
        void displayRightAnswer(String rightAnswer);
        void displayFailedAnswer(String rightAnswer);
        String prepareFileNameToDisplay(String fileName);
    }

    interface Presenter extends BasePresenter {
        // возвращает число вариантов ответов в этой игре
        int getChoices();
        //
        boolean checkUserAnswer(String answer);
    }
}

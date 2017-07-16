package ru.squel.flagquiz.Presenter;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ru.squel.flagquiz.Model.QuizModel;

/**
 * Created by sq on 15.07.2017.
 */
public class MainActivityFragmentPresenter implements MainActivityFragmentContract.Presenter {

    private static final String LOG_TAG = MainActivityFragmentPresenter.class.getSimpleName();

    // данные
    private QuizModel currentQuiz;
    // представление
    private MainActivityFragmentContract.View currentView;

    // результаты игры и текущего раунда
    private String currentAnswer;
    private int currentQuestionNumber = 0;
    private int rightAnswerNumber = 0;
    private int iterationAnswerNumber = 0;
    private int countQuestions = 0;
    private String questionsDefaultVal = "10";
    private String choicesDefaultVal = "8";
    private boolean randomRegionAnswerDefault = true;
    // выбор региона для вариантов ответа - true, если из всех возможных регионов,
    // false если только из того же региона, что и вопрос
    private boolean randomRegionAnswer;

    //Конструктор создает новый объект данных и сохраняет view
    public MainActivityFragmentPresenter(MainActivityFragmentContract.View view) {
        currentQuiz = new QuizModel();

        currentView = view;
        // установил этот презентер в фрагмент
        view.setPresenter(this);
    }

    /**
     * Запуск игры
     */
    public void start() {
        makeNewStep();
    }

    /**
     * Применение изменений параметров в настройках приложения
     * @param sharedPreferences - параметры приложения
     */
    public void updateQuizParameters(SharedPreferences sharedPreferences) {
        // получение праметров текущей игры
        int choices = (Integer.parseInt(sharedPreferences.getString("pref_numberOfChoices", choicesDefaultVal)));
        Set<String> regionsInGame = sharedPreferences.getStringSet("pref_regionsToInclude", null);
        HashMap<String, ArrayList<String>> countriesInGame = currentView.formCountriesList(regionsInGame);
        countQuestions = (Integer.parseInt(sharedPreferences.getString("pref_countOfQuestions", questionsDefaultVal)));
        randomRegionAnswer = (sharedPreferences.getBoolean("pref_regions_random", randomRegionAnswerDefault));

        // TODO Внести в параметры приложения выбор количества попыток ответа, сейчас не используется
        iterationAnswerNumber = 0;

        // обновление параметров Quiz
        currentQuiz.updateQuizeParameters(countQuestions, choices, regionsInGame, countriesInGame);
    }

    /**
     * Выполнить отображение нового вопроса
     */
    private void makeNewStep() {

        // полчуаю вопрос и варианты ответов
        String countryToQuestion = currentQuiz.getCountryToQuestion(null);

        ArrayList<String> list = currentQuiz.getCountryListForAnswers(countryToQuestion, randomRegionAnswer);
        // отправляю команду вьюхе отобразить вопрос
        currentView.getNewStepOfQuiz(currentQuestionNumber, countQuestions, countryToQuestion, list);
        // увеличиваю на единицу счетчик вопросов
        currentQuestionNumber += 1;
        // запоминаю правильный ответ
        currentAnswer = currentView.prepareFileNameToDisplay(countryToQuestion);
    }

    /**
     * Обработчик ответа пользователя
     * @param answer
     * @return
     */
    public boolean checkUserAnswer(String answer) {

        // если полученный ответ и сохраненный равны
        if (answer.intern() == currentAnswer.intern()) {
            rightAnswerNumber += 1;
            currentView.displayRightAnswer(currentAnswer);
        }
        else {
            // если не равны
            currentView.displayFailedAnswer(currentAnswer);
        }

        // если это был последний вопрос
        if (currentQuestionNumber == countQuestions) {
            currentView.gameOver(rightAnswerNumber, countQuestions);
            rightAnswerNumber = 0;
            currentQuestionNumber = 0;
        }
        else if (currentQuestionNumber < countQuestions) {
            // если нужны еще вопросы
            makeNewStep();
        }

        return false;
    }

    public int getChoices() {return currentQuiz.getChoices();}
}

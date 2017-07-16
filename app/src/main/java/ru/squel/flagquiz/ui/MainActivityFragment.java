package ru.squel.flagquiz.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import ru.squel.flagquiz.Presenter.MainActivityFragmentContract;
import ru.squel.flagquiz.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements MainActivityFragmentContract.View {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    // Презентер для этого фрагмента
    private MainActivityFragmentContract.Presenter mMainActivityFragmentPresenter;
    // Анимация неправильного ответа
    private Animation shakeAnimation;
    private Handler handler; // Для задержки загрузки следующего флага

    // Получение ссылок на компоненты графического интерфейса
    private LinearLayout guessLinearLayouts[];
    private LinearLayout quizLinearLayout;
    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private TextView answerTextView;

    /**
     * Пустой конструктор для создания фрагмента
     */
    public MainActivityFragment() {
    }

    @Override
    public void setPresenter(MainActivityFragmentContract.Presenter presenter) {
        mMainActivityFragmentPresenter = presenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // для основного макета
        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        // для заголовка с номером вопроса
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        // для флага
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        // для рядов кнопок
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        // для ответа
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);
        // configure listeners for the guess Buttons
        for (LinearLayout row : guessLinearLayouts)
        {
            for (int column = 0; column < row.getChildCount(); column++)
            {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        // Загрузка анимации для неправильных ответов
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(5); // Анимация повторяется 3 раза

        handler = new Handler();

        return view;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Выполняет отображение нового вопроса
     * @param questionNumber - номер вопроса в этой игре
     * @param totalQusetionsNumber - число вопросов
     * @param countryName - название региона-страны для отображения
     */
    public void getNewStepOfQuiz(final int questionNumber, final int totalQusetionsNumber, final String countryName, final ArrayList<String> answersList) {

        //сброс последнего варианта ответа
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                answerTextView.setText(" ");

                // Отображение номера текущего вопроса
                questionNumberTextView.setText(getString(R.string.question, questionNumber + 1, totalQusetionsNumber));

                // Извлечение региона из имени следующего изображения
                String region = countryName.substring(0, countryName.indexOf('-'));

                // Использование AssetManager для загрузки следующего изображения
                // asset - это аналог каталога res, только более абстрактный - нет идентификаторов ресурсов
                AssetManager assets = getActivity().getAssets();

                // Получение объекта InputStream для ресурса следующего флага
                // и попытка использования InputStream
                try (InputStream stream =
                             assets.open("FlagQuizImages/" + region + "/" + countryName + ".png")) {
                    // Загрузка графики в виде объекта Drawable и вывод на flagImageView
                    Drawable flag = Drawable.createFromStream(stream, countryName);
                    flagImageView.setImageDrawable(flag);
                    //animate(false); // Анимация появления флага на экране
                }
                catch (IOException exception) {
                    Log.e(LOG_TAG, "Error loading " + countryName, exception);
                }

                // скрывание всех кнопок
                for (LinearLayout layout : guessLinearLayouts)
                    layout.setVisibility(View.GONE);

                // перестановка вариантов ответа
                Collections.shuffle(answersList);
                // Добавление 2, 4, 6 или 8 кнопок в зависимости от значения guessRows
                int countOfRows = mMainActivityFragmentPresenter.getChoices()/2;
                for (int row = 0; row < countOfRows; row++) {
                    // отобразить кнопки
                    guessLinearLayouts[row].setVisibility(View.VISIBLE);
                    //размещение кнопок в currentTableRow
                    for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++) {
                        // Получение ссылки на Button
                        Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                        newGuessButton.setEnabled(true);

                        // Назначение названия страны текстом newGuessButton
                        String buttonTextAnswer = prepareFileNameToDisplay(answersList.get((row * 2) + column));
                        // Отоюразил текст
                        newGuessButton.setText(buttonTextAnswer);
                    }
                }
            }
        }, 500);
    }

    /**
     * Формирует список стран из участвующих в игре регионов
     * @param regionsInQuiz - вектор регионов, которые находятся в игре
     * @return вектор векторов с названиями стран в векторах с названиями регионов
     */
    public HashMap<String, ArrayList<String>> formCountriesList (Set<String> regionsInQuiz) {
        HashMap<String, ArrayList<String>> map = new HashMap();
        AssetManager assets = getActivity().getAssets();

        for (String s : regionsInQuiz) {
            ArrayList<String> tempList = new ArrayList<>();
            try {
                    String[] paths = assets.list("FlagQuizImages/" + s);
                    for (String path : paths) {
                        tempList.add(path.substring(0, path.length() - 4)); //удалил расширение
                    }
                map.put(s, tempList);
            }
            catch (IOException exception) {
                Log.e(LOG_TAG, "Error formCountriesList " + s + " ", exception);
            }
        }
        return map;
    }

    /**
     * Отображает завершение игры
     * @param rightAnswers
     * @param countQuestions
     */
    public void gameOver(final int rightAnswers, final int countQuestions) {
        // DialogFragment для вывода статистики и перезапуска
        DialogFragment quizResults =
                new DialogFragment() {
                    // Создание окна AlertDialog
                    @Override
                    public Dialog onCreateDialog(Bundle bundle) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(
                                getString(R.string.results, rightAnswers,
                                        (100 * (double)rightAnswers/countQuestions)));

                        // Кнопка сброса "Reset Quiz"
                        builder.setPositiveButton(R.string.reset_quiz,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Загрузка следующего флага после двухсекундной задержки
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMainActivityFragmentPresenter.start();
                                            }
                                        }, 500); // 500 миллисекунд для задержки
                                    }
                                }
                        );
                        return builder.create(); // Вернуть AlertDialog
                    }
                };
        // Использование FragmentManager для вывода DialogFragment
        quizResults.setCancelable(false);
        quizResults.show(getFragmentManager(), "quiz results");
    }

    /**
     * Отображает правильный вариант ответа
     * @param rightAnswer
     */
    public void displayRightAnswer(String rightAnswer) {
        answerTextView.setTextColor(getResources().getColor(R.color.correct_answer, getContext().getTheme()));
        answerTextView.setText(rightAnswer);
    }

    /**
     * Отображает НЕ правильный вариант ответа
     * @param rightAnswer
     */
    public void displayFailedAnswer(String rightAnswer) {
        answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer, getContext().getTheme()));
        answerTextView.setText(rightAnswer);
        flagImageView.startAnimation(shakeAnimation);
    }

    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            mMainActivityFragmentPresenter.checkUserAnswer(guess);
        }
    };

    /**
     * Подготавливает к отображению на кнопке название файла с флагом:
     * удаляет регион, заменяет нижние подчеркивания пробелами
     * @param fileName - название файла с флагом
     * @return
     */
    public String prepareFileNameToDisplay(String fileName) {
        String res;
        // Удаление региона из названия
        res = fileName.substring(fileName.indexOf('-') + 1);
        // замена подчеркиваний на пробелы
        res = res.replace("_", " ");
        return res;
    }


    // Весь макет quizLinearLayout появляется или исчезает с экрана
    private void animate(boolean animateOut) {
        // Вычисление координат центра
        int centerX = (quizLinearLayout.getLeft() +
                quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop() +
                quizLinearLayout.getBottom()) / 2;

        // Вычисление радиуса анимации
        int radius = Math.max(quizLinearLayout.getWidth(), quizLinearLayout.getHeight());

        Animator animator;
        // Если изображение должно исчезать с экрана
        if (animateOut) {
            // Создание круговой анимации
            animator = ViewAnimationUtils.createCircularReveal(
                    quizLinearLayout, centerX, centerY, radius, 0);
            animator.addListener(
                    new AnimatorListenerAdapter() {
                }
            );
        }
        else { // Если макет quizLinearLayout должен появиться
            animator = ViewAnimationUtils.createCircularReveal(
                    quizLinearLayout, centerX, centerY, 0, radius);
            }

        animator.setDuration(500); // Анимация продолжительностью 500 мс
        animator.start(); // Начало анимации
        }
}

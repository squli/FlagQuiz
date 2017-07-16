package ru.squel.flagquiz.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import ru.squel.flagquiz.Presenter.MainActivityFragmentPresenter;
import ru.squel.flagquiz.R;
import ru.squel.flagquiz.util.ActivityUtils;

public class MainActivity extends AppCompatActivity {

    // константы для поиска параметров
    private static final String CHOICES = "pref_numberOfChoices";
    private static final String REGIONS = "pref_regionsToInclude";

    // флаг работы на телефоне, будт разрешен только портетный режим
    private boolean phoneDevice = true;
    // флаг наличия изменений в настройках для обработки
    private boolean preferencesChanged = true;
    // презентер для работы с основным фрагментом
    private MainActivityFragmentPresenter mMainActivityFragmentPresenter;

    /**
     * Метод onCreate() вызывается при создании или перезапуска активности.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // установка значений по-умолчанию в SharedPreferences
        // flase - что устанавливаются только при первом вызове метода
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Регистрация слушателя для изменений SharedPreferences
        // для вызова обработчиков изменений при изменении настроек
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);

        // Определение размера экрана
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        // Для планшета phoneDevice = false
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            phoneDevice = false;
        }

        // Для телефона принудительно выставляется только портретная ориаентация
        if (phoneDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        /**
         * Код создания фрагмента и его презентера
         */
        MainActivityFragment quizFragment = (MainActivityFragment)
                getSupportFragmentManager().findFragmentById(
                        R.id.quizFragment);

        // если такого фрагмента еще нет, нужно создавать
        if (quizFragment == null) {

            quizFragment = new MainActivityFragment();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), quizFragment, R.id.quizFragment);

            // создаю нового презентера для основного фрагмента
            mMainActivityFragmentPresenter = new MainActivityFragmentPresenter(quizFragment);
        }
        else {
            // если фрагмент уже есть то проверяю, а есть ли у него презентер
            if (mMainActivityFragmentPresenter == null) {
                // создаю нового презентера для основного фрагмента
                mMainActivityFragmentPresenter = new MainActivityFragmentPresenter(quizFragment);
            }
        }
    }

    /*
     * При первом запуске приложения метод onStart вызывается после onCreate.
     * В этом случае вызов onStart гарантирует, что приложение будет правильно инициализировано в
     * состоянии по умолчанию при установке и первом запуске или в соответствии с
     * обновленной конфигурацией пользователя при последующих запусках.
     * Когда пользователь возвращается посел правки настроек к MainActivity,
     * снова вызывается метод onStart.
     */
    @Override
    protected void onStart() {
        // Вызывается после завершения выполнения onCreate
        super.onStart();

        if (preferencesChanged) {
            // После задания настроек по умолчанию инициализировать
            // MainActivityFragment и запустить викторину
            mMainActivityFragmentPresenter.updateQuizParameters(PreferenceManager.getDefaultSharedPreferences(this));
            mMainActivityFragmentPresenter.start();
            preferencesChanged = false;
        }
    }

    /**
     * Вызывается для инициализации стандартного меню активности, меню будет отображаться только
     * в портретной ориентации, поэтому создаваться будет не всегда.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else
            return false;
    }

    /**
     * Вызывается при выборе команды меню, там всего один пункт, Он будет вызывать
     * активити настроек.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent preferencesIntent = new Intent(this, SettingsActivity.class);
            startActivity(preferencesIntent);
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

    // Слушатель изменений в конфигурации SharedPreferences приложения
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    mMainActivityFragmentPresenter.updateQuizParameters(sharedPreferences);
                    preferencesChanged = true;
                }
            };
}

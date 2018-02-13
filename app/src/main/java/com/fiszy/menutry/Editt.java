package com.fiszy.menutry;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.fiszy.menutry.R.styleable.CompoundButton;

public class Editt extends AppCompatActivity {



    private EditText mTitle;
    private CheckBox mAlarmEnabled;
    private Spinner mOccurence;
    private Button mDateButton;
    private Button mTimeButton;

    private Alarm mAlarm;
    private DateTime mDateTime;

    private GregorianCalendar mCalendar;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;
    static final int DAYS_DIALOG_ID = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity);


        mTitle = (EditText)findViewById(R.id.title);
        mAlarmEnabled = (CheckBox)findViewById(R.id.alarm_checkbox);
        mOccurence = (Spinner)findViewById(R.id.occurence_spinner);
        mDateButton = (Button)findViewById(R.id.date_button);
        mTimeButton = (Button)findViewById(R.id.time_button);

        mAlarm = new Alarm(this);
        mAlarm.fromIntent(getIntent());

        mDateTime = new DateTime(this);

        mTitle.setText(mAlarm.getTitle());
        mTitle.addTextChangedListener(mTitleChangedListener);

        mOccurence.setSelection(mAlarm.getOccurence());
        mOccurence.setOnItemSelectedListener(mOccurenceSelectedListener);

        mAlarmEnabled.setChecked(mAlarm.getEnabled());
        mAlarmEnabled.setOnCheckedChangeListener(mAlarmEnabledChangeListener);

        mCalendar = new GregorianCalendar();
        mCalendar.setTimeInMillis(mAlarm.getDate());
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);

        updateButtons();
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        if (DATE_DIALOG_ID == id)
            return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        else if (TIME_DIALOG_ID == id)
            return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, mDateTime.is24hClock());
        else if (DAYS_DIALOG_ID == id)
            return DaysPickerDialog();
        else
            return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog)
    {
        if (DATE_DIALOG_ID == id)
            ((DatePickerDialog)dialog).updateDate(mYear, mMonth, mDay);
        else if (TIME_DIALOG_ID == id)
            ((TimePickerDialog)dialog).updateTime(mHour, mMinute);
    }

    public void onDateClick(View view)
    {
        if (Alarm.ONCE == mAlarm.getOccurence())
            showDialog(DATE_DIALOG_ID);
        else if (Alarm.WEEKLY == mAlarm.getOccurence())
            showDialog(DAYS_DIALOG_ID);
    }

    public void onTimeClick(View view)
    {
        showDialog(TIME_DIALOG_ID);
    }

    public void onDoneClick(View view)
    {
        Intent intent = new Intent();

        mAlarm.toIntent(intent);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCancelClick(View view)
    {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
    {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;

            mCalendar = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            mAlarm.setDate(mCalendar.getTimeInMillis());

            updateButtons();
        }
    };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener()
    {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute)
        {
            mHour = hourOfDay;
            mMinute = minute;

            mCalendar = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
            mAlarm.setDate(mCalendar.getTimeInMillis());

            updateButtons();
        }
    };

    private TextWatcher mTitleChangedListener = new TextWatcher()
    {
        public void afterTextChanged(Editable s)
        {
            mAlarm.setTitle(mTitle.getText().toString());
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }
    };

    private AdapterView.OnItemSelectedListener mOccurenceSelectedListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
        {
            mAlarm.setOccurence(position);
            updateButtons();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {
        }
    };

    public CompoundButton.OnCheckedChangeListener mAlarmEnabledChangeListener = new CompoundButton.OnCheckedChangeListener()
    {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            mAlarm.setEnabled(isChecked);
        }
    };

    private void updateButtons()
    {
        if (Alarm.ONCE == mAlarm.getOccurence())
            mDateButton.setText(mDateTime.formatDate(mAlarm));
        else if (Alarm.WEEKLY == mAlarm.getOccurence())
            mDateButton.setText(mDateTime.formatDays(mAlarm));
        mTimeButton.setText(mDateTime.formatTime(mAlarm));
    }

    private Dialog DaysPickerDialog()
    {
        AlertDialog.Builder builder;
        final boolean[] days = mDateTime.getDays(mAlarm);
        final String[] names = mDateTime.getFullDayNames();

        builder = new AlertDialog.Builder(this);

        builder.setTitle("Week days");

        builder.setMultiChoiceItems(names, days, new DialogInterface.OnMultiChoiceClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton, boolean isChecked)
            {
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                mDateTime.setDays(mAlarm, days);
                updateButtons();
            }
        });

        builder.setNegativeButton("Cancel", null);

        return builder.create();
    }
  /**  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_new).setVisible(false);
       // menu.findItem(R.id.menu_item_delete).setVisible(false);
        return result;
    }
**/

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
      //  MenuInflater inflater = getMenuInflater();

      //inflater.inflate(R.menu.menu, menu);
      getMenuInflater().inflate(R.menu.menu_new,menu);
       menu.findItem(R.id.menu_item_new).setVisible(false);
      // menu.findItem(R.id.menu_item_delete).setVisible(true);
      return super.onCreateOptionsMenu(menu);

  }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){

            case R.id.menu_about:
                Intent intent1 = new Intent(getBaseContext(), About.class);
                startActivity(intent1);
                return true;
            case R.id.menu_settings:
                Intent intent2 = new Intent(getBaseContext(), Preferences.class);
                startActivityForResult(intent2, MainActivity.PREFERENCES_ACTIVITY);
                return true;
            case R.id.menu_item_save:
                Intent intent = new Intent();

                mAlarm.toIntent(intent);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return false;
        }
    }
}



package com.unique.simplealarmclock.fragments;

import static com.unique.simplealarmclock.util.DayUtil.getAmPm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.unique.simplealarmclock.R;
import com.unique.simplealarmclock.databinding.FragmentCreateAlarmBinding;
import com.unique.simplealarmclock.model.Alarm;
import com.unique.simplealarmclock.util.DayUtil;
import com.unique.simplealarmclock.util.TimePickerUtil;
import com.unique.simplealarmclock.viewmodel.CreateAlarmViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;

public class CreateAlarmFragment extends Fragment {
    FragmentCreateAlarmBinding fragmentCreateAlarmBinding;
    LinearLayout fragmentCreateAlarmTimePickerLayout;
    TimePicker fragmentCreateAlarmTimePicker;
    private CreateAlarmViewModel createAlarmViewModel;
    boolean isVibrate = false;
    String tone;
    Alarm alarm;
    Ringtone ringtone;
    Dialog dialog;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
    final Calendar newCalendar = Calendar.getInstance();
    Date c = Calendar.getInstance().getTime();


    public CreateAlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            alarm = (Alarm) getArguments().getSerializable(getString(R.string.arg_alarm_obj));
        }
        createAlarmViewModel = ViewModelProviders.of(this).get(CreateAlarmViewModel.class);

        dialog = new Dialog(getContext(),R.style.Theme_Dialog);
        dialog.setContentView(R.layout.timepickerdialog);

        fragmentCreateAlarmTimePickerLayout = dialog.findViewById(R.id.fragment_create_alarm_timePickerLayout);
        fragmentCreateAlarmTimePicker = dialog.findViewById(R.id.fragment_createalarm_timePicker);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentCreateAlarmBinding = FragmentCreateAlarmBinding.inflate(inflater, container, false);
        View v = fragmentCreateAlarmBinding.getRoot();
        tone = String.valueOf(RingtoneManager.getActualDefaultRingtoneUri(this.getContext(), RingtoneManager.TYPE_ALARM));
        ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(tone));
        fragmentCreateAlarmBinding.fragmentCreatealarmSetToneName.setText(ringtone.getTitle(getContext()));
        //set current date
        fragmentCreateAlarmBinding.fragmentCreateAlarmDate.setText(dateFormat.format(c));
        fragmentCreateAlarmBinding.fragmentCreateAlarmTimePickerLayout1.setText(timeFormat.format(c));

        if (alarm != null) {
            updateAlarmInfo(alarm);
            String[] split = alarm.getDate().split("/");
            newCalendar.set(Calendar.YEAR, Integer.parseInt(split[2]));
            newCalendar.set(Calendar.MONTH, Integer.parseInt(split[1]));
            newCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(split[0]));
        }

        listener();
        return v;
    }

    private void listener() {
        final DatePickerDialog StartTime = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                fragmentCreateAlarmBinding.fragmentCreateAlarmDate.setText(dateFormat.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        fragmentCreateAlarmBinding.fragmentCreateAlarmDate.setOnClickListener(v -> {
            StartTime.show();
        });

        dialog.findViewById(R.id.canelAlarm).setOnClickListener(v -> {
            dialog.dismiss();
        });


        dialog.findViewById(R.id.okAlarm).setOnClickListener(v -> {
            dialog.dismiss();
        });

        fragmentCreateAlarmBinding.fragmentCreateAlarmTimePickerLayout1.setOnClickListener(v -> {
            dialog.show();
        });

        fragmentCreateAlarmBinding.fragmentCreatealarmRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fragmentCreateAlarmBinding.fragmentCreatealarmRecurringOptions.setVisibility(View.VISIBLE);
            } else {
                fragmentCreateAlarmBinding.fragmentCreatealarmRecurringOptions.setVisibility(View.GONE);
            }
        });

        fragmentCreateAlarmBinding.fragmentCreatealarmScheduleAlarm.setOnClickListener(v1 -> {
            if (alarm != null) {
                updateAlarm();
            } else {
                scheduleAlarm();
            }

            Navigation.findNavController(v1).navigate(R.id.action_createAlarmFragment_to_alarmsListFragment);
        });

        fragmentCreateAlarmBinding.fragmentCreatealarmCardSound.setOnClickListener(view -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(tone));
            startActivityForResult(intent, 5);
        });

        fragmentCreateAlarmBinding.fragmentCreatealarmVibrateSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            isVibrate = b;
        });

        fragmentCreateAlarmTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                fragmentCreateAlarmBinding.fragmentCreatealarmScheduleAlarmHeading.setText(DayUtil.getDay(TimePickerUtil.getTimePickerHour(timePicker), TimePickerUtil.getTimePickerMinute(timePicker)));

                String startTime = "2013-02-27 "+ timePicker.getHour()+":"+timePicker.getMinute()+":00";

                StringTokenizer tk = new StringTokenizer(startTime);
                String date = tk.nextToken();
                String time = tk.nextToken();

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
                Date dt;
                try {
                    dt = sdf.parse(time);
                    System.out.println("Time Display: " + sdfs.format(dt)); // <-- I got result here
                    fragmentCreateAlarmBinding.fragmentCreateAlarmTimePickerLayout1.setText(sdfs.format(dt));

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                }
        });
    }


    private void scheduleAlarm() {
        String alarmTitle = getString(R.string.alarm_title);
        String alarmDesc = getString(R.string.alarm_desc);
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        if (!fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString().isEmpty()) {
            alarmTitle = fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString();
        }
        if (!fragmentCreateAlarmBinding.fragmentCreatealarmDisc.getText().toString().isEmpty()) {
            alarmDesc = fragmentCreateAlarmBinding.fragmentCreatealarmDisc.getText().toString();
        }
        Alarm alarm = new Alarm(
                alarmId,
                TimePickerUtil.getTimePickerHour(fragmentCreateAlarmTimePicker),
                TimePickerUtil.getTimePickerMinute(fragmentCreateAlarmTimePicker),
                alarmTitle,
                true,
                alarmDesc,
                fragmentCreateAlarmBinding.fragmentCreatealarmRecurring.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckMon.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckTue.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckWed.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckThu.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckFri.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSat.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSun.isChecked(),
                tone,
                isVibrate,
                fragmentCreateAlarmBinding.fragmentCreateAlarmDate.getText().toString()
        );

        createAlarmViewModel.insert(alarm);

        alarm.schedule(requireContext());
    }

    private void updateAlarm() {
//        String alarmTitle=getString(R.string.alarm_title);
////        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
//        if(!fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString().isEmpty()){
//            alarmTitle=fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString();
//        }

        String alarmTitle = getString(R.string.alarm_title);
        String alarmDesc = getString(R.string.alarm_desc);
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        if (!fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString().isEmpty()) {
            alarmTitle = fragmentCreateAlarmBinding.fragmentCreatealarmTitle.getText().toString();
        }
        if (!fragmentCreateAlarmBinding.fragmentCreatealarmDisc.getText().toString().isEmpty()) {
            alarmDesc = fragmentCreateAlarmBinding.fragmentCreatealarmDisc.getText().toString();
        }
        Alarm updatedAlarm = new Alarm(
                alarm.getAlarmId(),
                TimePickerUtil.getTimePickerHour(fragmentCreateAlarmTimePicker),
                TimePickerUtil.getTimePickerMinute(fragmentCreateAlarmTimePicker),
                alarmTitle,
                true,
                alarmDesc,
                fragmentCreateAlarmBinding.fragmentCreatealarmRecurring.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckMon.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckTue.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckWed.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckThu.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckFri.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSat.isChecked(),
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSun.isChecked(),
                tone,
                isVibrate,
                fragmentCreateAlarmBinding.fragmentCreateAlarmDate.getText().toString()
        );
        createAlarmViewModel.update(updatedAlarm);
        updatedAlarm.schedule(requireContext());
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 5) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            ringtone = RingtoneManager.getRingtone(getContext(), uri);
            String title = ringtone.getTitle(getContext());
            if (uri != null) {
                tone = uri.toString();
                if (title != null && !title.isEmpty())
                    fragmentCreateAlarmBinding.fragmentCreatealarmSetToneName.setText(title);
            } else {
                fragmentCreateAlarmBinding.fragmentCreatealarmSetToneName.setText("");
            }
        }
    }

    private void updateAlarmInfo(Alarm alarm) {
        fragmentCreateAlarmBinding.fragmentCreatealarmTitle.setText(alarm.getTitle());
        fragmentCreateAlarmBinding.fragmentCreatealarmDisc.setText(alarm.getDesc());
        fragmentCreateAlarmTimePicker.setHour(alarm.getHour());
        fragmentCreateAlarmTimePicker.setMinute(alarm.getMinute());

        String startTime = "2013-02-27 "+ alarm.getHour()+":"+alarm.getMinute()+":00";

        StringTokenizer tk = new StringTokenizer(startTime);
        String date = tk.nextToken();
        String time = tk.nextToken();

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
        Date dt;
        try {
            dt = sdf.parse(time);
            System.out.println("Time Display: " + sdfs.format(dt)); // <-- I got result here
            fragmentCreateAlarmBinding.fragmentCreateAlarmTimePickerLayout1.setText(sdfs.format(dt));

        } catch (ParseException e) {
            e.printStackTrace();
        }

//        fragmentCreateAlarmBinding.fragmentCreateAlarmTimePickerLayout1.setText(alarm.getHour()+":"+alarm.getMinute());

        fragmentCreateAlarmBinding.fragmentCreateAlarmDate.setText(alarm.getDate());
        if (alarm.isRecurring()) {
            fragmentCreateAlarmBinding.fragmentCreatealarmRecurring.setChecked(true);
            fragmentCreateAlarmBinding.fragmentCreatealarmRecurringOptions.setVisibility(View.VISIBLE);
            if (alarm.isMonday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckMon.setChecked(true);
            if (alarm.isTuesday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckTue.setChecked(true);
            if (alarm.isWednesday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckWed.setChecked(true);
            if (alarm.isThursday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckThu.setChecked(true);
            if (alarm.isFriday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckFri.setChecked(true);
            if (alarm.isSaturday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSat.setChecked(true);
            if (alarm.isSunday())
                fragmentCreateAlarmBinding.fragmentCreatealarmCheckSun.setChecked(true);
            tone = alarm.getTone();
            ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(tone));
            fragmentCreateAlarmBinding.fragmentCreatealarmSetToneName.setText(ringtone.getTitle(getContext()));
            if (alarm.isVibrate())
                fragmentCreateAlarmBinding.fragmentCreatealarmVibrateSwitch.setChecked(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentCreateAlarmBinding = null;
    }
}
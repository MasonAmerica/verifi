package com.example.verifi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.verifi.databinding.FragmentConfigureBinding;

public class ConfigureFragment extends Fragment {

    private FragmentConfigureBinding binding;
    private TestPreference testPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        testPref = TestPreference.getInstance();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentConfigureBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //initialize Configure View values using values from TestPreference
        initConfigureView();

        //register Listeners
        registerListeners();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //initialize the Configure Test View using values from TestPreference object
    private void initConfigureView() {
        //init GPS
        if (!testPref.isEnableGPS()) {
            binding.rbGpsOff.setChecked(true);
            binding.etGPSInterval.setEnabled(false);
            binding.etGPSInterval.setFocusableInTouchMode(false);
        } else {
            switch(testPref.getGpsType()) {
                case IZATSDK:
                    binding.rbIzatSDK.setChecked(true);
                    break;
                case LOCMGR:
                    binding.rbAndroidLocMgr.setChecked(true);
                    break;
            }
            binding.etGPSInterval.setEnabled(true);
            binding.etGPSInterval.setFocusableInTouchMode(true);
        }
        binding.etGPSInterval.setText(String.valueOf(testPref.getGpsInterval()));

        //init Sensor
        if (!testPref.isEnableSensor()) {
            binding.rbSensorOff.setChecked(true);
            binding.etSensorInterval.setEnabled(false);
            binding.etSensorInterval.setFocusableInTouchMode(false);
        } else {
            switch(testPref.getSensorType()) {
                case PROXIMITY:
                    binding.rbProxOnBody.setChecked(true);
                    break;
                case ANTIOBJECT:
                    binding.rbAntiObject.setChecked(true);
                    break;
                case HEARTRATE:
                    binding.rbHeartRate.setChecked(true);
                    break;
            }
            binding.etSensorInterval.setEnabled(true);
            binding.etSensorInterval.setFocusableInTouchMode(true);
        }
        binding.etSensorInterval.setText(String.valueOf(testPref.getSensorInterval()));

        //init Data Connection
        if (!testPref.isEnableDataConn()) {
            binding.rbDataOff.setChecked(true);
            binding.etDataConnectionInterval.setEnabled(false);
            binding.etDataConnectionInterval.setFocusableInTouchMode(false);
        } else {
            switch(testPref.getDataConnType()) {
                case WIFI:
                    binding.rbWifi.setChecked(true);
                    break;
                case CELL:
                    binding.rbCell.setChecked(true);
                    break;
            }
            binding.etDataConnectionInterval.setFocusableInTouchMode(true);
            binding.etDataConnectionInterval.setEnabled(true);
        }
        binding.etDataConnectionInterval.setText(String.valueOf(testPref.getDataConnInterval()));
    }

    //register listeners for radio button and edit text changes as well as start button selection
    private void registerListeners() {

        //GPS radio buttons listener
        binding.rgGPS.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.rbIzatSDK:
                        testPref.setGpsType(GPSType.IZATSDK);
                        testPref.setEnableGPS(true);
                        binding.etGPSInterval.setEnabled(true);
                        binding.etGPSInterval.setFocusableInTouchMode(true);
                        break;
                    case R.id.rbAndroidLocMgr:
                        testPref.setGpsType(GPSType.LOCMGR);
                        testPref.setEnableGPS(true);
                        binding.etGPSInterval.setEnabled(true);
                        binding.etGPSInterval.setFocusableInTouchMode(true);
                        break;
                    case R.id.rbGpsOff:
                        testPref.setEnableGPS(false);
                        binding.etGPSInterval.setEnabled(false);
                        binding.etGPSInterval.setFocusableInTouchMode(false);
                        break;
                }
            }
        });

        //Sensor radio buttons listener
        binding.rgSensor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.rbProxOnBody:
                        testPref.setSensorType(SensorType.PROXIMITY);
                        testPref.setEnableSensor(true);
                        binding.etSensorInterval.setEnabled(true);
                        binding.etSensorInterval.setFocusableInTouchMode(true);
                        break;
                    case R.id.rbAntiObject:
                        testPref.setSensorType(SensorType.ANTIOBJECT);
                        testPref.setEnableSensor(true);
                        binding.etSensorInterval.setEnabled(true);
                        binding.etSensorInterval.setFocusableInTouchMode(true);
                        break;
                    case R.id.rbHeartRate:
                        testPref.setSensorType(SensorType.HEARTRATE);
                        testPref.setEnableSensor(true);
                        binding.etSensorInterval.setEnabled(true);
                        binding.etSensorInterval.setFocusableInTouchMode(true);
                        break;
                    case R.id.rbSensorOff:
                        testPref.setEnableSensor(false);
                        binding.etSensorInterval.setEnabled(false);
                        binding.etSensorInterval.setFocusableInTouchMode(false);
                        break;
                }
            }
        });

        //Data Connection radio buttons listener
        binding.rgDataConnection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.rbWifi:
                        testPref.setDataConnType(DataConnType.WIFI);
                        testPref.setEnableDataConn(true);
                        binding.etDataConnectionInterval.setFocusableInTouchMode(true);
                        binding.etDataConnectionInterval.setEnabled(true);
                        break;
                    case R.id.rbCell:
                        testPref.setDataConnType(DataConnType.CELL);
                        testPref.setEnableDataConn(true);
                        binding.etDataConnectionInterval.setFocusableInTouchMode(true);
                        binding.etDataConnectionInterval.setEnabled(true);
                        break;
                    case R.id.rbDataOff:
                        testPref.setEnableDataConn(false);
                        binding.etDataConnectionInterval.setEnabled(false);
                        binding.etDataConnectionInterval.setFocusableInTouchMode(false);
                        break;
                }
            }
        });

        //GPS interval edit text listener
        binding.etGPSInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(""))
                    testPref.setGpsInterval(Integer.parseInt(s.toString()));
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Sensor interval edit text listener
        binding.etSensorInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(""))
                    testPref.setSensorInterval(Integer.parseInt(s.toString()));
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Data Connection interval edit text listener
        binding.etDataConnectionInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(""))
                    testPref.setDataConnInterval(Integer.parseInt(s.toString()));
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.buttonStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!((MainActivity) requireActivity()).isTestStarted()) {

                    //send intent to TestService to start test
                    requireActivity().startService(new Intent(getContext(), MainService.class));

                    Toast.makeText(getActivity(),"START TEST",Toast.LENGTH_SHORT).show();

                    ((MainActivity) requireActivity()).setTestStarted(true);
                }

                ((MainActivity) requireActivity()).showStatusFragment();
                //NavHostFragment.findNavController(ConfigureFragment.this).navigate(R.id.action_ConfigureFragment_to_StatusFragment);
            }
        });
    }
}
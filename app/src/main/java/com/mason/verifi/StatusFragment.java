package com.mason.verifi;
/*
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mason.verifi.databinding.FragmentStatusBinding;

// This class is the controller for the Status Fragment view
public class StatusFragment extends Fragment implements StatusUpdater{
    private static final String TAG = "verifi.StatusFragment";

    private FragmentStatusBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentStatusBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvTestStatus.setMovementMethod(new ScrollingMovementMethod());

        //Stop button listener
        //The onClick listener will stop the MainService
        binding.buttonStopTest.setOnClickListener(view1 -> {
            if (((MainActivity) requireActivity()).isTestStarted()) {

                //send intent to TestService to stop test
                requireActivity().stopService(new Intent(getContext(), MainService.class));

                Toast.makeText(getActivity(),"STOP TEST",Toast.LENGTH_SHORT).show();

                ((MainActivity) requireActivity()).setTestStarted(false);

                ((MainActivity) requireActivity()).showConfigureFragment();
                //NavHostFragment.findNavController(StatusFragment.this).navigate(R.id.action_StatusFragment_to_ConfigureFragment);
            }

        });

        //TextView listener
        binding.tvTestStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN); //auto scroll to bottom
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                //override stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                //override stub
            }
        });

        Log.d(TAG, "onViewCreated");
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void updateStatus(String status) {
        binding.tvTestStatus.append("\n" + status);
    }

}
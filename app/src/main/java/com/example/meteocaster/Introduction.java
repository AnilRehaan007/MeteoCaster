package com.example.meteocaster;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Introduction extends Fragment {

    public Introduction() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_introduction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Animation top,bottom;

        top= AnimationUtils.loadAnimation(getContext(),R.anim.top_mover);
        bottom=AnimationUtils.loadAnimation(getContext(),R.anim.bottom_mover);


        ImageView market=view.findViewById(R.id.market);
        ImageView logo=view.findViewById(R.id.logo);

        market.setAnimation(top);
        logo.setAnimation(bottom);
    }
}
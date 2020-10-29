package com.example.jibi.di

import com.example.jibi.di.main.MainComponent
import dagger.Module

@Module(
    subcomponents = [
//        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule
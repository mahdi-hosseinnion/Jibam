package com.example.jibi.di

import com.example.jibi.di.main.MainComponent
import dagger.Module
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Module(
    subcomponents = [
//        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule
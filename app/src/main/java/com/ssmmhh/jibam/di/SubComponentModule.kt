package com.ssmmhh.jibam.di

import com.ssmmhh.jibam.di.main.MainComponent
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